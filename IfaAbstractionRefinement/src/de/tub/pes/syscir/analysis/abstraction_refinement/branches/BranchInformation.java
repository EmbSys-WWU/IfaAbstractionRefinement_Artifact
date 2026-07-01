package de.tub.pes.syscir.analysis.abstraction_refinement.branches;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;

import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;

public class BranchInformation implements TransitionInformation {

    private final BranchInformationHandler system;
    private final Branching branching;
    private final Set<Branch> onBranches;
    private final Set<BranchInformation> predecessors;
    private final Set<BranchInformation> successors;

    public BranchInformation(BranchInformationHandler system, Branching branching) {
        this.system = system;
        this.branching = branching;
        this.onBranches = new LinkedHashSet<>();
        this.predecessors = new LinkedHashSet<>();
        this.successors = new LinkedHashSet<>();
    }

    public Branching getBranching() {
        return branching;
    }

    public Set<Branch> onBranches() {
        return onBranches;
    }

    public Set<BranchInformation> getPredecessors() {
        return predecessors;
    }

    public Set<BranchInformation> getSuccessors() {
        return successors;
    }

    public void addOnBranch(Branch branch) {
        if (this.onBranches.contains(branch))
            return;
        Branching branching = branch.getOrigin();
        int prevCount = 0;
        for (Branch otherBranch : branching.branchMarks()) {
            if (this.onBranches.contains(otherBranch))
                prevCount++;
        }
        this.onBranches.add(branch);
        if (prevCount == 0)
            branching.currentPartialDescendants++;
        if (prevCount == branching.branchCount() - 1)
            branching.currentPartialDescendants--;
        this.system.onUpdatePartialDescendantsCount(branching, this);
    }

    public void removeOnBranch(Branch branch) {
        if (!this.onBranches.remove(branch))
            return;
        Branching branching = branch.getOrigin();
        int newCount = 0;
        for (Branch otherBranch : branching.branchMarks()) {
            if (this.onBranches.contains(otherBranch))
                newCount++;
        }
        if (newCount == 0)
            branching.currentPartialDescendants--;
        if (newCount == branching.branchCount() - 1)
            branching.currentPartialDescendants++;
        this.system.onUpdatePartialDescendantsCount(branching, this);
    }

    public void destroy() {
        if (!this.successors.isEmpty() || (this.branching != null && (!this.branching.branchMarks().isEmpty()
                || this.branching.currentPartialDescendants != 0)))
            throw new AssertionError("Only deletion of unexplored nodes is supported");
        for (Branch branch : new ArrayList<>(this.onBranches))
            removeOnBranch(branch);
    }

    @Override
    public BranchInformation clone() {
        return this;
    }

    @Override
    public TransitionInformation compose(TransitionInformation otherInfo) {
        BranchInformation other = (BranchInformation) otherInfo;
        // validate that other is newly created
        // (it should not be used anymore after composition/absorption)
        if ((this.branching == null ? other.branching != null
                : other.branching == null || this.branching.getExpression() != other.branching.getExpression()))
            throw new AssertionError("Can not absorb node of branching by a different expression");
        // absorb other into this
        if (other.branching != null)
            this.branching.getConditionValues().addAll(other.branching.getConditionValues());
        for (BranchInformation absorbedPredecessor : other.predecessors) {
            this.predecessors.add(absorbedPredecessor);
            absorbedPredecessor.successors.remove(other);
            absorbedPredecessor.successors.add(this);
        }
        Collection<Branch> propagateOnBranches = new ArrayList<>(other.onBranches());
        other.destroy();
        Set<BranchInformation> visited = new HashSet<>();
        Queue<BranchInformation> propagationWorklist = new ArrayDeque<>();
        propagationWorklist.add(this);
        visited.add(this);
        while (!propagationWorklist.isEmpty()) {
            BranchInformation node = propagationWorklist.poll();
            propagateOnBranches.forEach(node::addOnBranch);
            for (BranchInformation successor : node.successors) {
                if (visited.add(successor))
                    propagationWorklist.add(successor);
            }
        }
        return this;
    }

}
