package de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.GlobalState;
import de.tub.pes.syscir.analysis.statespace_exploration.TimedBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ArrayInstance;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.GlobalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.VariableHolder;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.VariableMap;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrapperUtil;
import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCConnectionInterface;
import de.tub.pes.syscir.sc_model.SCPortInstance;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.variables.SCArray;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCEvent;
import de.tub.pes.syscir.sc_model.variables.SCKnownType;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An abstraction of the global state of a SystemC design keeping the values of some global
 * variables.
 *
 * @author Jonas Becker-Kupczok, Lukas Ernst
 *
 * @param <ValueT> the type of abstracted value used in the analysis
 */
public class SomeVariablesGlobalState extends GlobalState implements VariableHolder<GlobalVariable<?, ?>> {
    
    private final VariableMap<GlobalVariable<?, ?>> variableValues;
    
    /**
     * Constructs a new, mutable SomeVariablesGlobalState with the given event scheduler states,
     * requested updates, simulation stoppage and variable values.
     * <p>
     * The map and set parameters are stored in the newly created object as is, without being copied.
     * They must be modifiable. Care must be taken not to modify them externally, especially after this
     * state has been locked.
     * 
     * @param eventStates map of each event to its scheduler state (no mapping means not pending, null
     *        values are not allowed)
     * @param requestedUpdates set of ports for which updates have been requested
     * @param simulationStopped whether or not sc_stop() has been called
     * @param variableValues the values of all stored global variables
     */
    public SomeVariablesGlobalState(Map<Event, TimedBlocker> eventStates, Set<WrappedSCClassInstance> requestedUpdates,
            boolean simulationStopped, VariableMap<GlobalVariable<?, ?>> variableValues) {
        super(eventStates, requestedUpdates, simulationStopped);
        
        this.variableValues = variableValues;
    }
    
    /**
     * Constructs a new, mutable copy of the given SomeVariablesGlobalState.
     * 
     * @param copyOf the state to copy
     */
    public SomeVariablesGlobalState(SomeVariablesGlobalState copyOf) {
        super(copyOf);
        
        this.variableValues = new VariableMap<>(copyOf.variableValues);
    }
    
    @Override
    public Map<GlobalVariable<?, ?>, AbstractedValue> getVariableValues() {
        return Collections.unmodifiableMap(this.variableValues.map());
    }
    
    @Override
    public void setVariableValue(GlobalVariable<?, ?> variable, AbstractedValue value) {
        requireNotLocked();
        resetHashCode();
        if (value == null) {
            this.variableValues.map().remove(variable);
        } else {
            this.variableValues.map().put(variable, value);
        }
    }
    
    // currently only sets up events, ports, signals and arrays
    public static VariableMap<GlobalVariable<?, ?>> initialVariableValues(SCSystem scSystem, AbstractedLogic logic) {
        VariableMap<GlobalVariable<?, ?>> values = new VariableMap<>();
        for (SCClassInstance instance : scSystem.getInstances()) {
            SCClass clazz = instance.getSCClass();
            for (SCEvent eventVariable : clazz.getEvents()) {
                values.map().put(new GlobalVariable<>(WrapperUtil.wrap(instance), eventVariable),
                        logic.value(new EventBlocker.Event(eventVariable)));
            }
            for (SCVariable variable : clazz.getMembers()) {
                if (variable.isSCClassInstance()) {
                    GlobalVariable<?, ?> var = new GlobalVariable<>(WrapperUtil.wrap(instance), variable);
                    String initString = variable.getInitializationString();
                    initString = initString.substring(" = ".length());
                    values.map().put(var, logic.value(scSystem.getInstanceByName(initString)));
                } else if (variable instanceof SCArray array) {
                    GlobalVariable<?, SCArray> var = new GlobalVariable<>(WrapperUtil.wrap(instance), array);
                    values.map().put(var, logic.value(new ArrayInstance(var)));
                }
            }
            for (SCConnectionInterface connection : scSystem.getPortSocketInstances()) {
                SCPortInstance portInstance = (SCPortInstance) connection;
                values.map().put(
                        new GlobalVariable<>(WrapperUtil.wrap(portInstance.getOwner()), portInstance.getPortSocket()),
                        logic.value(WrapperUtil.wrap(portInstance.getChannels().get(0))));
            }
            if (instance instanceof SCKnownType kt) {
                if (clazz.getName().equals("sc_signal_int")) {
                    Object init = kt.getInitialValueCount() == 0 ? 0
                            : ExpressionCrawler.parseConstant(kt.getInitializationString().substring(1));
                    values.map().put(new GlobalVariable<>(WrapperUtil.wrap(instance), clazz.getMemberByName("val")),
                            logic.value(init));
                    values.map().put(new GlobalVariable<>(WrapperUtil.wrap(instance), clazz.getMemberByName("_val")),
                            logic.value(init));
                } else if (kt.getSCClass().getName().contains("sc_fifo")) {
                    if (kt.getInitialValueCount() > 0) {
                        throw new UnsupportedOperationException("Initial values for sc_fifo are not supported yet");
                    }
                    values.map().put(new GlobalVariable<>(WrapperUtil.wrap(instance), clazz.getMemberByName("size")),
                            logic.value(16));
                    values.map().put(new GlobalVariable<>(WrapperUtil.wrap(instance), clazz.getMemberByName("free")),
                            logic.value(16));
                    values.map().put(new GlobalVariable<>(WrapperUtil.wrap(instance), clazz.getMemberByName("ri")),
                            logic.value(0));
                    values.map().put(new GlobalVariable<>(WrapperUtil.wrap(instance), clazz.getMemberByName("wi")),
                            logic.value(0));
                    values.map().put(
                            new GlobalVariable<>(WrapperUtil.wrap(instance), clazz.getMemberByName("num_readable")),
                            logic.value(0));
                    values.map().put(
                            new GlobalVariable<>(WrapperUtil.wrap(instance), clazz.getMemberByName("num_read")),
                            logic.value(0));
                    values.map().put(
                            new GlobalVariable<>(WrapperUtil.wrap(instance), clazz.getMemberByName("num_written")),
                            logic.value(0));
                }
            }
        }
        return values;
    }
    
    @Override
    public SomeVariablesGlobalState unlockedClone() {
        return new SomeVariablesGlobalState(this);
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
        
        return this.variableValues.equals(((SomeVariablesGlobalState) other).variableValues);
    }
    
    @Override
    public String toString() {
        return super.toString() + " vars " + this.variableValues.toString();
    }
    
}
