package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationLocation;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.util.IdentityHashSet;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A Sources implementation that keeps expressions as sources.
 * 
 * @author Lukas Ernst
 */
public class SourceExpressions implements Sources {
    
    public static SourcesType TYPE = () -> new SourceExpressions();
    
    public record ExpressionLocation(StackTraceView stack, List<Integer> location) {
        
        public static ExpressionLocation copyOf(StackTraceView stack, List<Integer> location) {
            stack = stack.copyStack();
            stack.calls().forEach(EvaluationLocation::lock);
            return new ExpressionLocation(stack, List.copyOf(location));
        }
        
        public Expression getExpression() {
            return new EvaluationLocation(this.stack.getTop(), this.location).getNextExpression();
        }
        
        @Override
        public String toString() {
            return this.stack.toString() + ":" + this.location.toString();
        }
    }
    
    protected final Set<ExpressionLocation> expressionLocations;
    
    public SourceExpressions() {
        this.expressionLocations = new IdentityHashSet<>();
    }
    
    public SourceExpressions(Collection<ExpressionLocation> expressionLocations) {
        this.expressionLocations = new LinkedHashSet<>(expressionLocations);
    }
    
    @Override
    public SourcesType getType() {
        return TYPE;
    }
    
    @Override
    public Sources copy() {
        return new SourceExpressions(this.expressionLocations);
    }
    
    @Override
    public void add(Sources sources) {
        this.expressionLocations.addAll(((SourceExpressions) sources).expressionLocations);
    }
    
    @Override
    public void remove(Sources sources) {
        this.expressionLocations.removeAll(((SourceExpressions) sources).expressionLocations);
    }
    
    @Override
    public void removeOtherThan(Sources sources) {
        this.expressionLocations.retainAll(((SourceExpressions) sources).expressionLocations);
    }
    
    @Override
    public boolean isSubsetOf(Sources sources) {
        return ((SourceExpressions) sources).expressionLocations.containsAll(this.expressionLocations);
    }
    
    @Override
    public boolean isEmpty() {
        return this.expressionLocations.isEmpty();
    }
    
    @Override
    public void add(Variable<?, ?> variable) {}
    
    @Override
    public void add(StackTraceView stack, List<Integer> location) {
        this.expressionLocations.add(ExpressionLocation.copyOf(stack, location));
    }
    
    @Override
    public void addRetains(Expression expression) {}
    
    @Override
    public boolean tracks(Variable<?, ?> variable) {
        return true;
    }
    
    @Override
    public boolean tracks(Expression expression, StackTraceView stack, List<Integer> location) {
        stack.calls().forEach(EvaluationLocation::resetHashCode);
        return this.expressionLocations.contains(new ExpressionLocation(stack, location));
    }
    
    @Override
    public boolean retains(Expression expression) {
        return true;
    }
    
    @Override
    public String toString() {
        return this.expressionLocations.toString();
    }
    
}
