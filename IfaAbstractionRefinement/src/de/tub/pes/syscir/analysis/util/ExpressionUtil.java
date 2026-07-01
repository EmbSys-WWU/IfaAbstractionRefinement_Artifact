package de.tub.pes.syscir.analysis.util;

import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.ForLoopExpression;

public class ExpressionUtil {

    /**
     * Returns the child of an expression that is evaluated at the given index. This might differ from
     * the result of {@link Expression#getChild(int)} if the evaluation order does not match the
     * iteration order of {@link Expression#crawlDeeper()}.
     * <p>
     * This Implementation matches the result of {@link Expression#getChild(int)}, except for the
     * following exceptions. In {@link ForLoopExpression}s, the initializer comes first, then the
     * condition, then all elements of the loop body and finally the iterator.
     * 
     * @param expression an expression
     * @param index an index
     * @return the child of the expression evaluated at that index
     */
    public static Expression getChildByIndex(Expression expression, int index) {
        if (expression instanceof ForLoopExpression ex) {
            if (index == 0) {
                return ex.getInitializer();
            } else if (index == 1) {
                return ex.getCondition();
            } else if ((index - 2 < ex.getLoopBody().size())) {
                return ex.getLoopBody().get(index - 2);
            } else if (index - 2 == ex.getLoopBody().size()) {
                return ex.getIterator();
            } else {
                throw new IndexOutOfBoundsException(index);
            }
        }

        return expression.getChild(index);
        // TODO: add special cases where necessary
    }

}
