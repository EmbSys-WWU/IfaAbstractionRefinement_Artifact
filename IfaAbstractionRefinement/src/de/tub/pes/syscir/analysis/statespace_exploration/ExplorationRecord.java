package de.tub.pes.syscir.analysis.statespace_exploration;

/**
 * Interface representing a record of the explorations made during a state space exploration.
 * 
 * Subclasses may arbitrarily decide which information is retained or discarded and in which form
 * information is stored, processed or presented.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of abstraction of the global state considered by this record
 * @param <ProcessStateT> the type of abstraction of the local state considered by this record
 * @param <InfoT> the type of additional transition information gathered by this record
 */
public interface ExplorationRecord {
    
    /**
     * Called whenever a new transition is explored by the {@link StateSpaceExploration}.
     * 
     * @param from the state from which the transition originates
     * @param to the state to which the model transitioned
     * @param info additional transition information provided by the {@link AnalyzedProcess}
     *        implementation.
     */
    void explorationMade(ConsideredState from, ConsideredState to, TransitionInformation info);
    
    /**
     * ExplorationRecord that records nothing.
     */
    public static final ExplorationRecord NONE = (_, _, _) -> {
    };
    
    /**
     * Constructs an ExplorationRecord that delegates calls to all specified records.
     */
    public static ExplorationRecord multiple(ExplorationRecord... records) {
        return (from, to, info) -> {
            for (ExplorationRecord record : records) {
                record.explorationMade(from, to, info);
            }
        };
    }
    
}
