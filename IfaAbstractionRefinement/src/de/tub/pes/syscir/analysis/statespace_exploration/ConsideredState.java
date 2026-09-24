package de.tub.pes.syscir.analysis.statespace_exploration;

import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.util.HashCachingLockableObject;
import de.tub.pes.syscir.analysis.util.TriFunction;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCFunction;
import de.tub.pes.syscir.sc_model.SCMODIFIER;
import de.tub.pes.syscir.sc_model.SCPROCESSTYPE;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Class representing an abstraction of the state of a SystemC design.
 * 
 * The state is split into a global state (including the scheduling states of all events) and one
 * local state per process (including the scheduling state of that process itself, i.e. whether it
 * is waiting for something and, if so, for what).
 * <p>
 * This state starts out as mutable but can be locked to be immutable by invoking the method
 * {@link #lock()}. When locked, any attempt to modify a state results in an
 * {@link IllegalStateException}.
 * <p>
 * The cached hashCode of a ConsideredState is reset whenever it is modified or any modifiable
 * reference to a part of its state is obtained. If any so obtained reference is mofified after the
 * hashCode has been computed, it must be assured that the hashCode is reset again.
 * 
 * @author Jonas Becker-Kupczok
 */
public class ConsideredState extends HashCachingLockableObject {
    
    public static interface GlobalStateConstructor
            extends TriFunction<Map<Event, TimedBlocker>, Set<WrappedSCClassInstance>, Boolean, GlobalState> {
    }
    public static interface ProcessConstructor
            extends TriFunction<SCSystem, SCProcess, SCClassInstance, AnalyzedProcess> {
    }
    public static interface ProcessStateConstructor
            extends BiFunction<ProcessBlocker, List<EvaluationContext>, ProcessState> {
    }
    public static interface InitialSensitivitiesGetter extends BiFunction<AnalyzedProcess, GlobalState, Set<Event>> {
    }
    
    /**
     * Creates the initial state of a SystemC model.
     * 
     * @param <GlobalStateT> the type of the global state
     * @param <ProcessStateT> the type of the state of one process
     * @param <InfoT> the type of additional transition information that a {@link AnalyzedProcess}
     *        considered in this state may provide
     * @param <ValueT> the type of abstracted values used for this state
     * @param system a SystemC model
     * @param globalStateConstructor a function constructing a ConsideredGlobalState from a map of
     *        pending events and a boolean for whether simulation was stopped
     * @param processConstructor a function constructing a Process from a given SCProcess
     * @param processStateConstructor a function constructing a ConsideredProcessState from a
     *        ProcessBlocker for which the process is waiting, a list for the execution stack and a list
     *        for the expression values
     * @return the initial state for that SystemC model
     */
    // TODO: THIS IS UGLY!
    public static ConsideredState getInitialState(SCSystem system, GlobalStateConstructor globalStateConstructor,
            ProcessConstructor processConstructor, ProcessStateConstructor processStateConstructor,
            InitialSensitivitiesGetter initialSensitivitiesGetter,
            Function<Object, AbstractedValue> determinedValueConstructor) {
        
        GlobalState globalState = globalStateConstructor.apply(new LinkedHashMap<>(), new LinkedHashSet<>(), false);
        
        Map<AnalyzedProcess, ProcessState> processStates = new LinkedHashMap<>();

        for (SCClassInstance instance : system.getInstances()) {
            for (SCProcess scProcess : instance.getSCClass().getProcesses()) {
                AnalyzedProcess process = processConstructor.apply(system, scProcess, instance);

                ProcessBlocker waitingFor;
                if (scProcess.getType() == SCPROCESSTYPE.SCMETHOD
                        && scProcess.getModifier().contains(SCMODIFIER.DONTINITIALIZE)) {
                    Set<Event> sensitivities = initialSensitivitiesGetter.apply(process, globalState);
                    waitingFor = new EventBlocker(sensitivities, true, null);
                } else {
                    waitingFor = null;
                }

                List<List<AbstractedValue>> expressionValues = new ArrayList<>();
                expressionValues.add(new ArrayList<>());

                List<EvaluationContext> executionStack =
                        List.of(new EvaluationContext(WrappedSCFunction.getWrapped(scProcess.getFunction()),
                                new ArrayList<>(), -1, expressionValues,
                                determinedValueConstructor.apply(new WrappedSCClassInstance(instance))));

                ProcessState processState = processStateConstructor.apply(waitingFor, executionStack);
                processStates.put(process, processState);
            }
        }
        
        ConsideredState result = new ConsideredState(globalState, processStates);
        result.lock();
        return result;
    }
    
    private GlobalState globalState;
    private Map<AnalyzedProcess, ProcessState> processStates;
    
    /**
     * Constructs a new, mutable ConsideredState with the given global and local portions.
     * 
     * The parameters are stored in the newly created object as is, without being copied. The map of
     * local states must be modifiable. Care must be taken not to modify it externally, especially after
     * this state has been locked. Locking this state writes through to the global state and all process
     * states.
     *
     * @param globalState the global portion of this state
     * @param processStates the local states per process (must be modifiable)
     */
    public ConsideredState(GlobalState globalState, Map<AnalyzedProcess, ProcessState> processStates) {
        this.globalState = globalState;
        this.processStates = processStates;
    }
    
    /**
     * Constructs a new, mutable (deep) copy of the given ConsideredState.
     * 
     * @param copyOf the state to copy
     */
    protected ConsideredState(ConsideredState copyOf) {
        super(copyOf);
        
        LinkedHashMap<AnalyzedProcess, ProcessState> newProcessStates =
                new LinkedHashMap<>(copyOf.processStates.size());
        copyOf.processStates.forEach((process, state) -> newProcessStates.put(process, state.unlockedClone()));
        
        this.globalState = copyOf.globalState.unlockedClone();
        this.processStates = newProcessStates;
    }
    
    @Override
    protected int hashCodeInternal() {
        int result = 31 * this.globalState.hashCode();
        for (Entry<AnalyzedProcess, ProcessState> entry : this.processStates.entrySet()) {
            result += entry.getKey().hashCode() * entry.getValue().hashCode();
        }
        return result;
    }
    
    /**
     * Returns the global portion of this state.
     * 
     * @return global state
     */
    public GlobalState getGlobalState() {
        resetHashCode();
        return this.globalState;
    }
    
    /**
     * Replaces the global portion of this state be the given parameter.
     * 
     * If this state has been locked, an {@link IllegalStateException} is thrown.
     * 
     * @param globalState the new global state
     */
    public void setGlobalState(GlobalState globalState) {
        requireNotLocked();
        resetHashCode();
        this.globalState = globalState;
    }
    
    /**
     * Returns a view of the local portion of this state that is modifiable iff this state is not
     * locked.
     * <p>
     * If the local portion is overwritten by {@link #setProcessStates(Map)}, a previously returned view
     * will no longer be up to date.
     * 
     * @return view of the process states
     */
    public Map<AnalyzedProcess, ProcessState> getProcessStates() {
        resetHashCode();
        return isLocked() ? Collections.unmodifiableMap(this.processStates) : this.processStates;
    }
    
    /**
     * Replaces the local portion of this state by the given parameter.
     * 
     * If this state has been locked, an {@link IllegalStateException} is thrown.
     * <p>
     * As at construction, the parameter is stored as is, without being copied. Care must be taken not
     * to modify it, especially after this state has been locked.
     * 
     * @param processStates the new process states
     */
    public void setProcessStates(Map<AnalyzedProcess, ProcessState> processStates) {
        requireNotLocked();
        resetHashCode();
        this.processStates = processStates;
    }
    
    /**
     * Returns the local state associated with the given process in this state.
     * 
     * Throws a {@link NullPointerException} if the given process is not associated with any local
     * state.
     * 
     * @param process a process
     * @return the local state associated with the given process
     * @throws NullPointerException if the given process is not associated with any local state
     */
    public ProcessState getProcessState(AnalyzedProcess process) throws NullPointerException {
        resetHashCode();
        return Objects.requireNonNull(this.processStates.get(process));
    }
    
    /**
     * Sets the local state to be associated with the given process.
     * 
     * If this state has been locked, an {@link IllegalStateException} is thrown.
     * 
     * @param process a process
     * @param state the new local state for that process
     */
    public void setState(AnalyzedProcess process, ProcessState state) {
        requireNotLocked();
        resetHashCode();
        this.processStates.put(process, state);
    }
    
    /**
     * Returns a collection of all processes which are associated with local states which imply them to
     * be ready to be scheduled.
     * 
     * The collection is guaranteed not to contain duplicates, but may not be a set for performance
     * reasons.
     *
     * @return collection of ready processes
     */
    public Collection<AnalyzedProcess> getReadyProcesses() {
        Collection<AnalyzedProcess> result = new ArrayList<>();
        for (Entry<AnalyzedProcess, ProcessState> entry : this.processStates.entrySet()) {
            if (entry.getValue().isReady()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
    
    @Override
    protected void resetHashCode() {
        super.resetHashCode();
    }
    
    /**
     * {@inheritDoc}
     * 
     * Locking this state writes through to the global state and all process states.
     */
    @Override
    public boolean lock() {
        if (!super.lock()) {
            return false;
        }
        
        this.globalState.lock();
        for (ProcessState processState : this.processStates.values()) {
            processState.lock();
        }
        
        return true;
    }
    
    @Override
    public ConsideredState unlockedClone() {
        return new ConsideredState(this);
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        ConsideredState s = (ConsideredState) other;
        return this.globalState.equals(s.globalState) && this.processStates.equals(s.processStates);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("(GlobalState: ").append(this.globalState.toString()).append(" ProcessStates: {");
        boolean first = true;
        for (Entry<AnalyzedProcess, ProcessState> entry : this.processStates.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append(entry.getKey().toString()).append(": ").append(entry.getValue().toString());
        }
        builder.append("})");
        
        return builder.toString();
    }
    
}
