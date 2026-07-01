package de.tub.pes.syscir.analysis.syscdg.node;

import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;

/**
 * The SummaryDeclassificationNode represents a summary declassification node in the SDG. It is also
 * summary node.
 *
 * @author Jan Maria Kirchner
 */
public class SummaryDeclassificationNode<S extends SecurityLevel<S>, ND extends NodeData> extends SummaryNode<S, ND> {

    /**
     * The constructor for the SummaryDeclassificationNode. It always declassifies, so the last
     * parameter is always true.
     *
     * @param nodeData the node data of the node
     * @param providedSecurityLevel the provided security level of the node
     * @param requiredSecurityLevel the required security level of the node
     */
    public SummaryDeclassificationNode(ND nodeData, S providedSecurityLevel, S requiredSecurityLevel) {
        super(nodeData, providedSecurityLevel, requiredSecurityLevel, true);
    }

}
