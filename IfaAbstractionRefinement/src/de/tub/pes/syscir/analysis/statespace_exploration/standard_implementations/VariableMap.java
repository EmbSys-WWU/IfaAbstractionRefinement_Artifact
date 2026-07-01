package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import java.util.LinkedHashMap;
import java.util.Map;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.util.CollectionUtil;
import de.tub.pes.syscir.analysis.util.CollectionUtil.MapEntryFilter;

/**
 * Map wrapper for variable values. {@link #hashCode()} and {@link #equals(Object)} ignore entries
 * with unknown values.
 * 
 * @author Lukas Ernst
 * @param <VariableT> Variable class
 */
public class VariableMap<VariableT> {

    private final Map<VariableT, AbstractedValue> values;

    public VariableMap() {
        this.values = new LinkedHashMap<>();
    }

    public VariableMap(VariableMap<VariableT> entries) {
        this.values = new LinkedHashMap<>(entries.values);
    }

    public Map<VariableT, AbstractedValue> map() {
        return this.values;
    }

    private <K> MapEntryFilter<K, AbstractedValue> entryKnown() {
        return (k, v) -> !v.isUnknown();
    }

    @Override
    public int hashCode() {
        return CollectionUtil.partialMapHashCode(this.values, entryKnown());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other == null || other.getClass() != this.getClass())
            return false;
        return CollectionUtil.partialMapEquals(((VariableMap<Object>) this).values, entryKnown(),
                ((VariableMap<Object>) other).values, entryKnown());
    }

    @Override
    public String toString() {
        return this.values.toString();
    }

}
