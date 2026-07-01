package de.tub.pes.syscir.analysis.syscdg.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import de.tub.pes.syscir.analysis.dependencies.BinarySecurityLevel;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgInformation;
import de.tub.pes.syscir.analysis.syscdg.SystemDependenceGraph;
import de.tub.pes.syscir.analysis.syscdg.edge.CallEdge;
import de.tub.pes.syscir.analysis.syscdg.edge.ControlEdge;
import de.tub.pes.syscir.analysis.syscdg.edge.DataFlowEdge;
import de.tub.pes.syscir.analysis.syscdg.edge.Edge;
import de.tub.pes.syscir.analysis.syscdg.edge.EventEdge;
import de.tub.pes.syscir.analysis.syscdg.edge.MemberEdge;
import de.tub.pes.syscir.analysis.syscdg.edge.ParameterInEdge;
import de.tub.pes.syscir.analysis.syscdg.edge.ParameterOutEdge;
import de.tub.pes.syscir.analysis.syscdg.edge.SdgEdgeData;
import de.tub.pes.syscir.analysis.syscdg.edge.SummaryEdge;
import de.tub.pes.syscir.analysis.syscdg.node.ActualInNode;
import de.tub.pes.syscir.analysis.syscdg.node.ActualOutNode;
import de.tub.pes.syscir.analysis.syscdg.node.CallNode;
import de.tub.pes.syscir.analysis.syscdg.node.ControlNode;
import de.tub.pes.syscir.analysis.syscdg.node.EntryNode;
import de.tub.pes.syscir.analysis.syscdg.node.FormalInNode;
import de.tub.pes.syscir.analysis.syscdg.node.FormalOutNode;
import de.tub.pes.syscir.analysis.syscdg.node.Node;
import de.tub.pes.syscir.analysis.syscdg.node.SdgNodeData;
import de.tub.pes.syscir.analysis.syscdg.node.StatementNode;
import de.tub.pes.syscir.analysis.syscdg.node.StatementNodeData;
import de.tub.pes.syscir.analysis.syscdg.node.SummaryDeclassificationNode;
import de.tub.pes.syscir.analysis.syscdg.node.SummaryNode;

/**
 * Exports a {@link SystemDependenceGraph} to a JSON file. The resulting file
 * contains besides, all nodes and edges with their respective attributes, also
 * information for every occuring pdg. The output resemble the suitable format
 * to import by the SdgVisualizer.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SdgJsonExporter {

	/**
	 * Exports the given SDG to a JSON file at the specified path. The file is
	 * structured into three arrays: pdgs, nodes, and edges.
	 *
	 * @param sdg  the system dependence graph to export
	 * @param path the file path to write the JSON output to
	 */
	public static void exportToJson(SystemDependenceGraph<BinarySecurityLevel, SdgNodeData<?>, SdgEdgeData> sdg,
			String path) {

		// Creating Maps - linking Hashs to IDs
		Map<PdgInformation, Integer> pdgToPdgID = new IdentityHashMap<>();
		Map<Node, Integer> nodeToNodeID = new IdentityHashMap<>();

		// Builing Json Arrays
		JsonArray pdgs = pdgJsonArrayBuilder(sdg, pdgToPdgID);
		JsonArray nodes = nodesJsonArrayBuilder(sdg, nodeToNodeID, pdgToPdgID);
		JsonArray edges = edgesJsonArrayBuilder(sdg, nodeToNodeID, pdgToPdgID);

		// Constructing Json Root
		JsonObject root = new JsonObject();
		root.add("pdgs", pdgs);
		root.add("nodes", nodes);
		root.add("edges", edges);

		// writing root to path
		try (FileWriter writer = new FileWriter(path)) {
			new Gson().toJson(root, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Builds the pdgs array by iterating over all entry nodes and extracting
	 * relavant information for later cluster hierachy build. Also populates
	 * pdgHashToPdgID for later reference by nodes and edges.
	 * 
	 * @param sdg        the system dependence graph to export
	 * @param pdgToPdgID map matching pdgIdentity to an ID
	 */
	private static JsonArray pdgJsonArrayBuilder(
			SystemDependenceGraph<BinarySecurityLevel, SdgNodeData<?>, SdgEdgeData> sdg,
			Map<PdgInformation, Integer> pdgToPdgID) {

		JsonArray pdgs = new JsonArray();
		int pdgID = 0;

		// Casting relevant objects needed for information extraction
		for (Node n : sdg.getNodes()) {
			if (!(n instanceof EntryNode))
				continue;
			if (!(n.getNodeData() instanceof SdgNodeData snd)) {
				System.err.println("Warning: Node " + n.toString() + " does not use SdgNodeData, skipping.");
				continue;
			}
			if (!(snd.getTransitionInformation() instanceof PdgInformation pdg)) {
				System.err.println("Warning: Node " + n.toString() + " does not use PdgInformation, skipping.");
				continue;
			}

			// Parsing identifyer String
			JsonObject pdgObject = new JsonObject();

			String identifyer = snd.getIdentifier().toString();
			identifyer = identifyer.substring(1, identifyer.length() - 1);

			String componentType = identifyer.split(" ", 2)[0];
			identifyer = identifyer.split(" ", 2)[1];

			String componentInstance = identifyer.split(";", 2)[0];
			identifyer = identifyer.split(", ", 2)[1];

			String executionUnit = identifyer.split(":", 2)[0];
			identifyer = identifyer.split(":", 2)[1];

			// Check wheater entry has incoming call edges
			boolean isProcedure = sdg.getIncomingEdges(n).stream().anyMatch(e -> e instanceof CallEdge);

			// Adding properties to json object
			pdgObject.addProperty("pdgID", pdgID);
			pdgObject.addProperty("componentType", componentType);
			pdgObject.addProperty("componentInstance", componentInstance);
			pdgObject.addProperty("executionUnit", executionUnit);
			pdgObject.addProperty("isProcedure", isProcedure);

			if (!isProcedure) {
				if (snd instanceof StatementNodeData stnd) {
					String callstack = stnd.getCallStack().toString();
					callstack = callstack.substring(0, callstack.length() - 1);
					callstack = callstack.split(":", 2)[1];
					pdgObject.addProperty("callStack", callstack);
				}
			}

			pdgToPdgID.put(pdg, pdgID);
			pdgID++;
			pdgs.add(pdgObject);
		}
		return pdgs;
	}

	/**
	 * Builds the nodes array by iterating over all nodes in the SDG. Each node
	 * stores its type, a reference to its containing PDG via pdgID, and
	 * type-specific display attributes. Also populates nodeHashToNodeID for later
	 * reference by edges.
	 * 
	 * @param sdg          the system dependence graph to export
	 * @param pdgToPdgID   map matching pdgIdentity to an ID
	 * @param nodeToNodeID map matching nodeIdentity to an ID
	 * 
	 */
	private static JsonArray nodesJsonArrayBuilder(
			SystemDependenceGraph<BinarySecurityLevel, SdgNodeData<?>, SdgEdgeData> sdg,
			Map<Node, Integer> nodeToNodeID, Map<PdgInformation, Integer> pdgToPdgID) {

		JsonArray nodes = new JsonArray();
		int nodeID = 0;

		// iterating over every node
		for (Node n : sdg.getNodes()) {

			// adding nodeID
			JsonObject nodeObject = new JsonObject();
			nodeObject.addProperty("nodeID", nodeID);

			// extracting pdg infomation of every node to add pdgID
			if (!(n.getNodeData() instanceof SdgNodeData snd)) {
				System.err.println("Warning: Node " + n.toString() + " does not use SdgNodeData, skipping.");
				continue;
			}
			if (!(snd.getTransitionInformation() instanceof PdgInformation pdg)) {
				System.err.println("Warning: Node " + n.toString() + " does not use PdgInformation, skipping.");
				continue;
			}

			int pdgID = pdgToPdgID.get(pdg);
			nodeObject.addProperty("pdgID", pdgID);

			// adding node-type-relevenat information
			if (n instanceof EntryNode) {
				nodeObject.addProperty("type", "ENTRY");
			} else if (n instanceof CallNode) {
				nodeObject.addProperty("type", "CALL");
				addCallStackAndExpression(nodeObject, n);
			} else if (n instanceof StatementNode) {
				nodeObject.addProperty("type", "STATEMENT");
				addCallStackAndExpression(nodeObject, n);
			} else if (n instanceof ControlNode) {
				nodeObject.addProperty("type", "CONTROL");
				addCallStackAndExpression(nodeObject, n);
			} else if (n instanceof ActualInNode) {
				nodeObject.addProperty("type", "ACTUAL_IN");
				addVariableInfo(nodeObject, n);
			} else if (n instanceof ActualOutNode) {
				nodeObject.addProperty("type", "ACTUAL_OUT");
				addVariableInfo(nodeObject, n);
			} else if (n instanceof SummaryDeclassificationNode) {
				nodeObject.addProperty("type", "DECLASSIFICATION");
			} else if (n instanceof SummaryNode) {
				nodeObject.addProperty("type", "SUMMARY");
			} else if (n instanceof FormalInNode) {
				nodeObject.addProperty("type", "FORMAL_IN");
			} else if (n instanceof FormalOutNode) {
				nodeObject.addProperty("type", "FORMAL_OUT");
			}

			nodeToNodeID.put(n, nodeID);
			nodeID++;
			nodes.add(nodeObject);
		}
		return nodes;
	}

	/**
	 * Builds the edges array by iterating over all edges in the SDG. Each edge
	 * stores its source and target node references, its type, and an
	 * isInterTransition flag indicating whether it crosses PDG boundaries.
	 * 
	 * @param sdg          the system dependence graph to export
	 * @param pdgToPdgID   map matching pdgIdentity to an ID
	 * @param nodeToNodeID map matching nodeIdentity to an ID
	 * 
	 */

	private static JsonArray edgesJsonArrayBuilder(
			SystemDependenceGraph<BinarySecurityLevel, SdgNodeData<?>, SdgEdgeData> sdg,
			Map<Node, Integer> nodeToNodeID, Map<PdgInformation, Integer> pdgToPdgID) {

		JsonArray edges = new JsonArray();
		int edgeID = 0;

		// iterating over every edge
		for (Edge e : sdg.getEdges()) {
			JsonObject edgeObject = new JsonObject();

			// adding edgeID
			edgeObject.addProperty("edgeID", edgeID);

			// extracting sourceNode and targetNode
			Node sourceNode = e.getSourceNode();
			Node targetNode = e.getTargetNode();

			// Adding nodeIDs
			int sourceNodeID = nodeToNodeID.get(sourceNode);
			edgeObject.addProperty("sourceNodeID", sourceNodeID);

			int targetNodeID = nodeToNodeID.get(targetNode);
			edgeObject.addProperty("targetNodeID", targetNodeID);

			// Adding repective pdgIDs of nodes
			if (!(sourceNode.getNodeData() instanceof SdgNodeData sourceSnd)) {
				System.err.println("Warning: Node " + sourceNode.toString() + " does not use SdgNodeData, skipping.");
				continue;
			}
			if (!(sourceSnd.getTransitionInformation() instanceof PdgInformation sourcePdg)) {
				System.err
						.println("Warning: Node " + sourceNode.toString() + " does not use PdgInformation, skipping.");
				continue;
			}

			int sourcePdgID = pdgToPdgID.get(sourcePdg);

			if (!(targetNode.getNodeData() instanceof SdgNodeData targetSnd)) {
				System.err.println("Warning: Node " + targetNode.toString() + " does not use SdgNodeData, skipping.");
				continue;
			}
			if (!(targetSnd.getTransitionInformation() instanceof PdgInformation targetPdg)) {
				System.err
						.println("Warning: Node " + sourceNode.toString() + " does not use PdgInformation, skipping.");
				continue;
			}

			int targetPdgID = pdgToPdgID.get(targetPdg);

			// Set wheater edge is an interPDG edge
			if (sourcePdgID == targetPdgID) {
				edgeObject.addProperty("isInterPDG", false);
				edgeObject.addProperty("pdgID", sourcePdgID);
			} else {
				edgeObject.addProperty("isInterPDG", true);
				edgeObject.addProperty("sourcePdgID", sourcePdgID);
				edgeObject.addProperty("targetPdgID", targetPdgID);
			}

			//// adding edge-type-relevenat information
			if (e instanceof ParameterInEdge) {
				edgeObject.addProperty("type", "PARAMETER_IN");
			} else if (e instanceof ParameterOutEdge) {
				edgeObject.addProperty("type", "PARAMETER_OUT");
			} else if (e instanceof DataFlowEdge) {
				edgeObject.addProperty("type", "DATAFLOW");
			} else if (e instanceof ControlEdge) {
				edgeObject.addProperty("type", "CONTROL");
			} else if (e instanceof MemberEdge) {
				edgeObject.addProperty("type", "MEMBER");
			} else if (e instanceof CallEdge) {
				edgeObject.addProperty("type", "CALL");
			} else if (e instanceof SummaryEdge) {
				edgeObject.addProperty("type", "SUMMARY");
			} else if (e instanceof EventEdge) {
				edgeObject.addProperty("type", "EVENT");
			}

			edgeID++;
			edges.add(edgeObject);
		}
		return edges;
	}

	/**
	 * Adds callStack and, if available, expression attributes to the given node
	 * object. Used for call, statement, and control nodes.
	 * 
	 * @param nodeObject, object exports node information
	 * @param node,       object holding node information
	 * 
	 */

	private static void addCallStackAndExpression(JsonObject nodeObject, Node node) {
		if (!(node.getNodeData() instanceof SdgNodeData snd)) {
			System.err.println("Warning: Node " + node.toString() + " does not use SdgNodeData, return.");
			return;
		}
		// Adding callstack
		String identifyer = snd.getIdentifier().toString();
		identifyer = identifyer.substring(1, identifyer.length() - 1);
		nodeObject.addProperty("callStack", identifyer.split(":", 2)[1]);

		// adding expression
		if (!(node.getNodeData() instanceof StatementNodeData stnd)) {
			System.err.println("Warning: Node " + node.toString() + " does not use SdgNodeData, return.");
			return;
		}
		
		String expression = stnd.getExpression().toString();
		while(expression.endsWith(";;")) {
			expression = expression.substring(0, expression.length() -1);
		}
		nodeObject.addProperty("expression", expression);
	}

	/**
	 * Adds variable-related attributes to the given node object. If global
	 * variables (GVar) storing additionally sourceComponentType,
	 * sourceComponentInstance.
	 * 
	 * @param nodeObject, object exports node information
	 * @param node,       object holding node information
	 * 
	 */
	private static void addVariableInfo(JsonObject nodeObject, Node node) {
		if (!(node.getNodeData() instanceof SdgNodeData snd)) {
			System.err.println("Warning: Node " + node.toString() + " does not use SdgNodeData, return.");
			return;
		}
		String identifyer = snd.getIdentifier().toString();
		
		//If varibale is global
		if (identifyer.startsWith("GVar")) {
			nodeObject.addProperty("isGlobal", true);
			identifyer = identifyer.split("\\[", 2)[1];

			String sourceComponentType = identifyer.split(" ", 2)[0];
			nodeObject.addProperty("sourceComponentType", sourceComponentType);
			identifyer = identifyer.split(" ", 2)[1];

			String sourceComponentInstance = identifyer.split(";", 2)[0];
			nodeObject.addProperty("sourceComponentInstance", sourceComponentInstance);
			identifyer = identifyer.split(";\\.", 2)[1];
			
			if(identifyer.contains("const ")) {
				String constString = identifyer.split(" ", 2)[0];
				identifyer = identifyer.split(" ", 2)[1];
				String variableType = identifyer.split(" ", 2)[0];
				nodeObject.addProperty("variableType", constString + " "+ variableType);
				identifyer = identifyer.split(" ", 2)[1];
				
				if (identifyer.contains(";")) {
					String varibaleName = identifyer.split(";", 2)[0];
					nodeObject.addProperty("variableName", varibaleName);
				}
				if (identifyer.contains(" ")) {
					String varibaleName = identifyer.split(" ", 2)[0];
					nodeObject.addProperty("variableName", varibaleName);
				}
				return;
			}
			
			

			String variableType = identifyer.split(" ", 2)[0];
			nodeObject.addProperty("variableType", variableType);
			identifyer = identifyer.split(" ", 2)[1];

			if (identifyer.contains(";")) {
				identifyer = identifyer.split(";", 2)[0];
			}
			if (identifyer.contains("]")) {
				identifyer = identifyer.split("]", 2)[0];
			}
			
				String varibaleName = identifyer;
				nodeObject.addProperty("variableName", varibaleName);
			
				
			
		//If variable is local
		} else if (identifyer.startsWith("LVar")) {
			nodeObject.addProperty("isGlobal", false);
			identifyer = identifyer.split("\\.", 2)[1];

			if(!identifyer.contains(" ")) {
				if(identifyer.contains(".")) {
					nodeObject.addProperty("variableType", identifyer.split("\\.")[0].replace("]", "").replace(";", ""));
					nodeObject.addProperty("variableName", identifyer.split("\\.")[1].replace("]", "").replace(";", ""));
					return;
				}
				else {
					identifyer = identifyer.replace("]", "").replace(";", "");
					nodeObject.addProperty("variableType", identifyer);
					nodeObject.addProperty("variableName", "");
					return;
				}
			}
			if(identifyer.contains("const ")) {
				String constString = identifyer.split(" ", 2)[0];
				identifyer = identifyer.split(" ", 2)[1];
				String variableType = identifyer.split(" ", 2)[0];
				nodeObject.addProperty("variableType", constString + " "+ variableType);
				identifyer = identifyer.split(" ", 2)[1];
				
				if (identifyer.contains(";")) {
					String varibaleName = identifyer.split(";", 2)[0];
					nodeObject.addProperty("variableName", varibaleName);
				}
				if (identifyer.contains(" ")) {
					String varibaleName = identifyer.split(" ", 2)[0];
					nodeObject.addProperty("variableName", varibaleName);
				}
				return;
			}
			
			
			String variableType = identifyer.split(" ", 2)[0];
			nodeObject.addProperty("variableType", variableType);
			identifyer = identifyer.split(" ", 2)[1];

			if (identifyer.contains(";")) {
				String varibaleName = identifyer.split(";", 2)[0];
				nodeObject.addProperty("variableName", varibaleName);
			}
			if (identifyer.contains(" ")) {
				String varibaleName = identifyer.split(" ", 2)[0];
				nodeObject.addProperty("variableName", varibaleName);
			}
		}
	}
}