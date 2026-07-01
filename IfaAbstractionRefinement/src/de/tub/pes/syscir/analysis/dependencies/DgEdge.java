package de.tub.pes.syscir.analysis.dependencies;

import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeId;
import java.util.Objects;

/**
 * Class representing an edge in a program dependence graph (PDG). This class is (shallowly)
 * immutable with respect to the type, source and target node.
 * 
 * @author Jonas Becker-Kupczok
 *
 */
public class DgEdge<EdgeT extends DgEdge<EdgeT, NodeT, NodeIdT>, NodeT extends DgNode<NodeT, NodeIdT, EdgeT>, NodeIdT extends NodeId> {
    
    /**
     * Enum for the type of an edge.
     *
     * @author Jonas Becker-Kupczok
     *
     */
    public static enum EdgeType {
        /**
         * Type representing that some node is a member of another, e.g. a field of an object or a parameter
         * of a function call.
         */
        MEMBER,
        /**
         * Type representing that whether or not some node is executed depends on another node.
         */
        CONTROL,
        /**
         * Type representing that data or something similar is read/used at a node after having been
         * written/determined at another.
         */
        DATA;
    }
    
    private final EdgeType type;
    private final NodeT source;
    private final NodeT target;
    private final Object data;
    
    private int hashCode;
    private boolean hashCodePrecomputed;
    
    /**
     * Creates a new PdgEdge of the given type between the given nodes.
     * 
     * If the parameter insert is true, the created edge is also inserted as incoming/outcoing for the
     * respective nodes.
     * 
     * @param type the type of the edge
     * @param source the source node
     * @param target the target node
     * @param insert whether or not to directly insert the edge at the nodes
     * @throws NullPointerException if any parameter is null
     * @throws IllegalStateException if insert is true but the insertion failed, i.e. because an equal
     *         edge was already present at one of the nodes
     */
    public DgEdge(EdgeType type, NodeT source, NodeT target, Object data)
            throws NullPointerException, IllegalStateException {
        this.type = Objects.requireNonNull(type);
        this.source = Objects.requireNonNull(source);
        this.target = Objects.requireNonNull(target);
        this.data = data;
    }
    
    /**
     * Returns the type of the edge.
     *
     * @return type of the edge
     */
    public EdgeType getType() {
        return this.type;
    }
    
    /**
     * Returns the source node of the edge.
     *
     * @return source node of the edge
     */
    public NodeT getSource() {
        return this.source;
    }
    
    /**
     * Returns the target node of the edge.
     *
     * @return target node of the edge
     */
    public NodeT getTarget() {
        return this.target;
    }
    
    /**
     * Returns the data associated with this edge.
     *
     * @return data of the edge (may be null)
     */
    public Object getData() {
        return this.data;
    }
    
    @Override
    public int hashCode() {
        // even if the PdgNodes are unlocked, their IDs are deeply immutable, so the edge's hashCode can be
        // cached
        if (!this.hashCodePrecomputed) {
            this.hashCode =
                    ((this.type.hashCode() * 31 + this.source.idHashCode()) * 31 + this.target.idHashCode()) * 31
                            + Objects.hashCode(this.data);
            this.hashCodePrecomputed = true;
        }
        return this.hashCode;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DgEdge edge)) {
            return false;
        }
        return this.type == edge.type && this.source.idEquals(edge.source) && this.target.idEquals(edge.target)
                && Objects.equals(this.data, edge.data);
    }
    
    @Override
    public String toString() {
        return this.source + "-" + this.type + "->" + this.target;
    }
    
}
