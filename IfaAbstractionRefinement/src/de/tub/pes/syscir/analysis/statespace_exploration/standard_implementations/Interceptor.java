package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.NoInformation;

/**
 * The main interception interface providing the different types of
 * interceptors.
 * 
 * @author Lukas Ernst
 */
public interface Interceptor {

    VariableInterceptor variables();

    EvaluationInterceptor evaluation();

    InformationHandler informationHandler();

    public static Interceptor of(VariableInterceptor variables) {
        return of(variables, NoInformation.HANDLER);
    }

    public static Interceptor of(VariableInterceptor variables, InformationHandler informationHandler) {
        return of(variables, EvaluationInterceptor.PASS_THROUGH, informationHandler);
    }

    public static Interceptor of(VariableInterceptor variables, EvaluationInterceptor evaluation,
            InformationHandler informationHandler) {
        return new Interceptor() {

            @Override
            public VariableInterceptor variables() {
                return variables;
            }

            @Override
            public EvaluationInterceptor evaluation() {
                return evaluation;
            }

            @Override
            public InformationHandler informationHandler() {
                return informationHandler;
            }
        };
    }
}
