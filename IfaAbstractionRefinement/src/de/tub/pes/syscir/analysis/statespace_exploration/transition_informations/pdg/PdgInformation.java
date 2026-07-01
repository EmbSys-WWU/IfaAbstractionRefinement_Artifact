package de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg;

import de.tub.pes.syscir.analysis.dependencies.DgEdge.EdgeType;
import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeType;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;
import de.tub.pes.syscir.analysis.util.HashCachingLockableObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Transition information forming a program dependence graph (PDG) of all statements evaluated
 * during the transition.
 * <p>
 * The graph contains the following nodes:
 * <!-- @formatter:off -->
 * <ul>
 *     <li>An entry node for each code block started during the transition.</li>
 *     <li>A node for each conditional expression of a loop or if-statement.</li>
 *     <li>A node for each value expression on which to switch, as well as each case label expression.</li>
 *     <li>A node for each expression passed to a function as a parameter.</li>
 *     <li>A node for each expression which determines the object on which a function is called (the x in x.foo()).</li>
 *     <li>A node for each function call expression.</li>
 *     <li>A node for each statement that reads or writes any variable.</li>
 *     <li>An in node for each variable which may be read during the transition before having been written.</li>
 *     <li>An out node for each variable which may have been written during the transition and may still be in scope afterwards.</li>
 * </ul>
 * It contains the following edges:
 * <ul>
 *     <li>Control edges between
 *         <ul>
 *             <li>each conditional expression of a loop or if-statement and every node which is directly control dependent on that conditional (which includes the conditional itself in the case of loops),</li>
 *             <li>each value expression on which to switch and every case label expression contained in the switch block,</li>
 *             <li>each case label expression and the next case label expression it may fall-through to,</li>
 *             <li>each case label expression and every node which is directly control dependent on that case,</li>
 *             <li>each function call expression and every node within the calling function which is directly control dependent on that function call, and</li>
 *             <li>each entry node and every other node belonging to that code block not control dependent on another node.</li>
 *         </ul>
 *     </li>
 *     <li>Data edges between each node writing a variable and every node which might read that variable without it having been overwritten (reaching definition). For this purpose,
 *         <ul>
 *             <li>access expressions with function call expressions as their right hand side children are considered to write the object on which the function is called,</li>
 *             <li>function call expressions are considered to read the object on which the function is called,</li>
 *             <li>expressions passed to function call expressions as parameters are considered to write the respective parameter variables,</li>
 *             <li>non-empty return statements are considered to write the result of a function,</li>
 *             <li>expressions containing a function call expression with a return value are considered reading the result of that expression, and</li>
 *             <li>in and out nodes are considered to write or write their variables, respectively.</li>
 *         </ul>
 *     </li>
 *     <li>Member edges between function call expressions and the expressions for their parameters.</li>
 * </ul>
 * <!-- @formatter:on -->
 * Subclasses of {@link PdgInformationHandler} may specify additional nodes and/or edges.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public class PdgInformation extends HashCachingLockableObject implements TransitionInformation {
    
    // persistent data
    
    private Map<PdgNodeId, PdgNode> nodes;
    
    // transient data
    
    private PdgNode currentEntryNode;
    // currently reaching variables writes, null-values means parameter or before atomic block
    private Map<Variable<?, ?>, Set<PdgNode>> reachingDefs;
    
    /**
     * Creates a new PDG with only an entry node and with no reaching definitions.
     */
    public PdgInformation() {
        this.nodes = new LinkedHashMap<>();
        
        this.reachingDefs = new LinkedHashMap<>();
    }
    
    /**
     * Creates a deep copy of the given PDG.
     *
     * @param copyOf the pdg to copy.
     */
    public PdgInformation(PdgInformation copyOf) {
        super(copyOf);
        
        // copy all nodes
        this.nodes = new LinkedHashMap<>(copyOf.nodes.size());
        for (Entry<PdgNodeId, PdgNode> entry : copyOf.nodes.entrySet()) {
            this.nodes.put(entry.getKey(), entry.getValue().unlockedClone());
        }
        
        this.currentEntryNode =
                copyOf.currentEntryNode == null ? null : this.nodes.get(copyOf.currentEntryNode.getId());
        
        // copy all edges
        for (Entry<PdgNodeId, PdgNode> entry : copyOf.nodes.entrySet()) {
            PdgNode copy = this.nodes.get(entry.getKey());
            for (PdgEdge edge : entry.getValue().getOutgoing()) {
                new PdgEdge(edge.getType(), copy, this.nodes.get(edge.getTarget().getId()), edge.getData(), true);
            }
        }
        
        // copy all reaching definitions
        this.reachingDefs = new LinkedHashMap<>(copyOf.reachingDefs.size());
        for (Entry<Variable<?, ?>, Set<PdgNode>> entry : copyOf.reachingDefs.entrySet()) {
            Set<PdgNode> reaching = new LinkedHashSet<>(entry.getValue().size());
            this.reachingDefs.put(entry.getKey(), reaching);
            for (PdgNode node : entry.getValue()) {
                reaching.add(node == null ? null : this.nodes.get(node.getId()));
            }
        }
    }
    
    /**
     * Returns the current entry node of this PDG (the last one added).
     *
     * @return entry node
     */
    public PdgNode getCurrentEntryNode() {
        return this.currentEntryNode;
    }
    
    public void setCurrentEntryNode(PdgNode node) {
        assert this.nodes.get(node.getId()) == node;
        this.currentEntryNode = node;
    }
    
    /**
     * Returns the nodes of this PDG stored by their IDs.
     *
     * The returned map is modifiable iff this PDG is unlocked. Any changes directly effect the PDG.
     *
     * @return nodes by IDs
     */
    public Map<PdgNodeId, PdgNode> getNodes() {
        resetHashCode();
        return this.nodes;
    }
    
    /**
     * Returns all edges of this PDG.
     *
     * The returned collection is newly created and not backed by this PDG.
     *
     * @return edges
     */
    public Set<PdgEdge> getEdges() {
        return this.nodes.values().stream().map(PdgNode::getOutgoing).flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    public Map<Variable<?, ?>, AbstractedValue> getReadVariables() {
        Map<Variable<?, ?>, AbstractedValue> result = new LinkedHashMap<>();
        this.nodes.values().stream().filter(n -> n.getType() == NodeType.IN)
                .map(n -> (Variable<?, ?>) n.getId().identifier())
                .forEach(v -> result.put(v, BinaryAbstractedValue.UNKNOWN));
        return result;
    }
    
    public Map<Variable<?, ?>, AbstractedValue> getWrittenVariables() {
        Map<Variable<?, ?>, AbstractedValue> result = new LinkedHashMap<>();
        this.nodes.values().stream().filter(n -> n.getType() == NodeType.OUT).forEach(n -> {
            Variable<?, ?> v = (Variable<?, ?>) n.getId().identifier();
            PdgNode inNode = this.nodes.get(new PdgNodeId(NodeType.IN, v));
            if (inNode != null && inNode.getOutgoing().contains(new PdgEdge(EdgeType.DATA, inNode, n, v, false))) {
                result.put(v, BinaryAbstractedValue.UNKNOWN);
            } else {
                result.put(v, BinaryAbstractedValue.of(true));
            }
        });
        return result;
    }
    
    /**
     * Returns the variable definitions that reach the current evaluation point.
     *
     * This assumes that the PDG is updated with every evaluation step. The returned map is modifiable
     * iff this PDG is unlocked. Any changes directly effect the PDG.
     *
     * @return
     */
    public Map<Variable<?, ?>, Set<PdgNode>> getReachingDefs() {
        resetHashCode();
        return this.reachingDefs;
    }
    
    @Override
    public PdgInformation compose(TransitionInformation otherInfo) {
        PdgInformation other = (PdgInformation) otherInfo;
        
        if (other == this) {
            assert isLocked();
            return this;
        }
        if (isLocked()) {
            return unlockedClone().compose(other);
        }
        
        if (!this.currentEntryNode.idEquals(other.currentEntryNode)) {
            throw new IllegalArgumentException("current entry nodes don't match");
        }
        
        // add all nodes absent in this pdg
        for (PdgNode node : other.nodes.values()) {
            this.nodes.computeIfAbsent(node.getId(), PdgNode::new);
        }
        
        // add all edges absent in this pdg. this can only be edges including at least one node from other
        for (PdgNode theirs : other.nodes.values()) {
            PdgNode ours = this.nodes.get(theirs.getId());
            // check outgoing edges of other
            for (PdgEdge edge : theirs.getOutgoing()) {
                if (ours.getOutgoing().contains(edge)) {
                    continue;
                }
                new PdgEdge(edge.getType(), ours, this.nodes.get(edge.getTarget().getId()), edge.getData(), true);
            }
            // check incoming edges of other
            for (PdgEdge edge : theirs.getIncoming()) {
                if (ours.getIncoming().contains(edge)) {
                    continue;
                }
                new PdgEdge(edge.getType(), this.nodes.get(edge.getSource().getId()), ours, edge.getData(), true);
            }
        }
        
        // a variable reaching in this but not in other may reach from before the transition, so add the
        // corresponding in node (or null, if one doesn't yet exist).
        for (Entry<Variable<?, ?>, Set<PdgNode>> entry : this.reachingDefs.entrySet()) {
            if (!other.reachingDefs.containsKey(entry.getKey())) {
                entry.getValue().add(this.nodes.get(new PdgNodeId(NodeType.IN, entry.getKey())));
            }
        }
        
        // merge reaching definitions
        for (Entry<Variable<?, ?>, Set<PdgNode>> entry : other.reachingDefs.entrySet()) {
            this.reachingDefs.compute(entry.getKey(), (k, v) -> {
                // a variable reaching in this but not in other may reach from before the transition, see above.s
                if (v == null) {
                    v = new LinkedHashSet<>();
                    v.add(this.nodes.get(new PdgNodeId(NodeType.IN, entry.getKey())));
                }
                // add all reaching definitions
                for (PdgNode toAdd : entry.getValue()) {
                    v.add(toAdd == null ? null : this.nodes.get(toAdd.getId()));
                }
                return v;
            });
        }
        
        resetHashCode();
        return this;
    }
    
    @Override
    public PdgInformation unlockedClone() {
        return new PdgInformation(this);
    }
    
    @Override
    public PdgInformation clone() {
        return unlockedClone();
    }
    
    @Override
    protected int hashCodeInternal() {
        return this.nodes.hashCode() * 31 + this.reachingDefs.hashCode();
    }
    
    @Override
    public boolean equals(Object comparedObject) {
        if (this == comparedObject) {
            return true;
        }
        if (comparedObject == null || getClass() != comparedObject.getClass()) {
            return false;
        }
        PdgInformation comparedNode = (PdgInformation) comparedObject;
        return Objects.equals(this.currentEntryNode, comparedNode.currentEntryNode)
                && this.reachingDefs.equals(comparedNode.reachingDefs) && this.nodes.equals(comparedNode.nodes);
    }
    
    @Override
    public boolean lock() {
        if (!super.lock()) {
            return false;
        }
        
        this.nodes.values().forEach(PdgNode::lock);
        this.nodes = Collections.unmodifiableMap(this.nodes);
        
        this.reachingDefs.replaceAll((k, v) -> Collections.unmodifiableSet(v));
        this.reachingDefs = Collections.unmodifiableMap(this.reachingDefs);
        
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PDG; Nodes: ");
        builder.append(this.nodes.values());
        builder.append(" Edges: ");
        builder.append(getEdges());
        return builder.toString();
    }
}
