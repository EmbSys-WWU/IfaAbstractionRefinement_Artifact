package de.tub.pes.syscir.analysis.syscdg.node;

import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;

/**
 * The CallNode class represents a call node.
 *
 * @author Jan Maria Kirchner
 */
public class CallNode<S extends SecurityLevel<S>, ND extends NodeData> extends StatementNode<S, ND> {

    /**
     * The constructor for the CallNode.
     *
     * @param nodeData the node data of the node
     * @param providedSecurityLevel the provided security level of the node
     * @param requiredSecurityLevel the required security level of the node
     * @param declassification whether the node is a declassification node
     */
    public CallNode(ND nodeData, S providedSecurityLevel, S requiredSecurityLevel, boolean declassification) {
        super(nodeData, providedSecurityLevel, requiredSecurityLevel, declassification);
    }

}
