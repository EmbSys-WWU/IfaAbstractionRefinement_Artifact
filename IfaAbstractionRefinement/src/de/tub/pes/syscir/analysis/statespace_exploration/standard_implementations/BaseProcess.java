package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import static de.tub.pes.syscir.analysis.util.WrapperUtil.wrap;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.DeltaTimeBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessTerminatedBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessTransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.RealTimedBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration.ExplorationAbortedException;
import de.tub.pes.syscir.analysis.statespace_exploration.TimedBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.util.LockableObject;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCPortInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCProcess;
import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCTIMEUNIT;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SequencedMap;
import java.util.Set;

/**
 * Class implementing a basic process semantic by traversing the expression tree and control flow
 * graph.
 * 
 * This class is immutable and thread-safe except for changes in the underlying SysCIR. Such changes
 * lead to undefined behavior.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of global state abstraction which this process implementation can
 *        handle
 * @param <ProcessStateT> the type of local state abstraction which this process implementation can
 *        handle
 * @param <InfoT> the type of additional transition information which this process implementation
 *        can provide.
 * @param <ValueT> the type of abstracted value which this process implementation can handle
 */
public abstract class BaseProcess extends ExpressionCrawler implements AnalyzedProcess {

    public int x;

    private final WrappedSCProcess scProcess;
    private final WrappedSCClassInstance scClassInstance;
    private final int hashCode;

    /**
     * Constructs a new BaseProcess representing the given SysCIR process, belonging to the given SysCIR
     * class instance (i.e. the module instance) and using the given scheduler.
     *
     * @param scSystem the SysCIR representation of the entire SystemC design
     * @param scProcess the SysCIR process
     * @param scClassInstance owning instance of this process
     * @param scheduler the scheduler used in this analysis
     */
    public BaseProcess(SCSystem scSystem, SCProcess scProcess, SCClassInstance scClassInstance, Scheduler scheduler,
            AbstractedLogic logic, Interceptor interceptor) {
        super(scSystem, scheduler, logic, interceptor);

        this.scProcess = wrap(scProcess);
        this.scClassInstance = wrap(scClassInstance);
        this.hashCode = this.scProcess.hashCode();
    }

    @Override
    public WrappedSCProcess getSCProcess() {
        return this.scProcess;
    }

    @Override
    public WrappedSCClassInstance getSCClassInstance() {
        return this.scClassInstance;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AnalyzedProcess p)) {
            return false;
        }
        return getSCProcess().equals(p.getSCProcess()) && getSCClassInstance().equals(p.getSCClassInstance());
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation repeatedly calls {@link #makeSmallStep(ProcessTransitionResult)} until
     * {@link SmallStepResult#endOfStep()} is true for every branch of the evaluation, returning the
     * gathered transition results. For efficiency, states are only locked in the end and reused in
     * between where possible.
     * 
     * @param currentState {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Set<ProcessTransitionResult> makeStep(ConsideredState currentState) {
        StateSpaceExploration explorer = StateSpaceExploration.getCurrentExplorer();

        /*
         * This uses a modified worklist (transitionsToHandle) algorithm to make one small step after the
         * other until the end of a (large) step is reached. Elements of the worklist are stored as map
         * entries, so that transitions that yield the same state but with different transition information
         * are detected. In such cases, the information is composed and the entry is put back into the
         * worklist to ensure that no transition information is lost.
         * 
         * Some transitions which may repeat themselves (e.g., loop conditions) are stored to detect if they
         * reappear and not put them into the worklist again.
         */

        Map<ConsideredState, TransitionInformation> seenTransitions = new LinkedHashMap<>();
        SequencedMap<ConsideredState, TransitionInformation> transitionsToHandle = new LinkedHashMap<>();
        Map<ConsideredState, TransitionInformation> resultingTransitions = new LinkedHashMap<>();

        ProcessTransitionResult initialTransitionResult =
                new ProcessTransitionResult(currentState, getInformationHandler().getInitialInformation(currentState));
        TransitionInformation initialInformation = getInformationHandler().handleStartOfCode(initialTransitionResult,
                getLocalState(initialTransitionResult));
        transitionsToHandle.put(LockableObject.unlockedVersion(currentState), initialInformation);

        while (!transitionsToHandle.isEmpty()) {
            if (explorer.isAborted()) {
                throw new ExplorationAbortedException();
            }

            ProcessTransitionResult nextToHandle = new ProcessTransitionResult(
                    transitionsToHandle.firstEntry().getKey(), transitionsToHandle.pollFirstEntry().getValue());
            SmallStepResult nextTransitions = makeSmallStep(nextToHandle);

            if (nextTransitions.endOfStep()) {
                // store resulting transitions
                for (TransitionResult transition : nextTransitions.transitions()) {
                    resultingTransitions.merge(transition.resultingState(), transition.transitionInformation(),
                            TransitionInformation::compose);
                }
            } else if (nextTransitions.possiblyRepeatingStep() || getInformationHandler().wantsMinimalSteps()) {
                // make sure that possibly repeating steps are only considered once

                // TODO: if info varies depending on taken path, this may be quite expensive. simply merging and
                // composing the information doesn't work though, because other info may already have been computed
                // based on the now to be composed info, which then wouldn't be updated. can this be solved better?
                for (TransitionResult transition : nextTransitions.transitions()) {
                    transition.resultingState().lock();
                    TransitionInformation oldInformation = seenTransitions.get(transition.resultingState());
                    if (oldInformation != null
                            && getInformationHandler().skipReexploration(oldInformation, transition)) {
                        continue;
                    }
                    if (transition.transitionInformation().equals(oldInformation)) {
                        continue;
                    }
                    TransitionInformation newInformation =
                            seenTransitions.merge(transition.resultingState().unlockedClone(),
                                    transition.transitionInformation().clone(), TransitionInformation::compose);
                    if (newInformation.equals(oldInformation)) {
                        continue;
                    }

                    transitionsToHandle.merge(transition.resultingState().unlockedClone(), newInformation,
                            TransitionInformation::compose);
                }
            } else {
                for (TransitionResult transition : nextTransitions.transitions()) {
                    transitionsToHandle.merge(LockableObject.unlockedVersion(transition.resultingState()),
                            transition.transitionInformation(), TransitionInformation::compose);
                }
            }
        }

        // finalize the result

        Set<ProcessTransitionResult> result = new LinkedHashSet<>(resultingTransitions.size());
        for (Entry<ConsideredState, TransitionInformation> transition : resultingTransitions.entrySet()) {
            result.add(
                    finalizeTransitionResult(new ProcessTransitionResult(transition.getKey(), transition.getValue())));
        }

        getInformationHandler().handleEndOfBlock();

        return result;
    }

    @Override
    public ProcessState getLocalState(TransitionResult currentState) {
        return currentState.resultingState().getProcessState(this);
    }

    /**
     * Returns the set of events that the process is statically sensitive on in the current state.
     *
     * @param currentState the current state
     * @param localState the local state of this process in the current state
     * @return the set of events the process is statically sensitive on
     */
    public abstract Set<Event> getSensitivities(TransitionResult currentState, ProcessState localState);

    @Override
    public SmallStepResult handleWaitExpression(TransitionResult currentState, LocalState localState,
            FunctionCallExpression expression, int comingFrom) {
        ProcessState processState = (ProcessState) localState;
        if (expression.getParameters().isEmpty()) {
            returnToParent(expression, localState);
            processState.setWaitingFor(
                    new EventBlocker(new LinkedHashSet<>(getSensitivities(currentState, processState)), true, null));
            return createSmallStepResult(expression, comingFrom, currentState, localState, true, false);
            // TODO: what if sensitivities is empty?
        }

        AbstractedValue firstParam = getValueOfChild(currentState, localState, 0);
        if (!firstParam.isDetermined()) {
            throw new InsufficientValueTrackingException(expression.toString(), firstParam);
        }
        Object firstValue = firstParam.get();

        if (expression.getParameters().size() == 1) {
            if (firstValue instanceof EventBlocker eb) {
                processState.setWaitingFor(eb);
            } else if (firstValue instanceof Event event) {
                processState.setWaitingFor(new EventBlocker(Set.of(event), true, null));
            } else if (firstValue instanceof TimedBlocker tb) {
                processState.setWaitingFor(tb);
            } else if (firstValue == SCTIMEUNIT.SC_ZERO_TIME) {
                processState.setWaitingFor(DeltaTimeBlocker.INSTANCE);
            } else {
                throw new RuntimeException("unexpected value of wait parameter: " + firstValue.getClass());
            }
            // TODO: add waiting for specific number of clock cycles (SC_CTHRAD)?
            returnToParent(expression, localState);
            return createSmallStepResult(expression, comingFrom, currentState, localState, true, false);
        }

        AbstractedValue secondParam = getValueOfChild(currentState, localState, 1);
        if (!secondParam.isDetermined()) {
            throw new InsufficientValueTrackingException(secondParam);
        }
        Object secondValue = secondParam.get();

        if (expression.getParameters().size() == 2) {
            if (secondValue instanceof SCTIMEUNIT unit) {
                int amount = (int) firstValue;
                processState
                        .setWaitingFor(amount == 0 ? DeltaTimeBlocker.INSTANCE : new RealTimedBlocker(amount, unit));
            } else if (secondValue instanceof Event event) {
                TimedBlocker timeout = (TimedBlocker) firstValue;
                processState.setWaitingFor(new EventBlocker(Set.of(event), true, timeout));
            } else if (secondValue instanceof EventBlocker eb) {
                TimedBlocker timeout = (TimedBlocker) firstValue;
                processState.setWaitingFor(eb.replaceTimeout(timeout));
            } else {
                throw new RuntimeException("unexpected value of wait parameter: " + secondValue.getClass());
            }
            returnToParent(expression, localState);
            return createSmallStepResult(expression, comingFrom, currentState, localState, true, false);
        }

        AbstractedValue thirdParam = getValueOfChild(currentState, localState, 2);
        if (!thirdParam.isDetermined()) {
            throw new InsufficientValueTrackingException(thirdParam);
        }
        Object thirdValue = thirdParam.get();

        if (expression.getParameters().size() != 3) {
            throw new RuntimeException("unexpected call to wait with more than 3 parameters");
        }

        int amount = (int) firstValue;
        SCTIMEUNIT unit = (SCTIMEUNIT) secondValue;
        TimedBlocker timeout = amount == 0 ? DeltaTimeBlocker.INSTANCE : new RealTimedBlocker(amount, unit);

        if (thirdValue instanceof Event event) {
            processState.setWaitingFor(new EventBlocker(Set.of(event), true, timeout));
        } else if (thirdValue instanceof EventBlocker eb) {
            processState.setWaitingFor(eb.replaceTimeout(timeout));
        } else {
            throw new RuntimeException("unexpected value of wait parameter: " + thirdValue.getClass());
        }
        returnToParent(expression, localState);
        return createSmallStepResult(expression, comingFrom, currentState, localState, true, false);
    }

    @Override
    public SmallStepResult handleRequestUpdateExpression(TransitionResult currentState, LocalState localState,
            FunctionCallExpression expression, int comingFrom) {
        AbstractedValue portValue = localState.getTopOfStack().getThisValue();
        if (!portValue.isDetermined()) {
            throw new InsufficientValueTrackingException(portValue);
        }

        WrappedSCClassInstance instanceToUpdate = null;
        if (portValue.get() instanceof WrappedSCClassInstance kt) {
            instanceToUpdate = kt;
        } else if (portValue.get() instanceof WrappedSCPortInstance pi) {
            instanceToUpdate = getChannel(pi);
        } else if (portValue.get() instanceof SCPort port) {
            instanceToUpdate = getChannel(port);
        } else {
            throw new ClassCastException();
        }

        currentState.globalState().getRequestedUpdates().add(instanceToUpdate);
        returnToParent(expression, localState);
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    @Override
    public SmallStepResult handleEndOfCodeReached(TransitionResult currentState, LocalState localState,
            List<EvaluationContext> stack) {
        ProcessState processState = (ProcessState) localState;
        Set<Event> sensitivities = getSensitivities(currentState, processState);
        if (sensitivities.isEmpty()) {
            processState.setWaitingFor(ProcessTerminatedBlocker.INSTANCE);
        } else {
            processState.setWaitingFor(new EventBlocker(new LinkedHashSet<>(sensitivities), true, null));
            List<List<AbstractedValue>> executionValues = new ArrayList<>();
            executionValues.add(new ArrayList<>());
            stack.add(new EvaluationContext(getSCProcess().getFunction(), new ArrayList<>(), -1, executionValues,
                    this.logic.value(getSCClassInstance())));
        }
        return createSmallStepResult(null, -2, currentState, localState, true, false);
        // TODO: is this behavior correct? do threads and methods behave differently here?
    }

}
