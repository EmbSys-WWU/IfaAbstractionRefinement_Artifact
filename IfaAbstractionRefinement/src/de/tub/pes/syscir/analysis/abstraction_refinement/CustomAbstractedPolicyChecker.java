package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.abstraction_refinement.InformationFlowPolicy.Entry;
import de.tub.pes.syscir.analysis.abstraction_refinement.SdgPathfinder.SdgPath;
import de.tub.pes.syscir.analysis.dependencies.Sdg;
import de.tub.pes.syscir.analysis.dependencies.SdgFromInterleavingCfg;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler.SimulationStopMode;
import de.tub.pes.syscir.analysis.statespace_exploration.SequentialStateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesScheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.CfgLikeRecord;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Interceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.AdvancedPdgInformationHandler;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgInformation;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class CustomAbstractedPolicyChecker extends TrackedExplorationBase {

    public CustomAbstractedPolicyChecker(RefinementConfig config, Sources trackedElements) {
        super(config, trackedElements, StandardValueManagement.INSTANCE, -1);
    }

    public void run() {
        Interceptor interceptor = Interceptor.of(this.variableAccessTracker, this.evaluationAccessTracker,
                new AdvancedPdgInformationHandler());
        Scheduler scheduler = new SomeVariablesScheduler(this.config.scSystem(), this.logic, interceptor,
                SimulationStopMode.SC_STOP_FINISH_IMMEDIATE, _ -> true);
        ConsideredState initialState = initialState(scheduler, interceptor);
        CfgLikeRecord cfgLikeRecord = new CfgLikeRecord(false, info -> ((PdgInformation) info).getReadVariables(),
                info -> ((PdgInformation) info).getWrittenVariables(), initialState);
        StateSpaceExploration exploration =
                new SequentialStateSpaceExploration(scheduler, cfgLikeRecord, Set.of(initialState));
        exploration.run();
        this.config.log().explorableAbstractionFound(exploration, this.trackedElements);
        Sdg sdg = SdgFromInterleavingCfg.create(cfgLikeRecord);
        this.config.log().sdgComputed(exploration, sdg);

        Consumer<SdgPath> noConsumer = _ -> {
        };
        List<Entry> violated =
                this.config.policy().check(entry -> SdgPathfinder.findPath(sdg, entry), noConsumer, this.config.log());
        if (violated.isEmpty()) {
            this.config.log().informationFlowOkay();
        } else {
            this.config.log().informationFlowFail(violated.getFirst().getType(), false);
        }
    }

}
