package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.util.IdentityHashSet;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A Sources implementation that keeps variable retainments as sources.
 * 
 * @author Lukas Ernst
 */
public class SourceRetainments implements Sources {
    
    public static SourcesType TYPE = () -> new SourceRetainments();
    
    private final Set<Expression> retainedExpressions;
    
    public SourceRetainments() {
        this.retainedExpressions = new IdentityHashSet<>();
    }
    
    public SourceRetainments(Collection<Expression> retainedExpressions) {
        this.retainedExpressions = new IdentityHashSet<>(retainedExpressions);
    }
    
    @Override
    public SourcesType getType() {
        return TYPE;
    }
    
    @Override
    public Sources copy() {
        return new SourceRetainments(this.retainedExpressions);
    }
    
    @Override
    public void add(Sources sources) {
        this.retainedExpressions.addAll(((SourceRetainments) sources).retainedExpressions);
    }
    
    @Override
    public void remove(Sources sources) {
        this.retainedExpressions.removeAll(((SourceRetainments) sources).retainedExpressions);
    }
    
    @Override
    public void removeOtherThan(Sources sources) {
        this.retainedExpressions.retainAll(((SourceRetainments) sources).retainedExpressions);
    }
    
    @Override
    public boolean isSubsetOf(Sources sources) {
        return ((SourceRetainments) sources).retainedExpressions.containsAll(this.retainedExpressions);
    }
    
    @Override
    public boolean isEmpty() {
        return this.retainedExpressions.isEmpty();
    }
    
    @Override
    public void add(Variable<?, ?> variable) {}
    
    @Override
    public void add(StackTraceView stack, List<Integer> location) {}
    
    @Override
    public void addRetains(Expression expression) {
        this.retainedExpressions.add(expression);
    }
    
    @Override
    public boolean tracks(Variable<?, ?> variable) {
        return true;
    }
    
    @Override
    public boolean tracks(Expression expression, StackTraceView stack, List<Integer> location) {
        return true;
    }
    
    @Override
    public boolean retains(Expression expression) {
        return this.retainedExpressions.contains(expression);
    }
    
    @Override
    public String toString() {
        return this.retainedExpressions.toString();
    }
    
}
