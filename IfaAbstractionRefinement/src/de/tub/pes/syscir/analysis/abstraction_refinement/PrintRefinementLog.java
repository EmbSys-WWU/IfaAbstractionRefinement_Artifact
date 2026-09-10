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
import java.io.PrintStream;
import java.util.Collection;

public class PrintRefinementLog extends StreamRefinementLog {

    public static final PrintRefinementLog INSTANCE = new PrintRefinementLog();

    /** Logs to standard output. */
    public PrintRefinementLog() {
        this(System.out);
    }

    /**
     * Logs to the supplied {@link PrintStream}.
     *
     * @param out destination stream; must not be {@code null}
     */
    public PrintRefinementLog(PrintStream out) {
        super(out);
    }

    @Override
    public void evaluation(Expression expression, AbstractedValue result) {}

    @Override
    public void insufficientTracking(InsufficientValueTrackingException exception) {
        this.out.println("Insufficient tracking of " + exception.getValue() + " : " + exception.getMessage());
    }

    @Override
    public void unknownControlCondition(Expression condition, AbstractedValue value) {}

    @Override
    public void identifiedSplitters(Collection<ReconstructionNode> splitters) {}

    @Override
    public void tooManyPartialDescendants(Branching branching) {}

    @Override
    public void restartingExploration(Sources addedTrackedElements) {
        this.out.println("Now additionally tracking " + addedTrackedElements);
    }

    @Override
    public void explorableAbstractionFound(StateSpaceExploration exploration, Sources trackedElements) {
        this.out.println("Explored with " + exploration.getNumExploredStates() + " explored states by tracking "
                + trackedElements);
    }

    @Override
    public void explorationTimedOut(StateSpaceExploration exploration, Sources trackedElements) {
        this.out.println("Exploration timed out after " + exploration.getNumExploredStates() + " states while tracking "
                + trackedElements);
    }

    @Override
    public void sdgComputed(StateSpaceExploration exploration, Sdg sdg) {
        this.out.println(
                "SDG generated with " + sdg.getNodes().size() + " nodes and " + sdg.getEdges().size() + " edges");
    }

    @Override
    public void sdgPathFound(Entry policyEntry, Object path, boolean legal) {
        this.out.println("Found " + policyEntry + " path " + path);
    }

    @Override
    public void sdgPathNotFound(Entry policyEntry, boolean legal) {
        if (!legal) {
            this.out.println("Missing " + policyEntry);
        }
    }

    @Override
    public void tryPathResolution(SdgPath forbiddenPath) {
        this.out.println("Trying to resolve path " + forbiddenPath);
    }

    @Override
    public void selectPathRefinement(SdgPath path, SdgNode pathNode, SdgNode controlDependence, Sources refinement) {
        if (controlDependence == null) {
            this.out.println("Trying all sources of " + pathNode + ": " + refinement);
        } else {
            // The influencing node is the control condition of pathNode for the first two
            // selection strategies of the path resolution heuristic, and an arbitrary node of
            // pathNode's backwards slice for the third one; printing it makes visible which
            // strategy produced this candidate.
            this.out.println("Trying sources of " + pathNode + " via influencing node " + controlDependence + ": "
                    + refinement);
        }
    }

    @Override
    public void pathResolutionSuccess(SdgPath forbiddenPath, Sources addedTrackedElements) {
        this.out.println("Resolved through additionally tracking " + addedTrackedElements);
    }

    @Override
    public void pathResolutionFailure(SdgPath forbiddenPath, Sources addedTrackedElements, SdgPath newSimilarPath) {
        this.out.println("Still found path " + newSimilarPath + ", looking for another refinement ...");
    }

    @Override
    public void informationFlowOkay() {
        this.out.println("Success: No forbidden information flow");
    }

    @Override
    public void informationFlowFail(EntryType violatedType, boolean triedRefinements) {
        if (triedRefinements && violatedType == EntryType.FORBIDDEN) {
            this.out.println("Failure: No more relevant elements to track found, information flow suspected");
        } else {
            this.out.println("Failure: Policy violation");
        }
    }
}
