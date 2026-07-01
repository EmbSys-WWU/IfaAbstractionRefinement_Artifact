package de.tub.pes.syscir.analysis.statespace_exploration.no_variables_implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BaseScheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue.BinaryAbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Interceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.VariableInterceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.NoInformation;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCFunction;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.Expression;


public class NoVariablesNoInformationScheduler extends BaseScheduler {

    public NoVariablesNoInformationScheduler(SCSystem scSystem, SimulationStopMode stopMode,
            Predicate<? super Event> eventConsiderationCondition) {
        super(scSystem, BinaryAbstractedLogic.INSTANCE,
                Interceptor.of(VariableInterceptor.trackNone(), NoInformation.HANDLER),
                stopMode, eventConsiderationCondition);
    }

    @Override
    public BinaryAbstractedValue aggregateExpressionValue(TransitionResult currentState, LocalState localState,
            Expression expression) {
        return BinaryAbstractedValue.UNKNOWN;
    }

    @Override
    public NoVariablesSchedulerLocalState constructLocalSchedulerState(WrappedSCClassInstance port,
            WrappedSCFunction entryPoint) {
        List<EvaluationContext> executionStack = new ArrayList<>();
        List<List<AbstractedValue>> expressionValues = new ArrayList<>();
        expressionValues.add(new ArrayList<>());
        executionStack.add(new EvaluationContext(entryPoint, new ArrayList<>(), -1, expressionValues,
                BinaryAbstractedValue.of(port)));
        return new NoVariablesSchedulerLocalState(executionStack);
    }

}
