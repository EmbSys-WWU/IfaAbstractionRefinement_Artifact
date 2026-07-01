package de.tub.pes.syscir.analysis.syscdg.edge;

import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;
import de.tub.pes.syscir.analysis.syscdg.node.ActualInNode;
import de.tub.pes.syscir.analysis.syscdg.node.FormalInNode;
import de.tub.pes.syscir.analysis.syscdg.node.NodeData;

/**
 * This class represents an parameter-in edge. It can only exist between an actual-in node and a
 * formal-in node. It is also a DataFlowEdge.
 * 
 * @author Jan Maria Kirchner
 */
public class ParameterInEdge<N extends ActualInNode<S, ND>, M extends FormalInNode<S, ND>, S extends SecurityLevel<S>, ED extends EdgeData, ND extends NodeData>
        extends DataFlowEdge<N, M, S, ED, ND> {

    /**
     * The constructor of the ParameterInEdge.
     * 
     * @param ED The edge data of the edge
     * @param N The source node of the edge
     * @param M The target node of the edge
     */
    public ParameterInEdge(ED edgeData, N sourceNode, M targetNode) {
        super(edgeData, sourceNode, targetNode);
    }

}
