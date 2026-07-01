package de.tub.pes.syscir.analysis.syscdg.node;

import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeType;
import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;

/**
 * The FormalOutNode class represents the formal-out node.
 *
 * @author Jan Maria Kirchner
 */
public class FormalOutNode<S extends SecurityLevel<S>, ND extends NodeData> extends Node<S, ND> {

    /**
     * The constructor for the FormalOutNode.
     *
     * @param nodeData the node data of the node
     * @param providedSecurityLevel the provided security level of the node
     * @param requiredSecurityLevel the required security level of the node
     * @param declassification whether the node is a declassification node
     */
    public FormalOutNode(ND nodeData, S providedSecurityLevel, S requiredSecurityLevel, boolean declassification) {
        super(nodeData, providedSecurityLevel, requiredSecurityLevel, declassification);
    }

    @Override
    public NodeType getType() {
        return NodeType.OUT;
    }

}
