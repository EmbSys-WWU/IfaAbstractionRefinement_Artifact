package de.tub.pes.syscir.analysis.syscdg.edge;

import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;
import de.tub.pes.syscir.analysis.syscdg.node.Node;
import de.tub.pes.syscir.analysis.syscdg.node.NodeData;
import java.util.Objects;

/**
 * The abstract implementation of edge.
 * 
 * @param <N> is a node type.
 * @param <M> is a node type.
 * @param <S> is a security level type.
 * @param <ED> is an edge data type.
 * 
 * @author Jan Kirchner
 */
public abstract class Edge<N extends Node<S, ND>, M extends Node<S, ND>, S extends SecurityLevel<S>, ED extends EdgeData, ND extends NodeData> {

    private final ED edgeData;
    private final N sourceNode;
    private final M targetNode;
    private final int hashCode;

    /**
     * The constructor of the edge.
     * 
     * All parameters need to be immutable to allow hashCode-caching.
     * 
     * @param ED The edge data of the actual-in edge
     * @param N The source node of the edge
     * @param M The target node of the edge
     */
    public Edge(ED edgeData, N sourceNode, M targetNode) {
        this.edgeData = edgeData;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;

        this.hashCode = Objects.hash(edgeData, sourceNode, targetNode);
    }

    /**
     * The getter of the EdgeData
     *
     * @return edgeData the edge data of the edge
     */
    public ED getEdgeData() {
        return edgeData;
    }

    /**
     * The getter of the source node.
     * 
     * @return sourceNode the source node of the edge.
     */
    public N getSourceNode() {
        return sourceNode;
    }

    /**
     * The getter of the target node.
     * 
     * @return targetNode the target node of the edge.
     */
    public M getTargetNode() {
        return targetNode;
    }

    /**
     * The equals functionality for the edge. It compares the instance of the object with a given
     * instance. If the given instance is null or the class of the given instance is not equal to the
     * class of the instance, the method returns false. Otherwise it compares the edge data, the source
     * node and the target node of the instance in combination with the given instance.
     * 
     * @param comparedObject the object which is compared with the instance.
     * @return true if the instance is equal to the given object, otherwise false.
     */
    @Override
    public boolean equals(Object comparedObject) {
        if (this == comparedObject)
            return true;
        if (comparedObject == null || getClass() != comparedObject.getClass())
            return false;
        Edge<?, ?, ?, ?, ?> edge = (Edge<?, ?, ?, ?, ?>) comparedObject;
        return edgeData.equals(edge.edgeData) && sourceNode.equals(edge.sourceNode)
                && targetNode.equals(edge.targetNode);
    }

    /**
     * The hashCode functionality for the edge. It returns the hashCode of the edge data, the source
     * node and the target node in combination. The hashCode is calculated only once and stored in the
     * finalHashCode variable. If the sdg is not immutable, the hashCode is calculated every time.
     * 
     * @return the hashCode of the edge.
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "<" + this.sourceNode.toString() + " --" + this.getClass() + "--> " + this.targetNode.toString() + ">";
    }
}
