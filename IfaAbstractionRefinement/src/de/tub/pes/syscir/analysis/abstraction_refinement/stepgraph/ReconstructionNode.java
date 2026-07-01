package de.tub.pes.syscir.analysis.abstraction_refinement.stepgraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

public class ReconstructionNode {

    private final NodeInfo info;
    final Collection<ReconstructionNode> predecessors;
    final Collection<ReconstructionNode> successors;
    final Collection<ReconstructionNode> alternatives;

    public ReconstructionNode(NodeInfo info) {
        this.info = info;
        this.predecessors = new LinkedHashSet<>();
        this.successors = new LinkedHashSet<>();
        this.alternatives = new LinkedHashSet<>();
    }

    public NodeInfo getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "Node <" + (info == null ? "" : info.getExpression()) + ">";
    }

    public Collection<ReconstructionNode> getPredecessors() {
        return predecessors;
    }

    public Collection<ReconstructionNode> getSuccessors() {
        return successors;
    }

    /**
     * Returns a set of the leaf nodes reachable from this node. Does not consider
     * alternatives, thus only works properly after alternatives resolution
     * ({@link ReconstructionContext#resolveAlternatives()})
     */
    public Set<ReconstructionNode> reachableLeaves() {
        Set<ReconstructionNode> seen = new HashSet<>();
        Set<ReconstructionNode> leaves = new HashSet<>();
        Stack<ReconstructionNode> remaining = new Stack<>();
        seen.add(this);
        remaining.push(this);
        while (!remaining.isEmpty()) {
            ReconstructionNode node = remaining.pop();
            Collection<ReconstructionNode> successors = node.getSuccessors();
            if (successors.isEmpty()) {
                leaves.add(node);
                continue;
            }
            for (ReconstructionNode successor : successors) {
                if (seen.add(successor))
                    remaining.push(successor);
            }
        }
        return leaves;
    }
}
