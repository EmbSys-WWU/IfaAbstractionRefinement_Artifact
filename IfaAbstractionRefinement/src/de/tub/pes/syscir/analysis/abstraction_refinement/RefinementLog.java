package de.tub.pes.syscir.analysis.abstraction_refinement;

import java.util.Collection;

import de.tub.pes.syscir.analysis.abstraction_refinement.InformationFlowPolicy.EntryType;
import de.tub.pes.syscir.analysis.abstraction_refinement.SdgPathfinder.SdgPath;
import de.tub.pes.syscir.analysis.abstraction_refinement.branches.Branching;
import de.tub.pes.syscir.analysis.abstraction_refinement.stepgraph.ReconstructionNode;
import de.tub.pes.syscir.analysis.dependencies.Sdg;
import de.tub.pes.syscir.analysis.dependencies.SdgNode;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.EvaluationInterceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler.InsufficientValueTrackingException;
import de.tub.pes.syscir.sc_model.expressions.Expression;

public interface RefinementLog {

    /**
     * Note that an expression was evaluated (from
     * {@link EvaluationInterceptor#evaluated(Expression, AbstractedValue)})
     */
    void evaluation(Expression expression, AbstractedValue result);

    /**
     * Note that tracking was insufficient for continuing the exploration because of
     * a crucial value being unknown, which may be a wait- or notify-parameter. This
     * is used by all exploration heuristics.
     */
    void insufficientTracking(InsufficientValueTrackingException exception);

    /**
     * Note that a control condition is unknown with SimpleConditionTracking.ALL
     */
    void unknownControlCondition(Expression condition, AbstractedValue value);

    /**
     * Note that block splitters are identified with the splitters heuristic
     */
    void identifiedSplitters(Collection<ReconstructionNode> splitters);

    /**
     * Note that the specified branching exceeds the partial descendants threshold
     */
    void tooManyPartialDescendants(Branching branching);

    /**
     * Note that the exploration is aborted and restart
     */
    void restartingExploration(Sources addedTrackedElements);

    /**
     * Note that an exploration heuristic completed an exploration
     */
    void explorableAbstractionFound(StateSpaceExploration exploration, Sources trackedElements);

    /**
     * Note that an exploration timed out
     */
    void explorationTimedOut(StateSpaceExploration exploration, Sources trackedElements);

    /**
     * Note that a SDG was computed
     */
    void sdgComputed(StateSpaceExploration exploration, Sdg sdg);

    /**
     * Note that a SDG path was found corresponding to the specified policy entry.
     * 
     * @param policyEntry the checked policy entry
     * @param path the found path
     * @param legal whether the path being found complies with the policy
     */
    void sdgPathFound(InformationFlowPolicy.Entry policyEntry, Object path, boolean legal);

    /**
     * Note that no SDG path was found corresponding to the specified policy entry.
     * 
     * @param policyEntry the checked policy entry
     * @param legal whether the path not being found complies with the policy
     */
    void sdgPathNotFound(InformationFlowPolicy.Entry policyEntry, boolean legal);

    /**
     * Note that a forbidden SDG path is chosen for resolution
     */
    void tryPathResolution(SdgPath forbiddenPath);

    /**
     * Note that a refinement group has been chosen for potential resolution of a
     * path.
     * 
     * @param path the path to resolve
     * @param pathNode the node on the path to resolve
     * @param controlDependence the control dependence of pathNode to refine or null
     * @param refinement the refinement group
     */
    void selectPathRefinement(SdgPath path, SdgNode pathNode, SdgNode controlDependence, Sources refinement);

    /**
     * Note that while using cumulative IFR, a forbidden path was successfully
     * resolved through the specified refinement (non-cumulative IFR does not
     * explicitly determine this)
     */
    void pathResolutionSuccess(SdgPath forbiddenPath, Sources refinement);

    /**
     * Note that while using cumulative IFR, a forbidden path was not resolved
     * through the specified refinement but instead the specified new similar path
     * was still found (non-cumulative IFR does not explicitly determine this)
     */
    void pathResolutionFailure(SdgPath forbiddenPath, Sources refinement, SdgPath newSimilarPath);

    /**
     * Note that there is no forbidden information flow
     */
    void informationFlowOkay();

    /**
     * Note resignation, suspecting information flow
     */
    void informationFlowFail(EntryType violatedType, boolean triedRefinements);
}
