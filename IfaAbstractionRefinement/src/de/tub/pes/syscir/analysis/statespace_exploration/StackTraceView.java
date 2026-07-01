package de.tub.pes.syscir.analysis.statespace_exploration;

import de.tub.pes.syscir.analysis.util.WrappedSCFunction;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class StackTraceView {
    
    public abstract WrappedSCFunction base();
    
    public abstract List<EvaluationLocation> calls();
    
    public StackTrace copyStack() {
        List<EvaluationLocation> copiedCalls = new ArrayList<>(calls().size());
        for (EvaluationLocation call : calls()) {
            copiedCalls.add(call.unlockedClone());
        }
        return new StackTrace(base(), copiedCalls);
    }
    
    public int size() {
        return calls().size() + 1;
    }
    
    public WrappedSCFunction getTop() {
        if (calls().isEmpty()) {
            return base();
        }
        return WrappedSCFunction
                .getWrapped(((FunctionCallExpression) calls().getLast().getNextExpression()).getFunction());
    }
    
    @Override
    public String toString() {
        if (calls().isEmpty()) {
            return base().getSCClass().getName() + "." + base().getName();
        }
        return base().getSCClass().getName() + "."
                + calls().stream().map(EvaluationLocation::toString).collect(Collectors.joining(".")) + "."
                + ((FunctionCallExpression) calls().getLast().getNextExpression()).getFunction().getName();
    }
    
    @Override
    public int hashCode() {
        return base().hashCode() * 31 + calls().hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StackTraceView st)) {
            return false;
        }
        return base().equals(st.base()) && calls().equals(st.calls());
    }
    
    public static class StackTrace extends StackTraceView {
        
        private WrappedSCFunction base;
        private List<EvaluationLocation> calls;
        
        public StackTrace(WrappedSCFunction base, List<EvaluationLocation> calls) {
            this.base = Objects.requireNonNull(base);
            this.calls = Objects.requireNonNull(calls);
        }
        
        @Override
        public WrappedSCFunction base() {
            return this.base;
        }
        
        @Override
        public List<EvaluationLocation> calls() {
            return this.calls;
        }
        
        public void addCall(EvaluationLocation call) {
            this.calls.add(call);
            
            if (!(call.getNextExpression() instanceof FunctionCallExpression)) {
                throw new IllegalArgumentException(
                        "The next expression of the evaluation location must be a function call expression.");
            }
        }
        
        public void removeCall() {
            this.calls.removeLast();
        }
    }
    
}
