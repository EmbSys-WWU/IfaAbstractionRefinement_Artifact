package de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic.BinaryOperation;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic.UnaryOperation;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState.StateInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState.StateInformationKey;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView.StackTrace;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler.SmallStepResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.GlobalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Interceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.LocalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.VariableHolder;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.sc_model.SCParameter;
import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.AccessExpression;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.SCPortSCSocketExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.expressions.UnaryExpression;
import de.tub.pes.syscir.sc_model.variables.SCEvent;
import de.tub.pes.syscir.sc_model.variables.SCPortEvent;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Class for the special treatment of some expressions by {@link SomeVariablesProcess} and
 * {@link SomeVariablesScheduler}.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <LocalStateT> the type of local state
 * @param <ValueT> the type of abstracted value
 * @param <TransitionResultT> the type of transition result
 */
public class SomeVariablesExpressionHandler {

    public static class AccessedVariablesInformation extends LinkedHashSet<Variable<?, ?>> implements StateInformation {

        private static final long serialVersionUID = -8549558425124611127L;

        public AccessedVariablesInformation() {
            super();
        }

        public AccessedVariablesInformation(Collection<? extends Variable<?, ?>> copyOf) {
            super(copyOf);
        }

        @Override
        public AccessedVariablesInformation copy() {
            return new AccessedVariablesInformation(this);
        }
    }

    public static final StateInformationKey<AccessedVariablesInformation> VARIABLES_READ_KEY =
            new StateInformationKey<>();
    public static final StateInformationKey<AccessedVariablesInformation> VARIABLES_WRITTEN_KEY =
            new StateInformationKey<>();
    /**
     * This pattern is used to detect compound assignments
     */
    public static final Pattern COMPOUND_ASSIGNMENT_PATTERN = Pattern.compile("^[^=!]=$");

    /**
     * Returns the internally used SCEvent for an SCPortEvent and the specific port instance.
     *
     * @param event the SCPortEvent
     * @param instance the actual instance of the channel interfaced by the port
     * @return the internally used SCEvent
     */
    public static SCEvent getPortEvent(String eventType, WrappedSCClassInstance instance) {
        if (instance.getType().startsWith("sc_signal")) {
            if (eventType.equals("default_event") || eventType.equals("change")) {
                return new SCEvent("change", false, false, List.of());
            }
            throw new NoSuchElementException(eventType + " on " + instance.toString());
        } else if (instance.getType().startsWith("sc_fifo")) {
            if (eventType.equals("data_read")) {
                return new SCEvent("data_read_event", false, false, List.of());
            } else if (eventType.equals("data_written")) {
                return new SCEvent("data_written_event", false, false, List.of());
            } else if (eventType.equals("default_event")) {
                throw new NoSuchElementException("fifo has no default_event");
            }
            throw new NoSuchElementException(eventType + " on " + instance.toString());
        } else if (instance.getType().equals("sc_clock")) {
            if (eventType.equals("default_event") || eventType.equals("change")) {
                return new SCEvent("change", false, false, List.of());
            }
            if (eventType.equals("edge")) {
                return new SCEvent("edge", false, false, List.of());
            }
            throw new NoSuchElementException(eventType + " on " + instance.toString());
        }

        throw new UnsupportedOperationException(eventType + " on " + instance.toString());
    }

    private final ExpressionCrawler crawler;
    private final AbstractedLogic logic;
    private final Interceptor interceptor;

    /**
     * Constructs a new SomeVariableNoInformationExpressionHandler.
     * 
     * @param crawler the expression crawler delegating some calls to this handler
     * @param globalVariableStorageCondition the condition under which values of global variables are
     *        stored
     * @param localVariableStorageCondition the condition under which values of local variables are
     *        stored
     */
    public SomeVariablesExpressionHandler(ExpressionCrawler crawler, AbstractedLogic logic, Interceptor interceptor) {
        this.crawler = crawler;
        this.logic = logic;
        this.interceptor = interceptor;
    }

    /**
     * Returns, as an abstracted value, the actual {@link Event} instance of an SCPortEvent, given some
     * global state.
     *
     * @param globalState the global state
     * @param portEvent an SCPortEvent
     * @return the actual {@link Event} instance
     */
    protected GlobalVariable<WrappedSCClassInstance, SCVariable> getEventVariable(SCPort port) {
        WrappedSCClassInstance instance = this.crawler.getChannel(port);
        SCVariable scVariable = getPortEvent("default_event", instance);

        return new GlobalVariable<>(instance, scVariable);
    }

    /**
     * Returns, as an abstracted value, the actual {@link Event} instance of an SCPortEvent, given some
     * global state.
     *
     * @param globalState the global state
     * @param portEvent an SCPortEvent
     * @return the actual {@link Event} instance
     */
    protected GlobalVariable<WrappedSCClassInstance, SCVariable> getEventVariable(SCPortEvent portEvent) {
        WrappedSCClassInstance instance = this.crawler.getChannel(portEvent.getPort());
        SCVariable scVariable = getPortEvent(portEvent.getEventType(), instance);

        return new GlobalVariable<>(instance, scVariable);
    }

    /**
     * Returns, as an abstracted value, the actual {@link Event} instance of an SCPortEvent, given some
     * global state.
     *
     * @param globalState the global state
     * @param portEvent an SCPortEvent
     * @return the actual {@link Event} instance
     */
    protected AbstractedValue getEventValue(SomeVariablesGlobalState globalState,
            GlobalVariable<WrappedSCClassInstance, SCVariable> eventVariable) {
        AbstractedValue eventValue = this.interceptor.variables().readGlobalVariable(globalState, eventVariable,
                globalState.getValue(eventVariable, this.logic::unknown));
        return eventValue;
    }

    /**
     * See
     * {@link ExpressionCrawler#handleSpecialExpression(TransitionResult, LocalState, Expression, int)}.
     * 
     * This implementation overrides the behavior for {@link SCVariableExpression}s and
     * {@link SCPortSCSocketExpression}s by finding the appropriate variable and using it or its stored
     * value as the evaluation result.
     *
     * @param currentState the current state of the evaluation
     * @param expression the next expression to be evaluated
     * @param comingFrom from where the expression is reached (-1 if from its parent, or the index of
     *        the child which has last been evaluated)
     * @return the result overwriting the usual logic of {@link #makeSmallStep(TransitionResult)}, or
     *         null if no special treatment is given
     */
    protected SmallStepResult handleSpecialExpression(TransitionResult currentState, LocalState localState,
            Expression expression, int comingFrom) {
        localState.setStateInformation(VARIABLES_READ_KEY, null);
        localState.setStateInformation(VARIABLES_WRITTEN_KEY, null);

        Object var;
        boolean global;

        if (expression instanceof SCVariableExpression ve) {
            var = ve.getVar();
            if (localState.getTopOfStack().getFunction().getParameters().stream()
                    .anyMatch(p -> p.getVar().equals(var))) {
                global = false;
            } else {
                global = (ve.getVar().getDeclaration() == null) || (ve.getVar().getDeclaration().getParent() == null);
            }
        } else if (expression instanceof SCPortSCSocketExpression pe) {
            var = pe.getSCPortSCSocket();
            global = true;
        } else {
            return null;
        }

        AccessedVariablesInformation readVariables = new AccessedVariablesInformation();
        localState.setStateInformation(VARIABLES_READ_KEY, readVariables);

        assert comingFrom == -1;

        // most variable expressions are treated as their values, but if they appear on the left hand side
        // of an assignment (or they are the rightmost part of an access which appears on the left hand side
        // of an assignment), they are treated as the variables
        boolean treatAsValue;
        if (expression.getParent() instanceof UnaryExpression ue
                && (ue.getOperator().equals("++") || ue.getOperator().equals("--"))) {
            treatAsValue = false;
        } else if (expression.getParent() instanceof BinaryExpression parent
                && (parent.getOp().equals("=") || COMPOUND_ASSIGNMENT_PATTERN.matcher(parent.getOp()).find())
                && expression == parent.getLeft()) {
            treatAsValue = false;
        } else if (expression.getParent() instanceof AccessExpression parent) {
            if (expression == parent.getLeft()) {
                treatAsValue = true;
            } else if (parent.getParent() instanceof BinaryExpression grandparent && grandparent.getOp().equals("=")
                    && parent == grandparent.getLeft()) {
                treatAsValue = false;
            } else {
                treatAsValue = true;
            }
        } else if (expression.getParent() instanceof SCVariableDeclarationExpression parent
                && expression == parent.getVariable()) {
            treatAsValue = false;
        } else {
            treatAsValue = true;
        }

        SomeVariablesGlobalState globalState = (SomeVariablesGlobalState) currentState.globalState();

        AbstractedValue result;
        if (treatAsValue) {
            // distinguish between local and global variable based on the existence of a DeclarationExpression
            if (!global) {
                LocalVariable<?> variable = new LocalVariable<>(localState.getStackTrace(), var);
                readVariables.add(variable);
                result = this.interceptor.variables().readLocalVariable(localState, variable,
                        ((VariableHolder<Variable<?, ?>>) localState).getValue(variable, this.logic::unknown));
            } else {
                // TODO: assume variable is global, is this correct?
                if (var instanceof SCPortEvent portEvent) {
                    GlobalVariable<WrappedSCClassInstance, SCVariable> variable = getEventVariable(portEvent);
                    readVariables.add(variable);
                    result = getEventValue(globalState, variable);
                } else if (var instanceof SCPort port && expression.getParent() instanceof FunctionCallExpression fe
                        && fe.getFunction().getName().equals("wait")) {
                    GlobalVariable<WrappedSCClassInstance, SCVariable> variable = getEventVariable(port);
                    readVariables.add(variable);
                    result = getEventValue(globalState, variable);
                } else {
                    AbstractedValue scope = getVariableQualifier(currentState, localState, expression);
                    if (!scope.isDetermined()) {
                        readVariables.add(new GlobalVariable<>(scope, var));
                        this.crawler.returnToParent(expression, localState, this.logic.unknown(scope));
                        return new SmallStepResult(List.of(currentState), false, false);
                    } else {
                        GlobalVariable<?, ?> variable = new GlobalVariable<>(scope.get(), var);
                        readVariables.add(variable);
                        result = this.interceptor.variables().readGlobalVariable(globalState, variable,
                                globalState.getValue(variable, this.logic::unknown));
                    }
                }
            }
        } else {
            // see above regarding global/local
            if (!global) {
                result = this.logic.value(new LocalVariable<>(localState.getStackTrace(), var));
            } else {
                AbstractedValue scope = getVariableQualifier(currentState, localState, expression);
                if (!scope.isDetermined()) {
                    this.crawler.returnToParent(expression, localState, this.logic.unknown(scope));
                    return new SmallStepResult(List.of(currentState), false, false);
                } else {
                    result = this.logic.value(new GlobalVariable<>(scope.get(), var));
                }
            }
        }

        this.crawler.returnToParent(expression, localState, result);
        return this.crawler.createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    /**
     * Returns, as an abstracted value, the qualifier for the variable accessed in the given expression.
     * 
     * See {@link Variable}.
     *
     * @param currentState the current state
     * @param localState the local portion of the state
     * @param expression an expression constituting a variable
     * @return the qualifier for the variable
     */
    protected AbstractedValue getVariableQualifier(TransitionResult currentState, LocalState localState,
            Expression expression) {
        // if the variable is the right hand side of an access, the result of the left hand side is the
        // scope. otherwise, this is the scope.
        if (expression.getParent() instanceof AccessExpression parent && parent.getRight() == expression) {
            AbstractedValue scopeVal = this.crawler.getValueOfExpression(currentState, localState, 1, 0);
            return scopeVal;
        } else {
            return localState.getTopOfStack().getThisValue();
        }
    }

    /**
     * This method finds the value of a given variable from the local or global state.
     * 
     * @param currentState the current state
     * @param localState the local portion of the state
     * @param expression an expression containing a variable
     * @param var the variable in question
     * @return the abstracted value of the variable
     */
    protected AbstractedValue determineVariableValue(TransitionResult currentState, LocalState localState,
            Expression expression, AbstractedValue var) {

        SomeVariablesGlobalState globalState = (SomeVariablesGlobalState) currentState.globalState();

        return switch (var.get()) {
            case LocalVariable<?> variable -> this.interceptor.variables().readLocalVariable(localState, variable,
                    ((VariableHolder<Variable<?, ?>>) localState).getValue(variable, this.logic::unknown));
            case GlobalVariable<?, ?> variable -> this.interceptor.variables().readGlobalVariable(globalState, variable,
                    globalState.getVariableValues().getOrDefault(variable, this.logic.unknown()));
            default -> throw new ClassCastException(
                    "Unexpected type of variable: " + var.get().getClass().getCanonicalName());
        };
    }

    /**
     * See {@link ExpressionCrawler#aggregateExpressionValue(TransitionResult, LocalState, Expression)}.
     *
     * @param currentState the current state of the evaluation
     * @param localState the local portion of the state
     * @param expression the expression to be evaluated
     * @return an abstraction of the value of the expression (may not be null, but may be non-determined
     *         or an abstraction of null)
     */
    protected AbstractedValue aggregateExpressionValue(TransitionResult currentState, LocalState localState,
            Expression expression) {
        SomeVariablesGlobalState globalState = (SomeVariablesGlobalState) currentState.globalState();

        // Differentiate between "++", "--" and other unaryExpressions due to their different handling in
        // handleSpecialExpression
        if (expression instanceof UnaryExpression ue
                && (ue.getOperator().equals("++") || ue.getOperator().equals("--"))) {
            // find the variable that this operation is for
            AbstractedValue variable = this.crawler.getValueOfChild(currentState, localState, 0);

            if (!variable.isDetermined()) {
                return this.logic.unknown();
            }

            // Determine value of the given variable
            AbstractedValue value = determineVariableValue(currentState, localState, expression, variable);

            // Calculate the value of the expression after the operation.
            AbstractedValue valueAfter = UnaryOperation.get(ue.getOperator()).apply(this.logic, value);

            // since the operation assigned a value, add the writing access to localState or globalState
            AccessedVariablesInformation writtenVariables = (AccessedVariablesInformation) localState
                    .getOrComputeStateInformation(VARIABLES_WRITTEN_KEY, AccessedVariablesInformation::new);

            if (variable.get() instanceof GlobalVariable<?, ?> gv) {
                writtenVariables.add(gv);
                globalState.setVariableValue(gv,
                        this.interceptor.variables().writeGlobalVariable(globalState, gv, valueAfter));
            } else if (variable.get() instanceof LocalVariable<?> lv) {
                writtenVariables.add(lv);
                ((VariableHolder<Variable<?, ?>>) localState).setVariableValue(lv,
                        this.interceptor.variables().writeLocalVariable(localState, lv, valueAfter));
            }

            // calculate the result of the operation
            if (ue.isPrepost()) {
                return valueAfter;
            } else {
                return value;
            }
        } else if (expression instanceof UnaryExpression ue) {
            // Determine value of the subexpression
            AbstractedValue value = this.crawler.getValueOfChild(currentState, localState, 0);

            // Calculate the value of the expression after the operation.
            return UnaryOperation.get(ue.getOperator()).apply(this.logic, value);
        }

        if (expression instanceof BinaryExpression be) {
            AbstractedValue left = this.crawler.getValueOfChild(currentState, localState, 0);
            AbstractedValue right = this.crawler.getValueOfChild(currentState, localState, 1);

            if (be.getOp().equals("=")) {
                if (!left.isDetermined()) {
                    return this.logic.unknown();
                }

                AccessedVariablesInformation writtenVariables = (AccessedVariablesInformation) localState
                        .getOrComputeStateInformation(VARIABLES_WRITTEN_KEY, AccessedVariablesInformation::new);

                if (left.get() instanceof GlobalVariable<?, ?> gv) {
                    writtenVariables.add(gv);
                    globalState.setVariableValue(gv,
                            this.interceptor.variables().writeGlobalVariable(globalState, gv, right));
                } else if (left.get() instanceof LocalVariable<?> lv) {
                    writtenVariables.add(lv);
                    ((VariableHolder<Variable<?, ?>>) localState).setVariableValue(lv,
                            this.interceptor.variables().writeLocalVariable(localState, lv, right));
                }

                return right;
            } else if (be.getOp().equals("==") || be.getOp().equals("!=")) {
                // This would be falsely flagged as a compound assignment.
                return BinaryOperation.get(be.getOp()).apply(this.logic, left, right);
            } else if (COMPOUND_ASSIGNMENT_PATTERN.matcher(be.getOp()).find()) {
                // check if the binary operator is some compound assignment with a single character for the
                // operation, because in that case it is a writing access

                // if the left side is undetermined, this cannot be calculated
                if (!left.isDetermined()) {
                    return this.logic.unknown();
                }

                // Determine value of left side of the compound assignment.
                AbstractedValue leftValue = determineVariableValue(currentState, localState, expression, left);

                // Calculate value after the execution.
                AbstractedValue valueAfter =
                        BinaryOperation.get(be.getOp().split("=")[0]).apply(this.logic, leftValue, right);

                // add this writing access to local and global state
                AccessedVariablesInformation writtenVariables = (AccessedVariablesInformation) localState
                        .getOrComputeStateInformation(VARIABLES_WRITTEN_KEY, AccessedVariablesInformation::new);

                if (left.get() instanceof GlobalVariable<?, ?> gv) {
                    writtenVariables.add(gv);
                    globalState.setVariableValue(gv,
                            this.interceptor.variables().writeGlobalVariable(globalState, gv, valueAfter));
                } else if (left.get() instanceof LocalVariable<?> lv) {
                    writtenVariables.add(lv);
                    ((VariableHolder<Variable<?, ?>>) localState).setVariableValue(lv,
                            this.interceptor.variables().writeLocalVariable(localState, lv, valueAfter));
                }

                // as the result of the operation, return the addition of the two values
                return valueAfter;
            } else {
                // TODO: add more operators?
                return BinaryOperation.get(be.getOp()).apply(this.logic, left, right);
            }
        }

        if (expression instanceof AccessExpression ae) {
            AbstractedValue left = this.crawler.getValueOfChild(currentState, localState, 0);
            AbstractedValue right = this.crawler.getValueOfChild(currentState, localState, 1);

            if (ae.getRight() instanceof FunctionCallExpression fe) {
                return right;
            }

            if (!left.isDetermined() || !right.isDetermined()) {
                return this.logic.unknown();
            }

            // TODO: is that right?
            // TODO: always treat VariableExpression as variable if in access and only evaluate in aggregation?
            GlobalVariable<?, ?> gv = new GlobalVariable<>(left.get(), (SCVariable) right.get());
            return this.interceptor.variables().readGlobalVariable(globalState, gv,
                    globalState.getVariableValues().getOrDefault(gv, this.logic.unknown()));
        }

        if (expression instanceof SCVariableDeclarationExpression de) {
            // a variable declaration has no return value, does it?..
            if (de.getInitialValues().isEmpty()) {
                return this.logic.unknown();
            }

            AbstractedValue left = this.crawler.getValueOfChild(currentState, localState, 0);
            AbstractedValue right = this.crawler.getValueOfChild(currentState, localState, 1);

            if (!left.isDetermined()) {
                return this.logic.unknown();
            }

            AccessedVariablesInformation writtenVariables = (AccessedVariablesInformation) localState
                    .getOrComputeStateInformation(VARIABLES_WRITTEN_KEY, AccessedVariablesInformation::new);

            if (left.get() instanceof LocalVariable<?> lv) {
                writtenVariables.add(lv);
                ((VariableHolder<Variable<?, ?>>) localState).setVariableValue(lv,
                        this.interceptor.variables().writeLocalVariable(localState, lv, right));
            }

            if (!right.isDetermined()) {
                return this.logic.unknown();
            }

            return right;
        }

        return this.logic.unknown();
    }

    /**
     * Called when a function has been called (i.e. entered), right before the SmallStepResult is
     * created. Not called when entering special case functions (wait, notify, request_update).
     * 
     * Used to adjust the resulting state, i.e. for setting parameter values.
     *
     * @param expression the evaluated function call expression
     * @param currentState the resulting state
     * @param localState the local part of that state
     */
    public void functionCalled(FunctionCallExpression expression, TransitionResult currentState,
            LocalState localState) {
        EvaluationContext callingContext =
                localState.getExecutionStack().get(localState.getExecutionStack().size() - 2);
        StackTrace stackTrace = localState.getStackTrace();

        AccessedVariablesInformation writtenVariables = (AccessedVariablesInformation) localState
                .getOrComputeStateInformation(VARIABLES_WRITTEN_KEY, AccessedVariablesInformation::new);

        int i = 0;
        for (SCParameter param : expression.getFunction().getParameters()) {
            LocalVariable<SCVariable> var = new LocalVariable<>(stackTrace, param.getVar());
            AbstractedValue value = callingContext.getExpressionValue(0, i);
            ((VariableHolder<Variable<?, ?>>) localState).setVariableValue(var,
                    this.interceptor.variables().writeLocalVariable(localState, var, value));
            writtenVariables.add(var);
            i++;
        }
    }


    /**
     * Called when a function has been returned from (i.e. left), right before the SmallStepResult is
     * created. Not called when leaving special case functions (wait, notify, request_update) or the top
     * level function.
     * 
     * Used to adjust the resulting state, i.e. for resetting parameter values.
     *
     * @param expression the function call expression which lead to the function being entered
     * @param currentState the resulting state
     * @param localState the local part of that state
     */
    protected void functionReturned(FunctionCallExpression expression, TransitionResult currentState,
            LocalState localState) {
        StackTrace stackTrace = localState.getStackTrace(1);
        stackTrace.addCall(localState.getTopOfStack().toLocation());

        for (SCParameter param : expression.getFunction().getParameters()) {
            LocalVariable<SCVariable> var = new LocalVariable<>(stackTrace, param.getVar());
            ((VariableHolder<Variable<?, ?>>) localState).setVariableValue(var,
                    this.interceptor.variables().writeLocalVariable(localState, var, null));
        }
    }

}
