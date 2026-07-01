package de.tub.pes.syscir.analysis.abstraction_refinement.stepgraph;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.sc_model.expressions.Expression;

public class NodeInfo {

    private final Expression expression;
    private final AbstractedValue conditionValue;

    public NodeInfo(Expression expression, AbstractedValue conditionValue) {
        this.expression = expression;
        this.conditionValue = conditionValue;
    }

    public Expression getExpression() {
        return expression;
    }

    public AbstractedValue getConditionValue() {
        return conditionValue;
    }
}
