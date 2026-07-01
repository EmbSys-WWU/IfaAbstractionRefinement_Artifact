package de.tub.pes.syscir.analysis.syscdg;

import de.tub.pes.syscir.analysis.syscdg.edge.Edge;
import de.tub.pes.syscir.analysis.syscdg.edge.EdgeData;
import de.tub.pes.syscir.analysis.syscdg.node.Node;
import de.tub.pes.syscir.analysis.syscdg.node.NodeData;
import de.tub.pes.syscir.analysis.util.HashCachingLockableObject;
import de.tub.pes.syscir.analysis.util.LockableObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class is an implementation of a System Dependence Graph based on its construction and
 * slicing algorithm after C. Hammer (2009) doi: 10.5445/KSP/1000012049 (cit. on pp. 6, 10–14, 16,
 * 29, 48) The class contains nodes and edges, which are mapped to the respective nodes as outgoing
 * or incoming. Additionally, the class can be set to immutable at any point, denying further
 * modification.
 *
 * @param <S> The type of SecurityLevel, the security levels the SDG use
 * @param <ND> The type of NodeData which are used on the nodes of the SDG
 * @param <ED> The type of EdgeData which are used on the edges of the SDG
 *
 * @author Jan Maria Kirchner
 */
public class SystemDependenceGraph<S extends SecurityLevel<S>, ND extends NodeData, ED extends EdgeData>
        extends HashCachingLockableObject {

    private final SecurityLattice<S> lattice;
    private final Set<Node<S, ND>> nodes;
    private final Map<Node<S, ND>, LinkedHashSet<Edge<Node<S, ND>, Node<S, ND>, S, ED, ND>>> outgoingEdges;
    private final Map<Node<S, ND>, LinkedHashSet<Edge<Node<S, ND>, Node<S, ND>, S, ED, ND>>> incomingEdges;

    /**
     * The constructor of the SDG. Nodes and edges are initialized as linkedHashMaps.
     *
     * @param lattice is a security lattice which is used in the SDG
     */
    public SystemDependenceGraph(SecurityLattice<S> lattice) {
        this.lattice = lattice;
        this.nodes = new LinkedHashSet<>();
        this.outgoingEdges = new LinkedHashMap<>();
        this.incomingEdges = new LinkedHashMap<>();
    }

    /**
     * The getter for the lattice
     *
     * @return the lattice of the SDG
     */
    public SecurityLattice<S> getLattice() {
        return lattice;
    }

    /**
     * The getter for the nodes
     *
     * @return the nodes of the SDG as unmodifiable set
     */
    public Set<Node<S, ND>> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    /**
     * Adds a node to the SDG.
     *
     * @throws UnsupportedOperationException if the graph is set to immutable.
     */
    public void addNode(Node<S, ND> node) {
        changeSafely(() -> {
            nodes.add(node);
        });
    }

    /**
     * Checks whether the node exists in the SDG.
     *
     * @throws UnsupportedOperationException if the graph is set to immutable.
     * @return true if the SDG contains the node.
     */
    public boolean hasNode(Node<S, ?> node) {
        return nodes.contains(node);
    }

    /**
     * Adds an edge to the SDG. Both source and target node of the edge must exist in the SDG before the
     * edge is added.
     *
     * @param edge the edge to be added.
     * @throws UnsupportedOperationException if the graph is set to immutable.
     */
    public void addEdge(Edge<Node<S, ND>, Node<S, ND>, S, ED, ND> edge) {
        changeSafely(() -> {
            if (!hasNode(edge.getSourceNode()) || !hasNode(edge.getTargetNode())) {
                throw new IllegalArgumentException("Both nodes of the edge must exist in the SDG.");
            }

            outgoingEdges.computeIfAbsent(edge.getSourceNode(), k -> new LinkedHashSet<>()).add(edge);
            incomingEdges.computeIfAbsent(edge.getTargetNode(), k -> new LinkedHashSet<>()).add(edge);
        });
    }

    /**
     * Getter for all edges of the SDG.
     *
     * @return a list of all edges
     */
    public List<Edge<Node<S, ND>, Node<S, ND>, S, ED, ND>> getEdges() {
        List<Edge<Node<S, ND>, Node<S, ND>, S, ED, ND>> result = new ArrayList<>();
        for (var node : this.nodes) {
            result.addAll(this.getIncomingEdges(node));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Getter for the outgoing edges for a node of the SDG.
     *
     * @param node a node of the SDG
     * @return a list of the outgoing edges
     */
    public Set<Edge<Node<S, ND>, Node<S, ND>, S, ED, ND>> getOutgoingEdges(Node<S, ND> node) {
        return Collections.unmodifiableSet(outgoingEdges.get(node));
    }

    /**
     * Getter for the incoming edges for a node of the SDG.
     *
     * @param node a node of the SDG
     * @return a list of the incoming edges
     */
    public Set<Edge<Node<S, ND>, Node<S, ND>, S, ED, ND>> getIncomingEdges(Node<S, ND> node) {
        try {
            return Collections.unmodifiableSet(incomingEdges.get(node));
        } catch (NullPointerException e) {
            return Collections.unmodifiableSet(new LinkedHashSet<>());
        }
    }

    /**
     * Generates the backwards slice for a given node, i.e. all nodes, that can reach this node.
     *
     * @return Set of these nodes.
     */
    public Set<Node<S, ND>> backwardsSlice(Node<S, ND> node) {
        Set<Node<S, ND>> result = new LinkedHashSet<>(Set.of(node));
        Deque<Node<S, ND>> worklist = new ArrayDeque<>(List.of(node));

        while (!worklist.isEmpty()) {
            Node<S, ND> current = worklist.poll();
            for (Edge<Node<S, ND>, Node<S, ND>, S, ED, ND> edge : this.getIncomingEdges(current)) {
                if (result.add(edge.getSourceNode())) {
                    worklist.add(edge.getSourceNode());
                }
            }
        }

        return result;
    }


    /**
     * Takes a Runnable as parameter. This Runnable is only executed if the graph is not set to
     * immutable. The method is used for methods which are modifying the SDG.
     *
     * @throws UnsupportedOperationException if the graph is set to immutable.
     * @param setterLogic the Runnable which is run if the graph is not immutable.
     */
    private void changeSafely(Runnable setterLogic) {
        requireNotLocked();
        setterLogic.run();
    }

    /**
     * Prints this class by adding all nodes and edges to a string.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SDG: Nodes: ");
        builder.append(this.nodes);
        builder.append("Edges: "); // only add outgoing edges, since incoming edges are the same, just from the other
                                   // direction
        ArrayList<Edge<?, ?, ?, ?, ?>> edges = new ArrayList<>(); // flatten list
        for (Set<Edge<Node<S, ND>, Node<S, ND>, S, ED, ND>> list : this.incomingEdges.values()) {
            for (Edge<?, ?, ?, ?, ?> element : list) {
                edges.add(element);
            }
        }
        builder.append(edges);
        return builder.toString();
    }

    /**
     * Generates the hashCode for this object and makes it immutable.
     */
    @Override
    protected int hashCodeInternal() {
        return Objects.hash(this.lattice, this.nodes, this.incomingEdges, this.outgoingEdges);
    }

    /**
     * Generates a clone of this object by creating a new SystemDependenceGraph, that holds the same
     * nodes and edges. Nodes and Edges are immutable, so it is safe to reuse them instead of cloning.
     */
    @Override
    public LockableObject unlockedClone() {
        // First add all nodes.
        SystemDependenceGraph<S, ND, ED> result = new SystemDependenceGraph<>(this.lattice);
        for (Node<S, ND> node : this.nodes) {
            result.addNode(node);
        }
        // Every Edge is an incomingEdge for some node, so we only need to iterate these.
        for (Set<Edge<Node<S, ND>, Node<S, ND>, S, ED, ND>> edges : this.incomingEdges.values()) {
            for (Edge<Node<S, ND>, Node<S, ND>, S, ED, ND> edge : edges) {
                result.addEdge(edge);
            }
        }
        return result;
    }

    @Override
    public boolean lock() {
        return super.lock();
    }
}
