package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import static de.tub.pes.syscir.analysis.util.WrapperUtil.wrap;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.DeltaTimeBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState.StateInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState.StateInformationKey;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessTransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.RealTimedBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration.ExplorationAbortedException;
import de.tub.pes.syscir.analysis.statespace_exploration.TimedBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.util.CollectionUtil;
import de.tub.pes.syscir.analysis.util.WrappedExpression;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCPortInstance;
import de.tub.pes.syscir.sc_model.SCConnectionInterface;
import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCPortInstance;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.AccessExpression;
import de.tub.pes.syscir.sc_model.expressions.ArrayAccessExpression;
import de.tub.pes.syscir.sc_model.expressions.ArrayInitializerExpression;
import de.tub.pes.syscir.sc_model.expressions.AssertionExpression;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.BracketExpression;
import de.tub.pes.syscir.sc_model.expressions.BreakExpression;
import de.tub.pes.syscir.sc_model.expressions.CaseExpression;
import de.tub.pes.syscir.sc_model.expressions.ConstantExpression;
import de.tub.pes.syscir.sc_model.expressions.ContinueExpression;
import de.tub.pes.syscir.sc_model.expressions.DeleteExpression;
import de.tub.pes.syscir.sc_model.expressions.DoWhileLoopExpression;
import de.tub.pes.syscir.sc_model.expressions.EmptyExpression;
import de.tub.pes.syscir.sc_model.expressions.EndlineExpression;
import de.tub.pes.syscir.sc_model.expressions.EnumElementExpression;
import de.tub.pes.syscir.sc_model.expressions.EventNotificationExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.ExpressionBlock;
import de.tub.pes.syscir.sc_model.expressions.ForLoopExpression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.IfElseExpression;
import de.tub.pes.syscir.sc_model.expressions.LoopExpression;
import de.tub.pes.syscir.sc_model.expressions.NameExpression;
import de.tub.pes.syscir.sc_model.expressions.NewExpression;
import de.tub.pes.syscir.sc_model.expressions.OutputExpression;
import de.tub.pes.syscir.sc_model.expressions.QuestionmarkExpression;
import de.tub.pes.syscir.sc_model.expressions.RefDerefExpression;
import de.tub.pes.syscir.sc_model.expressions.ReturnExpression;
import de.tub.pes.syscir.sc_model.expressions.SCClassInstanceExpression;
import de.tub.pes.syscir.sc_model.expressions.SCDeltaCountExpression;
import de.tub.pes.syscir.sc_model.expressions.SCPortSCSocketExpression;
import de.tub.pes.syscir.sc_model.expressions.SCStopExpression;
import de.tub.pes.syscir.sc_model.expressions.SCTimeStampExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableNonDetSet;
import de.tub.pes.syscir.sc_model.expressions.SwitchExpression;
import de.tub.pes.syscir.sc_model.expressions.TimeUnitExpression;
import de.tub.pes.syscir.sc_model.expressions.UnaryExpression;
import de.tub.pes.syscir.sc_model.expressions.WhileLoopExpression;
import de.tub.pes.syscir.sc_model.variables.SCTIMEUNIT;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.Function;

/**
 * Abstract class that captures the functionality of stepping through a program one expression at a
 * time.
 * 
 * This class serves as a basis for {@link BaseProcess} and {@link BaseScheduler} which both have to
 * evaluate code.
 * 
 * @author Jonas Becker-Kupczok
 */
public abstract class ExpressionCrawler {
    
    /**
     * Exception indicating that the current state information ins insufficiently precise for the
     * analysis to continue.
     *
     * @author Jonas Becker-Kupczok
     *
     */
    public static class InsufficientPrecisionException extends RuntimeException {
        
        private static final long serialVersionUID = -1470717330359116133L;
        
        public InsufficientPrecisionException() {
            super();
        }
        
        public InsufficientPrecisionException(String message, Throwable cause) {
            super(message, cause);
        }
        
        public InsufficientPrecisionException(String message) {
            super(message);
        }
        
        public InsufficientPrecisionException(Throwable cause) {
            super(cause);
        }
        
    }
    
    /**
     * Exception indicating that the exploration can not continue due to a value being unknown. This is
     * a more specific variant of {@link InsufficientPrecisionException}.
     * 
     * @author Lukas Ernst
     */
    public static class InsufficientValueTrackingException extends InsufficientPrecisionException {
        
        private static final long serialVersionUID = 1L;
        
        private final AbstractedValue value;
        
        public InsufficientValueTrackingException(AbstractedValue value) {
            super();
            this.value = value;
        }
        
        public InsufficientValueTrackingException(String msg, AbstractedValue value) {
            super(msg);
            this.value = value;
        }
        
        public AbstractedValue getValue() {
            return this.value;
        }
        
    }
    
    /**
     * Record capturing the intermediate result of one expression step.
     * 
     * The list of transition results captures the possible transitions that can be made, endOfStep
     * signals whether the analysis reaches the end of an atomic block, and possiblyRepeatingStep
     * indicates whether or not this intermediate result should be stored to make sure that it's not
     * dealt with again if it appears multiple times.
     * 
     * @author Jonas Becker-Kupczok
     */
    public static record SmallStepResult(List<TransitionResult> transitions, boolean endOfStep,
            boolean possiblyRepeatingStep) {}
    
    // TODO: this is not very elegant and not totally correct either (because the value of a variable
    // used in a condition can change later on). it's also not necessary for my (Jonas) research. is it
    // good for anything else? or should it go?
    public static class ExecutionConditions implements StateInformation {
        
        // one map per entry on call stack
        private List<SequencedMap<WrappedExpression, AbstractedValue>> conditions;
        
        public ExecutionConditions() {
            this.conditions = new ArrayList<>();
            addCall();
        }
        
        public ExecutionConditions(ExecutionConditions copyOf) {
            this.conditions = new ArrayList<>(copyOf.conditions.size());
            for (int i = 0; i < copyOf.conditions.size(); i++) {
                this.conditions.add(new LinkedHashMap<>(copyOf.conditions.get(i)));
            }
        }
        
        @Override
        public ExecutionConditions copy() {
            return new ExecutionConditions(this);
        }
        
        public void addCall() {
            this.conditions.add(new LinkedHashMap<>());
        }
        
        public void add(Expression expression, AbstractedLogic logic, AbstractedValue condition) {
            this.conditions.getLast().merge(wrap(expression), condition, logic::and);
        }
        
        // inclusive
        public void removeUntil(Expression expression) {
            WrappedExpression we = wrap(expression);
            WrappedExpression removed;
            do {
                // because all additional state information is cleared at the end of AnalyzedProcess#makeStep, the
                // conditions may contain less layers then expected by the expression crawler
                if (this.conditions.isEmpty() || this.conditions.getLast().isEmpty()) {
                    break;
                }
                removed = this.conditions.getLast().pollLastEntry().getKey();
            } while (!we.equals(removed));
        }
        
        public void removeCall() {
            if (!this.conditions.isEmpty()) {
                this.conditions.removeLast();
            }
            if (this.conditions.isEmpty()) {
                addCall();
            }
        }
        
        public List<AbstractedValue> getConditions() {
            return this.conditions.stream().flatMap(map -> map.values().stream()).toList();
        }
        
        @Override
        public String toString() {
            return this.conditions.toString();
        }
        
    }
    
    public static final StateInformationKey<ExecutionConditions> EXECUTION_CONDITION_KEY = new StateInformationKey<>();
    
    protected final SCSystem scSystem;
    protected final Scheduler scheduler;
    protected final AbstractedLogic logic;
    protected final Interceptor interceptor;
    
    /**
     * Creates a new ExpressionCralwer analyzing the given SystemC design and using the given scheduler.
     * <p>
     * If this implementation is itself a scheduler, the according parameter may be null.
     *
     * @param scSystem a SysCIR SystemC design
     * @param scheduler a scheduler, or null if this object is a scheduler
     * @param informationHandler an information handler providing {@link TransitionInformation}
     */
    public ExpressionCrawler(SCSystem scSystem, Scheduler scheduler, AbstractedLogic logic, Interceptor interceptor) {
        if (scheduler == null) {
            try {
                scheduler = (Scheduler) this;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(
                        "scheduler must be non-null if this is not a scheduler implementation");
            }
        }
        
        this.scSystem = scSystem;
        this.scheduler = scheduler;
        this.logic = logic;
        this.interceptor = interceptor;
    }
    
    /**
     * Returns the SysCIR SystemC design analyzed by this crawler.
     *
     * @return SysCIR SystemC design.
     */
    public SCSystem getSCSystem() {
        return this.scSystem;
    }
    
    /**
     * Returns the scheduler that is used in the state space exploration.
     *
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return this.scheduler;
    }
    
    public Interceptor getInterceptor() {
        return this.interceptor;
    }
    
    /**
     * Returns the information handler that provides {@link TransitionInformation} for this crawler.
     *
     * @return the information handler
     */
    public InformationHandler getInformationHandler() {
        return getInterceptor().informationHandler();
    }
    
    /**
     * Returns the information describing the small step that just occured.
     * 
     * Delegates the call to
     * {@link InformationHandler#handleExpressionEvaluation(Expression, int, TransitionResult, LocalState)}.
     * 
     * @param evaluated the expression that was just evaluated
     * @param comingFrom from where the expression was entered
     * @param resultingState the result of the small step
     * @param localState the local part of the result
     * @return the information describing the step
     */
    TransitionInformation getInformation(Expression evaluated, int comingFrom, TransitionResult resultingState,
            LocalState localState) {
        return getInformationHandler().handleExpressionEvaluation(evaluated, comingFrom, resultingState, localState);
    }
    
    /**
     * Creates a SmallStepResult from a list of TransitionResults by getting the new
     * TransitionInformation from the InformationHandler and composing it onto the result.
     *
     * @param evaluated the expression that was just evaluated
     * @param comingFrom from where the expression was entered
     * @param resultingStates the results of the small step
     * @param endOfStep whether the analysis reached the end of an atomic block
     * @param possiblyRepeatingStep whether or not this intermediate result should be stored to make
     *        sure that it's not dealt with again if it appears multiple times
     * @return the SmallStepResult, with appropriate TransitionInformation
     */
    public SmallStepResult createSmallStepResult(Expression evaluated, int comingFrom,
            List<TransitionResult> resultingStates, boolean endOfStep, boolean possiblyRepeatingStep) {
        resultingStates = new ArrayList<>(resultingStates);
        for (int i = 0; i < resultingStates.size(); i++) {
            TransitionResult resultingState = resultingStates.get(i);
            LocalState localState = getLocalState(resultingState);
            resultingStates.set(i, resultingState
                    .replaceTransitionInformation(getInformation(evaluated, comingFrom, resultingState, localState)));
        }
        return new SmallStepResult(resultingStates, endOfStep, possiblyRepeatingStep);
    }
    
    /**
     * Creates a SmallStepResult from a list of TransitionResults by getting the new
     * TransitionInformation from the InformationHandler and composing it onto the result.
     *
     * @param evaluated the expression that was just evaluated
     * @param comingFrom from where the expression was entered
     * @param resultingStates the result of the small step
     * @param localState the local part of the result
     * @param endOfStep whether the analysis reached the end of an atomic block
     * @param possiblyRepeatingStep whether or not this intermediate result should be stored to make
     *        sure that it's not dealt with again if it appears multiple times
     * @return the SmallStepResult, with appropriate TransitionInformation
     */
    public SmallStepResult createSmallStepResult(Expression evaluated, int comingFrom, TransitionResult resultingState,
            LocalState localState, boolean endOfStep, boolean possiblyRepeatingStep) {
        localState = localState == null ? getLocalState(resultingState) : localState;
        return new SmallStepResult(
                List.of(resultingState.replaceTransitionInformation(
                        getInformation(evaluated, comingFrom, resultingState, localState))),
                endOfStep, possiblyRepeatingStep);
    }
    
    public ProcessTransitionResult finalizeTransitionResult(ProcessTransitionResult result) {
        result = new ProcessTransitionResult(result.resultingState(),
                getInformationHandler().finalizeInformation(result.transitionInformation()));
        result.resultingState().getProcessStates().values().forEach(ProcessState::clearStateInformation);
        result.resultingState().lock();
        return result;
    }
    
    /**
     * Returns the local state covered by this crawler contained in the given state.
     *
     * @param currentState
     * @return local state
     */
    public abstract LocalState getLocalState(TransitionResult currentState);
    
    /**
     * Makes a small step in the evaluation, i.e. one step in the abstract syntax tree where every
     * expression is an individual node.
     * 
     * This implementation first finds the current expression to be evaluated. It then calls
     * {@link #handleSpecialExpression(ProcessTransitionResult, Expression, int)} to allow subclasses to
     * interject, otherwise calling a function to handle the specific type of expression found.
     * <p>
     * A special case is if the evaluation currently resides directly in a function body. In this case,
     * {@link #handleFunctionBody(ProcessTransitionResult, int)} is called (without a call to
     * {@link #handleSpecialExpression(ProcessTransitionResult, Expression, int)}.
     * <p>
     * If evaluation of the expression only allows for one follow up state, the current state is usually
     * modified instead of cloned for efficiency. Otherwise, clones are created and then modified for
     * every additional possible transition.
     * <p>
     * If the evaluation reaches the end of an atomic block (usually by encountering a wait statement or
     * the end of the top level function), {@link SmallStepResult#endOfStep()} will be true, otherwise
     * it will be false.
     *
     * @param currentState the current state from which to make the step
     * @return the possible transitions and whether or not the end of an atomic block has been reached
     */
    public SmallStepResult makeSmallStep(TransitionResult currentState) {
        StateSpaceExploration explorer = StateSpaceExploration.getCurrentExplorer();
        if (explorer.isAborted()) {
            throw new ExplorationAbortedException();
        }
        
        LocalState localState = getLocalState(currentState);
        EvaluationContext currentLocation =
                localState.getExecutionStack().get(localState.getExecutionStack().size() - 1);
        int comingFrom = currentLocation.getComingFrom();
        
        if (currentLocation.getExpressionIndices().isEmpty()) {
            return handleFunctionBody(currentState, localState, comingFrom);
        }
        
        Expression nextExpression = currentLocation.getNextExpression();
        
        getInterceptor().evaluation().prepareEvaluation(nextExpression);
        
        getInformationHandler().announceEvaluation(nextExpression, currentState, localState);
        
        SmallStepResult result = handleSpecialExpression(currentState, localState, nextExpression, comingFrom);
        if (result != null) {
            return result;
        }
        
        // End of function body
        if (nextExpression == null) {
            return returnFromFunction(currentState, localState, null, comingFrom);
        }
        
        // Atomic Expressions
        if (nextExpression instanceof ConstantExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom, c -> parseConstant(c.getValue()));
        } else if (nextExpression instanceof EndlineExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom, _ -> "\n");
        } else if (nextExpression instanceof EnumElementExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom,
                    EnumElementExpression::getEnumElement);
        } else if (nextExpression instanceof SCClassInstanceExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom, e -> wrap(e.getInstance()));
        } else if (nextExpression instanceof SCDeltaCountExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom, _ -> DeltaTimeBlocker.INSTANCE);
        } else if (nextExpression instanceof SCPortSCSocketExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom,
                    SCPortSCSocketExpression::getSCPortSCSocket);
        } else if (nextExpression instanceof SCVariableExpression ex && !(ex instanceof ArrayAccessExpression)) {
            return handleConstantExpression(currentState, localState, ex, comingFrom, SCVariableExpression::getVar);
        } else if (nextExpression instanceof TimeUnitExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom, TimeUnitExpression::getTimeUnit);
        }
        
        // Ignored expressions
        if (nextExpression instanceof AssertionExpression ex) {
            return handleIgnoredExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof EmptyExpression ex) {
            return handleIgnoredExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof NameExpression ex) {
            return handleIgnoredExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof SCTimeStampExpression ex) {
            return handleIgnoredExpression(currentState, localState, ex, comingFrom);
        }
        
        // Bottom-up evaluated expressions
        if (nextExpression instanceof AccessExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof ArrayAccessExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof ArrayInitializerExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof BinaryExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof BracketExpression ex) {
            return handleBracketExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof DeleteExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof ExpressionBlock ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof NewExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof OutputExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof RefDerefExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof SCVariableDeclarationExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof SCVariableNonDetSet ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof UnaryExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        }
        
        // Control structures
        if (nextExpression instanceof IfElseExpression ex) {
            return handleIfElseExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof WhileLoopExpression ex) {
            return handleWhileLoopExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof DoWhileLoopExpression ex) {
            return handleWhileLoopExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof ForLoopExpression ex) {
            return handleForLoopExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof SwitchExpression ex) {
            return handleSwitchExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof CaseExpression ex) {
            return handleCaseExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof BreakExpression ex) {
            return handleBreakExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof ContinueExpression ex) {
            return handleContinueExpression(currentState, localState, ex, comingFrom);
        }
        // TODO GoalAnnotation, continue, goto
        
        // Function calls
        if (nextExpression instanceof FunctionCallExpression ex) {
            return handleFunctionCallExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof ReturnExpression ex) {
            return handleReturnExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof EventNotificationExpression ex) {
            return handleEventNotificationExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof SCStopExpression ex) {
            return handleSCStopExpression(currentState, localState, ex, comingFrom);
        }
        
        // Other types of expressions
        return handleOtherExpression(currentState, localState, nextExpression, comingFrom);
        
        // TODO how to deal with SocketFunctionCallExpression? simply "not supported" for now?
    }
    
    /**
     * Returns the next expression to be executed given the execution stack.
     *
     * @param executionStack an execution stack on this process
     * @return the next expression of this process to be executed
     */
    public Expression getNextExpression(List<EvaluationContext> executionStack) {
        return executionStack.getLast().getNextExpression();
    }
    
    /**
     * Returns the abstracted evaluation result of the child of the currently evaluated expression at
     * the given index.
     * 
     * @param currentState the current state of the evaluation
     * @param indexOfChild the index of the child whose value is requested
     * @return the evaluation result of that child
     */
    public AbstractedValue getValueOfChild(TransitionResult currentState, LocalState localState, int indexOfChild) {
        return getValueOfExpression(currentState, localState, 0, indexOfChild);
    }
    
    /**
     * Returns the abstracted evaluation result of a child of some expression that has not yet been
     * fully evaluated.
     * 
     * @param currentState the current state of the evaluation
     * @param levelsAbove how many levels the considered parent expression is above the currently
     *        evaluated expression in the evaluation tree (where 0 yields the currently evaluated
     *        expression)
     * @param indexOfChild the index of the child whose value is requested
     * @return the evaluation result of that child
     */
    public AbstractedValue getValueOfExpression(TransitionResult currentState, LocalState localState, int levelsAbove,
            int indexOfChild) {
        EvaluationContext ec = localState.getExecutionStack().getLast();
        return ec.getExpressionValue(levelsAbove, indexOfChild);
    }
    
    /**
     * Returns whetehr or not the given expression cares about the (abstracted) evaluation result of its
     * child with the given index.
     * 
     * This information is used to discard evaluation results that have no effect on future evaluations
     * and would therefore unnecessarily enlarge the state space.
     *
     * @param expression an expression
     * @param indexOfChild the index of one of its children
     * @return whether or not the expression cares about the evaluation result of that child
     */
    public boolean caresAboutValue(Expression expression, int indexOfChild) {
        if (expression == null) {
            return false;
        }
        if (expression instanceof WhileLoopExpression _) {
            return indexOfChild == 0;
        }
        if (expression instanceof DoWhileLoopExpression _) {
            return indexOfChild == 0;
        }
        if (expression instanceof ForLoopExpression _) {
            return indexOfChild == 1;
        }
        if (expression instanceof IfElseExpression ex) {
            return indexOfChild == 0 || ex instanceof QuestionmarkExpression;
        }
        if (expression instanceof CaseExpression ex) {
            return indexOfChild == 0 && !ex.isDefaultCase();
        }
        
        return true;
    }
    
    public static class NullConstant {
        
        public static final NullConstant INSTANCE = new NullConstant();
        
        private NullConstant() {}
        
        @Override
        public String toString() {
            return "NULL";
        }
    }
    
    /**
     * Parses the specified textual C constant to a representative Java object.
     * 
     * @throws IllegalArgumentException if the type of the constant is unsupported
     */
    public static Object parseConstant(String constant) {
        if (constant.equals("NULL")) {
            return NullConstant.INSTANCE;
        }
        if (constant.equals("true")) {
            return true;
        }
        if (constant.equals("false")) {
            return false;
        }
        if (constant.startsWith("\"") && constant.endsWith("\"")) {
            return CString.deescape(constant.substring(1, constant.length() - 1), StandardCharsets.UTF_8);
        }
        try {
            return Integer.parseInt(constant);
        } catch (NumberFormatException e) {
        }
        try {
            return Double.parseDouble(constant);
        } catch (NumberFormatException e) {
        }
        throw new IllegalArgumentException("No supported type of constant for '" + constant + "'");
    }
    
    /**
     * Returns the port instance corresponding to a port variable.
     *
     * @param port a port variable
     * @return the port instance corresponding to that variable
     */
    public WrappedSCPortInstance getPortInstance(SCPort port) {
        WrappedSCPortInstance result = null;
        for (SCConnectionInterface con : getSCSystem().getPortSocketInstances()) {
            SCPortInstance instance = (SCPortInstance) con;
            if (port != instance.getPortSocket()) {
                continue;
            }
            if (result != null) {
                throw new InsufficientPrecisionException("SCPort is not unique");
            }
            result = wrap(instance);
        }
        return result;
    }
    
    /**
     * Returns the channel instance corresponding to a port instance.
     * 
     * @param portInstance a port instance
     * @return the channel instance corresponding to that port instance
     */
    public WrappedSCClassInstance getChannel(WrappedSCPortInstance portInstance) {
        return wrap(portInstance.getChannels().get(0));
    }
    
    /**
     * Returns the channel instance corresponding to a port variable.
     *
     * @param port a port variable
     * @return the channel instance corresponding to that variable
     */
    public WrappedSCClassInstance getChannel(SCPort port) {
        return getChannel(getPortInstance(port));
    }
    
    public ExecutionConditions getExecutionConditions(LocalState localState) {
        ExecutionConditions result = localState.getStateInformation(EXECUTION_CONDITION_KEY);
        if (result == null) {
            result = new ExecutionConditions();
            localState.setStateInformation(EXECUTION_CONDITION_KEY, result);
        }
        return result;
    }
    
    /**
     * Called by {@link #makeSmallStep(TransitionResult)} to allow subclasses to intervene in the
     * handling of certain expressions.
     * 
     * Subclasses can either return a {@link SmallStepResult} to overwrite the evaluation of
     * {@link #makeSmallStep(TransitionResult)} or null for {@link #makeSmallStep(TransitionResult)} to
     * continue normally.
     * <p>
     * Note that if subclasses intervene, they still have to call
     * {@link #enterChildExpression(LocalState, int, int)} or
     * {@link #returnToParent(LocalState, AbstractedValue)} respectively or handle the evaluation
     * location and expression values themselves.
     * 
     * @param currentState the current state of the evaluation
     * @param expression the next expression to be evaluated
     * @param comingFrom from where the expression is reached (-1 if from its parent, or the index of
     *        the child which has last been evaluated)
     * @return the result overwriting the usual logic of {@link #makeSmallStep(TransitionResult)}, or
     *         null if no special treatment is given
     */
    public SmallStepResult handleSpecialExpression(TransitionResult currentState, LocalState localState,
            Expression expression, int comingFrom) {
        return null;
    }
    
    /**
     * Called by {@link #makeSmallStep(TransitionResult)} to handle any kind of expression with constant
     * value.
     * 
     * This implementations simply stores the value in the expression values and returns to the parent.
     *
     * @param <X> the type of constant expression
     * @param currentState the current state of the evaluation
     * @param expression the expression to be evaluated
     * @param comingFrom from where the expression is reached (should always be -1 for constant
     *        expressions as they don't have children)
     * @param valueGetter a function for retrieving the constant value of the expression
     * @return the possible transitions and whether or not the end of an atomic block has been reached
     */
    public <ExpressionT extends Expression> SmallStepResult handleConstantExpression(TransitionResult currentState,
            LocalState localState, ExpressionT expression, int comingFrom, Function<ExpressionT, Object> valueGetter) {
        assert comingFrom == -1;
        
        returnToParent(expression, localState, this.logic.value(valueGetter.apply(expression)));
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }
    
    /**
     * Called by {@link #makeSmallStep(TransitionResult)} to handle expressions that are ignored by this
     * implementation.
     * 
     * @param currentState the current state of the evaluation
     * @param expression the expression to be evaluated
     * @param comingFrom from where the expression is reached
     * @return the possible transitions and whether or not the end of an atomic block has been reached
     */
    public SmallStepResult handleIgnoredExpression(TransitionResult currentState, LocalState localState,
            Expression expression, int comingFrom) {
        returnToParent(expression, localState, this.logic.unknown());
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }
    
    /**
     * Called by {@link #makeSmallStep(TransitionResult)} to handle expressions which are evaluated
     * bottom up, i.e. first their children are evaluated and then their resulting values are aggregated
     * in some way by calling {@link #aggregateExpressionValue(TransitionResult, Expression)}.
     * 
     * @param currentState the current state of the evaluation
     * @param expression the expression to be evaluated
     * @param comingFrom from where the expression is reached (-1 if from its parent, or the index of
     *        the child which has last been evaluated)
     * @return the possible transitions and whether or not the end of an atomic block has been reached
     */
    public SmallStepResult handleBottomUpExpression(TransitionResult currentState, LocalState localState,
            Expression expression, int comingFrom) {
        int numOfChildren = expression.getNumOfChildren();
        
        // coming from the last child
        if (comingFrom == numOfChildren - 1) {
            // handle event lists (e1 & e2 or e1 | e2) separately
            if (expression instanceof BinaryExpression be && (be.getOp().equals("&") || be.getOp().equals("|"))) {
                AbstractedValue left = getValueOfChild(currentState, localState, 0);
                AbstractedValue right = getValueOfChild(currentState, localState, 1);
                if (left.isDetermined() && right.isDetermined()) {
                    Object leftValue = left.get();
                    Object rightValue = right.get();
                    if ((leftValue instanceof Event || leftValue instanceof EventBlocker)
                            && rightValue instanceof Event re) {
                        EventBlocker result;
                        if (leftValue instanceof Event le) {
                            result = new EventBlocker(Set.of(le, re), be.getOp().equals("|"), null);
                        } else {
                            EventBlocker current = (EventBlocker) leftValue;
                            Set<Event> newEvents = new LinkedHashSet<>(current.getEvents());
                            newEvents.add(re);
                            result = current.replaceEvents(newEvents);
                            
                            assert current.isChoice() == be.getOp().equals("|");
                        }
                        returnToParent(expression, localState, this.logic.value(result));
                        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
                    }
                }
            }
            
            AbstractedValue expressionValue = aggregateExpressionValue(currentState, localState, expression);
            returnToParent(expression, localState, expressionValue);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        // stop execution early (e.g. in case of short circuit evaluation)?
        AbstractedValue expressionValue = stopEvaluationEarly(currentState, localState, expression, comingFrom);
        if (expressionValue != null) {
            returnToParent(expression, localState, expressionValue);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        // go into the next child
        enterChildExpression(localState, comingFrom + 1);
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }
    
    /**
     * Called after all children of an expression have been evaluated to aggregate their results
     * according to the state abstraction chosen for the analysis.
     * 
     * @param currentState the current state of the evaluation
     * @param localState the local portion of the state
     * @param expression the expression to be evaluated
     * @return an abstraction of the value of the expression (may not be null, but may be non-determined
     *         or an abstraction of null)
     */
    public abstract AbstractedValue aggregateExpressionValue(TransitionResult currentState, LocalState localState,
            Expression expression);
    
    /**
     * Called by {@link #handleBottomUpExpression(TransitionResult, Expression, int)} every time the
     * evaluation reaches an expression before its last child has been evaluated to determine whether or
     * not the evaluation should be stopped early, and if so, with which result.
     * 
     * A return value of null indicates that the evaluation shall continue.
     * 
     * @param currentState the current state of the evaluation
     * @param expression the expression currently evaluated
     * @param comingFrom the index of the last child that was evaluated
     * @return an abstraction of the value of the expression if the evaluation shall stop early, or null
     *         otherwise
     */
    public AbstractedValue stopEvaluationEarly(TransitionResult currentState, LocalState localState,
            Expression expression, int comingFrom) {
        return null;
    }
    
    public SmallStepResult handleBracketExpression(TransitionResult currentState, LocalState localState,
            BracketExpression expression, int comingFrom) {
        // entering from parent
        if (comingFrom == -1) {
            // go into condition
            enterChildExpression(localState, 0);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        returnToParent(expression, localState, getValueOfChild(currentState, localState, 0));
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }
    
    public SmallStepResult handleIfElseExpression(TransitionResult currentState, LocalState localState,
            IfElseExpression expression, int comingFrom) {
        // entering from parent
        if (comingFrom == -1) {
            // go into condition
            enterChildExpression(localState, 0);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        // returning from condition
        if (comingFrom == 0) {
            AbstractedValue conditionResult = getValueOfChild(currentState, localState, 0);
            
            if (!conditionResult.isDetermined()) {
                // go into both then and else block
                TransitionResult copyForThenCase = currentState.clone();
                LocalState localStateForThenCase = getLocalState(copyForThenCase);
                ExecutionConditions executionConditionsForThenCase = getExecutionConditions(localStateForThenCase);
                executionConditionsForThenCase.add(expression, this.logic, conditionResult);
                enterChildExpression(localStateForThenCase, 1);
                
                if (expression.getElseBlock().isEmpty()) {
                    returnToParent(expression, localState);
                } else {
                    ExecutionConditions executionConditionsForElseCase = getExecutionConditions(localState);
                    executionConditionsForElseCase.add(expression, this.logic, this.logic.not(conditionResult));
                    enterChildExpression(localState, expression.getThenBlock().size() + 1);
                }
                return createSmallStepResult(expression, comingFrom, List.of(copyForThenCase, currentState), false,
                        false);
            } else if ((boolean) conditionResult.get()) {
                // go into then block
                
                // add a condition result even though it is "true" because we always remove a condition upon leaving
                // the statement
                ExecutionConditions executionConditions = getExecutionConditions(localState);
                executionConditions.add(expression, this.logic, conditionResult);
                enterChildExpression(localState, 1);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            } else {
                if (expression.getElseBlock().isEmpty()) {
                    returnToParent(expression, localState);
                } else {
                    // go into else block
                    
                    // add a condition result even though it is "true" because we always remove a condition upon leaving
                    // the statement
                    ExecutionConditions executionConditions = getExecutionConditions(localState);
                    executionConditions.add(expression, this.logic, this.logic.not(conditionResult));
                    enterChildExpression(localState, expression.getThenBlock().size() + 1);
                }
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            }
        }
        
        int lengthOfThenBlock = expression.getThenBlock().size();
        
        // returning from then block
        if (comingFrom <= lengthOfThenBlock) {
            // returning from end of then block
            if (comingFrom >= lengthOfThenBlock) {
                // leave if-else-block
                ExecutionConditions executionConditions = getExecutionConditions(localState);
                executionConditions.removeUntil(expression);
                if (expression instanceof QuestionmarkExpression) {
                    assert lengthOfThenBlock == 1;
                    returnToParent(expression, localState, getValueOfChild(currentState, localState, 1));
                } else {
                    returnToParent(expression, localState);
                }
            }
            // returning from within then block
            else {
                // go to next expression in then block
                enterChildExpression(localState, comingFrom + 1);
            }
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        // returning from else block
        int lengthOfElseBlock = expression.getElseBlock().size();
        // returning from end of else block
        if (comingFrom >= lengthOfThenBlock + lengthOfElseBlock) {
            // leave if-else-block
            ExecutionConditions executionConditions = getExecutionConditions(localState);
            executionConditions.removeUntil(expression);
            if (expression instanceof QuestionmarkExpression) {
                assert lengthOfThenBlock == 1 && lengthOfElseBlock == 1;
                returnToParent(expression, localState, getValueOfChild(currentState, localState, 2));
            } else {
                returnToParent(expression, localState);
            }
            // can't repeat, but might improve performance to check whether taking one path or the other makes
            // any difference
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, true);
        }
        // returning form within else block
        else {
            // go to next expression in then block
            enterChildExpression(localState, comingFrom + 1);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
    }
    
    public SmallStepResult handleWhileLoopExpression(TransitionResult currentState, LocalState localState,
            LoopExpression expression, int comingFrom) {
        // entering from parent
        if (comingFrom == -1) {
            // add true condition so that removing one when breaking is always correct
            getExecutionConditions(localState).add(expression, this.logic, this.logic.value(true));
            
            if (expression instanceof WhileLoopExpression) {
                // go into condition
                enterChildExpression(localState, 0);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, true); // condition
                // check
                // might
                // repeat
            } else if (expression instanceof DoWhileLoopExpression) {
                // go into body
                enterChildExpression(localState, 1);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            }
        }
        
        // returning from condition
        if (comingFrom == 0) {
            AbstractedValue conditionResult = getValueOfChild(currentState, localState, 0);
            
            if (!conditionResult.isDetermined()) {
                // go into body and leave loop
                TransitionResult copyForLoopCase = currentState.clone();
                LocalState localStateForLoopCase = getLocalState(copyForLoopCase);
                ExecutionConditions executionConditionsForLoopCase = getExecutionConditions(localStateForLoopCase);
                executionConditionsForLoopCase.add(expression, this.logic, conditionResult);
                enterChildExpression(getLocalState(copyForLoopCase), 1);
                
                ExecutionConditions executionConditionsForBreakCase = getExecutionConditions(localState);
                executionConditionsForBreakCase.removeUntil(expression);
                // TODO: add negative of condition instead?
                returnToParent(expression, localState);
                return createSmallStepResult(expression, comingFrom, List.of(copyForLoopCase, currentState), false,
                        false);
            } else if ((boolean) conditionResult.get()) {
                // go into body
                
                // don't add execution condition because the condition result is determined, meaning the current
                // condition is still sufficient
                enterChildExpression(localState, 1);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            } else {
                // leave loop
                ExecutionConditions executionConditions = getExecutionConditions(localState);
                executionConditions.removeUntil(expression);
                returnToParent(expression, localState);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            }
        }
        
        // returning from loop body
        int lengthOfBody = expression.getLoopBody().size();
        
        // returning from end of loop body
        if (comingFrom >= lengthOfBody) {
            // go into condition
            enterChildExpression(localState, 0);
            // condition check might repeat
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, true);
        }
        // returning form within loop body
        else {
            // goto next expression of body
            enterChildExpression(localState, comingFrom + 1);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
    }
    
    public SmallStepResult handleForLoopExpression(TransitionResult currentState, LocalState localState,
            ForLoopExpression expression, int comingFrom) {
        // entering from parent
        if (comingFrom == -1) {
            // add true condition so that removing one when breaking is always correct
            getExecutionConditions(localState).add(expression, this.logic, this.logic.value(true));
            
            // go into initializer
            enterChildExpression(localState, 0);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        // returning from initializer
        if (comingFrom == 0) {
            // go into condition
            enterChildExpression(localState, 1);
            // condition check might repeat
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, true);
        }
        
        // returning from condition
        if (comingFrom == 1) {
            AbstractedValue conditionResult = getValueOfChild(currentState, localState, 1);
            
            if (!conditionResult.isDetermined()) {
                // go into body and leave loop
                TransitionResult copyForLoopCase = currentState.clone();
                LocalState localStateForLoopCase = getLocalState(copyForLoopCase);
                ExecutionConditions executionConditionsForLoopCase = getExecutionConditions(localStateForLoopCase);
                executionConditionsForLoopCase.add(expression, this.logic, conditionResult);
                enterChildExpression(getLocalState(copyForLoopCase), 2);
                
                ExecutionConditions executionConditionsForBreakCase = getExecutionConditions(localState);
                executionConditionsForBreakCase.removeUntil(expression);
                returnToParent(expression, localState);
                return createSmallStepResult(expression, comingFrom, List.of(copyForLoopCase, currentState), false,
                        false);
            } else if ((boolean) conditionResult.get()) {
                // go into body
                
                // don't add execution condition because the condition result is determined, meaning the current
                // condition is still sufficient
                enterChildExpression(localState, 2);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            } else {
                // leave loop
                
                ExecutionConditions executionConditions = getExecutionConditions(localState);
                executionConditions.removeUntil(expression);
                returnToParent(expression, localState);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            }
        }
        
        int lengthOfBody = expression.getLoopBody().size();
        
        // returning from iterator
        if (comingFrom == lengthOfBody + 2) {
            // go into condition
            enterChildExpression(localState, 1);
            // condition check might repeat
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, true);
        }
        
        // returning from loop body
        
        // returning from end of loop body
        if (comingFrom == lengthOfBody + 1) {
            // go into iterator
            enterChildExpression(localState, lengthOfBody + 2);
        }
        // returning form within loop body
        else {
            // goto next expression of body
            enterChildExpression(localState, comingFrom + 1);
        }
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }
    
    public SmallStepResult handleSwitchExpression(TransitionResult currentState, LocalState localState,
            SwitchExpression expression, int comingFrom) {
        // coming from parent
        if (comingFrom == -1) {
            // add true condition so that removing one when leaving is always correct
            getExecutionConditions(localState).add(expression, this.logic, this.logic.value(true));
            
            // go into expression to switch on
            enterChildExpression(localState, 0);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        // returning from expression to switch on
        if (comingFrom == 0) {
            // go into first case
            enterChildExpression(localState, 1);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        // returning from the last case
        if (comingFrom == expression.getNumOfChildren() - 1) {
            ExecutionConditions executionConditions = getExecutionConditions(localState);
            executionConditions.removeUntil(expression);
            
            returnToParent(expression, localState);
            // can't repeat, but might improve performance to check whether taking one path or the other makes
            // any difference
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, true);
        }
        
        // returning from some case other than the last: enter the next one
        enterChildExpression(localState, comingFrom + 1);
        // can't repeat, but might improve performance to check whether taking one path or the other makes
        // any difference
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, true);
    }
    
    public SmallStepResult handleCaseExpression(TransitionResult currentState, LocalState localState,
            CaseExpression expression, int comingFrom) {
        // TODO: use expression.getParent() instead of expression when adding execution conditions=
        
        // coming from parent
        if (comingFrom == -1) {
            // checking case or falling through?
            EvaluationContext el = localState.getTopOfStack();
            int myIndex = el.getExpressionIndices().getLast();
            boolean fallingThrough;
            if (myIndex <= 1) {
                fallingThrough = false;
            } else {
                fallingThrough = (boolean) getValueOfExpression(currentState, localState, 1, myIndex - 1).get();
            }
            
            if (fallingThrough) {
                // go into body directly, if body exists
                
                // don't add to execution condition because the one added when entering the matching case is still
                // sufficient.
                if (expression.getBody().isEmpty()) {
                    returnToParent(expression, localState, this.logic.value(true));
                } else {
                    enterChildExpression(localState, expression.isDefaultCase() ? 0 : 1);
                }
            } else {
                // does case apply?
                if (expression.isDefaultCase()) {
                    if (expression.getBody().isEmpty()) {
                        returnToParent(expression, localState, this.logic.value(true));
                    } else {
                        // yes, enter body
                        enterChildExpression(localState, 0);
                    }
                } else {
                    // evaluate case value
                    enterChildExpression(localState, 0);
                }
            }
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        // returning from case value evaluation
        if (comingFrom == 0 && !expression.isDefaultCase()) {
            AbstractedValue switchValue = getValueOfExpression(currentState, localState, 1, 0);
            AbstractedValue caseValue = getValueOfChild(currentState, localState, 0);
            AbstractedValue equality = this.logic.equal(switchValue, caseValue);
            
            if (!equality.isDetermined()) {
                // go into case and step over case
                TransitionResult copyForIntoCase = currentState.clone();
                LocalState localStateForIntoCase = getLocalState(copyForIntoCase);
                ExecutionConditions executionConditionsForIntoCase = getExecutionConditions(localStateForIntoCase);
                executionConditionsForIntoCase.add(expression, this.logic, equality);
                
                if (expression.getBody().isEmpty()) {
                    returnToParent(expression, getLocalState(copyForIntoCase), this.logic.value(true));
                } else {
                    enterChildExpression(getLocalState(copyForIntoCase), 1);
                }
                
                ExecutionConditions executionConditionsForSkipCase = getExecutionConditions(localState);
                executionConditionsForSkipCase.add(expression, this.logic, this.logic.not(equality));
                returnToParent(expression, localState, this.logic.value(false));
                return createSmallStepResult(expression, comingFrom, List.of(copyForIntoCase, currentState), false,
                        false);
            } else if ((boolean) equality.get()) {
                // go into case
                if (expression.getBody().isEmpty()) {
                    returnToParent(expression, localState, this.logic.value(true));
                } else {
                    enterChildExpression(localState, 1);
                }
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            } else {
                // step over case
                returnToParent(expression, localState, this.logic.value(false));
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            }
        }
        
        // returning from last body expression
        if (comingFrom == expression.getNumOfChildren() - 1) {
            returnToParent(expression, localState, this.logic.value(true));
        }
        // returning from other body expression
        else {
            // go to next
            enterChildExpression(localState, comingFrom + 1);
        }
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }
    
    public SmallStepResult handleBreakExpression(TransitionResult currentState, LocalState localState,
            BreakExpression expression, int comingFrom) {
        assert comingFrom == -1;
        
        String label = expression.getLabel();
        EvaluationContext ec = localState.getTopOfStack();
        List<Integer> indices = ec.getExpressionIndices();
        
        Expression targetted;
        int levelsAbove = 1;
        for (;; levelsAbove++) {
            targetted = ec.getNextExpression(levelsAbove);
            if (isTargetted(targetted, label, true)) {
                break;
            }
        }
        
        getExecutionConditions(localState).removeUntil(targetted);
        
        int removedIndex = 0;
        List<List<AbstractedValue>> expressionValues = ec.getExpressionValues();
        for (int i = 0; i <= levelsAbove; i++) {
            removedIndex = indices.remove(indices.size() - 1);
            expressionValues.remove(expressionValues.size() - 1);
        }
        ec.setComingFrom(removedIndex);
        
        List<AbstractedValue> parentValues = ec.getExpressionValues().getLast();
        CollectionUtil.addOrSet(parentValues, ec.getComingFrom(), null);
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }
    
    public SmallStepResult handleContinueExpression(TransitionResult currentState, LocalState localState,
            ContinueExpression expression, int comingFrom) {
        assert comingFrom == -1;
        
        String label = expression.getLabel();
        EvaluationContext ec = localState.getTopOfStack();
        List<Integer> indices = ec.getExpressionIndices();
        
        Expression childOfTargetted;
        Expression targetted = null;
        int levelsAbove = 1;
        for (;; levelsAbove++) {
            childOfTargetted = targetted;
            targetted = ec.getNextExpression(levelsAbove);
            if (isTargetted(targetted, label, true)) {
                break;
            }
        }
        
        getExecutionConditions(localState).removeUntil(childOfTargetted);
        
        List<List<AbstractedValue>> expressionValues = ec.getExpressionValues();
        for (int i = 0; i < levelsAbove; i++) {
            indices.remove(indices.size() - 1);
            expressionValues.remove(expressionValues.size() - 1);
        }
        if (targetted instanceof ForLoopExpression fe) {
            // continue with iterator
            ec.setComingFrom(fe.getLoopBody().size() + 1);
        } else {
            ec.setComingFrom(targetted.getNumOfChildren() - 1);
        }
        
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }
    
    /**
     * Returns whether or not an expression is the target of a break or continue statement.
     * 
     * Candidates must be checked from the inside out, as this method may falsely return true for
     * candidates that enclose the actually targetted expression.
     *
     * @param candidate the expression that might be targetted
     * @param label the label of the break or continue statement
     * @param breakStatement whether the targetting statement is a break statement
     * @return whether the candidate is targetted
     */
    public boolean isTargetted(Expression candidate, String label, boolean breakStatement) {
        if (!(candidate instanceof LoopExpression) && !(breakStatement && candidate instanceof SwitchExpression)) {
            return false;
        }
        return label.isEmpty() || label.equals(candidate.getLabel());
    }
    
    public SmallStepResult handleEventNotificationExpression(TransitionResult currentState, LocalState localState,
            EventNotificationExpression expression, int comingFrom) {
        // entering from parent
        if (comingFrom == -1) {
            enterChildExpression(localState, 0);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        int numOfParameters = expression.getParameters().size();
        
        // returning from the evaluation of a parameter (not the last one)
        if (comingFrom < numOfParameters) {
            enterChildExpression(localState, comingFrom + 1);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        // returning from the last parameter
        
        AbstractedValue eventObject = getValueOfChild(currentState, localState, 0);
        if (!eventObject.isDetermined()) {
            throw new InsufficientValueTrackingException(expression.toString(), eventObject);
        }
        
        Event event = (Event) eventObject.get();
        
        TimedBlocker delay;
        
        if (numOfParameters == 0) {
            // immediate notification
            delay = null;
        } else {
            AbstractedValue firstParam = getValueOfChild(currentState, localState, 1);
            if (!firstParam.isDetermined()) {
                throw new InsufficientValueTrackingException(firstParam);
            }
            
            if (numOfParameters == 1) {
                if (firstParam.get() instanceof TimedBlocker tb) {
                    delay = tb;
                } else if (firstParam.get() instanceof SCTIMEUNIT tu) {
                    // delta delayed notification
                    if (tu != SCTIMEUNIT.SC_ZERO_TIME) {
                        throw new UnsupportedOperationException(
                                "notification with just one parameter which is an SCTIMEUNIT must be delta");
                    }
                    delay = DeltaTimeBlocker.INSTANCE;
                } else {
                    throw new ClassCastException();
                }
            } else {
                AbstractedValue secondParam = getValueOfChild(currentState, localState, 2);
                if (!secondParam.isDetermined()) {
                    throw new InsufficientValueTrackingException(secondParam);
                }
                
                // delta delayed or timed notification
                assert numOfParameters == 2;
                
                int amount = (int) firstParam.get();
                SCTIMEUNIT unit = (SCTIMEUNIT) secondParam.get();
                delay = amount == 0 ? DeltaTimeBlocker.INSTANCE : new RealTimedBlocker(amount, unit);
            }
        }
        
        List<TransitionResult> transitions =
                CollectionUtil.asList(this.scheduler.notifyEvents(currentState, event, delay));
        for (TransitionResult transition : transitions) {
            returnToParent(expression, getLocalState(transition));
        }
        return createSmallStepResult(expression, comingFrom, transitions, false, false);
    }
    
    public SmallStepResult handleSCStopExpression(TransitionResult currentState, LocalState localState,
            SCStopExpression expression, int comingFrom) {
        returnToParent(expression, localState);
        Collection<TransitionResult> transitions = getScheduler().stopSimulation(currentState);
        return createSmallStepResult(expression, comingFrom, new ArrayList<>(transitions), false, false);
    }
    
    public SmallStepResult handleFunctionCallExpression(TransitionResult currentState, LocalState localState,
            FunctionCallExpression expression, int comingFrom) {
        int numOfParameters = expression.getParameters().size();
        
        // returning from the evaluation of a parameter (not the last one)
        if (comingFrom < numOfParameters - 1) {
            enterChildExpression(localState, comingFrom + 1);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        // returning from function call
        if (comingFrom >= numOfParameters) {
            returnToParent(expression, localState, getValueOfChild(currentState, localState, numOfParameters));
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        SmallStepResult result = handleSpecialCaseFunctionCall(currentState, localState, expression, comingFrom);
        if (result != null) {
            return result;
        }
        
        // returning from the last parameter, call function
        
        // first, find new value of "this"
        AbstractedValue thisValue;
        if (expression.getParent() instanceof AccessExpression _) {
            thisValue = getValueOfExpression(currentState, localState, 1, 0);
            // TODO: differentiate based on operator ('.' or '->')?
        } else {
            thisValue = localState.getTopOfStack().getThisValue();
        }
        
        List<List<AbstractedValue>> executionValues = new ArrayList<>();
        executionValues.add(new ArrayList<>());
        
        EvaluationContext newContext = new EvaluationContext(wrap(expression.getFunction()), new ArrayList<>(), -1,
                executionValues, thisValue);
        localState.getExecutionStack().add(newContext);
        getExecutionConditions(localState).addCall();
        
        functionCalled(expression, currentState, localState);
        
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }
    
    /**
     * Called when a function has been called (i.e. entered), right before the SmallStepResult is
     * created. Not called when entering special case functions (wait, notify, request_update).
     * 
     * Subclasses may override this method to adjust the resulting state, e.g. for setting parameter
     * values.
     *
     * @param expression the evaluated function call expression
     * @param currentState the resulting state
     * @param localState the local part of that state
     */
    protected void functionCalled(FunctionCallExpression expression, TransitionResult currentState,
            LocalState localState) {
        
    }
    
    /**
     * Called when a function has been returned from (i.e. left), right before the SmallStepResult is
     * created. Not called when leaving special case functions (wait, notify, request_update) or the top
     * level function.
     * 
     * Subclasses may override this method to adjust the resulting state, e.g. for resetting parameter
     * values.
     *
     * @param expression the function call expression which lead to the function beeing entered
     * @param currentState the resulting state
     * @param localState the local part of that state
     */
    protected void functionReturned(FunctionCallExpression expression, TransitionResult currentState,
            LocalState localState) {
        
    }
    
    /**
     * Handles function calls that require special treatment, such as wait statements or update
     * requests.
     * 
     * If no special treatment is necessary, null is returned.
     *
     * @param currentState the current state
     * @param localState the local part of the current state
     * @param expression the function call expression in question
     * @param comingFrom from where the evaluation is reaching the expression
     * @return the result of the special treatment, or null
     */
    public SmallStepResult handleSpecialCaseFunctionCall(TransitionResult currentState, LocalState localState,
            FunctionCallExpression expression, int comingFrom) {
        if (expression.getFunction().getName().equals("wait")) {
            return handleWaitExpression(currentState, localState, expression, comingFrom);
        }
        
        if (expression.getFunction().getName().equals("request_update")) {
            return handleRequestUpdateExpression(currentState, localState, expression, comingFrom);
        }
        
        return null;
    }
    
    public SmallStepResult handleReturnExpression(TransitionResult currentState, LocalState localState,
            ReturnExpression expression, int comingFrom) {
        // entering from parent
        if (comingFrom == -1 && expression.getReturnStatement() != null) {
            // evaluate return value
            enterChildExpression(localState, 0);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
        
        // returning after evaluation of return value
        return returnFromFunction(currentState, localState, expression, comingFrom,
                getValueOfChild(currentState, localState, 0));
    }
    
    public SmallStepResult handleFunctionBody(TransitionResult currentState, LocalState localState, int comingFrom) {
        EvaluationContext evaluationLocation = localState.getTopOfStack();
        
        if (comingFrom == -1) {
            enterChildExpression(localState, 0);
            return createSmallStepResult(null, comingFrom, currentState, localState, false, false);
        }
        
        int lengthOfBody = evaluationLocation.getFunction().getBody().size();
        
        if (comingFrom < lengthOfBody - 1) {
            enterChildExpression(localState, comingFrom + 1);
            return createSmallStepResult(null, comingFrom, currentState, localState, false, false);
        }
        
        return returnFromFunction(currentState, localState, null, comingFrom);
    }
    
    public SmallStepResult handleWaitExpression(TransitionResult currentState, LocalState localState,
            FunctionCallExpression expression, int comingFrom) {
        throw new UnsupportedOperationException("This expression crawler can't handle wait expressions.");
    }
    
    public SmallStepResult handleRequestUpdateExpression(TransitionResult currentState, LocalState localState,
            FunctionCallExpression expression, int comingFrom) {
        throw new UnsupportedOperationException("This expression crawler can't handle request update expressions.");
    }
    
    public SmallStepResult handleOtherExpression(TransitionResult currentState, LocalState localState,
            Expression expression, int comingFrom) {
        throw new UnsupportedOperationException(
                "Expression type " + expression.getClass().getName() + " not supported.");
    }
    
    /**
     * Updates the execution stack of the local state to reflect entering the current expression's child
     * with the given index.
     *
     * @param localState the local state
     * @param index the index of the entered child
     */
    public void enterChildExpression(LocalState localState, int index) {
        EvaluationContext top = localState.getTopOfStack();
        
        top.getExpressionIndices().add(index);
        top.setComingFrom(-1);
        top.getExpressionValues().add(new ArrayList<>());
    }
    
    /**
     * Updates the execution stack of the local state to reflect returning from the evaluation of a
     * child back to the parent.
     * 
     * No evaluation result of the child is specified.
     *
     * @param expression the expression whose parent to return to
     * @param localState the local state
     */
    public void returnToParent(Expression expression, LocalState localState) {
        returnToParent(expression, localState, null);
    }
    
    
    /**
     * Updates the execution stack of the local state to reflect returning from the evaluation of a
     * child back to the parent, with the given value as the evaluation result.
     * 
     * @param expression the expression whose parent to return to
     * @param localState the local state
     * @param result evaluation result of the child
     */
    public void returnToParent(Expression expression, LocalState localState, AbstractedValue result) {
        EvaluationContext top = localState.getTopOfStack();
        
        result = this.interceptor.evaluation().evaluated(expression, localState.getStackTraceView(),
                top.getExpressionIndices(), result);
        
        int removedIndex = top.getExpressionIndices().remove(top.getExpressionIndices().size() - 1);
        top.setComingFrom(removedIndex);
        
        
        top.getExpressionValues().remove(top.getExpressionValues().size() - 1);
        if (!caresAboutValue(expression.getParent(), removedIndex)) {
            return;
        }
        
        List<AbstractedValue> parentValues = top.getExpressionValues().get(top.getExpressionValues().size() - 1);
        CollectionUtil.addOrSet(parentValues, top.getComingFrom(), result);
    }
    
    /**
     * Updates the execution stack of the local state to reflect returning from a function call.
     * 
     * No return value is specified.
     *
     * @param currentState the current state
     * @param localState the local part of that state
     */
    public SmallStepResult returnFromFunction(TransitionResult currentState, LocalState localState,
            ReturnExpression expression, int comingFrom) {
        return returnFromFunction(currentState, localState, expression, comingFrom, null);
    }
    
    /**
     * Updates the execution stack of the local state to reflect returning from a function call with the
     * given return value.
     * 
     * @param currentState the current state
     * @param localState the local part of that state
     * @param result the return value
     */
    public SmallStepResult returnFromFunction(TransitionResult currentState, LocalState localState,
            ReturnExpression expression, int comingFrom, AbstractedValue result) {
        List<EvaluationContext> stack = localState.getExecutionStack();
        stack.remove(stack.size() - 1);
        
        if (stack.isEmpty()) {
            return handleEndOfCodeReached(currentState, localState, stack);
        }
        
        EvaluationContext top = stack.get(stack.size() - 1);
        top.setComingFrom(top.getComingFrom() + 1);
        
        List<AbstractedValue> parentValues = top.getExpressionValues().get(top.getExpressionValues().size() - 1);
        CollectionUtil.addOrSet(parentValues, top.getComingFrom(), result);
        
        getExecutionConditions(localState).removeCall();
        functionReturned((FunctionCallExpression) top.getNextExpression(), currentState, localState);
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }
    
    /**
     * Handles reaching the end of the top-level function considered by this expression crawler.
     *
     * @param currentState the current state
     * @param localState the local part of that state
     * @param stack the current local execution stack
     * @return the possible transitions and whether or not the end of an atomic block has been reached
     */
    public abstract SmallStepResult handleEndOfCodeReached(TransitionResult currentState, LocalState localState,
            List<EvaluationContext> stack);
    
}
