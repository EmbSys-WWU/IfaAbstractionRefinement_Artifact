package de.tub.pes.syscir.analysis.statespace_exploration;

/**
 * Class representing that a process cannot be scheduled because it has terminated.
 * 
 * A process that has terminated will typically never be able to be scheduled again.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public final class ProcessTerminatedBlocker extends ProcessBlocker {

    public static final ProcessTerminatedBlocker INSTANCE = new ProcessTerminatedBlocker();

    private ProcessTerminatedBlocker() {}

    @Override
    public String toString() {
        return "DONE";
    }

}
