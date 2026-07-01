package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tomatengames.util.StringUtil;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import java.util.Collection;
import java.util.List;

/**
 * A Sources implementation that keeps assignment expressions (their right sides) as sources.
 * 
 * @author Lukas Ernst
 */
public class SourceAssignments extends SourceExpressions {
    
    public static SourcesType TYPE = () -> new SourceAssignments();
    
    public static boolean isAssignmentValue(Expression expression) {
        Expression parent = expression.getParent();
        if (parent instanceof BinaryExpression be) {
            return be.getRight() == expression && StringUtil.count(be.getOp(), '=') == 1 && !be.getOp().contains("<")
                    && !be.getOp().contains(">") && !be.getOp().contains("!");
        } else if (parent instanceof SCVariableDeclarationExpression de) {
            for (Expression initialValue : de.getInitialValues()) {
                if (expression == initialValue) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }
    
    public SourceAssignments() {
        super();
    }
    
    public SourceAssignments(Collection<ExpressionLocation> expressionLocations) {
        super(expressionLocations);
    }
    
    @Override
    public SourcesType getType() {
        return TYPE;
    }
    
    @Override
    public boolean tracks(Expression expression, StackTraceView stack, List<Integer> location) {
        if (!isAssignmentValue(expression)) {
            return true;
        }
        return super.tracks(expression, stack, location);
    }
    
    @Override
    public Sources copy() {
        return new SourceAssignments(this.expressionLocations);
    }
}
