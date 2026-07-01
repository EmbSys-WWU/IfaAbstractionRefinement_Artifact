package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.abstraction_refinement.InformationFlowPolicy.Entry;
import de.tub.pes.syscir.analysis.abstraction_refinement.InformationFlowPolicy.EntryType;
import de.tub.pes.syscir.analysis.abstraction_refinement.SdgPathfinder.SdgPath;
import de.tub.pes.syscir.analysis.abstraction_refinement.branches.Branching;
import de.tub.pes.syscir.analysis.abstraction_refinement.stepgraph.ReconstructionNode;
import de.tub.pes.syscir.analysis.dependencies.Sdg;
import de.tub.pes.syscir.analysis.dependencies.SdgNode;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler.InsufficientValueTrackingException;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.Collection;

public class NoRefinementLog implements RefinementLog {

    public static final NoRefinementLog INSTANCE = new NoRefinementLog();

    @Override
    public void evaluation(Expression expression, AbstractedValue result) {}

    @Override
    public void insufficientTracking(InsufficientValueTrackingException exception) {}

    @Override
    public void unknownControlCondition(Expression condition, AbstractedValue value) {}

    @Override
    public void identifiedSplitters(Collection<ReconstructionNode> splitters) {}

    @Override
    public void tooManyPartialDescendants(Branching branching) {}

    @Override
    public void restartingExploration(Sources addedTrackedElements) {}

    @Override
    public void explorableAbstractionFound(StateSpaceExploration exploration, Sources trackedElements) {}

    @Override
    public void explorationTimedOut(StateSpaceExploration exploration, Sources trackedElements) {}

    @Override
    public void sdgComputed(StateSpaceExploration exploration, Sdg sdg) {}

    @Override
    public void sdgPathFound(Entry policyEntry, Object path, boolean legal) {}

    @Override
    public void sdgPathNotFound(Entry policyEntry, boolean legal) {}

    @Override
    public void tryPathResolution(SdgPath forbiddenPath) {}

    @Override
    public void selectPathRefinement(SdgPath path, SdgNode pathNode, SdgNode controlDependence, Sources refinement) {}

    @Override
    public void pathResolutionSuccess(SdgPath forbiddenPath, Sources refinement) {}

    @Override
    public void pathResolutionFailure(SdgPath forbiddenPath, Sources refinement, SdgPath newSimilarPath) {}

    @Override
    public void informationFlowOkay() {}

    @Override
    public void informationFlowFail(EntryType violatedType, boolean triedRefinements) {}

}
