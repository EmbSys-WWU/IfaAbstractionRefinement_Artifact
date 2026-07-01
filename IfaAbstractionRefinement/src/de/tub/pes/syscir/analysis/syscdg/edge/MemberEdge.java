package de.tub.pes.syscir.analysis.syscdg.edge;

import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;
import de.tub.pes.syscir.analysis.syscdg.node.Node;
import de.tub.pes.syscir.analysis.syscdg.node.NodeData;

/**
 * This class represents a member Edge.
 * 
 * @author twierbru
 */
public class MemberEdge<N extends Node<S, ND>, M extends Node<S, ND>, S extends SecurityLevel<S>, ED extends EdgeData, ND extends NodeData>
        extends Edge<N, M, S, ED, ND> {

    /**
     * The constructor of the MemberEdge.
     * 
     * @param ED The edge data of the edge
     * @param N The source node of the edge
     * @param M The target node of the edge
     */
    public MemberEdge(ED edgeData, N sourceNode, M targetNode) {
        super(edgeData, sourceNode, targetNode);
    }

}
