package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import static de.tub.pes.syscir.analysis.util.WrapperUtil.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.Predicate;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.DeltaTimeBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.GlobalState;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessTerminatedBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessTransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.RealTimedBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.SchedulerTransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration.ExplorationAbortedException;
import de.tub.pes.syscir.analysis.statespace_exploration.TimedBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.util.CollectionUtil;
import de.tub.pes.syscir.analysis.util.LockableObject;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCFunction;
import de.tub.pes.syscir.sc_model.SCSystem;

/**
 * Class implementing a basic scheduler semantic by advancing delta or real time as appropriate and
 * traversing the expression tree and control flow graph of update methods for update phases.
 *
 * This class appears as immutable and is thread-safe except for changes in the underlying SysCIR.
 * Such changes lead to undefined behavior.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of global state abstraction which this scheduler implementation can handle
 * @param <ProcessStateT> the type of process state abstraction which this scheduler implementation can handle
 * @param <LocalStateT> the type of local scheduler state abstraction which this scheduler implementation can
 *        handle
 * @param <InfoT> the type of additional transition information which this scheduler implementation can
 *        provide.
 * @param <ValueT> the type of abstracted value which this scheduler implementation can handle
 */
public abstract class BaseScheduler extends ExpressionCrawler implements Scheduler {

    private final SimulationStopMode stopMode;
    private final Predicate<? super Event> eventConsiderationCondition;

    // Cache ready processes because canAdvanceSimulation occurs right before getReadyProcesses in
    // StateSpaceExplorer#explore.
    private ThreadLocal<ConsideredState> lastOptainedReadyProcessesFor;
    private ThreadLocal<Collection<AnalyzedProcess>> lastOptainedReadyProcesses;

    /**
     * Creates a new StandardScheduler with the given stop mode.
     *
     * @param scSystem the SysCIR representation of the entire SystemC design
     * @param stopMode the stop mode for this scheduler
     */
    public BaseScheduler(SCSystem scSystem, AbstractedLogic logic, Interceptor interceptor,
            SimulationStopMode stopMode, Predicate<? super Event> eventConsiderationCondition) {
        super(scSystem, null, logic, interceptor);

        this.stopMode = Objects.requireNonNull(stopMode);
        this.eventConsiderationCondition = Objects.requireNonNull(eventConsiderationCondition);

        this.lastOptainedReadyProcessesFor = new ThreadLocal<>();
        this.lastOptainedReadyProcesses = new ThreadLocal<>();
    }

    @Override
    public SimulationStopMode getStopMode(ConsideredState currentState) {
        return this.stopMode;
    }

    @Override
    public LocalState getLocalState(TransitionResult currentState) {
        return ((SchedulerTransitionResult) currentState).schedulerState();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If {@link GlobalState#isSimulationStopped()} returns true, the result is an empty
     * collection.
     */
    @Override
    public Collection<AnalyzedProcess> getReadyProcesses(ConsideredState currentState) {
        if (currentState.getGlobalState().isSimulationStopped()
                && this.stopMode == SimulationStopMode.SC_STOP_FINISH_IMMEDIATE) {
            return Set.of();
        }

        Collection<AnalyzedProcess> guaranteedReady = getReadyProcessesInternal(currentState);
        Set<AnalyzedProcess> maybeReady = new LinkedHashSet<>();
        for (Entry<AnalyzedProcess, ProcessState> entry : currentState.getProcessStates().entrySet()) {
            ProcessState state = entry.getValue();
            if (!(state.getWaitingFor() instanceof EventBlocker eb) ){
                continue;
            }
            if (eb.isChoice()) {
                if (!eb.getEvents().stream().allMatch(this.eventConsiderationCondition)) {
                    maybeReady.add(entry.getKey());
                }
            } else {
                if (!eb.getEvents().stream().anyMatch(this.eventConsiderationCondition)) {
                    maybeReady.add(entry.getKey());
                }
            }
        }

        if (maybeReady.isEmpty()) {
            return guaranteedReady;
        }

        maybeReady.addAll(guaranteedReady);
        return maybeReady;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If {@link GlobalState#isSimulationStopped()} returns true, the result is false.
     */
    @Override
    public boolean canEndEvaluation(ConsideredState currentState) {
        if (currentState.getGlobalState().isSimulationStopped()
                && this.stopMode == SimulationStopMode.SC_STOP_FINISH_IMMEDIATE) {
            return false;
        }

        return getReadyProcessesInternal(currentState).isEmpty();
    }

    /**
     * Returns all processes that are guaranteed to be ready.
     *
     * This method ignores the result of {@link GlobalState#isSimulationStopped()}.
     *
     * @param currentState the current state
     * @return collection of processes guaranteed to be ready
     */
    protected Collection<AnalyzedProcess> getReadyProcessesInternal(ConsideredState currentState) {
        if (currentState == this.lastOptainedReadyProcessesFor.get()) {
            return this.lastOptainedReadyProcesses.get();
        }

        Collection<AnalyzedProcess> result = currentState.getReadyProcesses();
        if (currentState.isLocked()) {
            this.lastOptainedReadyProcessesFor.set(currentState);
            this.lastOptainedReadyProcesses.set(result);
        }
        return result;
    }

    @Override
    public Set<ProcessTransitionResult> endEvaluation(ConsideredState currentState) {
        StateSpaceExploration explorer = StateSpaceExploration.getCurrentExplorer();
        if (explorer.isAborted()) {
            throw new ExplorationAbortedException();
        }

        currentState = currentState.unlockedClone();

        // do update cycle
        Map<ConsideredState, TransitionInformation> updateResults = doUpdateCycle(currentState);

        Set<ProcessTransitionResult> results = new LinkedHashSet<>();
        for (Entry<ConsideredState, TransitionInformation> updateResult : updateResults.entrySet()) {
            if (explorer.isAborted()) {
                throw new ExplorationAbortedException();
            }

            ProcessTransitionResult asTransitionResult =
                    new ProcessTransitionResult(updateResult.getKey(), updateResult.getValue());
            ProcessTransitionResult result = advanceSimulation(asTransitionResult);
            if (result == null) {
                // TODO: wrong? explorer would want to explore further from here, leading to another scheduler call.
                // but, would that do anything bad?
                results.add(finalizeTransitionResult(asTransitionResult));
            } else {
                results.add(finalizeTransitionResult(result));
            }
        }

        if (!updateResults.isEmpty())
            getInformationHandler().handleEndOfBlock();

        return results;
    }

    // parameters and results unlocked
    // TODO: should always be deterministic, right? so only one result, no set?
    // TODO: meh, maybe not? at least order of updates doesn't matter, right? RIGHT?!
    public Map<ConsideredState, TransitionInformation> doUpdateCycle(ConsideredState currentState) {
        TransitionInformation initialInfo = getInformationHandler().getInitialInformation(currentState);
        List<WrappedSCClassInstance> requestedUpdates =
                new ArrayList<>(currentState.getGlobalState().getRequestedUpdates());
        currentState.getGlobalState().setRequestedUpdates(new LinkedHashSet<>());
        Map<ConsideredState, TransitionInformation> currentTransitions =
                Map.of(currentState, initialInfo);

        for (WrappedSCClassInstance requested : requestedUpdates) {
            currentTransitions = updatePort(requested, currentTransitions);
        }

        return currentTransitions;
    }

    public Map<ConsideredState, TransitionInformation> updatePort(
            WrappedSCClassInstance port,
            Map<ConsideredState, TransitionInformation> ingoingTransitions) {
        Map<SchedulerConsideredState, TransitionInformation> seenTransitions = new LinkedHashMap<>();
        SequencedMap<SchedulerConsideredState, TransitionInformation> transitionsToHandle = new LinkedHashMap<>();
        Map<ConsideredState, TransitionInformation> resultingTransitions = new LinkedHashMap<>();

        for (Entry<ConsideredState, TransitionInformation> ingoingTransition : ingoingTransitions.entrySet()) {
            // TODO: can there be more than one channel per port? why?
            WrappedSCFunction updateFunction = wrap(port.getSCClass().getMemberFunctionByName("update"));
            LocalState localState = constructLocalSchedulerState(port, updateFunction);

            SchedulerConsideredState initialConsideredState =
                    new SchedulerConsideredState(ingoingTransition.getKey(), localState);
            SchedulerTransitionResult initialTransitionResult =
                    new SchedulerTransitionResult(initialConsideredState, ingoingTransition.getValue());
            TransitionInformation initialInformation =
                    getInformationHandler().handleStartOfCode(initialTransitionResult, localState);
            transitionsToHandle.put(initialConsideredState, initialInformation);
        }

        while (!transitionsToHandle.isEmpty()) {
            SchedulerTransitionResult nextToHandle =
                    new SchedulerTransitionResult(transitionsToHandle.firstEntry().getKey(),
                            transitionsToHandle.pollFirstEntry().getValue());
            SmallStepResult nextTransitions = makeSmallStep(nextToHandle);

            if (nextTransitions.endOfStep()) {
                for (TransitionResult transition : nextTransitions.transitions()) {
                    resultingTransitions.merge(transition.resultingState(), transition.transitionInformation(),
                            TransitionInformation::compose);
                }
            } else if (nextTransitions.possiblyRepeatingStep() || getInformationHandler().wantsMinimalSteps()) {
                // make sure that possibly repeating steps are only considered once
                for (TransitionResult transition : nextTransitions.transitions()) {
                    SchedulerConsideredState state =
                            new SchedulerConsideredState((SchedulerTransitionResult) transition);
                    state.lock();

                    TransitionInformation oldInformation = seenTransitions.get(state);
                    if (oldInformation != null &&
                            getInformationHandler().skipReexploration(oldInformation, transition)) {
                        continue;
                    }
                    if (transition.transitionInformation().equals(oldInformation)) {
                        continue;
                    }
                    TransitionInformation newInformation = seenTransitions.merge(state,
                            transition.transitionInformation(), TransitionInformation::compose);
                    if (newInformation.equals(oldInformation)) {
                        continue;
                    }

                    transitionsToHandle.merge(state.unlockedClone(), newInformation.clone(),
                            TransitionInformation::compose);
                }
            } else {
                for (TransitionResult transition : nextTransitions.transitions()) {
                    SchedulerConsideredState state =
                            new SchedulerConsideredState((SchedulerTransitionResult) transition);
                    transitionsToHandle.merge(state.unlockedVersion(), transition.transitionInformation(),
                            TransitionInformation::compose);
                }
            }
        }

        return resultingTransitions;
    }

    public abstract LocalState constructLocalSchedulerState(WrappedSCClassInstance port, WrappedSCFunction entryPoint);

    @Override
    public SmallStepResult handleEndOfCodeReached(TransitionResult currentState, LocalState localState,
            List<EvaluationContext> stack) {
        return createSmallStepResult(null, -2, currentState, localState, true, false);
    }

    public ProcessTransitionResult advanceSimulation(ProcessTransitionResult updateResult) {
        ConsideredState currentState = updateResult.resultingState();
        TransitionInformation currentInformation = updateResult.transitionInformation();

        // finish simulation of sc_stop() has been called
        if (currentState.getGlobalState().isSimulationStopped()) {
            // stopMode must be finish delta, otherwise canAdvanceSimulation returns false
            assert this.stopMode == SimulationStopMode.SC_STOP_FINISH_DELTA;

            currentState.lock();
            return updateResult;
        }

        // group processes and events by what they are waiting for
        Set<Entry<AnalyzedProcess, ProcessState>> deltaWaitingProcesses = new LinkedHashSet<>();
        Set<Entry<AnalyzedProcess, ProcessState>> timeWaitingProcesses = new LinkedHashSet<>();
        Set<Entry<AnalyzedProcess, ProcessState>> eventWaitingProcesses = new LinkedHashSet<>();

        Set<Event> deltaWaitingEvents = new LinkedHashSet<>();

        RealTimedBlocker earliestBlocker = null;

        // find processes waiting for delta, time or event
        for (Entry<AnalyzedProcess, ProcessState> entry : currentState.getProcessStates().entrySet()) {
            if (entry.getValue().getWaitingFor() == DeltaTimeBlocker.INSTANCE) {
                deltaWaitingProcesses.add(entry);
            } else if (entry.getValue().getWaitingFor() instanceof RealTimedBlocker rtb) {
                timeWaitingProcesses.add(entry);
                if (earliestBlocker == null || rtb.compareTo(earliestBlocker) < 0) {
                    earliestBlocker = rtb;
                }
            } else if (entry.getValue().getWaitingFor() instanceof EventBlocker eb) {
                eventWaitingProcesses.add(entry);
                if (eb.getTimeout() != null) {
                    if (eb.getTimeout() == DeltaTimeBlocker.INSTANCE) {
                        deltaWaitingProcesses.add(entry);
                    } else {
                        timeWaitingProcesses.add(entry);
                        if (earliestBlocker == null || eb.getTimeout().compareTo(earliestBlocker) < 0) {
                            earliestBlocker = (RealTimedBlocker) eb.getTimeout();
                        }
                    }
                }
            } else {
                // no process should be ready!
                assert entry.getValue().getWaitingFor() == ProcessTerminatedBlocker.INSTANCE;
            }
        }

        // find events waiting for delta or time
        for (Entry<Event, TimedBlocker> entry : currentState.getGlobalState().getEventsWithStates()) {
            if (entry.getValue() == DeltaTimeBlocker.INSTANCE) {
                deltaWaitingEvents.add(entry.getKey());
            } else if (entry.getValue() instanceof RealTimedBlocker rtb) {
                if (earliestBlocker == null || rtb.compareTo(earliestBlocker) < 0) {
                    earliestBlocker = rtb;
                }
            } else {
                // events are either waiting for delta or for real time, or they are not pending (and then should
                // not be included in the map)
                assert false;
            }
        }

        // if something waits for delta, do delta cycle
        if (!deltaWaitingProcesses.isEmpty() || !deltaWaitingEvents.isEmpty()) {
            return doDeltaCycle(currentState, currentInformation, deltaWaitingProcesses, eventWaitingProcesses,
                    deltaWaitingEvents);
        }

        // if no process or event is waiting for delta or time, nothing will ever happen again
        if (timeWaitingProcesses.isEmpty() && currentState.getGlobalState().getEventStates().isEmpty()) {
            return null; // end of simulation
        }

        return letTimePass(currentState, currentInformation, timeWaitingProcesses, eventWaitingProcesses, entry -> true,
                earliestBlocker);
    }

    public ProcessTransitionResult doDeltaCycle(ConsideredState currentState, TransitionInformation currentInformation,
            Set<Entry<AnalyzedProcess, ProcessState>> deltaWaitingProcesses,
            Set<Entry<AnalyzedProcess, ProcessState>> eventWaitingProcesses, Set<Event> deltaWaitingEvents) {
        // compute new event states by replacing those waiting for delta by ones not pending
        Map<Event, TimedBlocker> eventStates = currentState.getGlobalState().getEventStates();
        for (Event event : deltaWaitingEvents) {
            eventStates.remove(event);
        }

        // processes waiting on events just notified are considered waiting for the same delta cycle
        // TODO: assuming that process waiting for delta-waiting event becomes ready in the same delta cycle
        // the event is called
        for (Entry<AnalyzedProcess, ProcessState> entry : eventWaitingProcesses) {
            currentInformation = notifyEventsForProcess(entry, currentState, deltaWaitingEvents, currentInformation);
        }

        // compute new process states by replacing those waiting for delta by ready ones
        for (Entry<AnalyzedProcess, ProcessState> entry : deltaWaitingProcesses) {
            entry.getValue().setWaitingFor(null);
            currentInformation = getInformationHandler().handleProcessWaitedForDelta(entry.getKey(), entry.getValue(),
                    currentInformation);
        }

        currentState.lock();
        return new ProcessTransitionResult(currentState, currentInformation);
    }

    public ProcessTransitionResult letTimePass(ConsideredState currentState, TransitionInformation currentInformation,
            Set<Entry<AnalyzedProcess, ProcessState>> timeWaitingProcesses,
            Set<Entry<AnalyzedProcess, ProcessState>> eventWaitingProcesses,
            Predicate<Entry<Event, TimedBlocker>> eventAdvancementCondition,
            RealTimedBlocker amountOfTime) {
        Set<Event> notifiedEvents = new LinkedHashSet<>();
        // compute new event states by replacing those waiting for the shortest time by ones not pending and
        // subtracting the waited time from all others
        Map<Event, TimedBlocker> eventStates = currentState.getGlobalState().getEventStates();
        Iterator<Entry<Event, TimedBlocker>> entryIt = eventStates.entrySet().iterator();
        while (entryIt.hasNext()) {
            Entry<Event, TimedBlocker> entry = entryIt.next();
            if (!(entry.getValue() instanceof RealTimedBlocker rtb)) {
                // when called from #skipTimingLoop, events waiting for delta might be present and should be
                // ignored
                continue;
            }
            if (!eventAdvancementCondition.test(entry)) {
                continue;
            }

            if (entry.getValue().equals(amountOfTime)) {
                notifiedEvents.add(entry.getKey());
                entryIt.remove();
            } else {
                entry.setValue(rtb.subtract(amountOfTime));
            }
        }

        // processes waiting on events just notified are considered waiting for the same delta cycle
        // TODO: assuming that process waiting for time-waiting event becomes ready immediately when the
        // event is called
        for (Entry<AnalyzedProcess, ProcessState> entry : eventWaitingProcesses) {
            currentInformation = notifyEventsForProcess(entry, currentState, notifiedEvents, currentInformation);
        }

        // compute new process states by replacing those waiting for the shortest time or for events that
        // are notified by ready ones and subtracting the waited time from all others
        for (Entry<AnalyzedProcess, ProcessState> entry : timeWaitingProcesses) {
            // cannot be waiting for delta because already filtered before passed as a parameter
            ProcessBlocker waitingFor = entry.getValue().getWaitingFor();
            RealTimedBlocker timer;
            if (waitingFor == null) {
                // might have become ready by notifying an event
                continue;
            } else if (entry.getValue().getWaitingFor() instanceof EventBlocker eb) {
                // waitingFor might already have been modified by removing some events
                timer = (RealTimedBlocker) eb.getTimeout();
            } else {
                timer = (RealTimedBlocker) waitingFor;
            }

            // reduce waiting time
            ProcessBlocker replacement = timer.equals(amountOfTime) ? null : timer.subtract(amountOfTime);
            if (replacement != null && waitingFor instanceof EventBlocker eb) {
                replacement = eb.replaceTimeout((RealTimedBlocker) replacement);
            }

            entry.getValue().setWaitingFor(replacement);
            currentInformation = getInformationHandler().handleProcessWaitedForTime(entry.getKey(), entry.getValue(),
                    currentInformation);
        }

        currentState.lock();
        return new ProcessTransitionResult(currentState, currentInformation);
    }

    /**
     * Called for each process waiting for an EventBlocker whenever events are notified.
     *
     * Updates the waitingFor state of the process accordingly.
     *
     * @param entry
     * @param currentState
     * @param notifiedEvents
     * @param notifiedProcesses
     * @param newProcessStates
     */
    private TransitionInformation notifyEventsForProcess(Entry<AnalyzedProcess, ProcessState> entry,
            ConsideredState currentState, Set<Event> notifiedEvents,
            TransitionInformation currentInformation) {
        EventBlocker blocker = ((EventBlocker) entry.getValue().getWaitingFor());

        Set<Event> remainingEvents = CollectionUtil.setDiff(blocker.getEvents(), notifiedEvents);

        if (remainingEvents.size() == blocker.getEvents().size()) {
            return currentInformation;
        }

        if (remainingEvents.isEmpty() || blocker.isChoice()) {
            entry.getValue().setWaitingFor(null);
        } else {
            entry.getValue().setWaitingFor(blocker.replaceEvents(remainingEvents));
        }
        return getInformationHandler().handleProcessWaitedForEvents(entry.getKey(), entry.getValue(), notifiedEvents,
                blocker,
                currentInformation);

    }

    @Override
    public Collection<TransitionResult> notifyEvents(TransitionResult transitionResult,
            Event event, TimedBlocker delay) {
        ConsideredState currentState = transitionResult.resultingState();

        if (currentState.getGlobalState().isSimulationStopped()
                && this.stopMode == Scheduler.SimulationStopMode.SC_STOP_FINISH_IMMEDIATE) {
            return Set.of(transitionResult);
        }

        if (!this.eventConsiderationCondition.test(event)) {
            return Set.of(transitionResult);
        }

        boolean wasLocked = currentState.isLocked();
        currentState = LockableObject.unlockedVersion(currentState);
        TransitionInformation currentInformation = transitionResult.transitionInformation();

        Map<Event, TimedBlocker> eventStates = currentState.getGlobalState().getEventStates();

        if (delay != null) {
            eventStates.merge(event, delay,
                    (oldBlock, newBlock) -> oldBlock.compareTo(newBlock) <= 0 ? oldBlock : newBlock);
        } else {
            eventStates.remove(event);

            Iterator<Entry<AnalyzedProcess, ProcessState>> entryIt =
                    currentState.getProcessStates().entrySet().iterator();
            while (entryIt.hasNext()) {
                Entry<AnalyzedProcess, ProcessState> entry = entryIt.next();

                if (!(entry.getValue().getWaitingFor() instanceof EventBlocker eb)) {
                    continue;
                }

                Set<Event> remainingEvents = CollectionUtil.setDiff(eb.getEvents(), Set.of(event));
                if (remainingEvents.size() == eb.getEvents().size()) {
                    continue;
                }

                if (eb.isChoice() || remainingEvents.isEmpty()) {
                    entry.getValue().setWaitingFor(null);
                } else {
                    entry.getValue().setWaitingFor(eb.replaceEvents(remainingEvents));
                }
                currentInformation = getInformationHandler().handleProcessWaitedForEvents(entry.getKey(), entry.getValue(),
                        Set.of(event), eb, currentInformation);
            }
        }

        if (wasLocked) {
            currentState.lock();
        }

        return Set.of(transitionResult.replaceResultingState(currentState));
    }

    @Override
    public Collection<TransitionResult> stopSimulation(TransitionResult transitionResult) {
        ConsideredState currentState = transitionResult.resultingState();

        boolean wasLocked = currentState.isLocked();
        currentState = LockableObject.unlockedVersion(currentState);

        currentState.getGlobalState().setSimulationStopped(true);

        for (Entry<AnalyzedProcess, ProcessState> entry : currentState.getProcessStates().entrySet()) {
            ProcessState state = entry.getValue();
            // if only stopping after delta cycle, ignore processes that are ready
            if (this.stopMode == SimulationStopMode.SC_STOP_FINISH_DELTA) {
                if (state.getWaitingFor() == null || state.getWaitingFor() instanceof EventBlocker) {
                    continue;
                }
            }

            state.setWaitingFor(ProcessTerminatedBlocker.INSTANCE);
        }

        currentState.getGlobalState().getEventStates().clear();

        if (wasLocked) {
            currentState.lock();
        }

        return Set.of(transitionResult.replaceResultingState(currentState));
    }

}
