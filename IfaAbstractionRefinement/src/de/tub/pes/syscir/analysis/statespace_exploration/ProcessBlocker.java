package de.tub.pes.syscir.analysis.statespace_exploration;

/**
 * Represents something which a {@link AnalyzedProcess} can be waiting for, either an event
 * ({@link EventBlocker}) or some time ({@link TimedBlocker}).
 * 
 * All instances of this class are immutable.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public abstract sealed class ProcessBlocker permits TimedBlocker, EventBlocker, ProcessTerminatedBlocker {

}
