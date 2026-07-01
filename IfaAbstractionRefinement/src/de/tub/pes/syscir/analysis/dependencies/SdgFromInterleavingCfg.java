package de.tub.pes.syscir.analysis.dependencies;

import de.tub.pes.syscir.analysis.dependencies.DgEdge.EdgeType;
import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeType;
import de.tub.pes.syscir.analysis.dependencies.SdgNode.SdgNodeId;
import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.CfgLikeRecord;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgEdge;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.StatementId;
import de.tub.pes.syscir.analysis.syscdg.SecurityLattice;
import de.tub.pes.syscir.analysis.syscdg.SystemDependenceGraph;
import de.tub.pes.syscir.analysis.syscdg.edge.ControlEdge;
import de.tub.pes.syscir.analysis.syscdg.edge.DataFlowEdge;
import de.tub.pes.syscir.analysis.syscdg.edge.Edge;
import de.tub.pes.syscir.analysis.syscdg.edge.MemberEdge;
import de.tub.pes.syscir.analysis.syscdg.edge.SdgEdgeData;
import de.tub.pes.syscir.analysis.syscdg.node.ActualInNode;
import de.tub.pes.syscir.analysis.syscdg.node.ActualOutNode;
import de.tub.pes.syscir.analysis.syscdg.node.CallNode;
import de.tub.pes.syscir.analysis.syscdg.node.EntryNode;
import de.tub.pes.syscir.analysis.syscdg.node.Node;
import de.tub.pes.syscir.analysis.syscdg.node.ProcessNodeData;
import de.tub.pes.syscir.analysis.syscdg.node.SdgNodeData;
import de.tub.pes.syscir.analysis.syscdg.node.StatementNode;
import de.tub.pes.syscir.analysis.syscdg.node.StatementNodeData;
import de.tub.pes.syscir.analysis.syscdg.node.VariableNodeData;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import java.util.LinkedHashMap;

public class SdgFromInterleavingCfg {
    
    /**
     * This record is used as keys for the hashMap that is used during the construction of the
     * SystemDependenceGraph for accessing previously created nodes. It is required since SdgNodeData
     * does not uniquely identify a node without the node's type being provided.
     */
    private static record NodeKey(SdgNodeData<?> data, NodeType type) {}
    
    /**
     * Method, that creates a SysCDG SystemDependenceGraph from a CfgRecord.
     *
     * @param record a CfgRecord, that results from the analysis
     * @return a SystemDependenceGraph, that represents the record.
     */
    public static SystemDependenceGraph<BinarySecurityLevel, SdgNodeData<?>, SdgEdgeData> createSyscDg(
            CfgLikeRecord record) {
        // Set up the SystemDependence Graph, that will be returned at the end.
        SecurityLattice<BinarySecurityLevel> securityLattice =
                new SecurityLattice<>(BinarySecurityLevel.HIGH, BinarySecurityLevel.LOW);
        SystemDependenceGraph<BinarySecurityLevel, SdgNodeData<?>, SdgEdgeData> sdg =
                new SystemDependenceGraph<>(securityLattice);
        
        // Set up a node map for faster access to previously added nodes.
        // Identifies nodes by their data and type.
        LinkedHashMap<NodeKey, Node<BinarySecurityLevel, SdgNodeData<?>>> nodeMap = new LinkedHashMap<>();
        
        // Go through nodes (SystemC Transitions) from the analysis and add them to the graph.
        for (CfgLikeRecord.Node cfgNode : record.getNodes()) {
            PdgInformation pdginfo = (PdgInformation) cfgNode.getTransitionInformation();
            if (pdginfo == null) {
                continue;
            }
            integratePdgIntoSdg(cfgNode, sdg, nodeMap);
        }
        
        // Lastly, add def-use chains like in the original method.
        
        var cfg = record.toCfg();
        var defUseChains = DefinitionReachabilityAnalysis.getDefUseChainsMemEff(cfg);
        
        for (var chain : defUseChains) {
            CfgLikeRecord.Node defNode = chain.def().node();
            CfgLikeRecord.Node useNode = chain.use();
            
            Variable<?, ?> v = chain.def().variable();
            
            VariableNodeData defData = new VariableNodeData(defNode, v);
            VariableNodeData useData = new VariableNodeData(useNode, v);
            Node<BinarySecurityLevel, SdgNodeData<?>> defDgNode = nodeMap.get(new NodeKey(defData, NodeType.OUT));
            Node<BinarySecurityLevel, SdgNodeData<?>> useDgNode = nodeMap.get(new NodeKey(useData, NodeType.IN));
            SdgEdgeData edgeData = new SdgEdgeData();
            Edge<Node<BinarySecurityLevel, SdgNodeData<?>>, Node<BinarySecurityLevel, SdgNodeData<?>>, BinarySecurityLevel, SdgEdgeData, SdgNodeData<?>> toAdd =
                    new DataFlowEdge<>(edgeData, defDgNode, useDgNode);
            
            sdg.addEdge(toAdd);
        }
        
        sdg.lock();
        return sdg;
    }
    
    /**
     * This method integrates a program dependence graph related to a SystemC-Transition into the system
     * dependence graph, that is used to represent the dependencies for the whole system.
     *
     * It is the SysCDG equivalent to the previous Sdg.integratePdg().
     *
     * @pre Inside the pdg all nodes are unique. This will fail an assertion if that is not provided.
     *
     * @param pdg program dependence graph, that is integrated.
     * @param sdg system dependence graph, that it will be integrated into.
     */
    private static void integratePdgIntoSdg(CfgLikeRecord.Node pdg,
            SystemDependenceGraph<BinarySecurityLevel, SdgNodeData<?>, SdgEdgeData> sdg,
            LinkedHashMap<NodeKey, Node<BinarySecurityLevel, SdgNodeData<?>>> nodeMap) {
        
        // retrieve pdgInfo from node
        PdgInformation pdgInfo = ((PdgInformation) pdg.getTransitionInformation());
        
        // First, add all Nodes from the Pdgs.
        for (PdgNode pdgNode : pdgInfo.getNodes().values()) {
            
            // retrieve the identifier object from the PdgId and create the corresponding SdgNode.
            final SdgNodeData<?> nodeData = createNodeData(pdg, pdgNode.getId().identifier());
            
            final Node<BinarySecurityLevel, SdgNodeData<?>> toAdd = switch (pdgNode.getType()) {
                case ENTRY -> new EntryNode<>(nodeData, BinarySecurityLevel.LOW, BinarySecurityLevel.LOW, false);
                case IN -> new ActualInNode<>(nodeData, BinarySecurityLevel.LOW, BinarySecurityLevel.LOW, false);
                case OUT -> new ActualOutNode<>(nodeData, BinarySecurityLevel.LOW, BinarySecurityLevel.LOW, false);
                case STATEMENT -> {
                    if (nodeData instanceof StatementNodeData snd) {
                        if (snd.getExpression() instanceof FunctionCallExpression) {
                            yield new CallNode<>(snd, BinarySecurityLevel.LOW, BinarySecurityLevel.LOW, false);
                        }
                    }
                    yield new StatementNode<>(nodeData, BinarySecurityLevel.LOW, BinarySecurityLevel.LOW, false);
                }
            };
            
            assert !sdg.hasNode(toAdd);
            nodeMap.put(new NodeKey(nodeData, pdgNode.getType()), toAdd);
            sdg.addNode(toAdd);
        }
        
        // Next step: add transitions between these nodes from the Pdg, they are contained in.
        for (PdgEdge edge : pdgInfo.getEdges()) {
            // create info for inNode.
            final SdgNodeData<?> sourceData = createNodeData(pdg, edge.getSource().getId().identifier());
            
            // create info for outNode.
            final SdgNodeData<?> targetData = createNodeData(pdg, edge.getTarget().getId().identifier());
            
            SdgEdgeData edgeData = new SdgEdgeData();
            
            Node<BinarySecurityLevel, SdgNodeData<?>> sourceNode =
                    nodeMap.get(new NodeKey(sourceData, edge.getSource().getType()));
            Node<BinarySecurityLevel, SdgNodeData<?>> targetNode =
                    nodeMap.get(new NodeKey(targetData, edge.getTarget().getType()));
            
            Edge<Node<BinarySecurityLevel, SdgNodeData<?>>, Node<BinarySecurityLevel, SdgNodeData<?>>, BinarySecurityLevel, SdgEdgeData, SdgNodeData<?>> toAdd =
                    switch (edge.getType()) {
                        case CONTROL -> new ControlEdge<>(edgeData, sourceNode, targetNode);
                        case DATA -> new DataFlowEdge<>(edgeData, sourceNode, targetNode);
                        case MEMBER -> new MemberEdge<>(edgeData, sourceNode, targetNode);
                    };
            
            sdg.addEdge(toAdd);
        }
    }
    
    private static SdgNodeData<?> createNodeData(CfgLikeRecord.Node cfgNode, Object identifier) {
        if (identifier instanceof StatementId sid) {
            return new StatementNodeData(cfgNode, sid);
        } else if (identifier instanceof Variable<?, ?> v) {
            return new VariableNodeData(cfgNode, v);
        } else if (identifier instanceof AnalyzedProcess ap) {
            return new ProcessNodeData(cfgNode, ap);
        } else {
            return null;
        }
    }
    
    public static Sdg create(CfgLikeRecord record) {
        Sdg sdg = new Sdg();
        
        for (var node : record.getNodes()) {
            PdgInformation pdginfo = (PdgInformation) node.getTransitionInformation();
            if (pdginfo == null) {
                continue;
            }
            sdg.integratePdg(pdginfo);
        }
        
        var cfg = record.toCfg();
        var defUseChains = DefinitionReachabilityAnalysis.getDefUseChainsMemEff(cfg);
        
        for (var chain : defUseChains) {
            CfgLikeRecord.Node defNode = chain.def().node();
            CfgLikeRecord.Node useNode = chain.use();
            
            SdgNodeId defNodeId = new SdgNodeId((PdgInformation) defNode.getTransitionInformation(), NodeType.OUT,
                    chain.def().variable());
            SdgNodeId useNodeId = new SdgNodeId((PdgInformation) useNode.getTransitionInformation(), NodeType.IN,
                    chain.def().variable());
            SdgNode defPdgNode = sdg.getNodes().get(defNodeId);
            SdgNode usePdgNode = sdg.getNodes().get(useNodeId);
            new SdgEdge(EdgeType.DATA, defPdgNode, usePdgNode, chain.def().variable(), true);
        }
        
        return sdg;
    }
    
}
