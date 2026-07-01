package de.tub.pes.syscir.analysis.abstraction_refinement.branches;

import de.tub.pes.syscir.analysis.abstraction_refinement.stepgraph.SplittersInformationHandler;
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
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.TwoInformations;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.TwoInformationsHandler;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.List;
import java.util.Set;

public class SplittersAndBranchInformationHandler
        implements InformationHandler, EvaluationInterceptor, ExplorationRecord {
    
    private final TwoInformationsHandler infoHandler;
    
    public SplittersAndBranchInformationHandler(SplittersInformationHandler splittersInformationHandler,
            BranchInformationHandler branchInformationHandler) {
        this.infoHandler = new TwoInformationsHandler(splittersInformationHandler, branchInformationHandler);
    }
    
    // ── InformationHandler ───────────────────────────────────────────────────
    // Delegate all InformationHandler methods to the internal TwoInformationsHandler
    
    @Override
    public TwoInformations getInitialInformation(ConsideredState state) {
        return this.infoHandler.getInitialInformation(state);
    }
    
    @Override
    public TwoInformations handleStartOfCode(TransitionResult currentState, LocalState localState) {
        return this.infoHandler.handleStartOfCode(currentState, localState);
    }
    
    @Override
    public void announceEvaluation(Expression expression, TransitionResult currentState, LocalState localState) {
        this.infoHandler.announceEvaluation(expression, currentState, localState);
    }
    
    @Override
    public TwoInformations handleExpressionEvaluation(Expression evaluated, int comingFrom,
            TransitionResult resultingState, LocalState localState) {
        return this.infoHandler.handleExpressionEvaluation(evaluated, comingFrom, resultingState, localState);
    }
    
    @Override
    public boolean skipReexploration(TransitionInformation oldInformation, TransitionResult newTransition) {
        return this.infoHandler.skipReexploration(oldInformation, newTransition);
    }
    
    @Override
    public boolean wantsMinimalSteps() {
        return this.infoHandler.wantsMinimalSteps();
    }
    
    @Override
    public TwoInformations finalizeInformation(TransitionInformation information) {
        return this.infoHandler.finalizeInformation(information);
    }
    
    @Override
    public void handleEndOfBlock() {
        this.infoHandler.handleEndOfBlock();
    }
    
    @Override
    public TwoInformations handleProcessWaitedForDelta(AnalyzedProcess process, ProcessState resultingState,
            TransitionInformation currentInformation) {
        return this.infoHandler.handleProcessWaitedForDelta(process, resultingState, currentInformation);
    }
    
    @Override
    public TwoInformations handleProcessWaitedForTime(AnalyzedProcess process, ProcessState resultingState,
            TransitionInformation currentInformation) {
        return this.infoHandler.handleProcessWaitedForTime(process, resultingState, currentInformation);
    }
    
    @Override
    public TwoInformations handleProcessWaitedForEvents(AnalyzedProcess process, ProcessState resultingState,
            Set<Event> events, EventBlocker blockerBefore, TransitionInformation currentInformation) {
        return this.infoHandler.handleProcessWaitedForEvents(process, resultingState, events, blockerBefore,
                currentInformation);
    }
    
    // ── EvaluationInterceptor ─────────────────────────────────────────────────
    
    @Override
    public void prepareEvaluation(Expression expression) {
        ((EvaluationInterceptor) this.infoHandler.firstHandler()).prepareEvaluation(expression);
        ((EvaluationInterceptor) this.infoHandler.secondHandler()).prepareEvaluation(expression);
    }
    
    @Override
    public AbstractedValue evaluated(Expression expression, StackTraceView stack, List<Integer> location,
            AbstractedValue result) {
        result = ((EvaluationInterceptor) this.infoHandler.firstHandler()).evaluated(expression, stack, location,
                result);
        result = ((EvaluationInterceptor) this.infoHandler.secondHandler()).evaluated(expression, stack, location,
                result);
        return result;
    }
    
    // ── ExplorationRecord ─────────────────────────────────────────────────────
    
    @Override
    public void explorationMade(ConsideredState from, ConsideredState to, TransitionInformation info) {
        // Only branchInformationHandler (second) implements ExplorationRecord
        ((BranchInformationHandler) this.infoHandler.secondHandler()).explorationMade(from, to,
                ((TwoInformations) info).second());
    }
    
}
