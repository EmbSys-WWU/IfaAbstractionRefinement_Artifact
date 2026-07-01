package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.ExplorationRecord;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;

/**
 * An ExplorationRecord that records all visited states and transitions in a transition graph.
 * 
 * This implementation is thread safe if and only if the according parameter is set to true at
 * construction.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of abstraction of the global state considered by this record
 * @param <ProcessStateT> the type of abstraction of the local state considered by this record
 * @param <InfoT> the type of additional transition information gathered by this record
 */
public class TransitionGraphRecord implements ExplorationRecord {

    public class Node {

        private final ConsideredState state;
        private final Set<Edge> edges;

        public Node(ConsideredState state) {
            this.state = state;
            this.edges = TransitionGraphRecord.this.threadSafe ? ConcurrentHashMap.newKeySet() : new LinkedHashSet<>();
        }

        public ConsideredState getState() {
            return this.state;
        }

        public Set<Edge> getEdges() {
            return Collections.unmodifiableSet(this.edges);
        }

        public int getId() {
            return TransitionGraphRecord.this.stateNames.get(this.state);
        }

        @Override
        public int hashCode() {
            return this.state.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TransitionGraphRecord.Node n)) {
                return false;
            }
            return this.state.equals(n.state) && this.edges.equals(n.edges);
        }

        @Override
        public String toString() {
            int nodeId = getId();
            StringBuilder result = new StringBuilder("Node ").append(nodeId);
            result.append(" ").append(this.state);
            result.append(" [");

            boolean first = true;
            for (Edge e : this.edges) {
                if (!first) {
                    result.append(", ");
                } else {
                    first = false;
                }
                result.append(e);
            }

            return result.append("]").toString();
        }

    }

    public class Edge {

        private final TransitionInformation info;
        private final Node source;
        private final Node target;

        public Edge(TransitionInformation info, Node source, Node target) {
            this.info = info;
            this.source = source;
            this.target = target;
        }

        @Override
        public int hashCode() {
            return (this.info.hashCode() * 31 + this.source.hashCode()) * 31 + this.target.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TransitionGraphRecord.Edge e)) {
                return false;
            }
            return Objects.equals(this.info, e.info) && this.source.equals(e.source) && this.target.equals(e.target);
        }

        public TransitionInformation getInfo() {
            return this.info;
        }

        public Node getSource() {
            return this.source;
        }

        public Node getTarget() {
            return this.target;
        }

        @Override
        public String toString() {
            int targetId = this.target.getId();
            StringBuilder result = new StringBuilder();
            result.append(targetId).append(" {").append(this.info).append("}");
            return result.toString();
        }
    }

    private boolean threadSafe;

    private Map<ConsideredState, Node> graph;

    private Map<ConsideredState, Integer> stateNames;
    private AtomicInteger nextId;

    /**
     * Constructs a new TransitionGraphRecord.
     * 
     * @param threadSafe whether or not a thread safe implementation is desired
     */
    public TransitionGraphRecord(boolean threadSafe) {
        this.threadSafe = threadSafe;
        this.graph = threadSafe ? new ConcurrentHashMap<>() : new LinkedHashMap<>();
        this.stateNames = threadSafe ? new ConcurrentHashMap<>() : new LinkedHashMap<>();
        this.nextId = new AtomicInteger(0);
    }

    /**
     * Returns the graph of all recorded states and transitions.
     *
     * @return graph of states and transitions
     */
    public Map<ConsideredState, Node> getGraph() {
        return Collections.unmodifiableMap(this.graph);
    }

    @Override
    public void explorationMade(ConsideredState from, ConsideredState to, TransitionInformation info) {
        this.stateNames.computeIfAbsent(from, s -> this.nextId.getAndIncrement());
        this.stateNames.computeIfAbsent(to, s -> this.nextId.getAndIncrement());

        Node fromNode = this.graph.computeIfAbsent(from, Node::new);
        Node toNode = this.graph.computeIfAbsent(to, Node::new);
        fromNode.edges.add(new Edge(info, fromNode, toNode));
    }

    @Override
    public String toString() {
        return this.graph.values().toString();
    }

}
