package de.tub.pes.syscir.analysis.statespace_exploration.no_variables_implementation;

import java.util.Map;
import java.util.Set;

import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.GlobalState;
import de.tub.pes.syscir.analysis.statespace_exploration.TimedBlocker;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;


public class NoVariablesGlobalState extends GlobalState {

    public NoVariablesGlobalState(Map<Event, TimedBlocker> eventStates, Set<WrappedSCClassInstance> requestedUpdates,
            boolean simulationStopped) {
        super(eventStates, requestedUpdates, simulationStopped);
    }

    public NoVariablesGlobalState(NoVariablesGlobalState copyOf) {
        super(copyOf);
    }

    @Override
    public NoVariablesGlobalState unlockedClone() {
        return new NoVariablesGlobalState(this);
    }

}
