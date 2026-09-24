package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import de.tub.pes.syscir.sc_model.variables.SCArray;

public record ArrayInstance(Variable<?, SCArray> creationVariable) {

    @Override
    public String toString() {
        return this.creationVariable.toString();
    }
}
