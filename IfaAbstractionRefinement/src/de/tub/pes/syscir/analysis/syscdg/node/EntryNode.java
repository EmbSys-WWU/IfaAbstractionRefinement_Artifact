package de.tub.pes.syscir.analysis.syscdg.node;

import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeType;
import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;

/**
 * The EntryNode class represents the entry node.
 *
 * @author Jan Maria Kirchner
 */
public class EntryNode<S extends SecurityLevel<S>, ND extends NodeData> extends Node<S, ND> {

    /**
     * The constructor for the EntryNode.
     *
     * @param nodeData the node data of the node
     * @param providedSecurityLevel the provided security level of the node
     * @param requiredSecurityLevel the required security level of the node
     * @param declassification whether the node is a declassification node
     */
    public EntryNode(ND nodeData, S providedSecurityLevel, S requiredSecurityLevel, boolean declassification) {
        super(nodeData, providedSecurityLevel, requiredSecurityLevel, declassification);
    }

    @Override
    public NodeType getType() {
        return NodeType.ENTRY;
    }

}
