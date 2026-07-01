package de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg;

import de.tub.pes.syscir.analysis.dependencies.DgEdge.EdgeType;
import de.tub.pes.syscir.analysis.dependencies.DgNode;
import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeId;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationLocation;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PdgNode extends DgNode<PdgNode, PdgNodeId, PdgEdge> {
    
    /**
     * Class representing the id of a PdgNode. This id consists of the {@link NodeType} and some
     * additional identifier object depending on the NodeType.
     * 
     * It is assumed that the chosen identifier object is deeply immutable, as this class may cache its
     * hashCode for efficiency.
     * 
     * @author Jonas Becker-Kupczok
     *
     */
    public static final class PdgNodeId implements NodeId {
        
        private final NodeType type;
        private final Object identifier;
        
        private volatile int hashCode;
        private volatile boolean hashCodePrecomputed;
        
        /**
         * Creates a new NodeId with the given type and identifier object.
         * 
         * The type may not be null.
         * 
         * @param type the type
         * @param identifier the additional identifier object
         */
        public PdgNodeId(NodeType type, Object identifier) {
            this.type = Objects.requireNonNull(type);
            this.identifier = identifier;
        }
        
        @Override
        public NodeType type() {
            return this.type;
        }
        
        public Object identifier() {
            return this.identifier;
        }
        
        @Override
        public String toString() {
            return this.identifier == null ? ("(" + this.type + ")") : ("(" + this.type + " " + this.identifier + ")");
        }
        
        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            return other instanceof PdgNodeId id && this.type == id.type
                    && Objects.equals(this.identifier, id.identifier);
        }
        
        @Override
        public int hashCode() {
            if (!this.hashCodePrecomputed) {
                this.hashCode = this.type.hashCode() * 31 + Objects.hashCode(this.identifier);
                this.hashCodePrecomputed = true;
            }
            return this.hashCode;
        }
    }
    
    /**
     * Record representing an identifier object for code statements, consisting of a
     * {@link WrappedSCClassInstance} which is the primary instance on which the code runs (e.g. a
     * process or port which is updated) and a call stack specifying the exact location of the
     * statement.
     * 
     * The elements of the call stack are locked, but not cloned, and stored in an unmodifiable list.
     *
     * @author Jonas Becker-Kupczok
     *
     */
    public static record StatementId(WrappedSCClassInstance initialThis, List<EvaluationLocation> callStack) {
        
        public StatementId(WrappedSCClassInstance initialThis, List<EvaluationLocation> callStack) {
            this.initialThis = initialThis;
            this.callStack = Collections.unmodifiableList(callStack);
            this.callStack.forEach(EvaluationLocation::lock);
        }
        
        @Override
        public String toString() {
            return Stream.concat(Stream.of(this.initialThis), this.callStack.stream()).map(String::valueOf)
                    .collect(Collectors.joining(", ", "[", "]"));
        }
    }
    
    private boolean hasControlDependency;
    
    public PdgNode(NodeType type, Object identifier) {
        this(new PdgNodeId(type, identifier));
    }
    
    /**
     * Creates a new PdgNode with the given id and without any edges.
     * 
     * @param id the id of the node
     */
    public PdgNode(PdgNodeId id) {
        super(id);
    }
    
    /**
     * Creates a copy of the given node without any edges.
     * 
     * @param copyOf the node to copy
     */
    protected PdgNode(PdgNode copyOf) {
        super(copyOf);
        
        this.hasControlDependency = copyOf.hasControlDependency;
    }
    
    boolean hasControlDependency() {
        return this.hasControlDependency;
    }
    
    // increase visibility
    @Override
    protected boolean addIncoming(PdgEdge edge) {
        if (edge.getType() == EdgeType.CONTROL) {
            this.hasControlDependency = true;
        }
        return super.addIncoming(edge);
    }
    
    // increase visibility
    @Override
    protected boolean removeIncoming(PdgEdge edge) {
        return super.removeIncoming(edge);
    }
    
    // increase visibility
    @Override
    protected boolean addOutgoing(PdgEdge edge) {
        return super.addOutgoing(edge);
    }
    
    // increase visibility
    @Override
    protected boolean removeOutgoing(PdgEdge edge) {
        return super.removeOutgoing(edge);
    }
    
    @Override
    public PdgNode unlockedClone() {
        return new PdgNode(this);
    }
    
    // increase visibility
    @Override
    public boolean lock() {
        return super.lock();
    }
}
