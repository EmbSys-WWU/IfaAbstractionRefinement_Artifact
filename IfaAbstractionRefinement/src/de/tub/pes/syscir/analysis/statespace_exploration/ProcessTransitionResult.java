package de.tub.pes.syscir.analysis.statespace_exploration;

/**
 * Record describing the result of taking a transition, consisting of the resulting state as well as
 * potentially some additional information provided by the {@link AnalyzedProcess} or
 * {@link Scheduler}.
 * <p>
 * This record is the minimal implementation of the interface {@link TransitionResult} and the only
 * one that is used on the top level of the exploration. Other implementations such as
 * {@link SchedulerTransitionResult} are used on lower levels.
 * 
 * @author Jonas Becker-Kupczok
 * 
 */
public record ProcessTransitionResult(ConsideredState resultingState, TransitionInformation transitionInformation)
        implements TransitionResult {

    @Override
    public ProcessTransitionResult clone() {
        return new ProcessTransitionResult(this.resultingState.unlockedClone(), this.transitionInformation.clone());
    }

    @Override
    public ProcessTransitionResult replaceResultingState(ConsideredState state) {
        return new ProcessTransitionResult(state, this.transitionInformation);
    }

    @Override
    public ProcessTransitionResult replaceTransitionInformation(TransitionInformation transitionInformation) {
        return new ProcessTransitionResult(this.resultingState, transitionInformation);
    }
}
