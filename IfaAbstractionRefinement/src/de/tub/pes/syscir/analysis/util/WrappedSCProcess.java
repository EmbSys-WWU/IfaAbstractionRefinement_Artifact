package de.tub.pes.syscir.analysis.util;

import de.tub.pes.syscir.sc_model.SCMODIFIER;
import de.tub.pes.syscir.sc_model.SCPROCESSTYPE;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.variables.SCEvent;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;


/**
 * Wrapper for {@link SCProcess} that allows no modifications and caches the hashCode. Assumes that
 * the original is not modified externally.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public class WrappedSCProcess {

    // cache of wrappers, avoiding the creation of a new wrapper every time the original is encountered
    private static final Map<SCProcess, WrappedSCProcess> wrapperCache = new WeakIdentityHashMap<>();

    /**
     * Returns a new wrapper around the original.
     * 
     * No guarantee is made with regards to the identity of the wrapper. If a wrapper already exists, it
     * may be reused. No check is made for whether or not the hashCode of that wrapper is still valid.
     * 
     * @param original an WrappedSCProcess
     * @return a wrapper around the original
     */
    public static WrappedSCProcess getWrapped(SCProcess original) {
        return wrapperCache.computeIfAbsent(original,
                orig -> new WrappedSCProcess(orig, WrappedSCFunction.getWrapped(orig.getFunction())));
    }

    private SCProcess original;

    private WrappedSCFunction function;
    private int hashCode;

    /**
     * Creates a new wrapper around the original, caching the originals current hashCode.
     *
     * @param original an SCProcess
     */
    public WrappedSCProcess(SCProcess original) {
        this(original, new WrappedSCFunction(original.getFunction()));
    }

    private WrappedSCProcess(SCProcess original, WrappedSCFunction function) {
        this.original = original;
        this.function = function;
        this.hashCode = original.hashCode();
    }

    public SCProcess getOriginal() {
        return this.original;
    }

    public String getName() {
        return this.original.getName();
    }

    public List<SCEvent> getSensitivity() {
        return this.original.getSensitivity();
    }

    public WrappedSCFunction getFunction() {
        return this.function;
    }

    public SCPROCESSTYPE getType() {
        return this.original.getType();
    }

    public EnumSet<SCMODIFIER> getModifier() {
        return this.original.getModifier();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WrappedSCProcess wrapped)) {
            return false;
        }
        return this.original.equals(wrapped.original);
    }

    @Override
    public String toString() {
        return this.original.toString();
    }

}
