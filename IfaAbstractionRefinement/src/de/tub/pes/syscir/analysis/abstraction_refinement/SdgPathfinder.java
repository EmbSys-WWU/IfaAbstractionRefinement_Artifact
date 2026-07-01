package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tomatengames.util.PathfinderUtil;
import de.tub.pes.syscir.analysis.abstraction_refinement.InformationFlowPolicy.Entry;
import de.tub.pes.syscir.analysis.dependencies.Sdg;
import de.tub.pes.syscir.analysis.dependencies.SdgEdge;
import de.tub.pes.syscir.analysis.dependencies.SdgNode;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * The regular SDG pathfinding methods used in either simple checking of a policy or cumulative
 * information flow refinement.
 * 
 * @author Lukas Ernst
 */
public class SdgPathfinder {

    private static List<SdgNode> fromIDText(Sdg sdg, String id) {
        List<SdgNode> out = new ArrayList<>();
        for (SdgNode node : sdg.getNodes().values()) {
            if (node.getId().id().toString().equals(id)) {
                out.add(node);
            }
        }
        return out;
    }

    public static class SdgPathNode extends PathfinderUtil.PathNode {

        public final int index;
        public final SdgPathNode previous;
        public final SdgEdge sdgedge;
        public final SdgNode sdgnode;

        public SdgPathNode(int index, SdgPathNode previous, SdgEdge sdgedge, SdgNode sdgnode) {
            super(1);
            this.index = index;
            this.previous = previous;
            this.sdgedge = sdgedge;
            this.sdgnode = sdgnode;
        }

        public boolean isInitial() {
            return this.index == -1;
        }

    }

    public static class SdgPath {

        private final SdgNode startNode;
        private final List<SdgEdge> edges;

        public SdgPath(SdgNode startNode, List<SdgEdge> edges) {
            this.startNode = startNode;
            this.edges = edges;
        }

        public List<SdgEdge> edges() {
            return this.edges;
        }

        public List<SdgNode> nodes() {
            List<SdgNode> nodes = new ArrayList<>();
            nodes.add(this.startNode);
            for (SdgEdge edge : this.edges) {
                nodes.add(edge.getTarget());
            }
            return nodes;
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            out.append(this.startNode);
            for (SdgEdge edge : this.edges) {
                out.append("-" + edge.getType() + "->").append(edge.getTarget());
            }
            return out.toString();
        }
    }

    public static SdgPath findPath(List<SdgNode> source, Predicate<SdgPathNode> target,
            IndexedNodePredicate indexCondition) {
        SdgPathNode goal = PathfinderUtil.find(new SdgPathNode(-1, null, null, null), new PathfinderUtil.World<>() {

            @Override
            public double estimateRemainingCost(SdgPathNode node) {
                return 0;
            }

            @Override
            public void insertNeighbors(SdgPathNode node, Collection<SdgPathNode> neighbors) {
                if (node.isInitial()) {
                    for (SdgNode initial : source) {
                        neighbors.add(new SdgPathNode(0, null, null, initial));
                    }
                } else {
                    for (SdgEdge next : node.sdgnode.getOutgoing()) {
                        if (indexCondition == null || indexCondition.accept(next.getTarget(), node.index + 1)) {
                            neighbors.add(new SdgPathNode(node.index + 1, node, next, next.getTarget()));
                        }
                    }
                }
            }

            @Override
            public boolean isGoal(SdgPathNode node) {
                if (node.isInitial()) {
                    return false;
                }
                return target.test(node);
            }

            @Override
            public int positionHash(SdgPathNode node) {
                if (node.isInitial()) {
                    return 0;
                }
                int hash = node.sdgnode.getId().hashCode();
                if (indexCondition != null) {
                    hash = hash * 31 + node.index;
                }
                return hash;
            }

            @Override
            public boolean positionEqual(SdgPathNode node1, SdgPathNode node2) {
                if (indexCondition != null && node1.index != node2.index) {
                    return false;
                }
                if (node1.isInitial() || node2.isInitial()) {
                    return node1.isInitial() && node2.isInitial();
                }
                return node1.sdgnode.getId().equals(node2.sdgnode.getId());
            }

        });

        if (goal == null) {
            return null;
        }
        if (goal.sdgedge == null) {
            return new SdgPath(goal.sdgnode, List.of());
        }

        ArrayList<SdgEdge> pathEdges = new ArrayList<>();
        SdgPathNode node = goal;
        do {
            pathEdges.add(node.sdgedge);
        } while ((node = node.previous).sdgedge != null);
        Collections.reverse(pathEdges);
        return new SdgPath(node.sdgnode, pathEdges);
    }

    public static interface IndexedNodePredicate {

        boolean accept(SdgNode node, int index);
    }

    public static SdgPath findPath(Sdg sdg, Entry entry) {
        List<SdgNode> sourceNodes = fromIDText(sdg, entry.getSource());
        List<SdgNode> targetNodes = fromIDText(sdg, entry.getTarget());
        if (sourceNodes.isEmpty()) {
            throw new NoSuchElementException(
                    "Node " + entry.getSource() + " from policy entry " + entry + " not present in SDG");
        }
        if (targetNodes.isEmpty()) {
            throw new NoSuchElementException(
                    "Node " + entry.getSource() + " from policy entry " + entry + " not present in SDG");
        }
        return findPath(sourceNodes, node -> node.sdgnode.getId().id().toString().equals(entry.getTarget()), null);
    }

    public static SdgPath refindPath(Sdg sdg, List<PdgNodeId> pathSignature) {
        List<SdgNode> sourceNodes = sdg.getNodes().values().stream()
                .filter(node -> node.getId().id().equals(pathSignature.getFirst())).toList();
        return findPath(sourceNodes, node -> node.index == pathSignature.size() - 1,
                (node, index) -> node.getId().id().equals(pathSignature.get(index)));
    }

}
