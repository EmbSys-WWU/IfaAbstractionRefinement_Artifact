package de.tub.pes.syscir.analysis.statespace_exploration.transition_informations;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.util.HashCachingLockableObject;


public class VariablesReadWrittenInformation extends HashCachingLockableObject implements TransitionInformation {

    private final AbstractedLogic logic;
    private Map<Variable<?, ?>, AbstractedValue> read;
    private Map<Variable<?, ?>, AbstractedValue> written;

    public VariablesReadWrittenInformation(AbstractedLogic logic, Map<Variable<?, ?>, AbstractedValue> read,
            Map<Variable<?, ?>, AbstractedValue> written) {
        this.logic = logic;
        this.read = new LinkedHashMap<>(read);
        this.written = new LinkedHashMap<>(written);
    }

    public VariablesReadWrittenInformation(VariablesReadWrittenInformation copyOf) {
        super(copyOf);

        this.logic = copyOf.logic;
        this.read = new LinkedHashMap<>(copyOf.read);
        this.written = new LinkedHashMap<>(copyOf.written);
    }

    public VariablesReadWrittenInformation(AbstractedLogic logic) {
        this(logic, Map.of(), Map.of());
    }

    public Map<Variable<?, ?>, AbstractedValue> getRead() {
        return Collections.unmodifiableMap(this.read);
    }

    public Map<Variable<?, ?>, AbstractedValue> getWritten() {
        return Collections.unmodifiableMap(this.written);
    }

    public VariablesReadWrittenInformation concat(VariablesReadWrittenInformation other) {
        requireNotLocked();
        for (Map.Entry<Variable<?, ?>, AbstractedValue> entry : other.getRead().entrySet()) {
            this.read.merge(entry.getKey(), entry.getValue(), this.logic::or);
        }
        for (Map.Entry<Variable<?, ?>, AbstractedValue> entry : other.getWritten().entrySet()) {
            this.written.merge(entry.getKey(), entry.getValue(), this.logic::or);
        }
        resetHashCode();
        return this;
    }

    @Override
    public VariablesReadWrittenInformation compose(TransitionInformation other) {
        VariablesReadWrittenInformation otherrw = (VariablesReadWrittenInformation) other;
        requireNotLocked();
        for (Map.Entry<Variable<?, ?>, AbstractedValue> entry : otherrw.getRead().entrySet()) {
            this.read.merge(entry.getKey(), entry.getValue(), this.logic::union);
        }
        for (Map.Entry<Variable<?, ?>, AbstractedValue> entry : otherrw.getWritten().entrySet()) {
            this.written.merge(entry.getKey(), entry.getValue(), this.logic::union);
        }
        resetHashCode();
        return this;
    }

    @Override
    public VariablesReadWrittenInformation clone() {
        return unlockedClone();
    }

    @Override
    public VariablesReadWrittenInformation unlockedClone() {
        return new VariablesReadWrittenInformation(this);
    }

    @Override
    protected int hashCodeInternal() {
        return 31 * this.read.hashCode() + this.written.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        VariablesReadWrittenInformation vrwi = (VariablesReadWrittenInformation) other;
        return this.read.equals(vrwi.read) && this.written.equals(vrwi.written);
    }

    @Override
    public String toString() {
        return "[read=" + this.read + ", written=" + this.written + "]";
    }

}
