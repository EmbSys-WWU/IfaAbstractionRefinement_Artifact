package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import java.util.Set;

import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.sc_model.expressions.Expression;

/**
 * An interface for classes which can provide some kind of transition information.
 *
 * Unless otherwise specified it is left to the implementation whether for methods to return the
 * same instance as passed with modified values or a completely new instance.
 *
 * @author Jonas Becker-Kupczok
 */
public interface InformationHandler {

    /**
     * Returns initial transition information to begin a large step. For information
     * that is local to the large step, this would typically represent no
     * information, i.e. be neutral with respect to
     * {@link ComposableTransitionInformation#compose(ComposableTransitionInformation)}.
     *
     * @param state the state from which the large step begins
     * @return initial transition information
     */
    TransitionInformation getInitialInformation(ConsideredState state);

    /**
     * Called once before the evaluation of some code starts, i.e. at the start of
     * {@link AnalyzedProcess#makeStep(ConsideredState)} and before any update within
     * {@link BaseScheduler#doUpdateCycle(ConsideredState)}.
     * 
     * The result should take into account the information from previous steps already contained in
     * resultingState.
     * 
     * @param <LocalStateT> the type of local start used for the evaluation of that code
     * @param currentState the state at the start of the evaluation
     * @param localState the local part of that state
     * @return the information describing the start of the evaluation
     */
    TransitionInformation handleStartOfCode(TransitionResult currentState, LocalState localState);

    /**
     * Called before any expression is evaluated to allow the handler to prepare for the subsequent
     * invocation of {@link #handleExpressionEvaluation(Expression, int, TransitionResult, LocalState)}.
     * 
     * @param expression the expression to be evaluated (is null when evaluating function body)
     * @param currentState the state before the evaluation
     * @param localState the local part of the state
     */
    default void announceEvaluation(Expression expression, TransitionResult currentState, LocalState localState) {}

    /**
     * Returns the information describing the evaluation step that just occured.
     * 
     * The result should take into account the information from previous steps already contained in
     * resultingState.
     * 
     * @param evaluated the expression that was just evaluated (is null when evaluating function body)
     * @param comingFrom from where the expression was entered
     * @param resultingState the result of the small step
     * @param localState the local part of the result
     * @return the information describing the step
     */
    TransitionInformation handleExpressionEvaluation(Expression evaluated,
            int comingFrom, TransitionResult resultingState, LocalState localState);

    default boolean skipReexploration(TransitionInformation oldInformation, TransitionResult newTransition) {
        return false;
    }

    /**
     * Whether large steps should consist of a minimal amount of small steps. If
     * this is true, any recurring state from a small step is guaranteed to be
     * recognized, so the generated TransitionInformations would be composed.
     * Otherwise, only the most important (typically repetitive) states in a small
     * step will be stored for that purpose, saving memory.
     */
    default boolean wantsMinimalSteps() {
        return false;
    }

    /**
     * Called once a state transition is complete to allow the handler to clean up transient data or
     * otherwise finalize the information before it is returned to the {@link StateSpaceExploration}.
     *
     * @param information the information gathered for the transition
     * @return the finalized version of that information
     */
    default TransitionInformation finalizeInformation(TransitionInformation information) {
        return information;
    }

    default void handleEndOfBlock() {
    }

    /**
     * Called whenver a process was made ready because the delta cycle it waited for ended.
     * 
     * The result should take into account the current information from previous steps.
     *
     * @param process the made ready process
     * @param resultingState the resulting state of that process
     * @param currentInformation the information gathered from previous steps
     * @return the information describing this step
     */
    TransitionInformation handleProcessWaitedForDelta(AnalyzedProcess process, ProcessState resultingState,
            TransitionInformation currentInformation);


    /**
     * Called whenver a process was made ready because the time it waited for passed.
     * 
     * The result should take into account the current information from previous steps.
     *
     * @param process the made ready process
     * @param resultingState the resulting state of that process
     * @param currentInformation the information gathered from previous steps
     * @return the information describing this step
     */
    TransitionInformation handleProcessWaitedForTime(AnalyzedProcess process, ProcessState resultingState,
            TransitionInformation currentInformation);


    /**
     * Called whenver an event a process was waiting for was triggered.
     * 
     * The result should take into account the current information from previous steps. The process may
     * not have waited for any of the notified events and therefore remain uneffected.
     *
     * @param process the process
     * @param resultingState the resulting state of that process
     * @param events the events that were notified
     * @param blockerBefore the blocker that was blocking the process before this step
     * @param currentInformation the information gathered from previous steps
     * @return the information describing this step
     */
    TransitionInformation handleProcessWaitedForEvents(AnalyzedProcess process,
            ProcessState resultingState, Set<Event> events, EventBlocker blockerBefore,
            TransitionInformation currentInformation);

}
