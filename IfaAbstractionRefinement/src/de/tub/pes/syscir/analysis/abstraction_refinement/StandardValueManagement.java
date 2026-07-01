package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue.BinaryAbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.List;

/**
 * Simple ValueManagement implementation that uses the static {@link BinaryAbstractedValue#UNKNOWN}
 * for unknown values.
 * 
 * @author Lukas Ernst
 */
public class StandardValueManagement implements ValueManagement {
    
    public static final StandardValueManagement INSTANCE = new StandardValueManagement();
    
    @Override
    public AbstractedLogic makeLogic() {
        return BinaryAbstractedLogic.INSTANCE;
    }
    
    @Override
    public AbstractedValue abstractValue(AbstractedValue value) {
        return BinaryAbstractedValue.UNKNOWN;
    }
    
    @Override
    public AbstractedValue abstractFromVariable(AbstractedValue value, Variable<?, ?> variable) {
        return BinaryAbstractedValue.UNKNOWN;
    }
    
    @Override
    public AbstractedValue abstractFromExpression(AbstractedValue value, Expression expression, StackTraceView stack,
            List<Integer> location) {
        return BinaryAbstractedValue.UNKNOWN;
    }
    
    @Override
    public AbstractedValue unretainedValue(AbstractedValue value, Expression retainableExpression) {
        return BinaryAbstractedValue.UNKNOWN;
    }
    
}
