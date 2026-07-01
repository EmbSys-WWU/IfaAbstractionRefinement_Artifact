package de.tub.pes.syscir.analysis.syscdg;

/**
 * This class is an implementation of a Security Level. It includes methods which are needed for
 * proper Information Flow Analysis.
 *
 * @param <T> the type of the class, self referenced
 *
 * @author Jan Maria Kirchner
 */
public interface SecurityLevel<T extends SecurityLevel<T>> {

    public default boolean lessOrEqualTo(T other) {
        return this.leastUpperBound(other).equals(other);
    }

    // least upper bound
    public abstract T leastUpperBound(T other);

    // greatest lower bound
    public abstract T greatestLowerBound(T other);

    public abstract boolean equals(T other);
}
