package de.tub.pes.syscir.analysis.statespace_exploration;

import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import java.util.Collection;
import java.util.Set;

/**
 * Class representing the SystemC scheduler for the purpose of state space exploration.
 * <p>
 * Importantly, subclasses specify the semantics of the SystemC scheduler on a given state
 * abstraction by implementing {@link #endEvaluation(ConsideredState)}. Non-abstract subclasses will
 * therefore substitute the generic parameters for the specific state abstractions and types of
 * additional transition information for which they are designed.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of global state abstraction which this scheduler implementation
 *        can handle
 * @param <ProcessStateT> the type of local state abstraction which this scheduler implementation
 *        can handle
 * @param <InfoT> the type of additional transition information which this scheduler implementation
 *        can provide.
 * @param <ProcessTypeT> the type of process implementation this scheduler implementation can work
 *        together with
 */
public interface Scheduler {

    /**
     * Enum that describes the two possible stop modes in SystemC.
     *
     * @author Jonas Becker-Kupczok
     *
     */
    public static enum SimulationStopMode {
        /**
         * Indicates that after a call to sc_stop(), the simulation should terminate after the current delta
         * and update cycle has finished.
         */
        SC_STOP_FINISH_DELTA,

        /**
         * Indicates that after a call to sc_stop(), the simulation should terminate directly once the
         * calling process has yielded control.
         */
        SC_STOP_FINISH_IMMEDIATE;
    }

    /**
     * Returns the current stop mode.
     *
     * Usually, this will not depend on the state, as calls to sc_set_stop_mode() are not allowed from
     * the initialization phase onwards.
     *
     * @param currentState the current state
     * @return the current stop mode
     */
    SimulationStopMode getStopMode(ConsideredState currentState);

    /**
     * Returns the set of processes that might be ready to be scheduled in the given state.
     *
     * In addition to processes returned by {@link ConsideredState#getReadyProcesses()}, this may also
     * include processes whose scheduler state has been completely abstracted away such that they might
     * be ready, but are not guaranteed to be. The result may not contain duplicates.
     *
     * @param currentState the current state
     * @return all processes that might be ready to be scheduled in the current state
     */
    Collection<AnalyzedProcess> getReadyProcesses(ConsideredState currentState);

    /**
     * Returns whether or not the evaluation phase can be ended in the given state.
     *
     * This is typically the case if no process is guaranteed to be currently ready for scheduling
     * ({@link ConsideredState#getReadyProcesses()} is empty).
     *
     * @param currentState the current state
     * @return whether or not the evaluation phase can be ended in that state
     */
    boolean canEndEvaluation(ConsideredState currentState);

    /**
     * Advances to the next delta cycle or lets real time pass until a process becomes ready (see
     * {@link #canEndEvaluation(ConsideredState)}). Advancing to the next delta cycle includes
     * performing the update phase.
     *
     * The evaluation must be able to be ended in the given state. The returned set may be empty if the
     * end of the simulation has been reached. All states in the result must be locked.
     *
     * @param currentState the current state from which to advance the simulation
     * @return the set of possible transitions this scheduler might make when ending the evaluation in
     *         the given state
     */
    Set<ProcessTransitionResult> endEvaluation(ConsideredState currentState);

    /**
     * Notifies the given events. If the delay is null, this is an immediate notification.
     *
     * If the given current state is locked, all states in the result must be locked. Otherwise, all
     * resulting states must be unlocked and implementations are free to reuse the current state object.
     *
     * @param <TransitionResultT> the type of TransitionResult given and returned
     * @param transitionResult the current transition result
     * @param event the event to be scheduled
     * @param delay the delay after which to call the event, or null for an immediate notification
     * @return the set of possible transitions this scheduler might make by notifying the given event
     */
    Collection<TransitionResult> notifyEvents(TransitionResult transitionResult, Event event, TimedBlocker delay);

    /**
     * Simulates a call to sc_stop(), making sure the simulation terminates once the current process
     * yields or after the current delta cycle ends (depending on the simulation stop mode).
     *
     * If the given current state is locked, all states in the result must be locked. Otherwise, all
     * resulting states must be unlocked and implementations are free to reuse the current state object.
     *
     * @param <TransitionResultT> the type of TransitionResult given and returned
     * @param transitionResult the current transition result
     * @return the set of possible transitions this scheduler might make by stopping the simulation
     */
    Collection<TransitionResult> stopSimulation(TransitionResult transitionResult);

}
