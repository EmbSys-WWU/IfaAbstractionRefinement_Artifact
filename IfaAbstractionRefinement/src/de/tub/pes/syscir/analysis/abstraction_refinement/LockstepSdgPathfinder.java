package de.tub.pes.syscir.analysis.abstraction_refinement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tomatengames.util.PathfinderUtil;
import de.tomatengames.util.StringUtil;
import de.tub.pes.syscir.analysis.dependencies.Sdg;
import de.tub.pes.syscir.analysis.dependencies.SdgEdge;
import de.tub.pes.syscir.analysis.dependencies.SdgNode;
import de.tub.pes.syscir.analysis.dependencies.SdgNode.SdgNodeId;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;

/**
 * Lockstep SDG pathfinding used for non-cumulative information flow refinement.
 * 
 * @author Lukas Ernst
 */
public class LockstepSdgPathfinder {

    public static class LockstepSdgNode extends PathfinderUtil.PathNode {

        private final LockstepSdgNode previous;
        public final String pdgNodeId;
        public final List<Set<SdgNodeId>> sdgPositions;

        protected LockstepSdgNode(LockstepSdgNode previous, double cost, String pdgNodeId,
                List<Set<SdgNodeId>> sdgPositions) {
            super(cost);
            this.previous = previous;
            this.pdgNodeId = pdgNodeId;
            this.sdgPositions = sdgPositions;
        }

    }

    public static class LockstepSdgPath {
        private final List<LockstepSdgNode> path;

        public LockstepSdgPath(List<LockstepSdgNode> path) {
            this.path = path;
        }

        public List<LockstepSdgNode> nodes() {
            return path;
        }

        public String toSimpleString() {
            return StringUtil.join(this.path, node -> node.pdgNodeId, "->");
        }

        public String toVerboseString() {
            return StringUtil.join(this.path,
                    node -> node.pdgNodeId + " {" + StringUtil.join(node.sdgPositions, Object::toString, "/") + "}",
                    " -> ");
        }

        @Override
        public String toString() {
            return toSimpleString();
        }
    }

    public static Set<SdgNodeId> findSdgNodes(Sdg sdg, String pdgId) {
        Set<SdgNodeId> out = new LinkedHashSet<>();
        for (SdgNodeId sdgId : sdg.getNodes().keySet())
            if (sdgId.id().toString().equals(pdgId))
                out.add(sdgId);
        return out;
    }

    public static LockstepSdgPath findPath(String source, String target, List<Sdg> sdgs) {

        LockstepSdgNode startNode =
                new LockstepSdgNode(null, 0, source, sdgs.stream().map(sdg -> findSdgNodes(sdg, source)).toList());

        if (startNode.sdgPositions.stream().anyMatch(Set::isEmpty)) {
            // some SDG does not contain any source node
            // (also, starting with no positions the SDG would
            // not be able to follow anywhere)
            return null;
        }

        LockstepSdgNode goalNode = PathfinderUtil.find(startNode, new PathfinderUtil.World<LockstepSdgNode>() {
            @Override
            public double estimateRemainingCost(LockstepSdgNode node) {
                return 0;
            }
            @Override
            public void insertNeighbors(LockstepSdgNode node, Collection<LockstepSdgNode> neighbors) {
                // get candidates based on first sdg
                Sdg baseSdg = sdgs.get(0);
                Set<SdgNodeId> basePositions = node.sdgPositions.get(0);
                Set<PdgNodeId> nextCandidates = new LinkedHashSet<>();
                for (SdgNodeId basePosition : basePositions) {
                    SdgNode baseNode = baseSdg.getNodes().get(basePosition);
                    for (SdgEdge edge : baseNode.getOutgoing())
                        nextCandidates.add(edge.getTarget().getId().id());
                }
                // get actual followers of candidates, discarding the candidate if any sdg can not follow
                candidate_loop: for (PdgNodeId candidate : nextCandidates) {
                    List<Set<SdgNodeId>> nextSdgsPositions = new ArrayList<>();
                    for (int i = 0; i < sdgs.size(); i++) {
                        Sdg sdg = sdgs.get(i);
                        Set<SdgNodeId> currentPositions = node.sdgPositions.get(i);
                        Set<SdgNodeId> nextPositions = new LinkedHashSet<>();
                        for (SdgNodeId currentPosition : currentPositions) {
                            SdgNode currentNode = sdg.getNodes().get(currentPosition);
                            for (SdgEdge edge : currentNode.getOutgoing()) {
                                SdgNode nextNode = edge.getTarget();
                                if (nextNode.getId().id().equals(candidate))
                                    nextPositions.add(nextNode.getId());
                            }
                        }
                        if (nextPositions.isEmpty())
                            continue candidate_loop; // sdg i can not follow, discard candidate
                        nextSdgsPositions.add(nextPositions);
                    }
                    // all sdgs can follow, add follower node
                    neighbors.add(new LockstepSdgNode(node, 1, candidate.toString(), nextSdgsPositions));
                }
            }
            @Override
            public boolean isGoal(LockstepSdgNode node) {
                return node.pdgNodeId.equals(target);
            }
            @Override
            public int positionHash(LockstepSdgNode node) {
                return node.sdgPositions.hashCode();
            }
            @Override
            public boolean positionEqual(LockstepSdgNode node1, LockstepSdgNode node2) {
                return node1.sdgPositions.equals(node2.sdgPositions);
            }

        });

        return goalNode == null ? null : new LockstepSdgPath(PathfinderUtil.listPath(goalNode, node -> node.previous));

    }

    /**
     * Finds a concrete SdgPath in the specified Sdg from a lockstep/common path.
     * The specified Sdg must have been at the specified index of the lockstep
     * pathfinding operation which the specified lockstep path originates from.
     */
    public static SdgPathfinder.SdgPath backtracePath(LockstepSdgPath path, Sdg sdg, int sdgIndex) {
        Iterator<LockstepSdgNode> pathIt = path.nodes().reversed().iterator();
        SdgNode lastNode = sdg.getNodes().get(pathIt.next().sdgPositions.get(sdgIndex).stream().findFirst().get());
        SdgNode currentFirstNode = lastNode;
        List<SdgEdge> edges = new LinkedList<>();
        path_loop: while (pathIt.hasNext()) {
            LockstepSdgNode pathNode = pathIt.next();
            for (SdgEdge edge : currentFirstNode.getIncoming()) {
                if (pathNode.sdgPositions.get(sdgIndex).contains(edge.getSource().getId())) {
                    edges.addFirst(edge);
                    currentFirstNode = edge.getSource();
                    continue path_loop;
                }
            }
            throw new AssertionError("Missing applicable predecessor");
        }
        return new SdgPathfinder.SdgPath(currentFirstNode, edges);
    }

}
