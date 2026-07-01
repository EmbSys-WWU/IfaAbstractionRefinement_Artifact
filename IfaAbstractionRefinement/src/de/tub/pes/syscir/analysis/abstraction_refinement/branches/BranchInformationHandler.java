package de.tub.pes.syscir.analysis.abstraction_refinement.branches;

import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementUtil;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.ExplorationRecord;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.EvaluationInterceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.InformationHandler;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BranchInformationHandler implements InformationHandler, EvaluationInterceptor, ExplorationRecord {
    
    private Expression currentCondition;
    private AbstractedValue currentConditionValue;
    
    private final Map<ConsideredState, TransitionInformation> largeStepResults = new HashMap<>();
    
    @Override
    public TransitionInformation getInitialInformation(ConsideredState state) {
        TransitionInformation info = this.largeStepResults.get(state);
        return info != null ? info : new BranchInformation(this, null);
    }
    
    @Override
    public TransitionInformation handleStartOfCode(TransitionResult currentState, LocalState localState) {
        return currentState.transitionInformation();
    }
    
    @Override
    public void announceEvaluation(Expression expression, TransitionResult currentState, LocalState localState) {
        this.currentCondition = RefinementUtil.isControlCondition(expression) ? expression : null;
        this.currentConditionValue = null;
    }
    
    @Override
    public AbstractedValue evaluated(Expression expression, StackTraceView stack, List<Integer> location,
            AbstractedValue result) {
        if (this.currentCondition != null && this.currentCondition == expression) {
            this.currentCondition = expression;
            this.currentConditionValue = result;
        }
        return result;
    }
    
    @Override
    public TransitionInformation handleExpressionEvaluation(Expression evaluated, int comingFrom,
            TransitionResult resultingState, LocalState localState) {
        Branching branching = null;
        int branchCount;
        if (this.currentCondition != null && this.currentCondition == evaluated && this.currentConditionValue != null
                && !this.currentConditionValue.isDetermined()
                && (branchCount = RefinementUtil.getPotentialBranchCount(evaluated)) >= 2) {
            // does this really hold for only the final pass where the result becomes
            // available?
            branching = new Branching(evaluated, this.currentConditionValue, branchCount);
        }
        BranchInformation prevInfo = (BranchInformation) resultingState.transitionInformation();
        BranchInformation newInfo = new BranchInformation(this, branching);
        prevInfo.getSuccessors().add(newInfo);
        newInfo.getPredecessors().add(prevInfo);
        prevInfo.onBranches().forEach(newInfo::addOnBranch);
        if (prevInfo.getBranching() != null) {
            newInfo.addOnBranch(prevInfo.getBranching().addBranchMark());
        }
        return newInfo;
    }
    
    @Override
    public boolean skipReexploration(TransitionInformation oldInformation, TransitionResult newTransition) {
        oldInformation.compose(newTransition.transitionInformation());
        return true;
    }
    
    @Override
    public boolean wantsMinimalSteps() {
        return true;
    }
    
    @Override
    public void explorationMade(ConsideredState from, ConsideredState to, TransitionInformation info) {
        this.largeStepResults.merge(to, info, TransitionInformation::compose);
    }
    
    public void onUpdatePartialDescendantsCount(Branching branching, BranchInformation node) {}
    
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
