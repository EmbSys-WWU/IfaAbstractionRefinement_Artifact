package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;

/**
 * Record representing a local variable (not its value), i.e. one that is process specific.
 * 
 * The variable consists of the execution stack (the qualifier) to differentiate different instances
 * of the same SCVariable for different function calls as well as the SCVariable specifying it.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <SCVarT> the type specifying the variable (usually a subclass of SCVariable or SCPort)
 */
public record LocalVariable<SCVarT>(StackTraceView stack, SCVarT scVariable)
        implements Variable<StackTraceView, SCVarT> {
    
    // object indicating the special "this" variable if used for the scVariable field
    public static final Object THIS_VAR = new Object() {
        
        @Override
        public String toString() {
            return "this";
        }
    };
    
    // object indicating a variable for the return result if used for the scVariable field
    public static final Object RESULT_VAR = new Object() {
        
        @Override
        public String toString() {
            return "return";
        }
    };
    
    public static LocalVariable<Object> getThisVariable(StackTraceView stack) {
        return new LocalVariable<>(stack, THIS_VAR);
    }
    
    public static LocalVariable<Object> getResultVariable(StackTraceView stack) {
        return new LocalVariable<>(stack, RESULT_VAR);
    }
    
    public LocalVariable(StackTraceView stack, SCVarT scVariable) {
        this.stack = stack;
        this.scVariable = scVariable;
    }
    
    @Override
    public StackTraceView getQualifier() {
        return stack();
    }
    
    @Override
    public SCVarT getSCVariable() {
        return scVariable();
    }
    
    public boolean isThis() {
        return this.scVariable == THIS_VAR;
    }
    
    public boolean isResult() {
        return this.scVariable == RESULT_VAR;
    }
    
    @Override
    public String toString() {
        String stackString = this.stack.toString();
        return "LVar[" + stackString + "." + this.scVariable + "]";
    }
    
}
