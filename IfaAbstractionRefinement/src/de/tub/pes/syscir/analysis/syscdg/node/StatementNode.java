package de.tub.pes.syscir.analysis.syscdg.node;

import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeType;
import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;

/**
 * The StatementNode class represents a statement node.
 *
 * @author Tom Wierbrügge (twierbru@uni-muenster.de)
 */
public class StatementNode<S extends SecurityLevel<S>, ND extends NodeData> extends Node<S, ND> {

    /**
     * The constructor for the StatementNode.
     *
     * @param nodeData the node data of the node
     * @param providedSecurityLevel the provided security level of the node
     * @param requiredSecurityLevel the required security level of the node
     * @param declassification whether the node is a declassification node
     */
    public StatementNode(ND nodeData, S providedSecurityLevel, S requiredSecurityLevel, boolean declassification) {
        super(nodeData, providedSecurityLevel, requiredSecurityLevel, declassification);
    }

    @Override
    public NodeType getType() {
        return NodeType.STATEMENT;
    }

}
