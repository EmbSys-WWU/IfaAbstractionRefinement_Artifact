package de.tub.pes.syscir.analysis.syscdg.edge;

import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;
import de.tub.pes.syscir.analysis.syscdg.node.NodeData;
import de.tub.pes.syscir.analysis.syscdg.node.SummaryNode;

/**
 * This class represents an Actual-Out Edge. It can only exist between summary-nodes.
 * 
 * @author Jan Maria Kirchner
 */
public class SummaryEdge<N extends SummaryNode<S, ND>, M extends SummaryNode<S, ND>, S extends SecurityLevel<S>, ED extends EdgeData, ND extends NodeData>
        extends Edge<N, M, S, ED, ND> {

    /**
     * The constructor of the SummaryEdge.
     * 
     * @param ED The edge data of the edge
     * @param N The source summary node of the edge
     * @param M The target summary node of the edge
     */
    public SummaryEdge(ED edgeData, N sourceNode, M targetNode) {
        super(edgeData, sourceNode, targetNode);
    }

}
