package de.tub.pes.syscir.analysis.util;

import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCPortInstance;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;

/**
 * Provides wrapper functions for all SC classes for which wrappers exist, allowing them to be
 * statically imported and used uniformly.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public class WrapperUtil {

    public static WrappedExpression wrap(Expression x) {
        return WrappedExpression.getWrapped(x);
    }

    public static WrappedSCClassInstance wrap(SCClassInstance x) {
        return WrappedSCClassInstance.getWrapped(x);
    }

    public static WrappedSCFunction wrap(SCFunction x) {
        return WrappedSCFunction.getWrapped(x);
    }

    public static WrappedSCPortInstance wrap(SCPortInstance x) {
        return WrappedSCPortInstance.getWrapped(x);
    }

    public static WrappedSCProcess wrap(SCProcess x) {
        return WrappedSCProcess.getWrapped(x);
    }

}
