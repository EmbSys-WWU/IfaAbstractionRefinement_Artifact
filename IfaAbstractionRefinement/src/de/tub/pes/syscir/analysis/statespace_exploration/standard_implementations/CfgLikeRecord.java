package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.tub.pes.syscir.analysis.dependencies.CfgEdge;
import de.tub.pes.syscir.analysis.dependencies.CfgNode;
import de.tub.pes.syscir.analysis.dependencies.ControlFlowGraph;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.ExplorationRecord;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;

public class CfgLikeRecord implements ExplorationRecord {

    private static record Transition(
            ConsideredState fromState, ConsideredState toState, TransitionInformation information) {}

    public class Node implements CfgNode<Node, Edge, Variable<?, ?>, TransitionInformation> {

        private final int id;

        private final ConsideredState fromState;
        private final ConsideredState toState;
        private final TransitionInformation information;

        private volatile Map<Variable<?, ?>, AbstractedValue> read;
        private volatile Map<Variable<?, ?>, AbstractedValue> written;

        private final Set<Edge> incoming;
        private final Set<Edge> outgoing;

        public Node(int id, Transition transition) {
            this(id, transition.fromState(), transition.toState(), transition.information());
        }

        public Node(int id, ConsideredState fromState, ConsideredState toState, TransitionInformation information) {
            this.id = id;

            this.fromState = fromState;
            this.toState = toState;
            this.information = information;

            this.incoming = createSet();
            this.outgoing = createSet();
        }

        public int getId() {
            return this.id;
        }

        public ConsideredState getFromState() {
            return this.fromState;
        }

        public ConsideredState getToState() {
            return this.toState;
        }

        @Override
        public TransitionInformation getTransitionInformation() {
            return this.information;
        }

        public Map<Variable<?, ?>, AbstractedValue> getVariablesRead() {
            // TODO: not strictly correct for initial node, where system parameters are "written"
            if (this.read == null) {
                synchronized (this) {
                    if (this.read == null) {
                        this.read = this.information == null ? createMap()
                                : createMap(CfgLikeRecord.this.variablesReadGetter.apply(this.information));
                        this.read = Collections.unmodifiableMap(this.read);
                    }
                }
            }
            return this.read;
        }

        public Map<Variable<?, ?>, AbstractedValue> getVariablesWritten() {
            // TODO: not strictly correct for initial node, where system parameters are "written"
            if (this.written == null) {
                synchronized (this) {
                    if (this.written == null) {
                        this.written = this.information == null ? createMap()
                                : createMap(CfgLikeRecord.this.variablesWrittenGetter.apply(this.information));
                        this.written = Collections.unmodifiableMap(this.written);
                    }
                }
            }
            return this.written;
        }

        @Override
        public AbstractedValue isVariableRead(Variable<?, ?> var) {
            return getVariablesRead().getOrDefault(var, BinaryAbstractedValue.of(false));
        }

        @Override
        public AbstractedValue isVariableWritten(Variable<?, ?> var) {
            return getVariablesWritten().getOrDefault(var, BinaryAbstractedValue.of(false));
        }

        @Override
        public Set<Variable<?, ?>> getPossiblyWrittenVariables() {
            return getVariablesWritten().keySet();
        }

        @Override
        public Set<CfgLikeRecord.Edge> getIncomingEdges() {
            return Collections.unmodifiableSet(this.incoming);
        }

        @Override
        public Set<CfgLikeRecord.Edge> getOutgoingEdges() {
            return Collections.unmodifiableSet(this.outgoing);
        }

        @Override
        public String toString() {
            return toString(true);
        }

        public String toString(boolean includeInfo) {
            Integer fromStateId = CfgLikeRecord.this.states.getOrDefault(this.fromState, null);
            Integer toStateId = CfgLikeRecord.this.states.getOrDefault(this.toState, null);
            String infoString = includeInfo ? " with " + this.information : "";
            return "Node " + this.id + " " + "(" + fromStateId + " -> " + toStateId + infoString + "), outgoing "
            + (this.outgoing.isEmpty() ? "none"
                    : this.outgoing.stream().map(Edge::getTarget).map(Node::getId).map(String::valueOf)
                    .collect(Collectors.joining(", ")));
        }

    }

    public class Edge implements CfgEdge<Node, Edge> {

        private final Node source;
        private final Node target;

        public Edge(Node source, Node target) {
            this.source = source;
            this.target = target;

            source.outgoing.add(this);
            target.incoming.add(this);
        }

        @Override
        public Node getSource() {
            return this.source;
        }

        @Override
        public Node getTarget() {
            return this.target;
        }

    }

    private boolean threadSafe;
    private Function<TransitionInformation, Map<Variable<?, ?>, ? extends AbstractedValue>> variablesReadGetter;
    private Function<TransitionInformation, Map<Variable<?, ?>, ? extends AbstractedValue>> variablesWrittenGetter;

    private AtomicInteger nextNodeId;
    private AtomicInteger nextStateId;

    private Node entryNode;
    private Map<Transition, Node> nodes;
    private Map<ConsideredState, Set<Node>> nodesReachingThisState;
    private Map<ConsideredState, Set<Node>> nodesLeavingThisState;
    private Map<ConsideredState, Integer> states;

    public CfgLikeRecord(boolean threadSafe,
            Function<TransitionInformation, Map<Variable<?, ?>, ? extends AbstractedValue>> variablesReadGetter,
            Function<TransitionInformation, Map<Variable<?, ?>, ? extends AbstractedValue>> variablesWrittenGetter,
            ConsideredState initialState) {
        this.threadSafe = threadSafe;
        this.variablesReadGetter = Objects.requireNonNull(variablesReadGetter);
        this.variablesWrittenGetter = Objects.requireNonNull(variablesWrittenGetter);

        this.nextNodeId = new AtomicInteger();
        this.nextStateId = new AtomicInteger();

        Objects.requireNonNull(initialState);
        this.entryNode = new Node(this.nextNodeId.getAndIncrement(), null, initialState, null);
        this.nodes = createMap();
        this.nodes.put(null, this.entryNode);
        this.nodesReachingThisState = createMap();
        this.nodesReachingThisState.computeIfAbsent(initialState, x -> createSet()).add(this.entryNode);
        this.nodesLeavingThisState = createMap();
        this.states = createMap();
        this.states.put(initialState, this.nextStateId.getAndIncrement());
    }

    @Override
    public void explorationMade(ConsideredState from, ConsideredState to, TransitionInformation info) {
        this.states.computeIfAbsent(from, x -> this.nextStateId.getAndIncrement());
        this.states.computeIfAbsent(to, x -> this.nextStateId.getAndIncrement());

        this.nodes.computeIfAbsent(new Transition(from, to, info), t -> {
            Set<Node> baseSourceNodes = this.nodesReachingThisState.get(from);
            Set<Node> baseTargetNodes = this.nodesLeavingThisState.getOrDefault(to, Set.of());
            Node newNode = new Node(this.nextNodeId.getAndIncrement(), from, to, info);
            this.nodesReachingThisState.computeIfAbsent(to, x -> createSet()).add(newNode);
            this.nodesLeavingThisState.computeIfAbsent(from, x -> createSet()).add(newNode);
            for (Node sourceNode : baseSourceNodes) {
                new Edge(sourceNode, newNode);
            }
            for (Node targetNode : baseTargetNodes) {
                new Edge(newNode, targetNode);
            }
            return newNode;
        });
    }

    private <X, Y> Map<X, Y> createMap() {
        return this.threadSafe ? new ConcurrentHashMap<>() : new LinkedHashMap<>();
    }

    private <X, Y> Map<X, Y> createMap(Map<? extends X, ? extends Y> copyOf) {
        return this.threadSafe ? new ConcurrentHashMap<>(copyOf) : new LinkedHashMap<>(copyOf);
    }

    private <X> Set<X> createSet() {
        return this.threadSafe ? Collections.newSetFromMap(new ConcurrentHashMap<>()) : new LinkedHashSet<>();
    }

    // private <X> Set<X> createSet(Set<? extends X> copyOf) {
    // if (this.threadSafe) {
    // Set<X> result = Collections.newSetFromMap(new ConcurrentHashMap<>(copyOf.size()));
    // result.addAll(copyOf);
    // }
    // return new LinkedHashSet<>(copyOf);
    // }

    public Node getRoot() {
        return this.entryNode;
    }

    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(this.nodes.values());
    }

    public ControlFlowGraph<Node, Edge, Variable<?, ?>, TransitionInformation> toCfg() {
        return new ControlFlowGraph<>(this.entryNode, this.nodes.values().stream().filter(n -> n.outgoing.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    @Override
    public String toString() {
        return this.states.entrySet().stream().map(entry -> "State " + entry.getValue() + " " + entry.getKey())
                .collect(Collectors.joining(", ", "[", "]"))
                + ", "
                + this.nodes.values().stream().map(n -> n.toString(false)).collect(Collectors.joining(", ", "[", "]"));
    }

}
