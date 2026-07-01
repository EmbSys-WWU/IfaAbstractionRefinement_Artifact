package de.tub.pes.syscir.analysis.statespace_exploration.no_variables_implementation;

import java.util.List;

import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;


public class NoVariablesProcessState extends ProcessState {

    public NoVariablesProcessState(ProcessBlocker waitingFor,
            List<EvaluationContext> executionStack) {
        super(waitingFor, executionStack);
    }

    protected NoVariablesProcessState(NoVariablesProcessState copyOf) {
        super(copyOf);
    }

    @Override
    public NoVariablesProcessState unlockedClone() {
        return new NoVariablesProcessState(this);
    }

}
