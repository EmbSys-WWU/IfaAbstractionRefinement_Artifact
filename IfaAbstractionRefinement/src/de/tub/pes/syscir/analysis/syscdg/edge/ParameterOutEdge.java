package de.tub.pes.syscir.analysis.syscdg.edge;

import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;
import de.tub.pes.syscir.analysis.syscdg.node.ActualOutNode;
import de.tub.pes.syscir.analysis.syscdg.node.FormalOutNode;
import de.tub.pes.syscir.analysis.syscdg.node.NodeData;

/**
 * This class represents an parameter-out edge. It can only exist between an actual formal-out node
 * and a formal-in node.
 * 
 * @author Jan Maria Kirchner
 */
public class ParameterOutEdge<N extends FormalOutNode<S, ND>, M extends ActualOutNode<S, ND>, S extends SecurityLevel<S>, ED extends EdgeData, ND extends NodeData>
        extends DataFlowEdge<N, M, S, ED, ND> {

    /**
     * The constructor of the ParameterOutEdge.
     * 
     * @param ED The edge data of the edge
     * @param N The source node of the edge
     * @param M The target node of the edge
     */
    public ParameterOutEdge(ED edgeData, N sourceNode, M targetNode) {
        super(edgeData, sourceNode, targetNode);
    }

}
