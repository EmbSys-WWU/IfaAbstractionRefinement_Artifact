package de.tub.pes.syscir.analysis.abstraction_refinement;

import java.io.PrintStream;
import java.util.Objects;


public abstract class StreamRefinementLog implements RefinementLog {

    protected final PrintStream out;

    public StreamRefinementLog(PrintStream out) {
        this.out = Objects.requireNonNull(out);
    }

}
