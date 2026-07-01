package de.tub.pes.syscir.analysis.util;

import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.Map;

/**
 * Wrapper for {@link SCFunction} that allows no modifications and caches the hashCode. Assumes that
 * the original is not modified externally.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public class WrappedExpression<T extends Expression> {

    // cache of wrappers, avoiding the creation of a new wrapper every time the original is encountered
    private static final Map<Expression, WrappedExpression<?>> wrapperCache = new WeakIdentityHashMap<>();

    /**
     * Returns a new wrapper around the original.
     * 
     * No guarantee is made with regards to the identity of the wrapper. If a wrapper already exists, it
     * may be reused. No check is made for whether or not the hashCode of that wrapper is still valid.
     * 
     * @param original an WrappedSCFunction
     * @return a wrapper around the original
     */
    @SuppressWarnings("unchecked")
    public static <T extends Expression> WrappedExpression<T> getWrapped(T original) {
        return (WrappedExpression<T>) wrapperCache.computeIfAbsent(original, WrappedExpression::new);
    }

    private T original;

    private int hashCode;

    /**
     * Creates a new wrapper around the original, caching the originals current hashCode.
     *
     * @param original an SCFunction
     */
    public WrappedExpression(T original) {
        this.original = original;
        this.hashCode = original.hashCode();
    }

    public T getOriginal() {
        return this.original;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WrappedExpression wrapped)) {
            return false;
        }
        return this.original.equals(wrapped.original);
    }

    @Override
    public String toString() {
        return this.original.toString();
    }

}
