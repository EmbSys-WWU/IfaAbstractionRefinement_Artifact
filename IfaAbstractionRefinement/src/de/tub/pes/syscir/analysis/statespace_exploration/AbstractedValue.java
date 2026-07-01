package de.tub.pes.syscir.analysis.statespace_exploration;

/**
 * Interface describing any abstraction of a semantic value.
 * <p>
 * Any such abstraction must account for absolutely determined values on the one hand as well as
 * values about which nothing is known on the other. Arbitrary levels of information in between are
 * permissible at the abstraction's discretion.
 * <p>
 * Instances of this class are assumed to be immutable.
 * 
 * @author Jonas Becker-Kupczok, Lukas Ernst
 */
public interface AbstractedValue {

    /**
     * Returns whether or not the value is absolutely determined.
     * 
     * @return whether the value is determined
     */
    boolean isDetermined();

    /**
     * Returns whether or not the value is at least somewhat undetermined.
     *
     * @return whether the value is undetermiend
     */
    default boolean isUndetermined() {
        return !isDetermined();
    }

    /**
     * Returns whether the underlying value is fully undetermined, i.e. equivalent to nonpresence. In
     * contrast to {@link #isUndetermined()}, this method would return false when an interval for the
     * value is known.
     * 
     * @return whether the value is fully unknown
     */
    boolean isUnknown();

    /**
     * Returns the determined value.
     * 
     * If this value is not determined, a RuntimeException at the discretion of the implementation is
     * thrown.
     * 
     * @return the determined value.
     * @throws RuntimeException if the value is not determined
     */
    Object get() throws RuntimeException;

}
