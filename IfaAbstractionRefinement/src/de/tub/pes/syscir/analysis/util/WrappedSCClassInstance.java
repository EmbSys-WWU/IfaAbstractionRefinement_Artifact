package de.tub.pes.syscir.analysis.util;

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCConnectionInterface;
import de.tub.pes.syscir.sc_model.SCEnumType;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for {@link SCClassInstance} that allows no modifications and caches the hashCode. Assumes
 * that the original is not modified externally.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public class WrappedSCClassInstance {

    // cache of wrappers, avoiding the creation of a new wrapper every time the original is encountered
    private static final Map<SCClassInstance, WrappedSCClassInstance> wrapperCache = new WeakIdentityHashMap<>();

    /**
     * Returns a new wrapper around the original.
     * 
     * No guarantee is made with regards to the identity of the wrapper. If a wrapper already exists, it
     * may be reused. No check is made for whether or not the hashCode of that wrapper is still valid.
     * 
     * @param original an SCClassInstance
     * @return a wrapper around the original
     */
    public static WrappedSCClassInstance getWrapped(SCClassInstance original) {
        return wrapperCache.computeIfAbsent(original, WrappedSCClassInstance::new);
    }

    private SCClassInstance original;

    private int hashCode;

    /**
     * Creates a new wrapper around the original, caching the originals current hashCode.
     *
     * @param original an SCClassInstance
     */
    public WrappedSCClassInstance(SCClassInstance original) {
        this.original = original;
        this.hashCode = original.hashCode();
    }

    public SCClassInstance getOriginal() {
        return this.original;
    }

    public String getName() {
        return this.original.getName();
    }

    public String getTypeWithoutSize() {
        return this.original.getTypeWithoutSize();
    }

    public boolean isStatic() {
        return this.original.isStatic();
    }

    public boolean isConst() {
        return this.original.isConst();
    }

    public SCVariableDeclarationExpression getDeclaration() {
        return this.original.getDeclaration();
    }

    public SCClass getSCClass() {
        return this.original.getSCClass();
    }

    public List<String> otherMods() {
        return this.original.otherMods();
    }

    public SCClass getOuterClass() {
        return this.original.getOuterClass();
    }

    public List<SCConnectionInterface> getPortSocketInstances() {
        return this.original.getPortSocketInstances();
    }

    public SCConnectionInterface getPortSocketInstanceByName(String name) {
        return this.original.getPortSocketInstanceByName(name);
    }

    public String getDeclarationString() {
        return this.original.getDeclarationString();
    }

    public String getInstanceLabel() {
        return this.original.getInstanceLabel();
    }

    public void print() {
        this.original.print();
    }

    public String getInitializationString() {
        return this.original.getInitializationString();
    }

    public SCEnumType getEnumType() {
        return this.original.getEnumType();
    }

    public String getType() {
        return this.original.getType();
    }

    public boolean isSCModule() {
        return this.original.isSCModule();
    }

    public boolean isNotSCModule() {
        return this.original.isNotSCModule();
    }

    public boolean isSCKnownType() {
        return this.original.isSCKnownType();
    }

    public boolean isChannel() {
        return this.original.isChannel();
    }

    public boolean isNotChannel() {
        return this.original.isNotChannel();
    }

    public List<List<String>> getProcessNamesToInitialize(LinkedList<String> predecessors) {
        return this.original.getProcessNamesToInitialize(predecessors);
    }

    public boolean hasInitialValue() {
        return this.original.hasInitialValue();
    }

    public int getInitialValueCount() {
        return this.original.getInitialValueCount();
    }

    public boolean hasDeclaration() {
        return this.original.hasDeclaration();
    }

    public boolean isSCClassInstance() {
        return this.original.isSCClassInstance();
    }

    public boolean isArrayOfSCClassInstances() {
        return this.original.isArrayOfSCClassInstances();
    }

    public boolean isSCClassInstanceOrArrayOfSCClassInstances() {
        return this.original.isSCClassInstanceOrArrayOfSCClassInstances();
    }

    public SCClass getSClassIfPossible() {
        return this.original.getSClassIfPossible();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WrappedSCClassInstance wrapped)) {
            return false;
        }
        return this.original.equals(wrapped.original);
    }

    @Override
    public String toString() {
        return this.original.toString();
    }

}
