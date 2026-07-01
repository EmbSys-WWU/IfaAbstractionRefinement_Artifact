package de.tub.pes.syscir.analysis.statespace_exploration.no_variables_implementation;

import java.util.Set;

import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BaseProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue.BinaryAbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Interceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.VariableInterceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.NoInformation;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;


public class NoVariablesNoInformationProcess extends BaseProcess {

    public NoVariablesNoInformationProcess(SCSystem scSystem, SCProcess scProcess, SCClassInstance scClassInstance,
            NoVariablesNoInformationScheduler scheduler) {
        super(scSystem, scProcess, scClassInstance, scheduler, BinaryAbstractedLogic.INSTANCE,
                Interceptor.of(VariableInterceptor.trackNone(), NoInformation.HANDLER));
    }

    @Override
    public BinaryAbstractedValue aggregateExpressionValue(TransitionResult currentState, LocalState localState,
            Expression expression) {
        return BinaryAbstractedValue.UNKNOWN;
    }

    @Override
    public Set<Event> getSensitivities(TransitionResult currentState, ProcessState localState) {
        if (getSCProcess().getSensitivity().isEmpty()) {
            return Set.of();
        }
        throw new InsufficientPrecisionException("Nonempty process sensitivities");
    }
}
