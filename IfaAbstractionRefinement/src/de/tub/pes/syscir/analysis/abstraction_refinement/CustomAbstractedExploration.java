package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.ExplorationRecord;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler.SimulationStopMode;
import de.tub.pes.syscir.analysis.statespace_exploration.SequentialStateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesScheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Interceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.NoInformation;
import java.util.Set;

public class CustomAbstractedExploration extends TrackedExplorationBase {

    public CustomAbstractedExploration(RefinementConfig config, Sources trackedElements) {
        super(config, trackedElements, StandardValueManagement.INSTANCE, -1);
        this.trackedElements.add(trackedElements);
    }

    public void run() {
        Interceptor interceptor =
                Interceptor.of(this.variableAccessTracker, this.evaluationAccessTracker, NoInformation.HANDLER);
        Scheduler scheduler = new SomeVariablesScheduler(this.config.scSystem(), this.logic, interceptor,
                SimulationStopMode.SC_STOP_FINISH_IMMEDIATE, _ -> true);
        ConsideredState initialState = initialState(scheduler, interceptor);
        StateSpaceExploration exploration =
                new SequentialStateSpaceExploration(scheduler, ExplorationRecord.NONE, Set.of(initialState));
        exploration.run();
        this.config.log().explorableAbstractionFound(exploration, this.trackedElements);
    }

}
