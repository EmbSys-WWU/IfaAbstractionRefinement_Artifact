package de.tub.pes.syscir.analysis.statespace_exploration;

/**
 * Class indicating that a {@link AnalyzedProcess} or {@link Event} is waiting for the next delta
 * cycle.
 * 
 * This class is a singleton, always use the singleton value {@link #INSTANCE}.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public final class DeltaTimeBlocker extends TimedBlocker {

    public static final DeltaTimeBlocker INSTANCE = new DeltaTimeBlocker();

    private DeltaTimeBlocker() {

    }

    @Override
    public int compareTo(TimedBlocker other) {
        if (other instanceof RealTimedBlocker) {
            return 1;
        }

        assert other instanceof DeltaTimeBlocker;
        return 0;
    }

    @Override
    public String toString() {
        return "Δ";
    }

}
