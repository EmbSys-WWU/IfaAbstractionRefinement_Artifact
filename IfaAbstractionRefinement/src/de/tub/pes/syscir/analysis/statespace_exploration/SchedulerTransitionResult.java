package de.tub.pes.syscir.analysis.statespace_exploration;

import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.SchedulerConsideredState;

public record SchedulerTransitionResult(ConsideredState resultingState, LocalState schedulerState,
        TransitionInformation transitionInformation) implements TransitionResult {

    public SchedulerTransitionResult(SchedulerConsideredState state,
            TransitionInformation transitionInformation) {
        this(state.consideredState(), state.schedulerState(), transitionInformation);
    }

    @Override
    public SchedulerTransitionResult clone() {
        return new SchedulerTransitionResult(this.resultingState.unlockedClone(), this.schedulerState.unlockedClone(),
                this.transitionInformation.clone());
    }

    @Override
    public SchedulerTransitionResult replaceResultingState(ConsideredState state) {
        return new SchedulerTransitionResult(state, this.schedulerState, this.transitionInformation);
    }

    @Override
    public SchedulerTransitionResult replaceTransitionInformation(TransitionInformation transitionInformation) {
        return new SchedulerTransitionResult(this.resultingState, this.schedulerState, transitionInformation);
    }

}
