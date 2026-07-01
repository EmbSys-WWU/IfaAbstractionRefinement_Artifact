package de.tub.pes.syscir.analysis.abstraction_refinement.stepgraph;

import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementUtil;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.EvaluationInterceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.InformationHandler;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.List;
import java.util.Set;

public abstract class StepGraphInformationHandler implements InformationHandler, EvaluationInterceptor {
    
    private final ThreadLocal<ReconstructionContext> contexts = new ThreadLocal<>();
    
    private ReconstructionContext startContext() {
        ReconstructionContext context = this.contexts.get();
        if (context != null) {
            return context;
        }
        this.contexts.set(context = new ReconstructionContext());
        return context;
    }
    
    private ReconstructionContext stopContext() {
        ReconstructionContext context = this.contexts.get();
        if (context == null) {
            throw new IllegalStateException("No context initialized");
        }
        this.contexts.set(null);
        return context;
    }
    
    private ReconstructionContext getContext() {
        ReconstructionContext context = this.contexts.get();
        if (context == null) {
            throw new IllegalStateException("No context initialized");
        }
        return context;
    }
    
    @Override
    public TransitionInformation getInitialInformation(ConsideredState state) {
        ReconstructionContext context = startContext();
        return context.wrap(context.addRoot());
    }
    
    @Override
    public TransitionInformation handleStartOfCode(TransitionResult currentState, LocalState localState) {
        return currentState.transitionInformation();
    }
    
    @Override
    public void announceEvaluation(Expression expression, TransitionResult currentState, LocalState localState) {
        ReconstructionContext context = getContext();
        context.currentConditionExpression = RefinementUtil.isControlCondition(expression) ? expression : null;
        context.currentConditionValue = null;
    }
    
    @Override
    public AbstractedValue evaluated(Expression expression, StackTraceView stack, List<Integer> location,
            AbstractedValue result) {
        ReconstructionContext context = getContext();
        if (context.currentConditionExpression != null && context.currentConditionExpression == expression) {
            context.currentConditionValue = result;
        }
        return result;
    }
    
    @Override
    public TransitionInformation handleExpressionEvaluation(Expression evaluated, int comingFrom,
            TransitionResult resultingState, LocalState localState) {
        SmallStepInformation id = (SmallStepInformation) resultingState.transitionInformation();
        ReconstructionContext context = getContext();
        return context.wrap(context.append(id.getNode(), new NodeInfo(evaluated, context.currentConditionValue)));
    }
    
    @Override
    public boolean skipReexploration(TransitionInformation oldInformation, TransitionResult newTransition) {
        SmallStepInformation presentId = (SmallStepInformation) oldInformation;
        SmallStepInformation alternativeId = (SmallStepInformation) newTransition.transitionInformation();
        getContext().alternative(presentId.getNode(), alternativeId.getNode());
        return true;
    }
    
    @Override
    public boolean wantsMinimalSteps() {
        return true;
    }
    
    @Override
    public void handleEndOfBlock() {
        ReconstructionContext context = stopContext();
        context.resolveAlternatives();
        finishStep(context);
    }
    
    protected abstract void finishStep(ReconstructionContext stepGraph);
    
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
