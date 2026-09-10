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
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ArrayInstance;
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
import de.tub.pes.syscir.sc_model.SCREFERENCETYPE;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.AccessExpression;
import de.tub.pes.syscir.sc_model.expressions.ArrayAccessExpression;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.RefDerefExpression;
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
    protected GlobalVariable<WrappedSCClassInstance, SCEvent> getEventVariable(SCPort port) {
        WrappedSCClassInstance instance = this.crawler.getChannel(port);
        SCEvent scVariable = getPortEvent("default_event", instance);

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
    protected GlobalVariable<WrappedSCClassInstance, SCEvent> getEventVariable(SCPortEvent portEvent) {
        WrappedSCClassInstance instance = this.crawler.getChannel(portEvent.getPort());
        SCEvent scVariable = getPortEvent(portEvent.getEventType(), instance);

        return new GlobalVariable<>(instance, scVariable);
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
        boolean array = false;

        /*
         * A note on arrays: Unfortunately, due to how ArrayAccessExpression is modeled, they require custom
         * treatment. Specifically, it is not a BinaryExpression, with the array being left and the index
         * being right. Instead, it is an SCVariableExpression with the array being the variable and any
         * number of indices being subexpressions. Therefore, we cannot just handle them bottom up,
         * evaluating the array followed by one index.
         * 
         * Instead, we do the following: First, we evaluate the indices bottom up. Then, in one smallStep,
         * we evalute the array, its sub-arrays (in the case of multi-dimensional accesses), and (if reading
         * instead of writing it) the value of the array member. Most of that logic is delegated to
         * handleArrayAccessEvaluation. What we do here is figuring out whether the selected array element
         * is treated as a value or a variable (i.e. read or written), and getting the instance of the top
         * level array.
         * 
         * Because the SCVariable of an ArrayAccessExpression is the array itself, even if we want to treat
         * the array member as a variable, we always need to treat the array itself as a value.
         */

        if (expression instanceof ArrayAccessExpression ae && comingFrom < ae.getNumOfChildren() - 1) {
            return null;
        } else if (expression instanceof SCVariableExpression ve) {
            if (expression instanceof ArrayAccessExpression) {
                array = true;
            }
            var = ve.getVar();
            if (findParameter(localState, var) != null) {
                global = false;
            } else {
                // distinguish between local and global variable based on the existence of a DeclarationExpression
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

        assert array || comingFrom == -1;

        // most variable expressions are treated as their values, but if they appear on the left hand side
        // of an assignment (or they are the rightmost part of an access which appears on the left hand side
        // of an assignment), they are treated as the variables
        boolean treatAsValue = treatVariableAsValue(expression);

        AbstractedValue variable = getVariableObject(currentState, localState, expression, var, global);

        AbstractedValue result;
        if (!variable.isDetermined()) {
            result = this.logic.unknown(variable);
        } else if (treatAsValue || array) {
            // even if an array is not treated as a value, we still need to get the array instance - which is
            // why we always treat it as a value in this step
            result = getVariableValue((SomeVariablesGlobalState) currentState.globalState(), localState,
                    (Variable<?, ?>) variable.get(), readVariables);
        } else {
            result = variable;
        }

        if (array) {
            result = handleArrayAccessEvaluation((ArrayAccessExpression) expression, comingFrom, currentState,
                    localState, treatAsValue, result, readVariables);
        }

        this.crawler.returnToParent(expression, localState, result);
        return this.crawler.createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    /**
     * Determines whether the variable denoted by the given expression should be treated as its value
     * (i.e. read) rather than as the variable itself (i.e. written or references), based on the
     * expression's context within its parent.
     * 
     * A variable expression is treated as the variable itself (not as a value) if it is:
     * <ul>
     * <li>the operand of a "++" or "--" {@link UnaryExpression},</li>
     * <li>the left hand side of an assignment or compound assignment {@link BinaryExpression},</li>
     * <li>the right hand side of an {@link AccessExpression} whose own parent is the left hand side of
     * an assignment or a by-reference argument of a {@link FunctionCallExpression} (see
     * {@link #isByReferenceArgument(FunctionCallExpression, Expression)}),</li>
     * <li>the variable being declared in an {@link SCVariableDeclarationExpression},</li>
     * <li>the child of a referencing {@link RefDerefExpression}, or</li>
     * <li>a by-reference argument of a {@link FunctionCallExpression} (see
     * {@link #isByReferenceArgument(FunctionCallExpression, Expression)}).</li>
     * </ul>
     * In all other cases (including the left hand side of an {@link AccessExpression}, which always
     * needs to be resolved to its value/instance regardless of context), the expression is treated as a
     * value.
     * <p>
     * Note: passing an array access or a deeper/chained access expression by reference is not
     * recognized here (see the class documentation), meaning such arguments are always treated as
     * values, i.e. passed by (a copy of) their value rather than by reference.
     *
     * @param expression the expression denoting a variable, whose treatment is to be determined
     * @return {@code true} if the variable should be treated as its (read) value, {@code false} if it
     *         should be treated as the variable itself (to be written to)
     */
    protected boolean treatVariableAsValue(Expression expression) {
        if (expression.getParent() instanceof UnaryExpression ue
                && (ue.getOperator().equals("++") || ue.getOperator().equals("--"))) {
            return false;
        } else if (expression.getParent() instanceof BinaryExpression parent
                && (parent.getOp().equals("=") || COMPOUND_ASSIGNMENT_PATTERN.matcher(parent.getOp()).find())
                && expression == parent.getLeft()) {
            return false;
        } else if (expression.getParent() instanceof AccessExpression parent) {
            if (expression == parent.getLeft()) {
                return true;
            } else if (parent.getParent() instanceof BinaryExpression grandparent && grandparent.getOp().equals("=")
                    && parent == grandparent.getLeft()) {
                return false;
            } else if (parent.getParent() instanceof FunctionCallExpression grandparent
                    && isByReferenceArgument(grandparent, parent)) {
                return false;
            } else {
                return true;
            }
        } else if (expression.getParent() instanceof SCVariableDeclarationExpression parent
                && expression == parent.getVariable()) {
            return false;
        } else if (expression.getParent() instanceof RefDerefExpression re) {
            return !re.isReferencing();
        } else if (expression.getParent() instanceof FunctionCallExpression parent) {
            return !isByReferenceArgument(parent, expression);
        } else {
            return true;
        }
    }

    /**
     * Returns whether {@code argument} is passed as the argument of a by-reference ({@code &})
     * parameter of {@code call}, i.e. whether the corresponding {@link SCParameter#getRefType()} is
     * {@link SCREFERENCETYPE#BYREFERENCE}.
     * 
     * See the class documentation for the limitations of this check (only directly matching arguments,
     * not e.g. array accesses or deeper/chained access expressions, are recognized).
     *
     * @param call the function call expression
     * @param argument one of {@code call}'s parameter expressions (compared by identity)
     * @return whether {@code argument} is passed by reference
     */
    protected boolean isByReferenceArgument(FunctionCallExpression call, Expression argument) {
        List<Expression> arguments = call.getParameters();
        List<SCParameter> parameters = call.getFunction().getParameters();
        for (int i = 0; i < arguments.size() && i < parameters.size(); i++) {
            if (arguments.get(i) == argument) {
                return parameters.get(i).getRefType() == SCREFERENCETYPE.BYREFERENCE;
            }
        }
        return false;
    }

    /**
     * Returns the {@link SCParameter} of the currently executing function (the topmost element of
     * {@code localState}'s execution stack) whose variable equals {@code var}, or {@code null} if
     * {@code var} is not a parameter of that function.
     *
     * @param localState the local portion of the state
     * @param var the underlying variable identifier
     * @return the matching parameter, or {@code null}
     */
    protected SCParameter findParameter(LocalState localState, Object var) {
        return localState.getTopOfStack().getFunction().getParameters().stream().filter(p -> p.getVar().equals(var))
                .findFirst().orElse(null);
    }

    /**
     * Returns, as an abstracted value, the {@link Variable} object that is accessed by the given
     * expression.
     * 
     * If {@code global} is {@code false}, a {@link LocalVariable} is constructed using the current
     * stack trace (see the note on references below for special handling of by-reference parameters).
     * Otherwise, a {@link GlobalVariable} is constructed: special handling is applied if the accessed
     * variable is an {@link SCPortEvent}, or an {@link SCPort} that is the argument of a {@code wait}
     * call (both resulting in the corresponding event variable); in all other cases, the qualifier
     * (scope) of the variable is determined via
     * {@link #getVariableQualifier(TransitionResult, LocalState, Expression)} and used together with
     * {@code var} to construct the {@link GlobalVariable}.
     *
     * @param currentState the current state of the evaluation
     * @param localState the local portion of the state
     * @param expression the expression denoting the variable access, used to determine context (e.g.
     *        whether it is the argument of a {@code wait} call)
     * @param var the underlying variable identifier (e.g. an {@link SCVariable}, {@link SCPort}, or
     *        {@link SCPortEvent})
     * @param global whether the variable is a global variable ({@code true}) or a local variable
     *        ({@code false})
     * @return an abstraction of the resulting {@link Variable} object (may be non-determined if the
     *         qualifier of a global variable, or the target of a by-reference parameter, cannot be
     *         determined)
     */
    protected AbstractedValue getVariableObject(TransitionResult currentState, LocalState localState,
            Expression expression, Object var, boolean global) {
        /*
         * A note on references (&): Unlike pointers, the SysCIR object model does not represent C++
         * references as a distinct kind of variable - the only place a reference is recognizable at all is
         * SCParameter#getRefType() being SCREFERENCETYPE#BYREFERENCE. We support such parameters by
         * transparently aliasing the argument's underlying Variable: at the call site (see functionCalled)
         * the argument is resolved to its Variable instead of its value (see treatVariableAsValue and
         * isByReferenceArgument) and stored, as is, as the "value" of the parameter's LocalVariable slot -
         * the same trick used for pointers (see RefDerefExpression handling in aggregateExpressionValue),
         * except that here every subsequent access of the parameter is implicitly resolved right below,
         * without requiring an explicit "*"/"&" in the source. This is expected to also work for reference
         * parameters that themselves alias a pointer or an array member, since the aliasing mechanism only
         * relies on Variable being a self-contained address, not on the kind of variable it addresses -
         * this has, however, not been extensively tested.
         * 
         * Binding (in functionCalled) and unbinding (in functionReturned) such a parameter is intentionally
         * NOT recorded in AccessedVariablesInformation, as it is not a genuine, user-visible variable
         * access but merely establishes/clears an alias; actual reads/writes performed through the
         * reference are attributed to the aliased target variable instead, which we consider more accurate.
         * Consequently, we also read the raw stored value below directly, bypassing the read interceptor.
         */

        if (!global) {
            LocalVariable<Object> localVariable = new LocalVariable<>(localState.getStackTrace(), var);

            SCParameter parameter = findParameter(localState, var);
            if (parameter != null && parameter.getRefType() == SCREFERENCETYPE.BYREFERENCE) {
                AbstractedValue target =
                        ((VariableHolder<Variable<?, ?>>) localState).getValue(localVariable, this.logic::unknown);
                if (!target.isDetermined()) {
                    return this.logic.unknown(target);
                }
                return this.logic.value(target.get());
            }

            return this.logic.value(localVariable);
        } else {
            // TODO: assume variable is global, is this correct?
            if (var instanceof SCPortEvent portEvent) {
                return this.logic.value(getEventVariable(portEvent));
            } else if (var instanceof SCPort port && expression.getParent() instanceof FunctionCallExpression fe
                    && fe.getFunction().getName().equals("wait")) {
                return this.logic.value(getEventVariable(port));
            } else {
                AbstractedValue scope = getVariableQualifier(currentState, localState, expression);
                if (!scope.isDetermined()) {
                    return this.logic.unknown(scope);
                } else {
                    return this.logic.value(new GlobalVariable<>(scope.get(), var));
                }
            }
        }
    }

    /**
     * Returns, as an abstracted value, the current value stored for the given variable, delegating the
     * actual read to the {@link Interceptor}'s variable interceptor and recording the variable as read
     * in {@code readVariables}.
     * 
     * If {@code variable} is a {@link LocalVariable}, its value is read from {@code localState}. If it
     * is a {@link GlobalVariable}, its value is read from the global state contained in
     * {@code currentState}; if the global variable is an event variable,
     * {@link #getEventValue(SomeVariablesGlobalState, GlobalVariable)} is used instead of a plain
     * global variable read.
     *
     * @param globalState the global portion of the current state of the evaluation
     * @param localState the local portion of the current state of the evaluation
     * @param variable the variable whose value is to be read (either a {@link LocalVariable} or a
     *        {@link GlobalVariable})
     * @param readVariables the set of accessed variables to which {@code variable} is added
     * @return an abstraction of the current value of {@code variable}
     */
    protected AbstractedValue getVariableValue(SomeVariablesGlobalState globalState, LocalState localState,
            Variable<?, ?> variable, AccessedVariablesInformation readVariables) {
        return switch (variable) {
            case LocalVariable<?> lv -> {
                readVariables.add(variable);
                yield this.interceptor.variables().readLocalVariable(localState, lv,
                        ((VariableHolder<Variable<?, ?>>) localState).getValue(variable, this.logic::unknown));
            }
            case GlobalVariable<?, ?> gv -> {
                readVariables.add(variable);
                yield this.interceptor.variables().readGlobalVariable(globalState, gv,
                        globalState.getValue(gv, this.logic::unknown));
            }
        };
    }

    /**
     * Evaluates an {@link ArrayAccessExpression}, resolving the (possibly nested) array indices to
     * arrive at the accessed array member, given the (already evaluated) top level array instance.
     * 
     * Because an {@link ArrayAccessExpression} models the array as its variable and any number of
     * indices as subexpressions (instead of e.g. a {@link BinaryExpression} with array and index as
     * left and right), this method iterates over all but the last index child, reading the
     * corresponding array member (using its already evaluated index value) to obtain the next (nested)
     * array instance. If, at any point, the array instance or an index value is not determined, an
     * appropriate non-determined result is returned instead. For the last index, depending on
     * {@code treatAsValue}, the member's value is read and returned (if treated as a value) or the
     * {@link GlobalVariable} representing the array member itself is returned (if treated as a
     * variable, e.g. to be written to).
     *
     * @param expression the array access expression being evaluated
     * @param comingFrom the index of the child that triggered this evaluation (unused directly, but
     *        implies all index children have already been evaluated)
     * @param currentState the current state of the evaluation
     * @param localState the local portion of the state
     * @param treatAsValue whether the final accessed array member should be treated as its value (read)
     *        or as the variable itself (to be written to)
     * @param arrayInstanceValue an abstraction of the top level {@link ArrayInstance} being accessed
     * @param readVariables the set of accessed variables to which each read array member is added
     * @return an abstraction of the resulting value (if {@code treatAsValue} is {@code true}) or the
     *         {@link GlobalVariable} representing the accessed array member (if {@code treatAsValue} is
     *         {@code false}); may be non-determined if the array instance or any index value is not
     *         determined
     */
    protected AbstractedValue handleArrayAccessEvaluation(ArrayAccessExpression expression, int comingFrom,
            TransitionResult currentState, LocalState localState, boolean treatAsValue,
            AbstractedValue arrayInstanceValue, AccessedVariablesInformation readVariables) {

        SomeVariablesGlobalState globalState = (SomeVariablesGlobalState) currentState.globalState();

        for (int i = 0; i < expression.getNumOfChildren() - 1; i++) {
            if (!arrayInstanceValue.isDetermined()) {
                // throwing an InsufficientPrecisionException here would mandate always tracking all array indices,
                // even if the read/written values are of no interest to the analysis
                return this.logic.unknown(arrayInstanceValue);
            }

            AbstractedValue indexValue = localState.getTopOfStack().getExpressionValue(0, i);
            if (!indexValue.isDetermined()) {
                return this.logic.unknown(indexValue);
            }

            ArrayInstance arrayInstance = (ArrayInstance) arrayInstanceValue.get();
            GlobalVariable<ArrayInstance, Integer> memberVariable =
                    GlobalVariable.arrayMember(arrayInstance, (Integer) indexValue.get());
            readVariables.add(memberVariable);
            arrayInstanceValue = this.interceptor.variables().readGlobalVariable(globalState, memberVariable,
                    globalState.getValue(memberVariable, this.logic::unknown));
        }

        if (!arrayInstanceValue.isDetermined()) {
            return this.logic.unknown(arrayInstanceValue);
        }

        AbstractedValue indexValue =
                localState.getTopOfStack().getExpressionValue(0, expression.getNumOfChildren() - 1);
        if (!indexValue.isDetermined()) {
            return this.logic.unknown(indexValue);
        }

        GlobalVariable<ArrayInstance, Integer> memberVariable =
                GlobalVariable.arrayMember((ArrayInstance) arrayInstanceValue.get(), (Integer) indexValue.get());
        if (treatAsValue) {
            readVariables.add(memberVariable);
            return this.interceptor.variables().readGlobalVariable(globalState, memberVariable,
                    globalState.getValue(memberVariable, this.logic::unknown));
        } else {
            return this.logic.value(memberVariable);
        }
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
                return this.logic.unknown(variable);
            }

            // since the operation reads and writes a value, add the reading and writing access to localState or
            // globalState
            AccessedVariablesInformation readVariables = (AccessedVariablesInformation) localState
                    .getOrComputeStateInformation(VARIABLES_READ_KEY, AccessedVariablesInformation::new);
            AccessedVariablesInformation writtenVariables = (AccessedVariablesInformation) localState
                    .getOrComputeStateInformation(VARIABLES_WRITTEN_KEY, AccessedVariablesInformation::new);

            // Determine value of the given variable
            AbstractedValue value = getVariableValue((SomeVariablesGlobalState) currentState.globalState(), localState,
                    (Variable<?, ?>) variable.get(), readVariables);

            // Calculate the value of the expression after the operation.
            AbstractedValue valueAfter = UnaryOperation.get(ue.getOperator()).apply(this.logic, value);

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
                    return this.logic.unknown(left);
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
                    return this.logic.unknown(left);
                }

                // add this reading & writing access to local and global state
                AccessedVariablesInformation readVariables = (AccessedVariablesInformation) localState
                        .getOrComputeStateInformation(VARIABLES_READ_KEY, AccessedVariablesInformation::new);
                AccessedVariablesInformation writtenVariables = (AccessedVariablesInformation) localState
                        .getOrComputeStateInformation(VARIABLES_WRITTEN_KEY, AccessedVariablesInformation::new);

                // Determine value of left side of the compound assignment.
                AbstractedValue leftValue = getVariableValue((SomeVariablesGlobalState) currentState.globalState(),
                        localState, (Variable<?, ?>) left.get(), readVariables);

                // Calculate value after the execution.
                AbstractedValue valueAfter =
                        BinaryOperation.get(be.getOp().split("=")[0]).apply(this.logic, leftValue, right);

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
                return this.logic.unknown(left, right);
            }

            // TODO: is that right?
            // TODO: always treat VariableExpression as variable if in access and only evaluate in aggregation?
            GlobalVariable<?, ?> gv = new GlobalVariable<>(left.get(), right.get());
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
                return this.logic.unknown(left);
            }

            AccessedVariablesInformation writtenVariables = (AccessedVariablesInformation) localState
                    .getOrComputeStateInformation(VARIABLES_WRITTEN_KEY, AccessedVariablesInformation::new);

            if (left.get() instanceof LocalVariable<?> lv) {
                writtenVariables.add(lv);
                ((VariableHolder<Variable<?, ?>>) localState).setVariableValue(lv,
                        this.interceptor.variables().writeLocalVariable(localState, lv, right));
            }

            if (!right.isDetermined()) {
                return this.logic.unknown(right);
            }

            return right;
        }

        if (expression instanceof RefDerefExpression re) {
            AbstractedValue targetValue = this.crawler.getValueOfChild(currentState, localState, 0);
            if (!targetValue.isDetermined()) {
                return this.logic.unknown(targetValue);
            }
            Object target = targetValue.get();
            if (!(target instanceof Variable<?, ?> targetVariable)) {
                throw new ClassCastException(
                        "Cannot (de-)reference a non-variable: " + target.getClass().getCanonicalName());
            }

            if (re.isReferencing() || !treatVariableAsValue(expression)) {
                return targetValue;
            } else {
                AccessedVariablesInformation readVariables = (AccessedVariablesInformation) localState
                        .getOrComputeStateInformation(VARIABLES_READ_KEY, AccessedVariablesInformation::new);
                return getVariableValue((SomeVariablesGlobalState) currentState.globalState(), localState,
                        targetVariable, readVariables);
            }
        }

        return this.logic.unknown();
    }

    /**
     * Called when a function has been called (i.e. entered), right before the SmallStepResult is
     * created. Not called when entering special case functions (wait, notify, request_update).
     * 
     * Used to adjust the resulting state, i.e. for setting parameter values.
     * <p>
     * For by-reference parameters ({@link SCParameter#getRefType()} is
     * {@link SCREFERENCETYPE#BYREFERENCE}), {@code value} is already the argument's underlying
     * {@link Variable} rather than its value (see {@link #treatVariableAsValue(Expression)} and
     * {@link #isByReferenceArgument(FunctionCallExpression, Expression)}), and is stored as is -
     * without going through the write interceptor - as the parameter's local slot value, so that
     * {@link #getVariableObject(TransitionResult, LocalState, Expression, Object, boolean)}
     * transparently resolves any later access of the parameter to this aliased target. This binding is
     * intentionally not recorded in {@link AccessedVariablesInformation}, as it is not a genuine,
     * user-visible variable access (see the class documentation).
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

            if (param.getRefType() == SCREFERENCETYPE.BYREFERENCE) {
                ((VariableHolder<Variable<?, ?>>) localState).setVariableValue(var, value);
            } else {
                ((VariableHolder<Variable<?, ?>>) localState).setVariableValue(var,
                        this.interceptor.variables().writeLocalVariable(localState, var, value));
                writtenVariables.add(var);
            }
            i++;
        }
    }


    /**
     * Called when a function has been returned from (i.e. left), right before the SmallStepResult is
     * created. Not called when leaving special case functions (wait, notify, request_update) or the top
     * level function.
     * 
     * Used to adjust the resulting state, i.e. for resetting parameter values.
     * <p>
     * For by-reference parameters, the alias established in {@link #functionCalled} is cleared directly
     * (without going through the write interceptor and without recording it in
     * {@link AccessedVariablesInformation}), mirroring how it was established.
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
            if (param.getRefType() == SCREFERENCETYPE.BYREFERENCE) {
                ((VariableHolder<Variable<?, ?>>) localState).setVariableValue(var, null);
            } else {
                ((VariableHolder<Variable<?, ?>>) localState).setVariableValue(var,
                        this.interceptor.variables().writeLocalVariable(localState, var, null));
            }
        }
    }

}
