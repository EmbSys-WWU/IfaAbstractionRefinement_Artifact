package de.tub.pes.syscir.analysis.dependencies;

import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;

public class ControlDependenceAnalysis {

    public static record PostDominatorTree<NodeType extends CfgNode<NodeType, ?, ?, ?>> (Set<NodeType> roots,
            Map<NodeType, NodeType> parents, Map<NodeType, Set<NodeType>> children) {}

    private static <NodeType extends CfgNode<NodeType, ?, ?, ?>> BinaryOperator<Set<NodeType>> intersection() {
        return new BinaryOperator<> () {

            @Override
            public Set<NodeType> apply(Set<NodeType> t, Set<NodeType> u) {
                if (t == null) {
                    return u;
                }

                if (u == null) {
                    return t;
                }

                Set<NodeType> result = new LinkedHashSet<>(t);
                result.retainAll(u);
                return result;
            }

        };
    }

    // possible algorithm: https://web.cse.ohio-state.edu/~rountev.1/788/papers/cooper-spe01.pdf
    public static <NodeType extends CfgNode<NodeType, ?, ?, ?>> PostDominatorTree<NodeType> getPostDominatorTree(
            ControlFlowGraph<NodeType, ?, ?, ?> graph) {
        Map<NodeType, Set<NodeType>> postDominators = new LinkedHashMap<>();
        Set<NodeType> orderedNodes = new LinkedHashSet<>(graph.getExitNodes());

        Deque<NodeType> worklist = new LinkedList<>(graph.getExitNodes());
        while (!worklist.isEmpty()) {
            NodeType node = worklist.poll();
            for (CfgEdge<? extends NodeType, ?> edge : node.getIncomingEdges()) {
                if (orderedNodes.add(edge.getSource())) {
                    worklist.add(edge.getSource());
                }
            }
        }

        boolean changed = true;
        while (changed) {
            changed = false;

            for (NodeType node : orderedNodes) {
                Set<NodeType> updatedPostDoms = new LinkedHashSet<>(List.of(node));
                // Funny: Changing the lambda to CfgEdge::getTarget triggers a compiler bug, throwing an error.
                Set<NodeType> intersectedSuccessorPostDoms = node.getOutgoingEdges().stream()
                        .map(edge -> edge.getTarget())
                        .map(postDominators::get).reduce(null, intersection());
                if (intersectedSuccessorPostDoms != null) {
                    updatedPostDoms.addAll(intersectedSuccessorPostDoms);
                } // null means no successors, thus no post-dominators

                Set<NodeType> currentPostDoms = postDominators.get(node);
                if (!Objects.equals(currentPostDoms, updatedPostDoms)) {
                    postDominators.put(node, updatedPostDoms);
                    changed = true;
                }
            }
        }
        // post-dominators found, must now find direkt post-dominators

        // first, flip mapping and filter to strict post domination
        Map<NodeType, Set<NodeType>> strictlyPostDominates = new LinkedHashMap<>();
        for (Map.Entry<NodeType, Set<NodeType>> domination : postDominators.entrySet()) {
            NodeType dominated = domination.getKey();
            for (NodeType dominator : domination.getValue()) {
                if (dominated == dominator) {
                    // we want strict post domination
                    continue;
                }
                strictlyPostDominates.computeIfAbsent(dominator, x -> new LinkedHashSet<>()).add(dominated);
            }
        }

        Map<NodeType, Set<NodeType>> directlyPostDominates = new LinkedHashMap<>();
        Map<NodeType, NodeType> directlyPostDominated = new LinkedHashMap<>();
        // then, refine to direct post domination
        for (NodeType current : postDominators.keySet()) {
            Set<NodeType> strictlyPostDominated =
                    new LinkedHashSet<>(strictlyPostDominates.getOrDefault(current, Set.of()));
            List<Set<NodeType>> transitiveDomination = strictlyPostDominated.stream()
                    .map(dominated -> strictlyPostDominates.getOrDefault(dominated, Set.of())).toList();
            for (Set<NodeType> transitivelyDominated : transitiveDomination) {
                strictlyPostDominated.removeAll(transitivelyDominated);
            }
            directlyPostDominates.put(current, strictlyPostDominated);
            for (NodeType dominated : strictlyPostDominated) {
                directlyPostDominated.put(dominated, current);
            }
        }

        Set<NodeType> roots = new LinkedHashSet<>(graph.getExitNodes());
        return new PostDominatorTree<>(roots, directlyPostDominated, directlyPostDominates);
    }

}
