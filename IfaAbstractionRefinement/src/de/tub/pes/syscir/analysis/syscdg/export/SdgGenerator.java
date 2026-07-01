package de.tub.pes.syscir.analysis.syscdg.export;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.tub.pes.syscir.analysis.dependencies.BinarySecurityLevel;
import de.tub.pes.syscir.analysis.dependencies.SdgFromInterleavingCfg;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler.SimulationStopMode;
import de.tub.pes.syscir.analysis.statespace_exploration.SequentialStateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesGlobalState;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesScheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue.BinaryAbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.CfgLikeRecord;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Interceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.VariableInterceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgInformationHandler;
import de.tub.pes.syscir.analysis.syscdg.SystemDependenceGraph;
import de.tub.pes.syscir.analysis.syscdg.edge.Edge;
import de.tub.pes.syscir.analysis.syscdg.edge.SdgEdgeData;
import de.tub.pes.syscir.analysis.syscdg.node.CallNode;
import de.tub.pes.syscir.analysis.syscdg.node.Node;
import de.tub.pes.syscir.analysis.syscdg.node.SdgNodeData;
import de.tub.pes.syscir.analysis.syscdg.node.StatementNodeData;
import de.tub.pes.syscir.engine.Engine;
import de.tub.pes.syscir.sc_model.SCSystem;

public class SdgGenerator {

	public static SystemDependenceGraph<BinarySecurityLevel, SdgNodeData<?>, SdgEdgeData> generate(String absolutPathName) { 

        SCSystem system = Engine.buildModelFromFile(absolutPathName);
        
        AbstractedLogic logic = BinaryAbstractedLogic.INSTANCE;
        
        Interceptor interceptor = Interceptor.of(VariableInterceptor.trackNone(), new PdgInformationHandler());
        
        Scheduler scheduler = new SomeVariablesScheduler(
        		system, 
        		logic,
                interceptor,
                SimulationStopMode.SC_STOP_FINISH_IMMEDIATE,
                event -> true
                );
        
        ConsideredState initialState = ConsideredState.getInitialState(
        		system,
                (eventStates, requestedUpdates, simulationStopped) -> {
                   SomeVariablesGlobalState globalState =
                            new SomeVariablesGlobalState(eventStates, requestedUpdates, simulationStopped,
                                    SomeVariablesGlobalState.initialVariableValues(system, logic));
                    return globalState;
                }, 
                
                (s, p, i) -> {
                    return new SomeVariablesProcess(s, p, i, scheduler, logic, interceptor);
                }, SomeVariablesProcessState::new, (process, globalState) -> {
                    return ((SomeVariablesProcess) process).getSensitivities((SomeVariablesGlobalState) globalState);
                    
                }, 
                BinaryAbstractedValue::of);

        CfgLikeRecord record = new CfgLikeRecord(false, 
        		info -> ((PdgInformation) info).getReadVariables(),
                info -> ((PdgInformation) info).getWrittenVariables(), initialState);
        
        StateSpaceExploration exploration = new SequentialStateSpaceExploration(scheduler, record, Set.of(initialState));
        exploration.run();
        
       return SdgFromInterleavingCfg.createSyscDg(record);
    }
	
	@SuppressWarnings("rawtypes")
	public static void printSDG(SystemDependenceGraph<BinarySecurityLevel, SdgNodeData<?>, SdgEdgeData> sdg, String dirName) { 
		

		String sdgString = "SDG:\n\nNodes:\n";
		for(Node n : sdg.getNodes()) {
			
			String nodeLine = n.toString();
			if(n instanceof CallNode) {
				nodeLine = nodeLine.replace("Statement-Node:", "Call-Node:");
			}
			sdgString += nodeLine;
			
			if(n.getNodeData() instanceof StatementNodeData) {
				StatementNodeData nd = (StatementNodeData) n.getNodeData();
				try {
						
						String expression = "~~ex~~" + nd.getExpression();
						expression = expression.replaceAll("\n"," ");
						sdgString += expression;
					
				} catch (IndexOutOfBoundsException e) {
					String expression = "~~exFehler~~";
					expression = expression.replaceAll("\n"," ");
					sdgString += expression;
				}
			}
			sdgString += "\n";
		}
		
		sdgString += "\nEdges:\n";
		
		for(Edge e : sdg.getEdges()) {
			String edgeLine = e.toString();
			if(e.getSourceNode() instanceof CallNode) {
				edgeLine = edgeLine.replaceFirst("Statement-Node", "Call-Node:");
			}
			if(e.getTargetNode() instanceof CallNode) {
				edgeLine = edgeLine.replaceFirst("--> Statement-Node:", "--> Call-Node:");
			}
			sdgString += edgeLine + "\n";
		}
		
		Map<Integer, Integer> testMap = new HashMap<>();
		int counter = 0;
		for(Node n : sdg.getNodes()) {
			if(n.getNodeData() instanceof SdgNodeData s) {
				if(s.getTransitionInformation() instanceof PdgInformation p) {
					int pdgHash = System.identityHashCode(p);
					if(!testMap.containsKey(pdgHash)) {
						testMap.put(pdgHash, counter);
						counter++;
					}
					
				}
			}
		}
		
		sdgString = sdgString.replaceAll("de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgInformation", "");
		sdgString = sdgString.replaceAll("class de.tub.pes.syscir.analysis.syscdg.", "");
		System.out.println(sdgString);
		System.out.println("Nodecount: " + sdg.getNodes().size());
		System.out.println("Edgecount: " + sdg.getEdges().size());
		System.out.println("PdgCount: " + testMap.size());
		
		
        
        if(dirName != null) { 
        	 try {
     			Files.writeString(Path.of("../SdgVisualizer/example/"+dirName+"/sdg.txt"), sdgString);
     		} catch (IOException e) {
     			e.printStackTrace();
     		}
        }
	}

}
