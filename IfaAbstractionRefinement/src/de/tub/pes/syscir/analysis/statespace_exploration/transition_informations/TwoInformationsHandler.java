package de.tub.pes.syscir.analysis.statespace_exploration.transition_informations;

import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.InformationHandler;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.Set;
import java.util.function.Function;

public record TwoInformationsHandler(InformationHandler firstHandler, InformationHandler secondHandler)
        implements InformationHandler {
    
    private Function<TransitionInformation, TransitionInformation> firstMask() {
        return info -> ((TwoInformations) info).first();
    }
    
    private Function<TransitionInformation, TransitionInformation> firstReverseMask(TransitionResult transitionResult) {
        return ((TwoInformations) transitionResult.transitionInformation())::setFirst;
    }
    
    private Function<TransitionInformation, TransitionInformation> secondMask() {
        return info -> ((TwoInformations) info).second();
    }
    
    private Function<TransitionInformation, TransitionInformation> secondReverseMask(
            TransitionResult transitionResult) {
        return ((TwoInformations) transitionResult.transitionInformation())::setSecond;
    }
    
    private TransitionResult maskForFirst(TransitionResult transitionResult) {
        return transitionResult.maskTransitionInformation(firstMask(), firstReverseMask(transitionResult));
    }
    
    private TransitionResult maskForSecond(TransitionResult transitionResult) {
        return transitionResult.maskTransitionInformation(secondMask(), secondReverseMask(transitionResult));
    }
    
    @Override
    public TwoInformations getInitialInformation(ConsideredState state) {
        return new TwoInformations(this.firstHandler.getInitialInformation(state),
                this.secondHandler.getInitialInformation(state));
    }
    
    @Override
    public TwoInformations handleStartOfCode(TransitionResult currentState, LocalState localState) {
        return new TwoInformations(this.firstHandler.handleStartOfCode(maskForFirst(currentState), localState),
                this.secondHandler.handleStartOfCode(maskForSecond(currentState), localState));
    }
    
    @Override
    public void announceEvaluation(Expression expression, TransitionResult currentState, LocalState localState) {
        this.firstHandler.announceEvaluation(expression, maskForFirst(currentState), localState);
        this.secondHandler.announceEvaluation(expression, maskForSecond(currentState), localState);
    }
    
    @Override
    public TwoInformations handleExpressionEvaluation(Expression evaluated, int comingFrom,
            TransitionResult resultingState, LocalState localState) {
        return new TwoInformations(
                this.firstHandler.handleExpressionEvaluation(evaluated, comingFrom, maskForFirst(resultingState),
                        localState),
                this.secondHandler.handleExpressionEvaluation(evaluated, comingFrom, maskForSecond(resultingState),
                        localState));
    }
    
    @Override
    public boolean skipReexploration(TransitionInformation oldInformation, TransitionResult newTransition) {
        boolean firstSkip = this.firstHandler.skipReexploration(((TwoInformations) oldInformation).first(),
                maskForFirst(newTransition));
        boolean secondSkip = this.secondHandler.skipReexploration(((TwoInformations) oldInformation).second(),
                maskForSecond(newTransition));
        return firstSkip && secondSkip;
    }
    
    @Override
    public boolean wantsMinimalSteps() {
        return this.firstHandler.wantsMinimalSteps() || this.secondHandler.wantsMinimalSteps();
    }
    
    @Override
    public TwoInformations finalizeInformation(TransitionInformation information) {
        TwoInformations tinfo = (TwoInformations) information;
        return new TwoInformations(this.firstHandler.finalizeInformation(tinfo.first()),
                this.secondHandler.finalizeInformation(tinfo.second()));
    }
    
    @Override
    public void handleEndOfBlock() {
        this.firstHandler.handleEndOfBlock();
        this.secondHandler.handleEndOfBlock();
    }
    
    @Override
    public TwoInformations handleProcessWaitedForDelta(AnalyzedProcess process, ProcessState resultingState,
            TransitionInformation currentInformation) {
        TwoInformations tinfo = (TwoInformations) currentInformation;
        return new TwoInformations(
                this.firstHandler.finalizeInformation(
                        this.firstHandler.handleProcessWaitedForDelta(process, resultingState, tinfo.first())),
                this.secondHandler.finalizeInformation(
                        this.secondHandler.handleProcessWaitedForDelta(process, resultingState, tinfo.second())));
    }
    
    @Override
    public TwoInformations handleProcessWaitedForTime(AnalyzedProcess process, ProcessState resultingState,
            TransitionInformation currentInformation) {
        TwoInformations tinfo = (TwoInformations) currentInformation;
        return new TwoInformations(
                this.firstHandler.finalizeInformation(
                        this.firstHandler.handleProcessWaitedForTime(process, resultingState, tinfo.first())),
                this.secondHandler.finalizeInformation(
                        this.secondHandler.handleProcessWaitedForTime(process, resultingState, tinfo.second())));
    }
    
    @Override
    public TwoInformations handleProcessWaitedForEvents(AnalyzedProcess process, ProcessState resultingState,
            Set<Event> events, EventBlocker effectedBlocker, TransitionInformation currentInformation) {
        TwoInformations tinfo = (TwoInformations) currentInformation;
        return new TwoInformations(
                this.firstHandler.finalizeInformation(this.firstHandler.handleProcessWaitedForEvents(process,
                        resultingState, events, effectedBlocker, tinfo.first())),
                this.secondHandler.finalizeInformation(this.secondHandler.handleProcessWaitedForEvents(process,
                        resultingState, events, effectedBlocker, tinfo.second())));
    }
    
}
