package de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessTransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesExpressionHandler.AccessedVariablesInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BaseProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.GlobalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Interceptor;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCEvent;
import de.tub.pes.syscir.sc_model.variables.SCPortEvent;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Process implementation storing the values of some variables and providing no additional
 * transition information.
 * 
 * @author Jonas Becker-Kupczok
 */
public class SomeVariablesProcess extends BaseProcess {

    private SomeVariablesExpressionHandler expressionHandler;

    /**
     * Constructs a new SomeVariableNoInformationProcess representing the given SysCIR process,
     * belonging to the given SysCIR class instance (i.e. the module instance) and using the given
     * scheduler. The given conditions are used to determine whether or not the value of a variable
     * shall be stored.
     * 
     * @param scSystem the SysCIR representation of the entire SystemC design
     * @param scProcess the SysCIR process
     * @param scClassInstance owning instance of this process
     * @param scheduler the scheduler used in this analysis
     * @param globalVariableStorageCondition the condition under which the values of global variables
     *        are stored
     * @param localVariableStorageCondition the condition under which the values of local variables are
     *        stored
     */
    public SomeVariablesProcess(SCSystem scSystem, SCProcess scProcess, SCClassInstance scClassInstance,
            Scheduler scheduler, AbstractedLogic logic, Interceptor interceptor) {
        super(scSystem, scProcess, scClassInstance, scheduler, logic, interceptor);

        this.expressionHandler = new SomeVariablesExpressionHandler(this, logic, interceptor);
    }

    @Override
    public Set<Event> getSensitivities(TransitionResult currentState, ProcessState localState) {
        return getSensitivities((SomeVariablesGlobalState) currentState.globalState());
    }

    /**
     * See {@link #getSensitivities(ProcessTransitionResult, SomeVariablesProcessState)}.
     * 
     * @param globalState the global state
     * @return the set of events the process is statically sensitive on
     */
    public Set<Event> getSensitivities(SomeVariablesGlobalState globalState) {
        Set<Event> result = new LinkedHashSet<>();

        for (SCEvent scEvent : getSCProcess().getSensitivity()) {
            // the readVariables are not relevant here, since we only want to find the events on which the
            // process is statically sensitive
            AbstractedValue eventValue = getEventValue(globalState, scEvent, new AccessedVariablesInformation());
            if (!eventValue.isDetermined()) {
                throw new InsufficientValueTrackingException(eventValue);
            }
            result.add((Event) eventValue.get());
        }

        return result;
    }

    /**
     * See {@link SomeVariablesExpressionHandler#getEventValue(SomeVariablesGlobalState, SCPortEvent)}.
     * This method is meant only to find the events on which the process is statically sensitive (see
     * {@link #getSensitivities(SomeVariablesGlobalState)}), it may yield false results in other
     * contexts.
     *
     * @param globalState the global state
     * @param scEvent the SysCIR variable for the event on which the process is statically sensitive
     * @param readVariables the information about which variables have been read in the current
     *        transition
     * @return the (abstracted) event value
     */
    public AbstractedValue getEventValue(SomeVariablesGlobalState globalState, SCEvent scEvent,
            AccessedVariablesInformation readVariables) {
        GlobalVariable<?, ?> var;
        if (!(scEvent instanceof SCPortEvent portEvent)) {
            var = new GlobalVariable<>(getSCClassInstance(), scEvent);
        } else {
            var = this.expressionHandler.getEventVariable(portEvent);
        }

        // since var is a global variable, we can pass null for the local state (not nice, but ok)
        return this.expressionHandler.getVariableValue(globalState, null, var, readVariables);
    }

    @Override
    public SmallStepResult handleSpecialExpression(TransitionResult currentState, LocalState localState,
            Expression expression, int comingFrom) {
        return this.expressionHandler.handleSpecialExpression(currentState, localState, expression, comingFrom);
    }

    @Override
    public AbstractedValue aggregateExpressionValue(TransitionResult currentState, LocalState localState,
            Expression expression) {
        return this.expressionHandler.aggregateExpressionValue(currentState, localState, expression);
    }

    @Override
    protected void functionCalled(FunctionCallExpression expression, TransitionResult currentState,
            LocalState localState) {
        this.expressionHandler.functionCalled(expression, currentState, localState);
    }

    @Override
    protected void functionReturned(FunctionCallExpression expression, TransitionResult currentState,
            LocalState localState) {
        this.expressionHandler.functionReturned(expression, currentState, localState);
    }

}
