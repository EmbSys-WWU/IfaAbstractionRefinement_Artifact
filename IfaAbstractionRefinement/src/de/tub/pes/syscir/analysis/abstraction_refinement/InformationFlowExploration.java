package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.dependencies.Sdg;
import de.tub.pes.syscir.analysis.dependencies.SdgFromInterleavingCfg;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler.SimulationStopMode;
import de.tub.pes.syscir.analysis.statespace_exploration.SequentialStateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesScheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.CfgLikeRecord;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.EvaluationInterceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Interceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.AdvancedPdgInformationHandler;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgInformation;
import java.util.Set;

/**
 * Exploration process capsule that generates an SDG while additionally mapping expressions to
 * sources for the information flow refinement to make refinement decisions.
 * 
 * @author Lukas Ernst
 */
public class InformationFlowExploration extends TrackedExplorationBase {
    
    public InformationFlowExploration(RefinementConfig config, Sources trackedElements) {
        super(config, trackedElements, new SourcedValueManagement(config::newEmptySources), -1);
    }
    
    public Sdg run() {
        Interceptor interceptor = Interceptor.of(this.variableAccessTracker,
                EvaluationInterceptor.sequence(this.evaluationAccessTracker), new AdvancedPdgInformationHandler());
        Scheduler scheduler = new SomeVariablesScheduler(this.config.scSystem(), this.logic, interceptor,
                SimulationStopMode.SC_STOP_FINISH_IMMEDIATE, _ -> true);
        ConsideredState initialState = initialState(scheduler, interceptor);
        CfgLikeRecord cfgLikeRecord = new CfgLikeRecord(false, info -> ((PdgInformation) info).getReadVariables(),
                info -> ((PdgInformation) info).getWrittenVariables(), initialState);
        StateSpaceExploration exploration =
                new SequentialStateSpaceExploration(scheduler, cfgLikeRecord, Set.of(initialState));
        exploration.run();
        Sdg sdg = SdgFromInterleavingCfg.create(cfgLikeRecord);
        this.config.log().sdgComputed(exploration, sdg);
        return sdg;
    }
    
}
