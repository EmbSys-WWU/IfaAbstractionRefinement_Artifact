package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationLocation;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCEvent;
import java.util.Collections;
import java.util.List;

/**
 * Record representing a variable (not its value) that is not process-specific, i.e. not a local
 * variable.
 * 
 * The variable consists of the instance to which it belongs (the qualifier) as well as the
 * SCVariable specifying it.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <InstanceT> the type of instance the variable belongs to
 * @param <SCVarT> the type specifying the variable (usually a subclass of SCVariable or SCPort)
 */
public record GlobalVariable<InstanceT, SCVarT> (InstanceT instance, SCVarT scVariable) implements Variable<InstanceT, SCVarT> {

    public static GlobalVariable<WrappedSCClassInstance, List<EvaluationLocation>> blockTrigger(
            WrappedSCClassInstance processInstance,
            List<EvaluationLocation> startOfBlock) {
        startOfBlock.stream().forEach(EvaluationLocation::lock);
        return new GlobalVariable<>(processInstance, Collections.unmodifiableList(startOfBlock));
    }

    public static GlobalVariable<Event, ?> eventTrigger(Event eventInstance) {
        return new GlobalVariable<>(eventInstance, null);
    }

    public static GlobalVariable<ArrayInstance, Integer> arrayMember(ArrayInstance arrayInstance, int index) {
        return new GlobalVariable<>(arrayInstance, index);
    }

    public boolean isBlockTrigger() {
        return getQualifier() instanceof WrappedSCClassInstance && getSCVariable() instanceof List;
    }
    public boolean isEventTrigger() {
        return getQualifier() instanceof Event && getSCVariable() == null;
    }

    public boolean isArrayMember() {
        return getQualifier() instanceof ArrayInstance && getSCVariable() instanceof Integer;
    }

    public boolean isEventVariable() {
        return getQualifier() instanceof WrappedSCClassInstance && getSCVariable() instanceof SCEvent;
    }

    @Override
    public InstanceT getQualifier() {
        return instance();
    }

    @Override
    public SCVarT getSCVariable() {
        return scVariable();
    }

    @Override
    public String toString() {
        if (isArrayMember()) {
            return "GVar[" + this.instance + "[" + this.scVariable + "]]";
        }
        if (isEventTrigger()) {
            return "GVar[" + this.instance + "]";
        }
        return "GVar[" + this.instance + "." + this.scVariable + "]";
    }

}
