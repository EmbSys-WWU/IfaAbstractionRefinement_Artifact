package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.SchedulerTransitionResult;

public record SchedulerConsideredState(ConsideredState consideredState, LocalState schedulerState) {

    public SchedulerConsideredState(SchedulerTransitionResult transitionResult) {
        this(transitionResult.resultingState(), transitionResult.schedulerState());
    }

    public void lock() {
        this.consideredState.lock();
        this.schedulerState.lock();
    }

    public SchedulerConsideredState unlockedClone() {
        return new SchedulerConsideredState(this.consideredState.unlockedClone(), this.schedulerState.unlockedClone());
    }

    public SchedulerConsideredState unlockedVersion() {
        if (this.consideredState.isLocked() || this.schedulerState.isLocked()) {
            return unlockedClone();
        }
        return this;
    }

}
