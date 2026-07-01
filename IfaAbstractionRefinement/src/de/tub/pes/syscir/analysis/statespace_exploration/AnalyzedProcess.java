package de.tub.pes.syscir.analysis.statespace_exploration;

import java.util.Set;

import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCProcess;

/**
 * Interface representing a SystemC process for the purpose of state space exploration.
 * <p>
 * Importantly, implementing classes specify the semantics of SystemC models on a given state
 * abstraction by implementing {@link #makeStep(ConsideredState)}. Non-abstract implementing classes
 * will therefore substitute the generic parameters for the specific state abstractions and types of
 * additional transition information for which they are designed.
 * <p>
 * This interface only represents the implemented process, not its state. See {@link ProcessState}
 * for that. The visible state of implementing classes should be immutable.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of global state abstraction which this process implementation can
 *        handle
 * @param <ProcessStateT> the type of local state abstraction which this process implementation can
 *        handle
 * @param <InfoT> the type of additional transition information which this process implementation
 *        can provide.
 */
public interface AnalyzedProcess {

    /**
     * Computes the set of possible transitions that this process could make from the given state.
     * 
     * This process must be ready to be scheduled in the given state. The returned set may not be empty.
     * All resulting states contained in the returned set must be locked (see
     * {@link ConsideredState#lock()}).
     * 
     * @param currentState the current (abstracted) state the SystemC model
     * @return the set of possible transitions this process might make when scheduled with the given
     *         state
     */
    Set<ProcessTransitionResult> makeStep(ConsideredState currentState);

    /**
     * Returns the SysCIR process represented by this process.
     *
     * @return the SysCIR process
     */
    WrappedSCProcess getSCProcess();

    /**
     * Returns the name of the SysCIR process represented by this process.
     *
     * @return the name of the SysCIR process
     */
    public default String getName() {
        return getSCProcess().getName();
    }

    /**
     * Returns the SysCIR class instance to which this process belongs.
     *
     * @return the SysCIR instance
     */
    WrappedSCClassInstance getSCClassInstance();

}
