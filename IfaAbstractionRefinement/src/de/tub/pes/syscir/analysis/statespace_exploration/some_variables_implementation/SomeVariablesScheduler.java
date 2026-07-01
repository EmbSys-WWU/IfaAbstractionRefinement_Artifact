package de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BaseScheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Interceptor;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCFunction;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;

/**
 * Scheduler implementation storing the values of some variables and providing no additional
 * transition information.
 * 
 * @author Jonas Becker-Kupczok
 */
public class SomeVariablesScheduler extends BaseScheduler {

    private SomeVariablesExpressionHandler expressionHandler;

    /**
     * Creates a new StandardScheduler with the given stop mode.
     *
     * @param scSystem the SysCIR representation of the entire SystemC design
     * @param stopMode the stop mode for this scheduler
     * @param globalVariableStorageCondition the condition under which the values of global variables
     *        are stored
     * @param localVariableStorageCondition the condition under which the values of local variables are
     *        stored
     */
    public SomeVariablesScheduler(SCSystem scSystem, AbstractedLogic logic, Interceptor interceptor,
            SimulationStopMode stopMode, Predicate<? super Event> eventConsiderationCondition) {
        super(scSystem, logic, interceptor, stopMode, eventConsiderationCondition);

        this.expressionHandler = new SomeVariablesExpressionHandler(this, logic, interceptor);
    }

    @Override
    public SomeVariablesSchedulerState constructLocalSchedulerState(WrappedSCClassInstance port,
            WrappedSCFunction entryPoint) {
        List<EvaluationContext> executionStack = new ArrayList<>();
        List<List<AbstractedValue>> expressionValues = new ArrayList<>();
        expressionValues.add(new ArrayList<>());
        executionStack.add(
                new EvaluationContext(entryPoint, new ArrayList<>(), -1, expressionValues, this.logic.value(port)));
        return new SomeVariablesSchedulerState(executionStack);
    }

    @Override
    public SmallStepResult handleSpecialExpression(TransitionResult currentState, LocalState localState,
            Expression expression, int comingFrom) {
        return this.expressionHandler.handleSpecialExpression(currentState,
                localState, expression, comingFrom);
    }

    @Override
    public AbstractedValue aggregateExpressionValue(TransitionResult currentState, LocalState localState,
            Expression expression) {
        return this.expressionHandler.aggregateExpressionValue(currentState,
                localState, expression);
    }

    @Override
    protected void functionCalled(FunctionCallExpression expression, TransitionResult currentState,
            LocalState localState) {
        this.expressionHandler.functionCalled(expression, currentState,
                localState);
    }

    @Override
    protected void functionReturned(FunctionCallExpression expression, TransitionResult currentState,
            LocalState localState) {
        this.expressionHandler.functionReturned(expression, currentState,
                localState);
    }

}
