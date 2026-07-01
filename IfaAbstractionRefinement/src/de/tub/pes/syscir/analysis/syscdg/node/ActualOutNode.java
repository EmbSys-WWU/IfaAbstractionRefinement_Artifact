package de.tub.pes.syscir.analysis.syscdg.node;

import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeType;
import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;

/**
 * This class represents an actual-out node. It is also summary node.
 *
 * @author Jan Maria Kirchner
 */
public class ActualOutNode<S extends SecurityLevel<S>, ND extends NodeData> extends SummaryNode<S, ND> {

    /**
     * The constructor for the ActualOutNode.
     *
     * @param nodeData the node data of the node
     * @param providedSecurityLevel the provided security level of the node
     * @param requiredSecurityLevel the required security level of the node
     * @param declassification whether the node is a declassification node
     */
    public ActualOutNode(ND nodeData, S providedSecurityLevel, S requiredSecurityLevel, boolean declassification) {
        super(nodeData, providedSecurityLevel, requiredSecurityLevel, declassification);
    }

    @Override
    public NodeType getType() {
        return NodeType.OUT;
    }

}
