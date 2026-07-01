package de.tub.pes.syscir.analysis.statespace_exploration;

import de.tub.pes.syscir.analysis.util.ExpressionUtil;
import de.tub.pes.syscir.analysis.util.HashCachingLockableObject;
import de.tub.pes.syscir.analysis.util.WrappedSCFunction;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Class representing a location in the evaluatio.
 * 
 * The location targets an {@link Expression} in the code, not necessarily a complete line or
 * statement.
 * <p>
 * This location starts out as mutable but can be locked to be immutable by invoking the method
 * {@link #lock()}. When locked, any attempt to directly modify a state results in an
 * {@link IllegalArgumentException}. Note that deep immutability cannot be completely guaranteed and
 * that any successful modification after being locked results in undefined behavior.
 * 
 * @author Jonas Becker-Kupczok
 *
 */
public class EvaluationLocation extends HashCachingLockableObject {

    protected static class LocationView extends EvaluationLocation {

        public LocationView(EvaluationLocation of) {
            super(of);
            lock();
        }

    }

    private WrappedSCFunction function;

    // index of current expression in the top-level list, then index of the current sub-expression
    // within that, etc
    private List<Integer> expressionIndices;


    /**
     * Creates a new EvaluationLocation, specifying the location of the evaluation.
     * <p>
     * The location is specified by the expressionIndices: The first element of the list specifies the
     * expression in the body of the given function. The second element specifies the sub expression
     * within that, and so on.
     * <p>
     * Note that the order of expressions as targeted by the expression indices must not necessarily
     * align with the order in {@link Expression#crawlDeeper()}, but that in which they need to be
     * evaluated. Some expressions may also introduce gaps in the ordering, indicating that evaluation
     * should first return to the parent before (potentially) continuing with the next child.
     * <p>
     * The parameters are stored in the new object as is, without being copied. The list must be deeply
     * modifiable. Care must be taken not to modify them externally, especially after this state has
     * been locked.
     * 
     * @param function the SysCIR function in which this location is found
     * @param expressionIndices the indices of the expression and sub expressions where this location is
     *        found
     */
    public EvaluationLocation(WrappedSCFunction function, List<Integer> expressionIndices) {
        this.function = Objects.requireNonNull(function);
        this.expressionIndices = Objects.requireNonNull(expressionIndices);
    }

    /**
     * Creates a deep, unlocked copy of the given {@link EvaluationLocation}.
     * 
     * If retainedHashCode is true, the cached hashCode of the copy is initialised to the cached
     * hashCode of the original, if any.
     * 
     * @param copyOf the location to copy
     * @param retainHashCode whether to initialise the cached hashCode with that of the original
     */
    protected EvaluationLocation(EvaluationLocation copyOf, boolean retainHashCode) {
        super(copyOf);

        this.function = copyOf.function;
        this.expressionIndices = new ArrayList<>(copyOf.expressionIndices);

        if (!retainHashCode) {
            resetHashCode();
        }
    }

    private EvaluationLocation(EvaluationLocation viewOf) {
        super(viewOf);

        this.function = viewOf.function;
        this.expressionIndices = viewOf.expressionIndices;
    }

    /**
     * Returns the SysCIR function where this location is found.
     *
     * @return the SysCIR function
     */
    public WrappedSCFunction getFunction() {
        return this.function;
    }

    /**
     * Returns the indices of the expression and sub expressions where this location is found.
     * 
     * The returned list is mutable iff this location has not been locked.
     *
     * @return the indices of the expression and sub expressions where this location is found
     */
    public List<Integer> getExpressionIndices() {
        resetHashCode();
        return isLocked() ? Collections.unmodifiableList(this.expressionIndices) : this.expressionIndices;
    }

    /**
     * Returns the next expression targeted by this evaluation location.
     * 
     * @return the next expression
     */
    public Expression getNextExpression() {
        return getNextExpression(0);
    }

    /**
     * Returns the expression the given number of levels above the expression targetted by this
     * evaluation location.
     * 
     * @param levelsAbove how many levels above the targetted expression
     * @return the expression that many levels above the targetted expression
     */
    public Expression getNextExpression(int levelsAbove) {
        if (this.function.getBody().isEmpty()) {
            return null;
        }

        List<Integer> indices = this.expressionIndices.subList(0, this.expressionIndices.size() - levelsAbove);
        Expression current = this.function.getBody().get(indices.get(0));
        for (int i = 1; i < indices.size(); i++) {
            current = ExpressionUtil.getChildByIndex(current, indices.get(i));
        }
        return current;
    }

    @Override
    public boolean lock() {
        resetHashCode();
        return super.lock();
    }

    @Override
    public void resetHashCode() {
        super.resetHashCode();
    }

    @Override
    public EvaluationLocation unlockedClone() {
        return new EvaluationLocation(this, true);
    }

    @Override
    protected int hashCodeInternal() {
        return this.expressionIndices.hashCode() * 31 + this.function.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EvaluationLocation el)) {
            return false;
        }
        return this.expressionIndices.equals(el.expressionIndices) && this.function.equals(el.function);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append(this.function.getName()).append(":");
        builder.append(this.expressionIndices);
        return builder.toString();
    }

}
