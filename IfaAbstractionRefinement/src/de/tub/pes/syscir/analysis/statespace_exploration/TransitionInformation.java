package de.tub.pes.syscir.analysis.statespace_exploration;

/**
 * Interface representing some kind of additional information that a {@link AnalyzedProcess} or
 * {@link Scheduler} may provide for each transition.
 * 
 * @author Jonas Becker-Kupczok
 *
 */
public interface TransitionInformation {

    /**
     * Returns a clone of this object.
     * 
     * @return clone
     */
    TransitionInformation clone();

    /**
     * Returns a composition of this information with the specified other information. This may, but is
     * not required to, return itself with appropriate modification.
     *
     * @return the composed information
     */
    TransitionInformation compose(TransitionInformation other);

}
