package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import java.util.Map;
import java.util.function.Supplier;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;

/**
 * Interface for an entity that stores the values of variables.
 * 
 * Values may not be null, but may be non-determined or abstractions of null.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <VarT> the type of variable considered
 * @param <ValueT> the type of value stored
 */
public interface VariableHolder<VarT extends Variable<?, ?>> {

    /**
     * Returns the variable values as an unmodifiable map.
     *
     * @return unmodifiable map of variable values
     */
    Map<VarT, AbstractedValue> getVariableValues();

    /**
     * Returns the value stored for the given variable, or the result of the defaultGetter if the
     * variable isn't stored.
     *
     * @param variable the variable
     * @param defaultGetter the supplier of the default value
     * @return the value stored for the variable, or the default value
     */
    default AbstractedValue getValue(VarT variable, Supplier<AbstractedValue> defaultGetter) {
        AbstractedValue result = getVariableValues().get(variable);
        return result == null ? defaultGetter.get() : result;
    }

    /**
     * Sets the value of the given variable. If the given value is null, the variable is removed from
     * the storage.
     *
     * @param variable the variable
     * @param value the value or null
     */
    void setVariableValue(VarT variable, AbstractedValue value);

}
