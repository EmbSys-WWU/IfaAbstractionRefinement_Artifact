package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue.BinaryAbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.List;
import java.util.function.Supplier;

/**
 * ValueManagement implementation that tracks value composition by wrapping values in
 * {@link SourcedValue}s.
 * 
 * @author Lukas Ernst
 */
public class SourcedValueManagement implements ValueManagement {
    
    private final Supplier<Sources> emptySourcesSupplier;
    
    public SourcedValueManagement(Supplier<Sources> emptySourcesSupplier) {
        this.emptySourcesSupplier = emptySourcesSupplier;
    }
    
    @Override
    public AbstractedLogic makeLogic() {
        return new LogicTracker(BinaryAbstractedLogic.INSTANCE, (value, sourceValues) -> {
            Sources sources = this.emptySourcesSupplier.get();
            for (AbstractedValue sourceValue : sourceValues) {
                sources.add(((SourcedValue) sourceValue).getSources());
            }
            return new SourcedValue(value, sources);
        }, wrapped -> ((SourcedValue) wrapped).getRepresentedValue());
    }
    
    @Override
    public AbstractedValue abstractValue(AbstractedValue value) {
        return new SourcedValue(BinaryAbstractedValue.UNKNOWN, ((SourcedValue) value).getSources());
    }
    
    @Override
    public AbstractedValue abstractFromVariable(AbstractedValue value, Variable<?, ?> variable) {
        Sources sources = ((SourcedValue) value).getSources().copy();
        sources.add(variable);
        return new SourcedValue(BinaryAbstractedValue.UNKNOWN, sources);
    }
    
    @Override
    public AbstractedValue abstractFromExpression(AbstractedValue value, Expression expression, StackTraceView stack,
            List<Integer> location) {
        Sources sources = ((SourcedValue) value).getSources().copy();
        sources.add(stack, location);
        return new SourcedValue(BinaryAbstractedValue.UNKNOWN, sources);
    }
    
    @Override
    public AbstractedValue unretainedValue(AbstractedValue value, Expression retainable) {
        Sources futureSources = ((SourcedValue) value).getSources().copy();
        futureSources.addRetains(retainable);
        return new SourcedValue(BinaryAbstractedValue.UNKNOWN, futureSources);
    }
    
}
