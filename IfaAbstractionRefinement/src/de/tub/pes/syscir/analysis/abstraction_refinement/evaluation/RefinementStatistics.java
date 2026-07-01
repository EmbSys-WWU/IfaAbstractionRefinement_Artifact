package de.tub.pes.syscir.analysis.abstraction_refinement.evaluation;

import de.tub.pes.syscir.analysis.abstraction_refinement.InformationFlowPolicy.Entry;
import de.tub.pes.syscir.analysis.abstraction_refinement.InformationFlowPolicy.EntryType;
import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementLog;
import de.tub.pes.syscir.analysis.abstraction_refinement.SdgPathfinder.SdgPath;
import de.tub.pes.syscir.analysis.abstraction_refinement.Sources;
import de.tub.pes.syscir.analysis.abstraction_refinement.branches.Branching;
import de.tub.pes.syscir.analysis.abstraction_refinement.stepgraph.ReconstructionNode;
import de.tub.pes.syscir.analysis.dependencies.Sdg;
import de.tub.pes.syscir.analysis.dependencies.SdgNode;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler.InsufficientValueTrackingException;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.Collection;

public class RefinementStatistics implements RefinementLog {

    public int explorationCount = 0;
    public int finalStateCount = 0;
    public int sdgCount = 0;
    public int finalSdgNodeCount = 0;
    public int finalSdgEdgeCount = 0;
    public int maxIFStateCount = 0;
    public int expressionEvaluationCount = 0;
    public Boolean policyCompliance = null;

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        if (this.policyCompliance != null) {
            out.append(this.policyCompliance ? "Compliant; " : "Non-Compliant; ");
        }
        out.append("Explorations: " + this.explorationCount + "; final with " + this.finalStateCount
                + " states; max. during IFR with " + this.maxIFStateCount + " states; ");
        out.append("SDG Computations: " + this.sdgCount + "; final with " + this.finalSdgNodeCount + " nodes and "
                + this.finalSdgEdgeCount + " edges; ");
        out.append("Expressions evaluated: " + this.expressionEvaluationCount + ";");
        return out.toString();
    }

    @Override
    public void evaluation(Expression expression, AbstractedValue result) {
        this.expressionEvaluationCount++;
    }

    @Override
    public void insufficientTracking(InsufficientValueTrackingException exception) {}

    @Override
    public void unknownControlCondition(Expression condition, AbstractedValue value) {}

    @Override
    public void identifiedSplitters(Collection<ReconstructionNode> splitters) {}

    @Override
    public void tooManyPartialDescendants(Branching branching) {}

    @Override
    public void restartingExploration(Sources addedTrackedElements) {
        this.explorationCount++;
    }

    @Override
    public void explorableAbstractionFound(StateSpaceExploration exploration, Sources trackedElements) {
        this.explorationCount++;
        this.finalStateCount = exploration.getNumExploredStates();
        if (this.sdgCount > 0) {
            this.maxIFStateCount = Math.max(this.maxIFStateCount, exploration.getNumExploredStates());
        }
    }

    @Override
    public void explorationTimedOut(StateSpaceExploration exploration, Sources trackedElements) {
        this.explorationCount++;
    }

    @Override
    public void sdgComputed(StateSpaceExploration exploration, Sdg sdg) {
        this.explorationCount++;
        this.finalStateCount = exploration.getNumExploredStates();
        this.sdgCount++;
        this.finalSdgNodeCount = sdg.getNodes().size();
        this.finalSdgEdgeCount = sdg.getEdges().size();
    }

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
    public void informationFlowOkay() {
        this.policyCompliance = true;
    }

    @Override
    public void informationFlowFail(EntryType violatedType, boolean triedRefinements) {
        this.policyCompliance = false;
    }

}
