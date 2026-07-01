package de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg;

import static de.tub.pes.syscir.analysis.util.CollectionUtil.nullSet;

import de.tub.pes.syscir.analysis.dependencies.DgEdge.EdgeType;
import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeType;
import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationLocation;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView.StackTrace;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesExpressionHandler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.InformationHandler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.LocalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.StatementId;
import de.tub.pes.syscir.analysis.util.LockableObject;
import de.tub.pes.syscir.sc_model.SCParameter;
import de.tub.pes.syscir.sc_model.expressions.AccessExpression;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.BreakExpression;
import de.tub.pes.syscir.sc_model.expressions.CaseExpression;
import de.tub.pes.syscir.sc_model.expressions.ContinueExpression;
import de.tub.pes.syscir.sc_model.expressions.EventNotificationExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.ExpressionBlock;
import de.tub.pes.syscir.sc_model.expressions.ForLoopExpression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.IfElseExpression;
import de.tub.pes.syscir.sc_model.expressions.LoopExpression;
import de.tub.pes.syscir.sc_model.expressions.ReturnExpression;
import de.tub.pes.syscir.sc_model.expressions.SCPortSCSocketExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.expressions.SwitchExpression;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Information handler providing a "simple" (i.e. with only established features, in contrast to
 * {@link AdvancedPdgInformationHandler}) program dependence graph (PDG) capturing the dependencies
 * between statements, in- and otputs within each transition.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <ValueT> the type of abstracted value used in the exploration
 */
public class PdgInformationHandler implements InformationHandler {
    
    private ThreadLocal<List<EvaluationLocation>> announcedLocation;
    
    public PdgInformationHandler() {
        this.announcedLocation = new ThreadLocal<>();
    }
    
    protected List<EvaluationLocation> getAnnouncedLocation() {
        return this.announcedLocation.get();
    }
    
    @Override
    public PdgInformation getInitialInformation(ConsideredState state) {
        return new PdgInformation();
    }
    
    @Override
    public void announceEvaluation(Expression expression, TransitionResult currentState, LocalState localState) {
        if (!isRelevant(expression, localState.getTopOfStack().getComingFrom())) {
            return;
        }
        
        // since handleExpressionEvaluation is only called after the small step, the then "current"
        // evaluation location is not that of the evaluated expression. store the real one so that it can be
        // retrieved if necessary
        List<EvaluationLocation> stack = localState.getExecutionStack().stream().map(EvaluationContext::toLocation)
                .collect(Collectors.toCollection(ArrayList::new));
        for (EvaluationLocation location : stack) {
            location.lock();
        }
        this.announcedLocation.set(stack);
    }
    
    /**
     * Returns whether or not the given expression when coming from the given child is (or might be)
     * relevant to the PDG.
     *
     * @param expression an expression
     * @param comingFrom from where it is/was entered
     * @return true if the evaluation might be relevant to the PDG
     */
    protected boolean isRelevant(Expression expression, int comingFrom) {
        /*
         * Checks that may return something other then true must come last, also more expensive checks
         * should come as late as possible.
         */
        if (expression == null) {
            return comingFrom == -2;
        }
        boolean endOfExpression = comingFrom == expression.getNumOfChildren() - 1;
        
        if (expression instanceof SCVariableExpression ve) {
            return true;
        }
        if (expression instanceof SCPortSCSocketExpression pe) {
            return true;
        }
        if (expression instanceof SCVariableDeclarationExpression de && endOfExpression
                && !de.getInitialValues().isEmpty()) {
            return true;
        }
        if (expression instanceof BinaryExpression be && endOfExpression && be.getOp().equals("=")) {
            return true;
        }
        if (expression instanceof FunctionCallExpression fe && comingFrom == fe.getParameters().size() - 1) {
            return true;
        }
        if (expression instanceof EventNotificationExpression ee && endOfExpression) {
            return true;
        }
        
        if (comingFrom >= 0 && comingFrom < expression.getNumOfChildren()
                && expression.getChild(comingFrom) instanceof FunctionCallExpression fe
                && fe.getFunction().hasReturnType() && !isTopOfStatement(fe)) {
            return true;
        }
        
        if (expression.getParent() != null && comingFrom == -1) {
            int controllingIndex = getControllingIndex(expression, 1); // quite hacky: 1 never gives special case
            if (controllingIndex != -1 && expression == expression.getParent().getChild(controllingIndex)) {
                return true;
            }
        }
        
        if (expression instanceof ReturnExpression re) {
            return comingFrom == 0;
        }
        
        if (expression.getParent() instanceof FunctionCallExpression fe) {
            return endOfExpression;
        }
        
        if (expression.getParent() instanceof AccessExpression ae) {
            if (ae.getLeft() == expression && ae.getRight() instanceof FunctionCallExpression fe) {
                return endOfExpression;
            }
        }
        
        return false;
    }
    
    /**
     * Returns a copy of the given location list with the last location trimmed by
     * {@link #trimLocation(EvaluationLocation, Expression)}.
     *
     * @param location a stack of locations
     * @param expression the expression indicated by that stack
     * @return the stack of locations with the last (i.e. topmost) location trimmed
     */
    public static List<EvaluationLocation> trimLocation(List<EvaluationLocation> location, Expression expression) {
        List<EvaluationLocation> result = new ArrayList<>(location);
        result.set(result.size() - 1, trimLocation(location.getLast(), expression));
        result.forEach(EvaluationLocation::lock);
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Discards indices from {@link EvaluationLocation#getExpressionIndices()} until
     * {@link #formsOwnNode(Expression)} is true for the expression indicated by the location.
     *
     * @param location a location
     * @param expression the expression indicated by that location
     * @return the {@link EvaluationLocation#unlockedVersion()} of that location trimmed as described
     *         above
     */
    public static EvaluationLocation trimLocation(EvaluationLocation location, Expression expression) {
        if (formsOwnNode(expression)) {
            return location;
        }
        location = LockableObject.unlockedVersion(location);
        location.getExpressionIndices().removeLast();
        expression = expression.getParent();
        return trimLocation(location, expression);
    }
    
    /**
     * Returns whether or not the given expression corresponds to its own node in the PDG, in contrast
     * to beeing subsumed in its parent's (or further ancestor's) node.
     *
     * @param expression an expression
     * @return true iff that expression has its own node in the PDG
     */
    public static boolean formsOwnNode(Expression expression) {
        if (expression == null) {
            return true;
        }
        if (expression instanceof FunctionCallExpression || expression.getParent() instanceof FunctionCallExpression) {
            return true;
        }
        if (expression instanceof EventNotificationExpression ee) {
            return true;
        }
        if (expression.getParent() instanceof AccessExpression ae && ae.getLeft() == expression
                && ae.getRight() instanceof FunctionCallExpression) {
            return true;
        }
        return isTopOfStatement(expression);
    }
    
    /**
     * Returns whether or not the given expression is the top of a statement, i.e. the topmost
     * expression ended by a semicolon or forming the conditional of some control structure.
     *
     * @param expression an expression
     * @return true iff that expression is the top of a statement
     */
    public static boolean isTopOfStatement(Expression expression) {
        if (expression.getParent() == null) {
            return true;
        }
        
        if (expression.getParent() instanceof SwitchExpression) {
            return true;
        } else if (expression.getParent() instanceof CaseExpression) {
            return true;
        } else if (expression.getParent() instanceof IfElseExpression) {
            return true;
        } else if (expression.getParent() instanceof LoopExpression) {
            return true;
        } else if (expression.getParent() instanceof ExpressionBlock) {
            return true;
        }
        
        if (expression instanceof FunctionCallExpression fe && expression.getParent() instanceof AccessExpression ae
                && fe == ae.getRight()) {
            return isTopOfStatement(ae);
        }
        return false;
    }
    
    @Override
    public TransitionInformation handleExpressionEvaluation(Expression evaluated, int comingFrom,
            TransitionResult resultingState, LocalState localState) {
        /*
         * The bulk of this method serves to gather read and written variables. Then, data dependencies are
         * added accordingly and finally control dependencies computed. All the while, necessary bookkeeping
         * takes place.
         */
        
        if (!isRelevant(evaluated, comingFrom)) {
            return resultingState.transitionInformation();
        }
        
        PdgInformation currentInfo =
                LockableObject.unlockedVersion((PdgInformation) resultingState.transitionInformation());
        
        // end of code reached
        if (evaluated == null) {
            assert comingFrom == -2;
            // all local variables are now out of scope
            currentInfo.getReachingDefs().keySet().removeIf(variable -> variable instanceof LocalVariable<?>);
            return currentInfo;
        }
        
        // TODO: remove local variables from reaching defs after end of block (not function) where they are
        // in scope
        
        boolean evaluationFinished = comingFrom == evaluated.getNumOfChildren() - 1;
        List<EvaluationLocation> currentLocation = this.announcedLocation.get();
        List<EvaluationLocation> trimmedLocation = trimLocation(currentLocation, evaluated);
        StatementId currentStatementId = new StatementId(localState.getInitialThisValue(), currentLocation);
        StatementId trimmedStatementId = new StatementId(localState.getInitialThisValue(), trimmedLocation);
        Set<Variable<?, ?>> variablesRead =
                localState.getStateInformation(SomeVariablesExpressionHandler.VARIABLES_READ_KEY);
        variablesRead = new LinkedHashSet<>(variablesRead == null ? Set.of() : variablesRead);
        Set<Variable<?, ?>> variablesWritten =
                localState.getStateInformation(SomeVariablesExpressionHandler.VARIABLES_WRITTEN_KEY);
        variablesWritten = new LinkedHashSet<>(variablesWritten == null ? Set.of() : variablesWritten);
        
        PdgNode currentNode = null;
        
        if (evaluationFinished && evaluated.getParent() instanceof FunctionCallExpression fe) {
            // parameter evaluated, add variable representing it as written
            StackTrace stackTrace = localState.getStackTrace(1);
            EvaluationLocation callLocation = currentLocation.getLast().unlockedClone();
            callLocation.getExpressionIndices().removeLast();
            stackTrace.addCall(callLocation);
            int indexOfChild = currentLocation.getLast().getExpressionIndices().getLast();
            LocalVariable<?> paramVar;
            String functionName = fe.getFunction().getName();
            if (functionName.equals("wait") || functionName.equals("notify")) {
                // special case for functions with variable number of parameters
                paramVar = new LocalVariable<>(stackTrace, indexOfChild);
            } else if (fe.getFunction().getParameters().size() != fe.getNumOfChildren()) {
                // TODO look at all special cases
                paramVar = new LocalVariable<>(stackTrace, indexOfChild);
            } else {
                SCParameter param = fe.getFunction().getParameters().get(indexOfChild);
                paramVar = new LocalVariable<>(stackTrace, param.getVar());
            }
            
            variablesWritten.add(paramVar);
        }
        
        if (evaluationFinished && evaluated.getParent() instanceof AccessExpression ae && ae.getLeft() == evaluated
                && ae.getRight() instanceof FunctionCallExpression fe) {
            // new "this" for function call evaluated, add variable representing "this" for the called function
            // as written
            StackTrace stackTrace = localState.getStackTrace(1);
            EvaluationLocation callLocation = currentLocation.getLast().unlockedClone();
            List<Integer> indices = callLocation.getExpressionIndices();
            indices.set(indices.size() - 1, indices.getLast() + 1);
            stackTrace.addCall(callLocation);
            variablesWritten.add(LocalVariable.getThisVariable(stackTrace));
        }
        
        // handle function calls, with special cases first
        if (evaluated instanceof FunctionCallExpression fe && comingFrom == fe.getParameters().size() - 1
                && fe.getFunction().getName().equals("request_update")) {
            currentNode = handleRequestUpdate(currentInfo, currentNode, variablesRead, variablesWritten, fe, comingFrom,
                    resultingState, localState, currentLocation);
        } else if (evaluated instanceof FunctionCallExpression fe && comingFrom == fe.getParameters().size() - 1
                && fe.getFunction().getName().equals("wait")) {
            currentNode = currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.STATEMENT, currentStatementId),
                    PdgNode::new);
            currentNode = handleWait(currentInfo, currentNode, variablesRead, variablesWritten, fe, comingFrom,
                    resultingState, localState, currentLocation);
        } else if (evaluated instanceof FunctionCallExpression fe && comingFrom == fe.getParameters().size() - 1) {
            // entering into function call, reset written variables (parameters are contained at this point, but
            // were already handled earlier) and add variable representing "this" for the called function
            // as read
            variablesWritten = new LinkedHashSet<>();
            
            StackTrace stackTrace = localState.getStackTrace();
            
            if (!(fe.getParent() instanceof AccessExpression ae && ae.getRight() == fe)) {
                // current "this" becomes new "this"
                List<EvaluationLocation> calls = stackTrace.calls();
                variablesRead.add(LocalVariable
                        .getThisVariable(new StackTrace(stackTrace.base(), calls.subList(0, calls.size() - 1))));
                variablesWritten.add(LocalVariable.getThisVariable(stackTrace));
            }
            
            variablesRead.add(LocalVariable.getThisVariable(stackTrace));
        }
        
        if (evaluated instanceof FunctionCallExpression fe && evaluationFinished) {
            // return from function call, remove local variables no longer in scope from reaching definitions
            LocalVariable<?> protectedResultVar;
            int sizeOfReturnedFromStack;
            if (fe.getFunction().hasReturnType() && !isTopOfStatement(fe)) {
                // do not remove the return result
                StackTrace returnedFromStack = localState.getStackTrace(1);
                returnedFromStack.addCall(currentLocation.getLast());
                sizeOfReturnedFromStack = returnedFromStack.size();
                protectedResultVar = LocalVariable.getResultVariable(returnedFromStack);
            } else {
                protectedResultVar = null;
                sizeOfReturnedFromStack = localState.getExecutionStack().size() + 1;
            }
            currentInfo.getReachingDefs().keySet()
                    .removeIf(variable -> isVariableOutOfScope(variable, sizeOfReturnedFromStack, protectedResultVar));
        }
        
        if (evaluated instanceof ReturnExpression re && comingFrom == 0) {
            // non-empty return statement writes result of function call
            StackTrace stackTrace = new StackTrace(currentLocation.getFirst().getFunction(),
                    currentLocation.subList(0, currentLocation.size() - 1));
            variablesWritten.add(LocalVariable.getResultVariable(stackTrace));
        }
        
        LocalVariable<?> usedReturnValue = null;
        if (comingFrom >= 0 && comingFrom < evaluated.getNumOfChildren()
                && evaluated.getChild(comingFrom) instanceof FunctionCallExpression fe
                && fe.getFunction().hasReturnType() && !isTopOfStatement(fe)) {
            // if the result of a function call is used, add the corresponding variable as read and store it to
            // be removed after the data dependency has been registered
            StackTrace stackTrace = localState.getStackTrace(1);
            EvaluationLocation call = currentLocation.getLast().unlockedClone();
            call.getExpressionIndices().add(comingFrom);
            stackTrace.addCall(call);
            usedReturnValue = LocalVariable.getResultVariable(stackTrace);
            variablesRead.add(usedReturnValue);
        }
        
        if (evaluationFinished && evaluated instanceof EventNotificationExpression ee) {
            currentNode = handleNotify(currentInfo, currentNode, variablesRead, variablesWritten, ee, comingFrom,
                    resultingState, localState, currentLocation);
        }
        
        // ========== read and written variables have been determined ========== //
        
        if (!variablesRead.isEmpty()) {
            // if variables were read, add the corresponding data dependencies
            currentNode = currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.STATEMENT, trimmedStatementId),
                    PdgNode::new);
            
            for (Variable<?, ?> read : variablesRead) {
                Set<PdgNode> writtenAt = currentInfo.getReachingDefs().get(read);
                if (writtenAt == null) {
                    writtenAt = nullSet();
                }
                
                for (PdgNode sourceNode : writtenAt) {
                    if (sourceNode == null) {
                        // a null value means written before the current transition, so create an in node for that
                        // variable
                        sourceNode =
                                currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.IN, read), PdgNode::new);
                    }
                    PdgEdge edge = new PdgEdge(EdgeType.DATA, sourceNode, currentNode, read, false);
                    edge.insert();
                }
            }
        }
        
        if (usedReturnValue != null) {
            // the return value is only in scope for the expression directly using it and removed from the
            // reaching definitions afterwards
            currentInfo.getReachingDefs().remove(usedReturnValue);
        }
        
        if (!variablesWritten.isEmpty()) {
            // if variabes were written, insert a node (to capture control dependencies) and add it the reaching
            // definitions
            currentNode = currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.STATEMENT, trimmedStatementId),
                    PdgNode::new);
            
            for (Variable<?, ?> written : variablesWritten) {
                currentInfo.getReachingDefs().put(written, new LinkedHashSet<>(Set.of(currentNode)));
            }
        }
        
        // ========== now it's time for control dependencies ========== //
        
        // insert a node if this statement might be controlling other statements (e.g. a loop condition)
        Expression parent = evaluated.getParent();
        int controllingIndex = getControllingIndex(evaluated, 1); // quite hacky: 1 never gives special case
        if (currentNode == null && parent != null && comingFrom == -1 && controllingIndex != -1
                && evaluated == parent.getChild(controllingIndex)) {
            currentNode = currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.STATEMENT, trimmedStatementId),
                    PdgNode::new);
        }
        
        if (currentNode != null && comingFrom == evaluated.getNumOfChildren() - 1
                && evaluated.getParent() instanceof FunctionCallExpression fe) {
            // add member edge for function call parameters
            
            List<EvaluationLocation> callNodeLocation = new ArrayList<>(this.announcedLocation.get());
            EvaluationLocation topLocation = callNodeLocation.removeLast().unlockedClone();
            topLocation.getExpressionIndices().removeLast();
            callNodeLocation.add(topLocation);
            StatementId callNodeId = new StatementId(localState.getInitialThisValue(), callNodeLocation);
            
            PdgNode callNode =
                    currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.STATEMENT, callNodeId), PdgNode::new);
            PdgEdge edge = new PdgEdge(EdgeType.MEMBER, callNode, currentNode, null, false);
            edge.insert();
        } else if (currentNode != null && !currentNode.hasControlDependency()) {
            // add control dependence edges
            
            Set<List<EvaluationLocation>> controllingLocations = getControlling(evaluated, currentLocation);
            
            if (controllingLocations.isEmpty()) {
                PdgNode controllingNode = currentInfo.getCurrentEntryNode();
                PdgEdge edge = new PdgEdge(EdgeType.CONTROL, controllingNode, currentNode, null, false);
                edge.insert();
            } else {
                for (List<EvaluationLocation> controllingLocation : controllingLocations) {
                    StatementId controllingId = controllingLocation == null ? null
                            : new StatementId(localState.getInitialThisValue(), controllingLocation);
                    PdgNode controllingNode = controllingId == null ? currentInfo.getCurrentEntryNode()
                            : currentInfo.getNodes().get(new PdgNodeId(NodeType.STATEMENT, controllingId));
                    if (controllingNode == null) {
                        controllingNode = currentInfo.getCurrentEntryNode();
                    }
                    PdgEdge edge = new PdgEdge(EdgeType.CONTROL, controllingNode, currentNode, null, false);
                    edge.insert();
                }
            }
        }
        
        return currentInfo;
    }
    
    /**
     * Returns the set of locations (as call stacks) of all expression controlling the given expression.
     *
     * @param expression an expression
     * @param location the location (as a call stack) of the expression
     * @return the set of locations of all expressions controlling the expression
     */
    protected Set<List<EvaluationLocation>> getControlling(Expression expression, List<EvaluationLocation> location) {
        /*
         * Find the controlling locations of a statement by iteratively moving up the expression tree
         * (towards parents) until the current expression's parent is a control structure (e.g. if/else,
         * loop, etc). this will usually result in only one location, with two exceptions: the conditions of
         * loops (which control themselves in addition to being controlled by some expression higher up) and
         * case labels which are controlled by the switch expression as well as the case label above them if
         * fall-through is possbible.
         */
        
        EvaluationLocation currentLocation = location.getLast();
        Set<List<EvaluationLocation>> result = new LinkedHashSet<>();
        while (true) {
            if (expression == null || currentLocation.getExpressionIndices().isEmpty()) {
                // reached the top of the top-most function on the call stack
                if (location.size() == 1) {
                    // if this is the lowest (oldest) function call on the stack, the controlling "location" is the
                    // entry node
                    result.add(null);
                    return result;
                }
                
                // the controlling location is the function call
                List<EvaluationLocation> controllingLoc = location.subList(0, location.size() - 1).stream()
                        .map(EvaluationLocation::unlockedClone).collect(Collectors.toCollection(ArrayList::new));
                
                controllingLoc.set(controllingLoc.size() - 1,
                        trimLocation(controllingLoc.getLast(), controllingLoc.getLast().getNextExpression()));
                controllingLoc.forEach(EvaluationLocation::lock);
                result.add(controllingLoc);
                return result;
            }
            int currentIndex = currentLocation.getExpressionIndices().getLast();
            int controllingIndex = getControllingIndex(expression, currentIndex);
            
            if (expression instanceof CaseExpression ce && currentIndex >= 2) {
                // case expressions need special treatment because of potential fall-through
                CaseExpression before = (CaseExpression) ((SwitchExpression) ce.getParent()).getChild(currentIndex - 1);
                
                Expression last = before.getBody().getLast();
                while (last instanceof ExpressionBlock eb) {
                    last = eb.getBlock().getLast();
                }
                
                if (!(last instanceof BreakExpression || last instanceof ReturnExpression
                        || last instanceof ContinueExpression)) {
                    // fall-through is possible, add before as a controlling location
                    List<EvaluationLocation> controllingLoc = location.stream().map(EvaluationLocation::unlockedClone)
                            .collect(Collectors.toCollection(ArrayList::new));
                    controllingLoc.getLast().getExpressionIndices().removeLast();
                    controllingLoc.getLast().getExpressionIndices().add(currentIndex - 1);
                    controllingLoc.forEach(EvaluationLocation::lock);
                    result.add(controllingLoc);
                }
            }
            
            if (controllingIndex == -1) {
                // the current parent has no controlling child (with regards to the current expression), move
                // further up
                currentLocation = LockableObject.unlockedVersion(currentLocation);
                currentLocation.getExpressionIndices().removeLast();
                expression = expression.getParent();
                continue;
            }
            
            if (controllingIndex == currentIndex) {
                // the current expression is the controlling child of it's parent (with regards to itself), add it
                // as a controlling location and move further up.
                List<EvaluationLocation> controllingLoc = location.stream().map(EvaluationLocation::unlockedClone)
                        .collect(Collectors.toCollection(ArrayList::new));
                controllingLoc.forEach(EvaluationLocation::lock);
                result.add(controllingLoc);
                
                currentLocation = LockableObject.unlockedVersion(currentLocation);
                currentLocation.getExpressionIndices().removeLast();
                expression = expression.getParent();
                continue;
            }
            
            // add the current parent's controlling child as the controlling location and return
            List<EvaluationLocation> controllingLoc = location.subList(0, location.size() - 1).stream()
                    .map(EvaluationLocation::unlockedClone).collect(Collectors.toCollection(ArrayList::new));
            currentLocation = LockableObject.unlockedVersion(currentLocation);
            currentLocation.getExpressionIndices().set(currentLocation.getExpressionIndices().size() - 1,
                    controllingIndex);
            controllingLoc.add(currentLocation);
            controllingLoc.forEach(EvaluationLocation::lock);
            result.add(controllingLoc);
            return result;
        }
    }
    
    /**
     * Returns the index of the expression controlling the given expression within the expression's
     * parent.
     * <p>
     * If the expression is directly within a controlling expression (e.g. a loop), the index of the
     * conditional expression within that controlling expression is returned (e.g. 0 in the case of a
     * while loop). If the expression is not directly contained in a controlling expression, -1 is
     * returned. If the expression is the conditional expression of its containing control expression,
     * and the control expression is not a loop (i.e. the conditional doesn't influence itself), -1 is
     * returned.
     *
     * @apiNote with indexInParent = 1, this method won't run into any special case, thus just returning
     *          the controlling index of the expression's parent.
     * @param expression an expression
     * @param indexInParent that expression's index in its parent
     * @return the controlling index of this expression in its parent
     */
    protected int getControllingIndex(Expression expression, int indexInParent) {
        Expression parent = (expression == null) ? null : expression.getParent();
        if (indexInParent == -1) {
            return -1;
        } else if (parent instanceof IfElseExpression ie) {
            return indexInParent == 0 ? -1 : 0;
        } else if (parent instanceof ForLoopExpression fe) {
            // the innitialization is not dependent on the conditional
            return indexInParent == 0 ? -1 : 1;
        } else if (parent instanceof LoopExpression le) {
            return 0;
        } else if (parent instanceof SwitchExpression se) {
            return indexInParent == 0 ? -1 : 0;
        } else if (parent instanceof CaseExpression ce) {
            return ce.isDefaultCase() || indexInParent == 0 ? -1 : 0;
        } else {
            return -1;
        }
    }
    
    /**
     * Returns whether the given variable is out of scope after returning from a stack of the given
     * size.
     *
     * @param variable a variable
     * @param sizeOfReturnedFromStack the size of the call stack before the return
     * @param protectedVariable a protected variable that is always considered to remain in scope, or
     *        null
     * @return true if the variable is out of scope after the return
     */
    protected boolean isVariableOutOfScope(Variable<?, ?> variable, int sizeOfReturnedFromStack,
            Variable<?, ?> protectedVariable) {
        return variable instanceof LocalVariable<?> lv && lv.stack().size() >= sizeOfReturnedFromStack
                && !lv.equals(protectedVariable);
    }
    
    /**
     * Called during {@link #handleExpressionEvaluation(Expression, int, TransitionResult, LocalState)}
     * when the encountered expression is a {@link EventNotificationExpression} (and the evaluation has
     * been finished).
     * 
     * Subclasses can use the information to modify currentInfo (which is guaranteed to be unlocked) as
     * well as variablesRead and variablesWritten (which are guaranteed to be modifiable) accordingly.
     * If currentNode is null, they must make sure a corresponding node it is present in the information
     * and return it.
     * 
     * @implSpec This implementation simply inserts a new corresponding node if one isn't yet present
     *           and returns it.
     * 
     * @param <LocalStateT> the type of local state provided
     * @param currentInfo the (unlocked) current PdgInformation
     * @param currentNode the node corresponding to the expression if it was already determined, or null
     *        else
     * @param variablesRead the (modifiable) set of read variables
     * @param variablesWritten the (modifiable) set of read variables
     * @param expression the evaluated expression
     * @param comingFrom from where the expression was entered
     * @param resultingState the state after the evaluation
     * @param localState the local part of that state
     * @param currentLocation the location of the expression
     * @return the node corresponding to that expression
     */
    protected PdgNode handleNotify(PdgInformation currentInfo, PdgNode currentNode, Set<Variable<?, ?>> variablesRead,
            Set<Variable<?, ?>> variablesWritten, EventNotificationExpression expression, int comingFrom,
            TransitionResult resultingState, LocalState localState, List<EvaluationLocation> currentLocation) {
        if (currentNode != null) {
            return currentNode;
        }
        return currentInfo.getNodes().computeIfAbsent(
                new PdgNodeId(NodeType.STATEMENT, new StatementId(localState.getInitialThisValue(), currentLocation)),
                PdgNode::new);
    }
    
    /**
     * Called during {@link #handleExpressionEvaluation(Expression, int, TransitionResult, LocalState)}
     * when the encountered expression is a {@link FunctionCallExpression} to wait (and all parameters
     * have been evaluated).
     * 
     * Subclasses can use the information to modify currentInfo (which is guaranteed to be unlocked) as
     * well as variablesRead and variablesWritten (which are guaranteed to be modifiable) accordingly.
     * If currentNode is null, they must make sure a corresponding node it is present in the information
     * and return it.
     * 
     * @implSpec This implementation simply inserts a new corresponding node if one isn't yet present
     *           and returns it.
     * 
     * @param <LocalStateT> the type of local state provided
     * @param currentInfo the (unlocked) current PdgInformation
     * @param currentNode the node corresponding to the expression if it was already determined, or null
     *        else
     * @param variablesRead the (modifiable) set of read variables
     * @param variablesWritten the (modifiable) set of read variables
     * @param expression the evaluated expression
     * @param comingFrom from where the expression was entered
     * @param resultingState the state after the evaluation
     * @param localState the local part of that state
     * @param currentLocation the location of the expression
     * @return the node corresponding to that expression
     */
    protected PdgNode handleWait(PdgInformation currentInfo, PdgNode currentNode, Set<Variable<?, ?>> variablesRead,
            Set<Variable<?, ?>> variablesWritten, FunctionCallExpression expression, int comingFrom,
            TransitionResult resultingState, LocalState localState, List<EvaluationLocation> currentLocation) {
        if (currentNode != null) {
            return currentNode;
        }
        return currentInfo.getNodes().computeIfAbsent(
                new PdgNodeId(NodeType.STATEMENT, new StatementId(localState.getInitialThisValue(), currentLocation)),
                PdgNode::new);
    }
    
    /**
     * Called during {@link #handleExpressionEvaluation(Expression, int, TransitionResult, LocalState)}
     * when the encountered expression is a {@link FunctionCallExpression} to request_update (and all
     * parameters have been evaluated).
     * 
     * Subclasses can use the information to modify currentInfo (which is guaranteed to be unlocked) as
     * well as variablesRead and variablesWritten (which are guaranteed to be modifiable) accordingly.
     * If currentNode is null, they must make sure a corresponding node it is present in the information
     * and return it.
     * 
     * @implSpec This implementation simply inserts a new corresponding node if one isn't yet present
     *           and returns it.
     * 
     * @param <LocalStateT> the type of local state provided
     * @param currentInfo the (unlocked) current PdgInformation
     * @param currentNode the node corresponding to the expression if it was already determined, or null
     *        else
     * @param variablesRead the (modifiable) set of read variables
     * @param variablesWritten the (modifiable) set of read variables
     * @param expression the evaluated expression
     * @param comingFrom from where the expression was entered
     * @param resultingState the state after the evaluation
     * @param localState the local part of that state
     * @param currentLocation the location of the expression
     * @return the node corresponding to that expression
     */
    protected PdgNode handleRequestUpdate(PdgInformation currentInfo, PdgNode currentNode,
            Set<Variable<?, ?>> variablesRead, Set<Variable<?, ?>> variablesWritten, FunctionCallExpression expression,
            int comingFrom, TransitionResult resultingState, LocalState localState,
            List<EvaluationLocation> currentLocation) {
        if (currentNode != null) {
            return currentNode;
        }
        return currentInfo.getNodes().computeIfAbsent(
                new PdgNodeId(NodeType.STATEMENT, new StatementId(localState.getInitialThisValue(), currentLocation)),
                PdgNode::new);
    }
    
    @Override
    public PdgInformation finalizeInformation(TransitionInformation currentInfo) {
        PdgInformation current = (PdgInformation) currentInfo;
        current = LockableObject.unlockedVersion(current);
        
        // introduce out nodes for all reaching definitions which are still in scope
        for (Entry<Variable<?, ?>, Set<PdgNode>> reachingDef : current.getReachingDefs().entrySet()) {
            PdgNode outNode = new PdgNode(NodeType.OUT, reachingDef.getKey());
            current.getNodes().put(outNode.getId(), outNode);
            if (reachingDef.getValue() == null) {
                // cannot have been written
                continue;
            }
            
            for (PdgNode definedAt : reachingDef.getValue()) {
                if (definedAt == null) {
                    definedAt = current.getNodes().computeIfAbsent(new PdgNodeId(NodeType.IN, reachingDef.getKey()),
                            PdgNode::new);
                }
                new PdgEdge(EdgeType.DATA, definedAt, outNode, reachingDef.getKey(), true);
            }
        }
        
        // reaching definitions are irrelevant after the transition is finished
        current.getReachingDefs().clear();
        
        current.lock();
        return current;
    }
    
    @Override
    public PdgInformation handleStartOfCode(TransitionResult currentState, LocalState localState) {
        PdgInformation currentInformation =
                LockableObject.unlockedVersion((PdgInformation) currentState.transitionInformation());
        
        // insert an entry node for the entered code block, identified by the initial this value and the
        // location stack
        
        List<EvaluationLocation> entryLocation =
                localState.getExecutionStack().stream().map(EvaluationContext::toLocation).toList();
        if (localState.getTopOfStack().getComingFrom() != -1) {
            // the start of the evaluation is the next expression targetted
            entryLocation.getLast().getExpressionIndices().add(localState.getTopOfStack().getComingFrom() + 1);
        }
        entryLocation.forEach(EvaluationLocation::lock);
        
        PdgNode entryNode =
                new PdgNode(NodeType.ENTRY, new StatementId(localState.getInitialThisValue(), entryLocation));
        assert !currentInformation.getNodes().containsKey(entryNode.getId());
        currentInformation.getNodes().put(entryNode.getId(), entryNode);
        currentInformation.setCurrentEntryNode(entryNode);
        return currentInformation;
    }
    
    @Override
    public TransitionInformation handleProcessWaitedForDelta(AnalyzedProcess process, ProcessState resultingState,
            TransitionInformation currentInformation) {
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
