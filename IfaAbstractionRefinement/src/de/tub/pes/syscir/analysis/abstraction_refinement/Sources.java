package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.List;

/**
 * An interface for sets of sources, which may represent an abstraction.
 * 
 * Types of sources are currently variables, expressions and variable retainments. (Assignments are
 * a subtype of expression sources.)
 * 
 * Sources support some set operations and, for use as an abstraction, type-specific tracking
 * decision functions.
 * 
 * @author Lukas Ernst
 */
public interface Sources {
    
    public static interface SourcesType {
        
        Sources makeEmptySources();
    }
    
    SourcesType getType();
    
    /**
     * Returns a copy of these Sources that may be modified separately.
     */
    Sources copy();
    
    /**
     * Adds the specified sources to this. The specified sources must be of the same type.
     */
    void add(Sources sources);
    
    /**
     * Removes the specified sources from this. The specified sources must be of the same type.
     */
    void remove(Sources sources);
    
    /**
     * Removes any sources from this that the specified sources do not contain. The specified sources
     * must be of the same type.
     */
    void removeOtherThan(Sources sources);
    
    /**
     * Returns whether the specified sources contain every source that this contains. The specified
     * sources must be of the same type.
     */
    boolean isSubsetOf(Sources sources);
    
    /**
     * Returns whether this contains no sources.
     */
    boolean isEmpty();
    
    /**
     * Adds the specified variable to this if this keeps variables.
     */
    void add(Variable<?, ?> variable);
    
    /**
     * Adds the specified expression to this if this keeps expressions.
     */
    void add(StackTraceView stack, List<Integer> location);
    
    /**
     * Adds the specified expression as a variable retainment if this keeps variable retainments.
     */
    void addRetains(Expression expression);
    
    /**
     * Returns whether this tracks the specified variable.
     */
    boolean tracks(Variable<?, ?> variable);
    
    /**
     * Returns whether this tracks the specified expression.
     */
    boolean tracks(Expression expression, StackTraceView stack, List<Integer> location);
    
    /**
     * Returns whether this retains variable values accessed in the specified expression.
     */
    boolean retains(Expression expression);
    
    /**
     * Returns a new Sources object that is the union of all specified sources. All specified sources
     * must be of the same type.
     */
    public static Sources union(Sources... sources) {
        if (sources.length <= 0) {
            throw new IllegalArgumentException("Cannot construct empty sources from no parameters");
        }
        Sources out = sources[0].copy();
        for (int i = 1; i < sources.length; i++) {
            out.add(sources[i]);
        }
        return out;
    }
    
}
