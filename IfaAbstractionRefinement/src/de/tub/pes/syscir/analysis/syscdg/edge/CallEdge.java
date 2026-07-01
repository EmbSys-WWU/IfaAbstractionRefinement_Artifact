package de.tub.pes.syscir.analysis.syscdg.edge;

import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;
import de.tub.pes.syscir.analysis.syscdg.node.CallNode;
import de.tub.pes.syscir.analysis.syscdg.node.EntryNode;
import de.tub.pes.syscir.analysis.syscdg.node.NodeData;

/**
 * This class represents an Actual-Out Edge. It can only exist between an call node and a entry
 * node.
 * 
 * @author Jan Maria Kirchner
 */
public class CallEdge<N extends CallNode<S, ND>, M extends EntryNode<S, ND>, S extends SecurityLevel<S>, ED extends EdgeData, ND extends NodeData>
        extends Edge<N, M, S, ED, ND> {

    /**
     * The constructor of the CallEdge.
     * 
     * @param ED The edge data of the edge
     * @param N The source call node of the edge
     * @param M The target entry node of the edge
     */
    public CallEdge(ED edgeData, N sourceNode, M targetNode) {
        super(edgeData, sourceNode, targetNode);
    }

}
