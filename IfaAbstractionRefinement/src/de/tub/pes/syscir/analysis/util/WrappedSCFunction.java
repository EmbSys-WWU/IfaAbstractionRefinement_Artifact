package de.tub.pes.syscir.analysis.util;

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCParameter;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.EventNotificationExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for {@link SCFunction} that allows no modifications and caches the hashCode. Assumes that
 * the original is not modified externally.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public class WrappedSCFunction {

    // cache of wrappers, avoiding the creation of a new wrapper every time the original is encountered
    private static final Map<SCFunction, WrappedSCFunction> wrapperCache = new WeakIdentityHashMap<>();

    /**
     * Returns a new wrapper around the original.
     * 
     * No guarantee is made with regards to the identity of the wrapper. If a wrapper already exists, it
     * may be reused. No check is made for whether or not the hashCode of that wrapper is still valid.
     * 
     * @param original an WrappedSCFunction
     * @return a wrapper around the original
     */
    public static WrappedSCFunction getWrapped(SCFunction original) {
        return wrapperCache.computeIfAbsent(original, WrappedSCFunction::new);
    }

    private SCFunction original;

    private int hashCode;

    /**
     * Creates a new wrapper around the original, caching the originals current hashCode.
     *
     * @param original an SCFunction
     */
    public WrappedSCFunction(SCFunction original) {
        this.original = original;
        this.hashCode = original.hashCode();
    }

    public SCFunction getOriginal() {
        return this.original;
    }

    public List<SCVariable> getLocalVariablesAndParametersAsSCVars() {
        return this.original.getLocalVariablesAndParametersAsSCVars();
    }

    public List<SCVariable> getLocalVariables() {
        return this.original.getLocalVariables();
    }

    public String getName() {
        return this.original.getName();
    }

    public List<Expression> getBody() {
        return this.original.getBody();
    }

    public List<SCParameter> getParameters() {
        return this.original.getParameters();
    }

    public SCParameter getParameter(String name) {
        return this.original.getParameter(name);
    }

    public String getReturnType() {
        return this.original.getReturnType();
    }

    public String getReturnTypeWithoutSize() {
        return this.original.getReturnTypeWithoutSize();
    }

    public SCVariable getLocalVariable(String var_nam) {
        return this.original.getLocalVariable(var_nam);
    }

    public SCVariable getLocalVariableOrParameterAsSCVar(String var_name) {
        return this.original.getLocalVariableOrParameterAsSCVar(var_name);
    }

    public boolean existVarWith(SCVariable var) {
        return this.original.existVarWith(var);
    }

    public List<Expression> getAllExpressions() {
        return this.original.getAllExpressions();
    }

    public boolean hasReturnType() {
        return this.original.hasReturnType();
    }

    public SCClass getSCClass() {
        return this.original.getSCClass();
    }

    public boolean getConsumesTime() {
        return this.original.getConsumesTime();
    }

    public boolean getTimingAnalyzed() {
        return this.original.getTimingAnalyzed();
    }

    public boolean isExtendedTimingAnalyzed() {
        return this.original.isExtendedTimingAnalyzed();
    }

    public boolean getIsCalled() {
        return this.original.getIsCalled();
    }

    public boolean hasRecursions() {
        return this.original.hasRecursions();
    }

    public boolean isCalledBy(SCFunction callerFunction) {
        return this.original.isCalledBy(callerFunction);
    }

    public LinkedList<FunctionCallExpression> getFunctionCalls() {
        return this.original.getFunctionCalls();
    }

    public Set<Integer> getPartitionNumbers() {
        return this.original.getPartitionNumbers();
    }

    public List<EventNotificationExpression> getEventNotifications() {
        return this.original.getEventNotifications();
    }

    public int compareTo(WrappedSCFunction otherFunc) {
        return this.original.compareTo(otherFunc.original);
    }

    public int compareTo(SCFunction otherFunc) {
        return this.original.compareTo(otherFunc);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WrappedSCFunction wrapped)) {
            return false;
        }
        return this.original.equals(wrapped.original);
    }

    @Override
    public String toString() {
        return this.original.toString();
    }

}
