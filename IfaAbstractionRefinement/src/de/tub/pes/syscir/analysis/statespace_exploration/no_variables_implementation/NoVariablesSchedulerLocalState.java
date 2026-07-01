package de.tub.pes.syscir.analysis.statespace_exploration.no_variables_implementation;

import java.util.List;

import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;


public class NoVariablesSchedulerLocalState extends LocalState {

    public NoVariablesSchedulerLocalState(List<EvaluationContext> executionStack) {
        super(executionStack);
    }

    public NoVariablesSchedulerLocalState(NoVariablesSchedulerLocalState copyOf) {
        super(copyOf);
    }

    @Override
    public NoVariablesSchedulerLocalState unlockedClone() {
        return new NoVariablesSchedulerLocalState(this);
    }

}
