package de.tub.pes.syscir.analysis.abstraction_refinement.branches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.sc_model.expressions.Expression;

public class Branching {

    private final Expression expression;
    private final List<AbstractedValue> conditions;
    private final int branchesCount;
    private final Collection<Branch> branches;
    public int currentPartialDescendants;

    public Branching(Expression expression, AbstractedValue condition, int branchesCount) {
        this.expression = expression;
        this.conditions = new ArrayList<>();
        this.conditions.add(condition);
        this.branchesCount = branchesCount;
        this.branches = new ArrayList<>();
        this.currentPartialDescendants = 0;
    }

    public Expression getExpression() {
        return expression;
    }

    public List<AbstractedValue> getConditionValues() {
        return conditions;
    }

    public int branchCount() {
        return branchesCount;
    }

    public Collection<Branch> branchMarks() {
        return this.branches;
    }

    public Branch addBranchMark() {
        Branch branch = new Branch(this);
        this.branches.add(branch);
        return branch;
    }

}
