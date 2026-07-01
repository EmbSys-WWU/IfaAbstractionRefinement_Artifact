package de.tub.pes.syscir.analysis.statespace_exploration.transition_informations;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesExpressionHandler;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesExpressionHandler.AccessedVariablesInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler.ExecutionConditions;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.InformationHandler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.SCPortSCSocketExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;


public class VariablesReadWrittenInformationHandler implements InformationHandler {

    private final AbstractedLogic logic;
    private final Predicate<Variable<?, ?>> variableConsiderationPredicate;

    public VariablesReadWrittenInformationHandler(AbstractedLogic logic,
            Predicate<Variable<?, ?>> variableConsiderationPredicate) {
        this.logic = Objects.requireNonNull(logic);
        this.variableConsiderationPredicate = Objects.requireNonNull(variableConsiderationPredicate);
    }

    @Override
    public VariablesReadWrittenInformation getInitialInformation(ConsideredState state) {
        return new VariablesReadWrittenInformation(this.logic);
    }

    @Override
    public TransitionInformation handleStartOfCode(TransitionResult currentState, LocalState localState) {
        return currentState.transitionInformation();
    }

    // TODO: make better (unnecessarily convoluted at the moment, seperate methods in information for
    // adding read/written)
    @Override
    public TransitionInformation handleExpressionEvaluation(Expression evaluated, int comingFrom,
            TransitionResult resultingState, LocalState localState) {
        // TODO: the current condition contains values that only hold at the moment the control decision
        // was made. the may have been changed afterwards without retroactively effecting the control
        // decision, making the current value obsolete. how to deal with that?

        VariablesReadWrittenInformation rwinfo =
                (VariablesReadWrittenInformation) resultingState.transitionInformation();

        if (evaluated instanceof SCVariableExpression || evaluated instanceof SCPortSCSocketExpression) {
            AccessedVariablesInformation readVariables =
                    localState.getStateInformation(SomeVariablesExpressionHandler.VARIABLES_READ_KEY);
            if (readVariables == null) {
                return rwinfo;
            }
            Map<Variable<?, ?>, AbstractedValue> informationMap =
                    createInformationMap(readVariables, getCurrentCondition(localState));
            return rwinfo.concat(new VariablesReadWrittenInformation(this.logic, informationMap, Map.of()));
        } else if (evaluated instanceof BinaryExpression be) {
            if (be.getOp().equals("=")) {
                AccessedVariablesInformation writtenVariables =
                        localState.getStateInformation(SomeVariablesExpressionHandler.VARIABLES_WRITTEN_KEY);
                if (writtenVariables == null) {
                    return rwinfo;
                }
                Map<Variable<?, ?>, AbstractedValue> informationMap =
                        createInformationMap(writtenVariables, getCurrentCondition(localState));
                return rwinfo.concat(new VariablesReadWrittenInformation(this.logic, Map.of(), informationMap));
            }
        }

        return rwinfo;
    }

    protected AbstractedValue getCurrentCondition(LocalState localState) {
        ExecutionConditions currentExecutionConditions =
                localState.getStateInformation(ExpressionCrawler.EXECUTION_CONDITION_KEY);
        if (currentExecutionConditions == null) {
            return this.logic.value(true);
        }

        return currentExecutionConditions.getConditions().stream().reduce(this.logic.value(true), this.logic::and);
    }

    protected Map<Variable<?, ?>, AbstractedValue> createInformationMap(AccessedVariablesInformation accessedVariables,
            AbstractedValue condition) {
        Map<Variable<?, ?>, AbstractedValue> result = new LinkedHashMap<>();
        for (Variable<?, ?> var : accessedVariables) {
            if (this.variableConsiderationPredicate.test(var))
                result.put(var, condition);
        }
        return result;
    }

    @Override
    public TransitionInformation handleProcessWaitedForDelta(AnalyzedProcess process,
            ProcessState resultingState, TransitionInformation currentInformation) {
        return currentInformation;
    }

    @Override
    public TransitionInformation handleProcessWaitedForTime(AnalyzedProcess process, ProcessState resultingState,
            TransitionInformation currentInformation) {
        return currentInformation;
    }

    @Override
    public TransitionInformation handleProcessWaitedForEvents(AnalyzedProcess process, ProcessState resultingState,
            Set<Event> events, EventBlocker blockerBefore, TransitionInformation currentInformation) {
        return currentInformation;
    }

}
