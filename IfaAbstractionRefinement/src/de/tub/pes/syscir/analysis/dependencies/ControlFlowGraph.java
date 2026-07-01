package de.tub.pes.syscir.analysis.dependencies;

import java.util.Collections;
import java.util.Set;

import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;

public class ControlFlowGraph<NodeType extends CfgNode<NodeType, EdgeType, VariableType, TransitionInformationType>, EdgeType extends CfgEdge<NodeType, EdgeType>, VariableType, TransitionInformationType extends TransitionInformation> {

    private NodeType entryNode;
    private Set<NodeType> exitNodes;

    public ControlFlowGraph(NodeType entryNode, Set<NodeType> exitNodes) {
        this.entryNode = entryNode;
        this.exitNodes = Collections.unmodifiableSet(exitNodes);
    }

    public NodeType getEntryNode() {
        return this.entryNode;
    }

    public Set<NodeType> getExitNodes() {
        return this.exitNodes;
    }

}
