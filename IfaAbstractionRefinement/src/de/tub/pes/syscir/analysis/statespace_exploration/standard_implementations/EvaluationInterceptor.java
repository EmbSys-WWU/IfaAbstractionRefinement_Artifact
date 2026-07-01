package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.List;

/**
 * Interceptor to intercept evaluations.
 * 
 * @author Lukas Ernst
 */
public interface EvaluationInterceptor {
    
    /**
     * Called when an expression evaluation is begun. This is currently called along with
     * {@link InformationHandler#announceEvaluation(Expression, TransitionResult, LocalState)}, so this
     * is currently also called when an expression is entered.
     */
    default void prepareEvaluation(Expression expression) {}
    
    /**
     * Intervene when an expression evaluation finishes.
     */
    AbstractedValue evaluated(Expression expression, StackTraceView stack, List<Integer> location,
            AbstractedValue result);
    
    /**
     * An EvaluationInterceptor that passes through all evaluation results (does nothing).
     */
    public static EvaluationInterceptor PASS_THROUGH = (_, _, _, result) -> result;
    
    /**
     * Constructs an EvaluationInterceptor that delegates calls to the specified EvaluationInterceptors
     * in sequence.
     */
    public static EvaluationInterceptor sequence(EvaluationInterceptor... interceptors) {
        return new EvaluationInterceptor() {
            
            @Override
            public void prepareEvaluation(Expression expression) {
                for (EvaluationInterceptor interceptor : interceptors) {
                    interceptor.prepareEvaluation(expression);
                }
            }
            
            @Override
            public AbstractedValue evaluated(Expression expression, StackTraceView stack, List<Integer> location,
                    AbstractedValue result) {
                for (EvaluationInterceptor interceptor : interceptors) {
                    result = interceptor.evaluated(expression, stack, location, result);
                }
                return result;
            }
        };
    }
    
}
