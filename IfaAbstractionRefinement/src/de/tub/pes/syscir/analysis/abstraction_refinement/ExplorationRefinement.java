package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementConfig.PartialDescendantsTracking;
import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementConfig.SimpleConditionTracking;
import de.tub.pes.syscir.analysis.abstraction_refinement.branches.BranchInformation;
import de.tub.pes.syscir.analysis.abstraction_refinement.branches.BranchInformationHandler;
import de.tub.pes.syscir.analysis.abstraction_refinement.branches.Branching;
import de.tub.pes.syscir.analysis.abstraction_refinement.branches.SplittersAndBranchInformationHandler;
import de.tub.pes.syscir.analysis.abstraction_refinement.stepgraph.ReconstructionNode;
import de.tub.pes.syscir.analysis.abstraction_refinement.stepgraph.SplittersInformationHandler;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.ExplorationRecord;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler.SimulationStopMode;
import de.tub.pes.syscir.analysis.statespace_exploration.SequentialStateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesScheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.EvaluationInterceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler.InsufficientValueTrackingException;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.InformationHandler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Interceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.TransitionGraphRecord;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.NoInformation;
import java.util.Collection;
import java.util.Set;

/**
 * This is the top-level exploration refinement implementation. It runs a loop constructing
 * exploration processes adhering to how the RefinementConfig configures exploration refinement.
 * 
 * @author Lukas Ernst
 */
public class ExplorationRefinement extends TrackedExplorationBase {
    
    public ExplorationRefinement(RefinementConfig config, Sources baseTrackedElements, long timeoutMillis) {
        super(config, baseTrackedElements.copy(), new SourcedValueManagement(config::newEmptySources), timeoutMillis);
    }
    
    /**
     * Checks whether tracking the specified sources is an effective refinement.
     * 
     * @param addTrackedElements the sources to additionally track
     * @return whether this refinement process's tracked sources changed, justifying a restart
     */
    public boolean checkRestart(Sources addTrackedElements) {
        Sources newElements = addTrackedElements(addTrackedElements);
        if (newElements.isEmpty()) {
            return false;
        }
        this.config.log().restartingExploration(newElements);
        return true;
    }
    
    /**
     * Adds the specified sources to this's tracked sources and returns the tracked sources newly
     * tracked through this operation (the intersection of previous tracked sources and those
     * specified).
     */
    public Sources addTrackedElements(Sources elements) {
        Sources newElements = elements.copy();
        newElements.remove(this.trackedElements);
        this.trackedElements.add(newElements);
        return newElements;
    }
    
    public SplittersInformationHandler makeSplittersInformationHandler() {
        return new SplittersInformationHandler() {
            
            @Override
            protected void handleSplitters(Collection<ReconstructionNode> splitters) {
                if (splitters.isEmpty()) {
                    return;
                }
                ExplorationRefinement.this.config.log().identifiedSplitters(splitters);
                Sources splitterSources = ExplorationRefinement.this.config.newEmptySources();
                for (ReconstructionNode splitter : splitters) {
                    splitterSources.add(((SourcedValue) splitter.getInfo().getConditionValue()).getSources());
                }
                if (checkRestart(splitterSources)) {
                    throw new RestartExplorationException();
                }
            }
        };
    }
    
    public BranchInformationHandler makePartialDescendantsInformationHandler(int threshold) {
        return new BranchInformationHandler() {
            
            @Override
            public void onUpdatePartialDescendantsCount(Branching branching, BranchInformation node) {
                if (branching.currentPartialDescendants <= threshold) {
                    return;
                }
                ExplorationRefinement.this.config.log().tooManyPartialDescendants(branching);
                Sources conditionSources = ExplorationRefinement.this.config.newEmptySources();
                for (AbstractedValue conditionValue : branching.getConditionValues()) {
                    conditionSources.add(((SourcedValue) conditionValue).getSources());
                }
                if (checkRestart(conditionSources)) {
                    throw new RestartExplorationException();
                }
            }
        };
    }
    
    public SplittersAndBranchInformationHandler makeSplittersAndPartialDescendantsInformationHandler(int threshold) {
        return new SplittersAndBranchInformationHandler(makeSplittersInformationHandler(),
                makePartialDescendantsInformationHandler(threshold));
    }
    
    public boolean run() {
        while (true) {
            ExplorationRecord record = new TransitionGraphRecord(false);
            InformationHandler informationHandler;
            EvaluationInterceptor evaluationInterceptor;
            if (this.config.conditionTrackingMode() == SimpleConditionTracking.NONE) {
                informationHandler = NoInformation.HANDLER;
                evaluationInterceptor = this.evaluationAccessTracker;
            } else if (this.config.conditionTrackingMode() == SimpleConditionTracking.ALL) {
                informationHandler = NoInformation.HANDLER;
                evaluationInterceptor =
                        EvaluationInterceptor.sequence(this.evaluationAccessTracker, (expr, _, _, result) -> {
                            if (RefinementUtil.isControlCondition(expr) && !result.isDetermined()) {
                                this.config.log().unknownControlCondition(expr, result);
                                if (checkRestart(((SourcedValue) result).getSources())) {
                                    throw new RestartExplorationException();
                                }
                            }
                            return result;
                        });
            } else if (this.config.conditionTrackingMode() == SimpleConditionTracking.BLOCK_SPLITTERS) {
                SplittersInformationHandler splittersHandler = makeSplittersInformationHandler();
                informationHandler = splittersHandler;
                evaluationInterceptor = EvaluationInterceptor.sequence(this.evaluationAccessTracker, splittersHandler);
            } else if (this.config.conditionTrackingMode() instanceof PartialDescendantsTracking pdConfig) {
                BranchInformationHandler pdHandler = makePartialDescendantsInformationHandler(pdConfig.threshold);
                informationHandler = pdHandler;
                evaluationInterceptor = EvaluationInterceptor.sequence(this.evaluationAccessTracker, pdHandler);
                record = pdHandler;
            } else if (this.config
                    .conditionTrackingMode() instanceof RefinementConfig.SplittersAndPartialDescendantsTracking spdConfig) {
                SplittersAndBranchInformationHandler spdHandler =
                        makeSplittersAndPartialDescendantsInformationHandler(spdConfig.threshold);
                informationHandler = spdHandler;
                evaluationInterceptor = EvaluationInterceptor.sequence(this.evaluationAccessTracker, spdHandler);
                record = spdHandler;
            } else {
                throw new AssertionError();
            }
            Interceptor interceptor =
                    Interceptor.of(this.variableAccessTracker, evaluationInterceptor, informationHandler);
            
            SomeVariablesScheduler scheduler = new SomeVariablesScheduler(this.config.scSystem(), this.logic,
                    interceptor, SimulationStopMode.SC_STOP_FINISH_IMMEDIATE, _ -> true);
            
            StateSpaceExploration exploration;
            boolean result;
            try {
                ConsideredState initialState = initialState(scheduler, interceptor);
                exploration = new SequentialStateSpaceExploration(scheduler, record, Set.of(initialState));
                result = runExploration(exploration);
            } catch (InsufficientValueTrackingException e) {
                this.config.log().insufficientTracking(e);
                if (checkRestart(((SourcedValue) e.getValue()).getSources())) {
                    continue;
                }
                throw e;
            } catch (RestartExplorationException e) {
                continue;
            }
            
            if (result) {
                this.config.log().explorableAbstractionFound(exploration, this.trackedElements);
            } else {
                this.config.log().explorationTimedOut(exploration, this.trackedElements);
            }
            return result;
        }
    }
    
    /**
     * Exception thrown to signal an exploration restart.
     */
    public static class RestartExplorationException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;
        
        public RestartExplorationException() {}
        
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
    
}
