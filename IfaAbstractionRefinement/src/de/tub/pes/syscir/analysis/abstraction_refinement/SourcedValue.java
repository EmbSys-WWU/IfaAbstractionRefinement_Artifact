package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;

/**
 * AbstractedValue wrapper including a sources set for tracking where the value
 * originates from, primarily if it is unknown.
 * 
 * @author Lukas Ernst
 */
public class SourcedValue implements AbstractedValue {

    private final AbstractedValue value;
    private final Sources sources;

    public SourcedValue(AbstractedValue value, Sources sources) {
        this.value = value;
        this.sources = sources;
    }

    public AbstractedValue getRepresentedValue() {
        return value;
    }

    public Sources getSources() {
        return sources;
    }

    @Override
    public boolean isDetermined() {
        return value.isDetermined();
    }

    @Override
    public boolean isUnknown() {
        return value.isUnknown();
    }

    @Override
    public Object get() throws RuntimeException {
        return value.get();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        return this.value.equals(((SourcedValue) obj).value);
    }

    @Override
    public String toString() {
        return "{" + value.toString() + " from " + sources + "}";
    }

}
