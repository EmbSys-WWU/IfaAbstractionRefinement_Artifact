package de.tub.pes.syscir.analysis.dependencies;

import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeId;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.StatementId;
import de.tub.pes.syscir.analysis.util.HashCachingLockableObject;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class representing a node in a program dependence graph (PDG).
 * 
 * @author Jonas Becker-Kupczok
 *
 */
public abstract class DgNode<NodeT extends DgNode<NodeT, NodeIdT, EdgeT>, NodeIdT extends NodeId, EdgeT extends DgEdge<EdgeT, NodeT, NodeIdT>>
        extends HashCachingLockableObject {
    
    /**
     * Enum for the types of PdgNodes.
     * 
     * @author Jonas Becker-Kupczok
     *
     */
    public static enum NodeType {
        /**
         * Type of the technical node which controls all statements within a code block not controlled by
         * other statements.
         * 
         * The identifier for this node is the {@link StatementId} of the first statement in the code block.
         */
        ENTRY,
        
        /**
         * Type of nodes representing code statements or parts thereof.
         * 
         * The identifiers for these nodes usually are {@link StatementId}s.
         */
        STATEMENT,
        
        /**
         * Type of nodes representing a variables that may be read by some code before it is written by that
         * code, i.e. a value may be read which was written externally.
         * 
         * The identifiers for these nodes usually are {@link Variable}s.
         */
        IN,
        
        /**
         * Type of nodes representing a variable that is written and still in scope after the considered
         * code is finished, i.e. a value that may be read externally.
         * 
         * The identifiers for these nodes usually are {@link Variable}s.
         */
        OUT;
    }
    
    public static interface NodeId {
        
        NodeType type();
    }
    
    private final NodeIdT id;
    
    private Set<EdgeT> incoming;
    private Set<EdgeT> outgoing;
    
    /**
     * Creates a new PdgNode with the given id and without any edges.
     * 
     * @param id the id of the node
     */
    public DgNode(NodeIdT id) {
        this.id = Objects.requireNonNull(id);
        
        this.incoming = new LinkedHashSet<>();
        this.outgoing = new LinkedHashSet<>();
    }
    
    /**
     * Creates a copy of the given node without any edges.
     * 
     * @param copyOf the node to copy
     */
    protected DgNode(DgNode<NodeT, NodeIdT, EdgeT> copyOf) {
        super(copyOf);
        
        this.id = copyOf.id;
        
        this.incoming = new LinkedHashSet<>(copyOf.incoming.size());
        this.outgoing = new LinkedHashSet<>(copyOf.outgoing.size());
        
        // resetHashCode();
    }
    
    /**
     * Returns the id of this node.
     *
     * @return node id
     */
    public NodeIdT getId() {
        return this.id;
    }
    
    /**
     * Returns the type of this node.
     *
     * @return node type
     */
    public NodeType getType() {
        return this.id.type();
    }
    
    /**
     * Returns an unmodifiable view of this node's incoming edges.
     *
     * @return incoming edges
     */
    public Set<EdgeT> getIncoming() {
        return Collections.unmodifiableSet(this.incoming);
    }
    
    /**
     * Adds the given edge to the incoming edges of this node.
     * 
     * This node must be the target of that edge.
     * 
     * @param edge an edge
     * @return true if the insertion was successfull, or false if the edge was already registered
     */
    protected boolean addIncoming(EdgeT edge) {
        assert edge.getTarget() == this;
        
        requireNotLocked();
        if (this.incoming.add(edge)) {
            // resetHashCode();
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes the given edge from the incoming edges of this node.
     *
     * @param edge an edge
     * @return true if the removal was successfull, or false if the edge was not present
     */
    protected boolean removeIncoming(EdgeT edge) {
        requireNotLocked();
        if (this.incoming.remove(edge)) {
            // resetHashCode();
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns an unmodifiable view of this node's outgoing edges.
     *
     * @return outgoing edges
     */
    public Set<EdgeT> getOutgoing() {
        return Collections.unmodifiableSet(this.outgoing);
    }
    
    /**
     * Adds the given edge to the outgoing edges of this node.
     * 
     * This node must be the source of that edge.
     * 
     * @param edge an edge
     * @return true if the insertion was successfull, or false if the edge was already present
     */
    protected boolean addOutgoing(EdgeT edge) {
        assert edge.getSource() == this;
        
        requireNotLocked();
        if (this.outgoing.add(edge)) {
            // resetHashCode();
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes the given edge from the outgoing edges of this node.
     *
     * @param edge an edge
     * @return true if the removal was successfull, or false if the edge was not present
     */
    protected boolean removeOutgoing(EdgeT edge) {
        requireNotLocked();
        if (this.outgoing.remove(edge)) {
            // resetHashCode();
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns whether or not the {@link PdgNodeId}s of this node and the parameter are equal.
     *
     * @param other another node
     * @return whether this and the other node have equal ids.
     */
    public boolean idEquals(DgNode<?, ?, ?> other) {
        return this.id.equals(other.id);
    }
    
    /**
     * Returns the hashCode of this node's {@link PdgNodeId}.
     *
     * @return hashCode of id
     */
    public int idHashCode() {
        return this.id.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DgNode node)) {
            return false;
        }
        
        return idEquals(node) && this.incoming.equals(node.incoming) && this.outgoing.equals(node.outgoing);
    }
    
    @Override
    protected int hashCodeInternal() {
        // return (idHashCode() * 31 + this.incoming.hashCode()) * 31 + this.outgoing.hashCode();
        return idHashCode();
    }
    
    @Override
    public String toString() {
        return this.id.toString();
    }
    
}
