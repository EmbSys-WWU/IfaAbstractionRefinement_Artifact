package de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.LocalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.VariableHolder;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.VariableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An abstraction of the process-local state of a SystemC design keeping the values of some local
 * variables.
 *
 * @author Jonas Becker-Kupczok
 */
public class SomeVariablesProcessState extends ProcessState implements VariableHolder<LocalVariable<?>> {
    
    private final VariableMap<LocalVariable<?>> variableValues;
    
    /**
     * Constructs a new, mutable SomeVariablesProcessState waiting for the given {@link ProcessBlocker}
     * at the given execution stack and with the given variable values.
     * <p>
     * The parameters are stored in the newly created object as is, without being copied. The lists must
     * be modifiable. Care must be taken not to modify them externally, especially after this state has
     * been locked.
     * 
     * @param waitingFor what this process is waiting for (null means it's ready to be scheduled)
     * @param executionStack where in the program this process is currently waiting (the last element in
     *        the list is at the top of the execution stack)
     * @param variableValues the values of all stored global variables
     */
    public SomeVariablesProcessState(ProcessBlocker waitingFor, List<EvaluationContext> executionStack,
            VariableMap<LocalVariable<?>> variableValues) {
        super(waitingFor, executionStack);
        
        this.variableValues = variableValues;
    }
    
    /**
     * Constructs a new, mutable SomeVariablesProcessState waiting for the given {@link ProcessBlocker}
     * at the given execution stack. No variables are initially stored.
     * <p>
     * The parameters are stored in the newly created object as is, without being copied. The lists must
     * be modifiable. Care must be taken not to modify them externally, especially after this state has
     * been locked.
     * 
     * @param waitingFor what this process is waiting for (null means it's ready to be scheduled)
     * @param executionStack where in the program this process is currently waiting (the last element in
     *        the list is at the top of the execution stack)
     */
    public SomeVariablesProcessState(ProcessBlocker waitingFor, List<EvaluationContext> executionStack) {
        this(waitingFor, executionStack, new VariableMap<>());
    }
    
    /**
     * Constructs a new, mutable copy of the given SomeVariablesProcessState.
     * 
     * @param copyOf the state to copy
     */
    public SomeVariablesProcessState(SomeVariablesProcessState copyOf) {
        super(copyOf);
        
        this.variableValues = new VariableMap<>(copyOf.variableValues);
    }
    
    @Override
    public Map<LocalVariable<?>, AbstractedValue> getVariableValues() {
        return Collections.unmodifiableMap(this.variableValues.map());
    }
    
    @Override
    public void setVariableValue(LocalVariable<?> variable, AbstractedValue value) {
        requireNotLocked();
        resetHashCode();
        if (value == null) {
            this.variableValues.map().remove(variable);
        } else {
            this.variableValues.map().put(variable, value);
        }
    }
    
    @Override
    public SomeVariablesProcessState unlockedClone() {
        return new SomeVariablesProcessState(this);
    }
    
    @Override
    protected int hashCodeInternal() {
        int result = super.hashCodeInternal();
        result = result * 31 + this.variableValues.hashCode();
        return result;
    }
    
    @Override
    protected int timingAgnosticHashCodeInternal() {
        int result = super.timingAgnosticHashCodeInternal();
        result = result * 31 + this.variableValues.hashCode();
        return result;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        
        if (!super.equals(other)) {
            return false;
        }
        
        return this.variableValues.equals(((SomeVariablesProcessState) other).variableValues);
    }
    
    @Override
    public String toString() {
        return super.toString() + " vars " + this.variableValues.toString();
    }
    
}
