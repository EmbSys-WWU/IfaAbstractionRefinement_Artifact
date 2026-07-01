package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import java.util.function.Predicate;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.GlobalState;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;

/**
 * Interceptor to intercept variable reading and writing.
 * 
 * @author Lukas Ernst
 */
public interface VariableInterceptor {

    /**
     * Intervene when a global variable is read.
     */
    AbstractedValue readGlobalVariable(GlobalState globalState, GlobalVariable<?, ?> variable, AbstractedValue value);

    /**
     * Intervene when a global variable is written. If null is returned, the global
     * variable will be deleted from the respective VariableMap.
     */
    AbstractedValue writeGlobalVariable(GlobalState globalState, GlobalVariable<?, ?> variable, AbstractedValue value);

    /**
     * Intervene when a local variable is read.
     */
    AbstractedValue readLocalVariable(LocalState localState, LocalVariable<?> variable, AbstractedValue value);

    /**
     * Intervene when a local variable is written. If null is returned, the local
     * variable will be deleted from the respective VariableMap.
     */
    AbstractedValue writeLocalVariable(LocalState localState, LocalVariable<?> variable, AbstractedValue value);

    /**
     * Constructs a VariableInterceptor that leads to global and local variables
     * being stored under the specified conditions. Variables that are not stored by
     * the condition are deleted on write.
     */
    public static VariableInterceptor track(Predicate<GlobalVariable<?, ?>> globalVariableStorageCondition,
            Predicate<LocalVariable<?>> localVariableStorageCondition) {
        return new VariableInterceptor() {
            
            @Override
            public AbstractedValue writeLocalVariable(LocalState localState, LocalVariable<?> variable, AbstractedValue value) {
                return localVariableStorageCondition.test(variable) ? value : null;
            }
            
            @Override
            public AbstractedValue writeGlobalVariable(GlobalState globalState, GlobalVariable<?, ?> variable,
                    AbstractedValue value) {
                return globalVariableStorageCondition.test(variable) ? value : null;
            }
            
            @Override
            public AbstractedValue readLocalVariable(LocalState localState, LocalVariable<?> variable, AbstractedValue value) {
                return value;
            }
            
            @Override
            public AbstractedValue readGlobalVariable(GlobalState globalState, GlobalVariable<?, ?> variable,
                    AbstractedValue value) {
                return value;
            }
        };
    }

    /**
     * Returns a VariableInterceptor that tracks no variables / deletes any on
     * write.
     */
    public static VariableInterceptor trackNone() {
        return track(g -> false, l -> false);
    }

    /**
     * Returns a VariableInterceptor that tracks all variables (full pass-through of
     * reads and writes).
     */
    public static VariableInterceptor trackAll() {
        return track(g -> true, l -> true);
    }

}
