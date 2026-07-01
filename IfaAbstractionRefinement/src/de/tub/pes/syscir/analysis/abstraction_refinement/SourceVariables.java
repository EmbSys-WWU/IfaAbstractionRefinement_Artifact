package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Sources implementation that keeps variables as sources.
 * 
 * @author Lukas Ernst
 */
public class SourceVariables implements Sources {
    
    public static SourcesType TYPE = () -> new SourceVariables();
    
    private final Set<Variable<?, ?>> variables;
    
    public SourceVariables() {
        this.variables = new HashSet<>();
    }
    
    public SourceVariables(Collection<Variable<?, ?>> variables) {
        this.variables = new HashSet<>(variables);
    }
    
    @Override
    public SourcesType getType() {
        return TYPE;
    }
    
    @Override
    public Sources copy() {
        return new SourceVariables(this.variables);
    }
    
    @Override
    public void add(Sources sources) {
        this.variables.addAll(((SourceVariables) sources).variables);
    }
    
    @Override
    public void remove(Sources sources) {
        this.variables.removeAll(((SourceVariables) sources).variables);
    }
    
    @Override
    public void removeOtherThan(Sources sources) {
        this.variables.retainAll(((SourceVariables) sources).variables);
    }
    
    @Override
    public boolean isSubsetOf(Sources sources) {
        return ((SourceVariables) sources).variables.containsAll(this.variables);
    }
    
    @Override
    public boolean isEmpty() {
        return this.variables.isEmpty();
    }
    
    @Override
    public void add(Variable<?, ?> variable) {
        this.variables.add(variable);
    }
    
    @Override
    public void add(StackTraceView stack, List<Integer> location) {}
    
    @Override
    public void addRetains(Expression expression) {}
    
    @Override
    public boolean tracks(Variable<?, ?> variable) {
        return this.variables.contains(variable);
    }
    
    @Override
    public boolean tracks(Expression expression, StackTraceView stack, List<Integer> location) {
        return true;
    }
    
    @Override
    public boolean retains(Expression expression) {
        return true;
    }
    
    @Override
    public String toString() {
        return this.variables.toString();
    }
    
}
