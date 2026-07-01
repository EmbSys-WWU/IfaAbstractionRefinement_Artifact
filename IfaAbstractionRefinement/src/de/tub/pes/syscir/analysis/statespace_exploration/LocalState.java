package de.tub.pes.syscir.analysis.statespace_exploration;

import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView.StackTrace;
import de.tub.pes.syscir.analysis.util.HashCachingLockableObject;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCFunction;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Class representing an abstraction of the local state of a process or the scheduler in a SystemC
 * design.
 * 
 * This includes the current execution stack.
 * <p>
 * This state starts out as mutable but can be locked to be immutable by invoking the method
 * {@link #lock()}. When locked, any attempt to modify this state results in an
 * {@link IllegalArgumentException}.
 * <p>
 * Additional information can be stored in this object and later retrieved without impacting its
 * equality or hashCode. This is intended for analyses to store pre-computed data. After a process
 * step ({@link AnalyzedProcess#makeStep(ConsideredState)}), all additional information is cleared.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <LocalStateT> the type of this state (to specify the return type of
 *        {@link #unlockedClone()})
 * @param <ValueT> the type of abstracted values used for this state
 */
public abstract class LocalState extends HashCachingLockableObject {
    
    public static class StateInformationKey<S extends StateInformation> {
        
        @Override
        public String toString() {
            return "StateInfoKey" + hashCode();
        }
    }
    
    public static interface StateInformation {
        
        StateInformation copy();
    }
    
    private List<EvaluationContext> executionStack; // TODO: allow abstraction of only storing top element?
    
    private Map<StateInformationKey<?>, StateInformation> stateInformation;
    
    /**
     * Constructs a new, mutable LocalState at the given execution stack.
     * <p>
     * The parameter is stored in the newly created object as is, without being copied. The list must be
     * modifiable. Care must be taken not to modify it externally, especially after this state has been
     * locked.
     * 
     * @param executionStack where in the program this state is currently located (the last element in
     *        the list is at the top of the execution stack)
     */
    public LocalState(List<EvaluationContext> executionStack) {
        this.executionStack = executionStack;
        
        this.stateInformation = new LinkedHashMap<>();
    }
    
    /**
     * Constructs a new, mutable copy of the given LocalState.
     * 
     * @param copyOf the state to copy
     */
    protected LocalState(LocalState copyOf) {
        super(copyOf);
        
        this.executionStack = new ArrayList<>(copyOf.executionStack.size());
        for (EvaluationContext ec : copyOf.executionStack) {
            this.executionStack.add(ec.unlockedClone());
        }
        
        this.stateInformation = new LinkedHashMap<>(copyOf.stateInformation);
        this.stateInformation.replaceAll((_, info) -> info.copy());
    }
    
    /**
     * Returns a modifiable or unmodifiable view of the execution stack at this state, depending on
     * whether or not this state is locked.
     * 
     * If the execution stack is overwritten by {@link #setExecutionStack(List)}, a previously returned
     * view will no longer be up to date.
     * 
     * @return modifiable or unmodifiable view of the execution stack
     */
    public List<EvaluationContext> getExecutionStack() {
        resetHashCode();
        return isLocked() ? Collections.unmodifiableList(this.executionStack) : this.executionStack;
    }
    
    /**
     * Returns the topmost element of the execution stack.
     *
     * @return top of stack
     */
    public EvaluationContext getTopOfStack() {
        resetHashCode();
        return getExecutionStack().get(getExecutionStack().size() - 1);
    }
    
    /**
     * Returns the stack of all function calls on the execution stack in their order on the execution
     * stack.
     * 
     * The result is not backed continously by the execution stack.
     *
     * @return stack trace
     */
    public StackTrace getStackTrace() {
        return getStackTrace(0);
    }
    
    /**
     * Returns the stack of all function calls on the execution stack in their order on the execution
     * stack.
     * 
     * The result is not backed continuously by the execution stack. The value of
     * {@link StackTrace#calls()} is a modifiable ArrayList with enough capacity for at least
     * additionalSpace more elements.
     *
     * @return stack trace
     */
    public StackTrace getStackTrace(int additionalSpace) {
        List<EvaluationLocation> stack = new ArrayList<>(this.executionStack.size() - 1 + additionalSpace);
        for (EvaluationContext ec : this.executionStack.subList(0, this.executionStack.size() - 1)) {
            stack.add(ec.toLocation());
        }
        return new StackTrace(this.executionStack.getFirst().getFunction(), stack);
    }
    
    public StackTraceView getStackTraceView() {
        return new StackTraceView() {
            
            @Override
            public WrappedSCFunction base() {
                return getExecutionStack().getFirst().getFunction();
            }
            
            @Override
            public List<EvaluationLocation> calls() {
                return new AbstractList<>() {
                    
                    @Override
                    public EvaluationLocation get(int index) {
                        return getExecutionStack().get(index).locationView();
                    }
                    
                    @Override
                    public int size() {
                        return getExecutionStack().size() - 1;
                    }
                };
            }
        };
    }
    
    public WrappedSCClassInstance getInitialThisValue() {
        return (WrappedSCClassInstance) this.executionStack.getFirst().getThisValue().get();
    }
    
    /**
     * Replaces the execution stack at this state by the given parameter.
     * 
     * As at construction, the parameter is stored as is, without being copied. Care must be taken not
     * to modify it, especially after this state has been locked.
     * 
     * @param executionStack the new execution stack
     */
    public void setExecutionStack(List<EvaluationContext> executionStack) {
        requireNotLocked();
        resetHashCode();
        this.executionStack = executionStack;
    }
    
    /**
     * Returns the additional state information that was stored for the given key, or null.
     *
     * @param <T> the type of information
     * @param key the key for the information
     * @return the information for the key
     */
    @SuppressWarnings("unchecked")
    public <T extends StateInformation> T getStateInformation(StateInformationKey<T> key) {
        return (T) this.stateInformation.get(key);
    }
    
    
    /**
     * Stores additional state information for the given key.
     * 
     * If the value is null, any previous mapping is removed. The information does not effect the
     * equality or hashCode of this state.
     *
     * @param <T> the type of information
     * @param key the key for the information
     * @param value the information for the key
     */
    public <T extends StateInformation> void setStateInformation(StateInformationKey<T> key, T value) {
        if (value == null) {
            this.stateInformation.remove(key);
        } else {
            this.stateInformation.put(key, value);
        }
    }
    
    /**
     * This method tries to get the info for a given key or computes and returns a new info if none was
     * found.
     * 
     * @param <T> Type of StateInformation that is used.
     * @param key StateInformationKey, which the info is searched for.
     * @param supplier Creates a fresh StateInformation.
     * @return Either the StateInformation for the given key, if there was one, or the newly entered
     *         info.
     */
    public <T extends StateInformation> StateInformation getOrComputeStateInformation(StateInformationKey<T> key,
            Supplier<T> supplier) {
        return this.stateInformation.computeIfAbsent(key, (_) -> supplier.get());
    }
    
    /**
     * Removes all currently stored additional state information.
     */
    public void clearStateInformation() {
        this.stateInformation.clear();
    }
    
    /**
     * {@inheritDoc}
     * 
     * Locking this state writes through to all elements of the execution stack.
     */
    @Override
    public boolean lock() {
        if (!super.lock()) {
            return false;
        }
        
        for (EvaluationContext loc : this.executionStack) {
            loc.lock();
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     * 
     * Implementations must make sure that all elements of the execution stack are cloned in the same
     * way, e.g. by invoking the copy-constructor {@link #LocalState(LocalState)}.
     * 
     * @return {@inheritDoc}
     */
    @Override
    public abstract LocalState unlockedClone();
    
    @Override
    protected int hashCodeInternal() {
        return this.executionStack.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LocalState l)) {
            return false;
        }
        return equalsInternal(l);
    }
    
    protected boolean equalsInternal(LocalState other) {
        return this.executionStack.equals(other.executionStack);
    }
    
    @Override
    public String toString() {
        return "At " + this.executionStack;
    }
    
}
