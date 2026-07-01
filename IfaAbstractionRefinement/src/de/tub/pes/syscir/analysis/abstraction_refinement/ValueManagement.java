package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.List;

/**
 * Interface for control over how values are abstracted away and transported. Designed to
 * optionalize value composition tracking.
 * 
 * @author Lukas Ernst
 */
public interface ValueManagement {
    
    AbstractedLogic makeLogic();
    
    AbstractedValue abstractValue(AbstractedValue value);
    
    AbstractedValue abstractFromVariable(AbstractedValue value, Variable<?, ?> variable);
    
    AbstractedValue abstractFromExpression(AbstractedValue value, Expression expression, StackTraceView stack,
            List<Integer> location);
    
    AbstractedValue unretainedValue(AbstractedValue value, Expression retainableExpression);
}
