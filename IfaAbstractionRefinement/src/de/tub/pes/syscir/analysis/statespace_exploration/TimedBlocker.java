package de.tub.pes.syscir.analysis.statespace_exploration;

/**
 * Class indicating that a Process or Event is waiting for some time, either for the next delta
 * cycle ({@link DeltaTimedBlocker} or for some real time ({@link RealTimedBlocker}).
 * <p>
 * Instances of this class are comparable, with delta cycles being less than any real time and equal
 * among themselves and shorter real times being less than larger real times.
 * 
 * @author Jonas Becker-Kupczok
 *
 */
public abstract sealed class TimedBlocker extends ProcessBlocker implements Comparable<TimedBlocker>
        permits DeltaTimeBlocker, RealTimedBlocker {

}
