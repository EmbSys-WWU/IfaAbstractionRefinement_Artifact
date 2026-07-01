package de.tub.pes.syscir.analysis.statespace_exploration;

import de.tub.pes.syscir.analysis.util.CollectionUtil;
import de.tub.pes.syscir.analysis.util.WrappedSCFunction;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.List;
import java.util.Objects;

/**
 * Class representing a location in the evaluation augmented with the values of already evaluated
 * expressions.
 * <p>
 * This context starts out as mutable but can be locked to be immutable by invoking the method
 * {@link #lock()}. When locked, any attempt to directly modify a state results in an
 * {@link IllegalArgumentException}. Note that deep immutability cannot be completely guaranteed and
 * that any successful modification after being locked results in undefined behavior.
 * 
 * @author Jonas Becker-Kupczok
 *
 */
public class EvaluationContext extends EvaluationLocation {
    
    // for the last expression indexed as described above, indicating whether the execution just entered
    // it from its parent (-1) or return to it from a child (index of the child)
    private int comingFrom;
    
    // values of the already evaluated children of the first, second, ..., expression indexed as
    // described in superclass
    private List<List<AbstractedValue>> expressionValues;
    
    // value of "this"
    private AbstractedValue thisValue;
    
    /**
     * Creates a new EvaluationContext, specifying the location of the evaluation and the values of
     * already evaluated expression.
     * <p>
     * The location is specified by the expressionIndices: The first element of the list specifies the
     * expression in the body of the given function. The second element specifies the sub expression
     * within that, and so on.
     * <p>
     * For the inner most expression indexed this way, comingFrom stores from where the execution
     * reached it. A value of -1 indicates that execution entered it from its parent, whereas any other
     * value indicates that execution returned to it from the child with that index.
     * <p>
     * Note that the order of expressions as targeted by the expression indices must not necessarily
     * align with the order in {@link Expression#crawlDeeper()}, but that in which they need to be
     * evaluated. Some expressions may also introduce gaps in the ordering, indicating that evaluation
     * should first return to the parent before (potentially) continuing with the next child.
     * <p>
     * The expression values are organized as follows: When the evaluation enters an Expression, a new
     * List is added to the outer list. Whenever evaluation of an Expression is done, its List is
     * removed from the outer List and its result is written into its parents List at the index in which
     * it appears in its parents children.
     * <p>
     * The parameters are stored in the new object as is, without being copied. The lists must be deeply
     * modifiable. Care must be taken not to modify them externally, especially after this state has
     * been locked.
     * 
     * @param function the SysCIR function in which this location is found
     * @param expressionIndices the indices of the expression and sub expressions where this location is
     *        found
     * @param comingFrom where the execution is coming from to the inner most expression
     * @param expressionValues the values of already evaluated expressions
     * @param thisValue the value of this
     */
    public EvaluationContext(WrappedSCFunction function, List<Integer> expressionIndices, int comingFrom,
            List<List<AbstractedValue>> expressionValues, AbstractedValue thisValue) {
        super(function, expressionIndices);
        
        this.comingFrom = comingFrom;
        this.expressionValues = Objects.requireNonNull(expressionValues);
        this.thisValue = Objects.requireNonNull(thisValue);
    }
    
    /**
     * Creates a deep copy of the given EvaluationContext.
     *
     * @param copyOf the context to copy
     */
    protected EvaluationContext(EvaluationContext copyOf) {
        super(copyOf, true);
        
        this.comingFrom = copyOf.comingFrom;
        this.expressionValues = CollectionUtil.deepCopy(copyOf.expressionValues);
        this.thisValue = copyOf.thisValue;
    }
    
    /**
     * Returns the from where the execution reached the innermost expression.
     * 
     * @return where the execution is coming from to the innermost expression
     */
    public int getComingFrom() {
        return this.comingFrom;
    }
    
    /**
     * Sets from where the execution reached the innermost expression.
     *
     * @param value from where execution reached the innermost expression
     */
    public void setComingFrom(int value) {
        requireNotLocked();
        resetHashCode();
        this.comingFrom = value;
    }
    
    /**
     * Returns a modifiable or unmodifiable view of the expression values of this context, depending on
     * whether or not this state is locked.
     * 
     * If the expression values are overwritten by {@link #setExpressionValues(List)}, a previously
     * returned view will no longer be up to date.
     * 
     * @return modifiable or unmodifiable view of the expression values
     */
    public List<List<AbstractedValue>> getExpressionValues() {
        resetHashCode();
        return isLocked() ? CollectionUtil.deeplyUnmodifiableList(this.expressionValues) : this.expressionValues;
    }
    
    /**
     * Replaces the expression values at this state by the given parameter.
     * 
     * As at construction, the parameter is stored as is, without being copied. Care must be taken not
     * to modify it, especially after this state has been locked.
     * 
     * @param expressionValues the new expression values
     */
    public void setExpressionValues(List<List<AbstractedValue>> expressionValues) {
        requireNotLocked();
        resetHashCode();
        this.expressionValues = expressionValues;
    }
    
    
    /**
     * Returns the abstracted evaluation result of a child of some expression that has not yet been
     * fully evaluated.
     * 
     * @param levelsAboveCurrent how many levels the considered parent expression is above the currently
     *        evaluated expression in the evaluation tree (where 0 yields the currently evaluated
     *        expression)
     * @param indexOfChild the index of the child whose value is requested
     * @return the evaluation result of that child
     */
    public AbstractedValue getExpressionValue(int levelsAboveCurrent, int indexOfChild) {
        List<AbstractedValue> values = this.expressionValues.get(this.expressionValues.size() - 1 - levelsAboveCurrent);
        return values.get(indexOfChild);
    }
    
    /**
     * Returns the this value of this context.
     * 
     * @return this values
     */
    public AbstractedValue getThisValue() {
        return this.thisValue;
    }
    
    /**
     * Replaces the this value of this context by the given parameter.
     * 
     * @param thisValue the new this value
     */
    public void setThisValue(AbstractedValue thisValue) {
        requireNotLocked();
        resetHashCode();
        this.thisValue = thisValue;
    }
    
    /**
     * Returns an {@link EvaluationLocation} with a deep copy of all location information within this
     * context.
     * 
     * The result is unlocked.
     *
     * @return location of this context
     */
    public EvaluationLocation toLocation() {
        return new EvaluationLocation(this, false);
    }
    
    public EvaluationLocation locationView() {
        return new LocationView(this);
    }
    
    @Override
    public EvaluationContext unlockedClone() {
        return new EvaluationContext(this);
    }
    
    @Override
    public int hashCodeInternal() {
        return ((super.hashCodeInternal() * 31 + this.comingFrom) * 31 + this.expressionValues.hashCode()) * 31
                + this.thisValue.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EvaluationContext ec)) {
            return false;
        }
        return this.comingFrom == ec.comingFrom && super.equals(other)
                && this.expressionValues.equals(ec.expressionValues) && this.thisValue.equals(ec.thisValue);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append("~").append(this.comingFrom);
        builder.append(" vals ").append(this.thisValue);
        builder.append(" ").append(this.expressionValues);
        return builder.toString();
    }
    
}
