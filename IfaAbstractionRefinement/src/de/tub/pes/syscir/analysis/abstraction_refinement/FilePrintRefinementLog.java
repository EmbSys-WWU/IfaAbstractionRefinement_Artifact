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
import de.tub.pes.syscir.analysis.util.TeeOutputStream;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Function;

/**
 * A {@link PrintRefinementLog} that writes all output to a file.
 *
 * <p>
 * Implements {@link AutoCloseable} so it can be used in a try-with-resources block to ensure the
 * underlying file is always closed:
 *
 * <pre>{@code
 * try (FilePrintRefinementLog log = new FilePrintRefinementLog("analysis.log")) {
 *     runAnalysis(log);
 * }
 * }</pre>
 */
public class FilePrintRefinementLog implements RefinementLog, AutoCloseable {

    protected final RefinementLog delegate;
    protected final PrintStream fileOut;
    protected final PrintStream bothOut;

    /**
     * Creates a log that writes text encoded with {@code charset} to {@code file}.
     *
     * @param file target file; created if it does not exist
     * @param charset charset used for encoding
     * @throws FileNotFoundException if the file cannot be opened for writing
     */
    @SuppressWarnings("resource")
    public FilePrintRefinementLog(Function<PrintStream, RefinementLog> delegate, File file)
            throws FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(file);
        // autoFlush = true so every println() reaches the file immediately
        this.bothOut = new PrintStream(new TeeOutputStream(System.out, fos), true);
        this.delegate = delegate.apply(this.bothOut);
        this.fileOut = new PrintStream(fos, true, StandardCharsets.UTF_8);
    }

    @Override
    public void sdgComputed(StateSpaceExploration exploration, Sdg sdg) {
        this.delegate.sdgComputed(exploration, sdg);

        this.fileOut.println("SDG: " + sdg);
        System.out.println("(SDG printed to file.)");
    }

    @Override
    public void evaluation(Expression expression, AbstractedValue result) {
        this.delegate.evaluation(expression, result);
    }

    @Override
    public void insufficientTracking(InsufficientValueTrackingException exception) {
        this.delegate.insufficientTracking(exception);
    }

    @Override
    public void unknownControlCondition(Expression condition, AbstractedValue value) {
        this.delegate.unknownControlCondition(condition, value);
    }

    @Override
    public void identifiedSplitters(Collection<ReconstructionNode> splitters) {
        this.delegate.identifiedSplitters(splitters);
    }

    @Override
    public void tooManyPartialDescendants(Branching branching) {
        this.delegate.tooManyPartialDescendants(branching);
    }

    @Override
    public void restartingExploration(Sources addedTrackedElements) {
        this.delegate.restartingExploration(addedTrackedElements);
    }

    @Override
    public void explorableAbstractionFound(StateSpaceExploration exploration, Sources trackedElements) {
        this.delegate.explorableAbstractionFound(exploration, trackedElements);
    }

    @Override
    public void explorationTimedOut(StateSpaceExploration exploration, Sources trackedElements) {
        this.delegate.explorationTimedOut(exploration, trackedElements);
    }

    @Override
    public void sdgPathFound(Entry policyEntry, Object path, boolean legal) {
        this.delegate.sdgPathFound(policyEntry, path, legal);
    }

    @Override
    public void sdgPathNotFound(Entry policyEntry, boolean legal) {
        this.delegate.sdgPathNotFound(policyEntry, legal);
    }

    @Override
    public void tryPathResolution(SdgPath forbiddenPath) {
        this.delegate.tryPathResolution(forbiddenPath);
    }

    @Override
    public void selectPathRefinement(SdgPath path, SdgNode pathNode, SdgNode controlDependence, Sources refinement) {
        this.delegate.selectPathRefinement(path, pathNode, controlDependence, refinement);
    }

    @Override
    public void pathResolutionSuccess(SdgPath forbiddenPath, Sources refinement) {
        this.delegate.pathResolutionSuccess(forbiddenPath, refinement);
    }

    @Override
    public void pathResolutionFailure(SdgPath forbiddenPath, Sources refinement, SdgPath newSimilarPath) {
        this.delegate.pathResolutionFailure(forbiddenPath, refinement, newSimilarPath);
    }

    @Override
    public void informationFlowOkay() {
        this.delegate.informationFlowOkay();
    }

    @Override
    public void informationFlowFail(EntryType violatedType, boolean triedRefinements) {
        this.delegate.informationFlowFail(violatedType, triedRefinements);
    }

    /**
     * Flushes and closes the underlying file. After this call the log must no longer be used.
     */
    @Override
    public void close() {
        this.bothOut.close();
        this.fileOut.close();
    }
}
