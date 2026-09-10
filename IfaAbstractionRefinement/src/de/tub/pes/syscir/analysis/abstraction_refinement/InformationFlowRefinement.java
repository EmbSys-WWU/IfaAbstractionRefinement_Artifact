package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.abstraction_refinement.InformationFlowPolicy.EntryType;
import de.tub.pes.syscir.analysis.abstraction_refinement.LockstepSdgPathfinder.LockstepSdgPath;
import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementConfig.InformationFlowRefinementMode;
import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementUtil.AbstractionExplorationResult;
import de.tub.pes.syscir.analysis.abstraction_refinement.SdgPathfinder.SdgPath;
import de.tub.pes.syscir.analysis.dependencies.DgEdge.EdgeType;
import de.tub.pes.syscir.analysis.dependencies.Sdg;
import de.tub.pes.syscir.analysis.dependencies.SdgEdge;
import de.tub.pes.syscir.analysis.dependencies.SdgNode;
import de.tub.pes.syscir.sc_model.expressions.EventNotificationExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementations of the cumulative and non-cumulative information flow refinement procedures.
 * 
 * @author Lukas Ernst
 */
public class InformationFlowRefinement {
    
    private final RefinementConfig config;
    
    public InformationFlowRefinement(RefinementConfig config) {
        this.config = config;
    }
    
    public void run() {
        if (this.config.informationFlowRefinementMode() == InformationFlowRefinementMode.CUMULATIVE) {
            runCumulative();
        } else if (this.config.informationFlowRefinementMode() == InformationFlowRefinementMode.NON_CUMULATIVE) {
            runNonCumulative();
        } else {
            throw new IllegalStateException(
                    "Unknown information flow refinement mode: " + this.config.informationFlowRefinementMode());
        }
    }
    
    public void runCumulative() {
        List<Sources> exploredSources = new ArrayList<>();
        List<Sdg> informationFlows = new ArrayList<>();
        List<Sources> exploredSourcesTimedOut = new ArrayList<>();
        AbstractionExplorationResult abstractionResult = RefinementUtil.findExplorableAbstraction(this.config);
        Sources trackedElementsEstablished = abstractionResult.trackedElements();
        long maxPastSuccessfulExecutionTime = abstractionResult.successfulExecutionTime();
        
        Sdg informationFlow = RefinementUtil.informationFlow(this.config, trackedElementsEstablished);
        exploredSources.add(trackedElementsEstablished);
        informationFlows.add(informationFlow);
        while (true) {
            List<SdgPath> forbiddenPaths = new ArrayList<>();
            Sdg sdg = informationFlow;
            List<InformationFlowPolicy.Entry> violatedEntries = this.config.policy()
                    .check(entry -> SdgPathfinder.findPath(sdg, entry), forbiddenPaths::add, this.config.log());
            if (forbiddenPaths.isEmpty()) {
                if (!violatedEntries.isEmpty()) {
                    this.config.log().informationFlowFail(EntryType.REQUIRED, false);
                    return;
                }
                this.config.log().informationFlowOkay();
                return;
            }
            SdgPath forbiddenPath = forbiddenPaths.getFirst();
            this.config.log().tryPathResolution(forbiddenPath);
            List<Sources> exploredSourcesForPath = new ArrayList<>();
            exploredSourcesForPath.add(trackedElementsEstablished);
            while (true) {
                Sources refinement =
                        selectRefinementSources(exploredSourcesForPath, exploredSourcesTimedOut, forbiddenPath);
                if (refinement == null) {
                    this.config.log().informationFlowFail(EntryType.FORBIDDEN, true);
                    return;
                }
                Sources candidateTrackedElements = trackedElementsEstablished.copy();
                candidateTrackedElements.add(refinement);
                int exploredIndex = subsetOfIndex(candidateTrackedElements, exploredSources);
                Sdg candidateInformationFlow;
                long candidateExecutionTime;
                if (exploredIndex >= 0) {
                    candidateTrackedElements = exploredSources.get(exploredIndex);
                    candidateInformationFlow = informationFlows.get(exploredIndex);
                    candidateExecutionTime = maxPastSuccessfulExecutionTime;
                } else {
                    abstractionResult = RefinementUtil.findExplorableAbstraction(this.config, candidateTrackedElements,
                            this.config.computeTimeout(maxPastSuccessfulExecutionTime));
                    if (abstractionResult == null) {
                        exploredSourcesTimedOut.add(refinement);
                        continue;
                    }
                    
                    candidateTrackedElements = abstractionResult.trackedElements();
                    candidateInformationFlow = RefinementUtil.informationFlow(this.config, candidateTrackedElements);
                    candidateExecutionTime = abstractionResult.successfulExecutionTime();
                    exploredSources.add(candidateTrackedElements);
                    informationFlows.add(candidateInformationFlow);
                }
                exploredSourcesForPath.add(candidateTrackedElements);
                SdgPath similarPath = SdgPathfinder.refindPath(candidateInformationFlow,
                        forbiddenPath.nodes().stream().map(node -> node.getId().id()).toList());
                if (similarPath != null) {
                    this.config.log().pathResolutionFailure(forbiddenPath, refinement, similarPath);
                    continue;
                }
                this.config.log().pathResolutionSuccess(forbiddenPath, refinement);
                trackedElementsEstablished = candidateTrackedElements;
                informationFlow = candidateInformationFlow;
                maxPastSuccessfulExecutionTime = Math.max(maxPastSuccessfulExecutionTime, candidateExecutionTime);
                break;
            }
        }
    }
    
    public void runNonCumulative() {
        // compute initial abstraction and sdg
        List<Sources> exploredSources = new ArrayList<>();
        List<Sdg> informationFlows = new ArrayList<>();
        List<Sources> exploredSourcesTimedOut = new ArrayList<>();
        AbstractionExplorationResult abstractionResult = RefinementUtil.findExplorableAbstraction(this.config);
        Sources trackedElementsBase = abstractionResult.trackedElements();
        long baseExecutionTime = abstractionResult.successfulExecutionTime();
        
        exploredSources.add(trackedElementsBase);
        informationFlows.add(RefinementUtil.informationFlow(this.config, trackedElementsBase));
        while (true) {
            // find any path
            List<LockstepSdgPath> forbiddenPaths = new ArrayList<>();
            List<InformationFlowPolicy.Entry> violatedEntries = this.config.policy().check(
                    entry -> LockstepSdgPathfinder.findPath(entry.getSource(), entry.getTarget(), informationFlows),
                    forbiddenPaths::add, this.config.log());
            // exit with success if no forbidden paths were found
            if (forbiddenPaths.isEmpty()) {
                if (!violatedEntries.isEmpty()) {
                    this.config.log().informationFlowFail(EntryType.REQUIRED, false);
                    return;
                }
                this.config.log().informationFlowOkay();
                return;
            }
            // select sources for refinement
            Sdg informationFlow = informationFlows.get(0);
            SdgPath forbiddenPath = LockstepSdgPathfinder.backtracePath(forbiddenPaths.get(0), informationFlow, 0);
            this.config.log().tryPathResolution(forbiddenPath);
            Sources refinement = selectRefinementSources(exploredSources, exploredSourcesTimedOut, forbiddenPath);
            if (refinement == null) {
                this.config.log().informationFlowFail(EntryType.FORBIDDEN, true);
                return;
            }
            // compute and add new sdg
            Sources refinedTrackedElements = trackedElementsBase.copy();
            refinedTrackedElements.add(refinement);
            abstractionResult = RefinementUtil.findExplorableAbstraction(this.config, refinedTrackedElements,
                    this.config.computeTimeout(baseExecutionTime));
            if (abstractionResult == null) {
                exploredSourcesTimedOut.add(refinement);
            } else {
                refinedTrackedElements = abstractionResult.trackedElements();
                informationFlows.add(RefinementUtil.informationFlow(this.config, refinedTrackedElements));
                exploredSources.add(refinedTrackedElements);
            }
        }
    }
    
    /**
     * Returns the first index in the specified iterable that is a superset of the specified singular
     * sources or -1 if there is none.
     */
    public static int subsetOfIndex(Sources sources, Iterable<Sources> references) {
        int index = 0;
        for (Sources reference : references) {
            if (sources.isSubsetOf(reference)) {
                return index;
            }
            index++;
        }
        return -1;
    }
    
    /**
     * Returns the first index in the specified iterable that is a subset of the specified singular
     * sources or -1 if there is none.
     */
    public static int supersetOfIndex(Sources sources, Iterable<Sources> references) {
        int index = 0;
        for (Sources reference : references) {
            if (reference.isSubsetOf(sources)) {
                return index;
            }
            index++;
        }
        return -1;
    }
    
    /**
     * Chooses the next refinement to attempt.
     * 
     * @param skipSubsets refinements that are subsets of any of these sources will not be considered
     * @param skipSupersets refinements that are supersets of any of these sources will not be
     *        considered
     * @param nodeSources provider of sources from SdgNodes, for example an
     *        {@link InformationFlowExploration.Output}
     * @param path the path to resolve
     * @return sources to refine
     */
    public Sources selectRefinementSources(Iterable<Sources> skipSubsets, Iterable<Sources> skipSupersets,
            SdgPath path) {
        // First, try to refine a control condition by tracking all its data dependencies.
        RefinementCandidate result =
                selectRefinementSources(skipSubsets, skipSupersets, path, RefinementUtil::controlDependencies,
                        node -> getControllConditionSources(node, edge -> edge.getType() == EdgeType.DATA));
        
        if (result == null) {
            // Second, try to refine a control condition by tracking all its dependencies.
            result = selectRefinementSources(skipSubsets, skipSupersets, path, RefinementUtil::controlDependencies,
                    node -> getControllConditionSources(node, _ -> true));
        }
        
        if (result == null) {
            // Finally, try to refine any node by tracking all its dependencies.
            result = selectRefinementSources(skipSubsets, skipSupersets, path, SdgNode::backwardsSlice,
                    node -> getBackwardsSliceSources(node, _ -> true));
        }
        
        if (result != null) {
            this.config.log().selectPathRefinement(path, result.pathNode(), result.candidateNode(), result.sources());
            return result.sources();
        }
        return null;
    }

    private record RefinementCandidate(SdgNode pathNode, SdgNode candidateNode, Sources sources) {}
    
    private RefinementCandidate selectRefinementSources(Iterable<Sources> skipSubsets, Iterable<Sources> skipSupersets,
            SdgPath path, Function<SdgNode, Iterable<SdgNode>> candidatesProvider,
            Function<SdgNode, Sources> sourcesProvider) {
        for (SdgNode pathNode : path.nodes()) {
            for (SdgNode nodeCandidate : candidatesProvider.apply(pathNode)) {
                Sources effectingSources = sourcesProvider.apply(nodeCandidate);
                if (effectingSources == null) {
                    continue;
                }
                if (subsetOfIndex(effectingSources, skipSubsets) >= 0
                        || supersetOfIndex(effectingSources, skipSupersets) >= 0) {
                    continue;
                }
                return new RefinementCandidate(pathNode, nodeCandidate, effectingSources);
            }
        }
        return null;
    }
    
    private Sources getControllConditionSources(SdgNode node, Predicate<SdgEdge> edgeFilter) {
        Expression expression = RefinementUtil.getNodeExpression(node);
        if (RefinementUtil.isControlCondition(expression) || expression instanceof EventNotificationExpression) {
            return getBackwardsSliceSources(node, edgeFilter);
        }
        if (RefinementUtil.isWaitStatement(expression)) {
            Sources result = this.config.newEmptySources();
            for (SdgNode parameter : RefinementUtil.listMembers(node)) {
                result.add(getBackwardsSliceSources(parameter, edgeFilter));
            }
            return result;
        }
        return null;
    }
    
    private Sources getBackwardsSliceSources(SdgNode node, Predicate<SdgEdge> edgeFilter) {
        Set<SdgNode> seen = new LinkedHashSet<>(Set.of(node));
        Deque<SdgNode> worklist = new ArrayDeque<>(Set.of(node));
        Sources result = this.config.newEmptySources();
        
        while (!worklist.isEmpty()) {
            SdgNode current = worklist.removeFirst();
            for (SdgEdge edge : current.getIncoming()) {
                if (!edgeFilter.test(edge)) {
                    continue;
                }
                SdgNode predecessor = edge.getSource();
                if (seen.add(predecessor)) {
                    worklist.addLast(predecessor);
                    RefinementUtil.addSourcesFromNode(predecessor, result);
                }
            }
        }
        
        return result;
    }
    
}
