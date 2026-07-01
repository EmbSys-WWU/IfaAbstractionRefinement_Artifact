package de.tub.pes.syscir.analysis.util;

import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCPortInstance;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCKnownType;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for {@link SCPortInstance} that allows no modifications and caches the hashCode. Assumes
 * that the original is not modified externally.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public class WrappedSCPortInstance {

    // cache of wrappers, avoiding the creation of a new wrapper every time the original is encountered
    private static final Map<SCPortInstance, WrappedSCPortInstance> wrapperCache = new WeakIdentityHashMap<>();

    /**
     * Returns a new wrapper around the original.
     * 
     * No guarantee is made with regards to the identity of the wrapper. If a wrapper already exists, it
     * may be reused. No check is made for whether or not the hashCode of that wrapper is still valid.
     * 
     * @param original an WrappedSCPortInstance
     * @return a wrapper around the original
     */
    public static WrappedSCPortInstance getWrapped(SCPortInstance original) {
        return wrapperCache.computeIfAbsent(original, WrappedSCPortInstance::new);
    }

    private SCPortInstance original;

    private int hashCode;

    /**
     * Creates a new wrapper around the original, caching the originals current hashCode.
     *
     * @param original an WrappedSCPortInstance
     */
    public WrappedSCPortInstance(SCPortInstance original) {
        this.original = original;
        this.hashCode = original.hashCode();
    }

    public String getName() {
        return this.original.getName();
    }

    public SCPort getPortSocket() {
        return this.original.getPortSocket();
    }

    public SCClassInstance getOwner() {
        return this.original.getOwner();
    }

    public SCKnownType getChannel(String name) {
        return this.original.getChannel(name);
    }

    public SCPortInstance getPortSocketInstance(String psi_nam) {
        return this.original.getPortSocketInstance(psi_nam);
    }

    public List<SCKnownType> getChannels() {
        return this.original.getChannels();
    }

    public List<SCPortInstance> getPortSocketInstances() {
        return this.original.getPortSocketInstances();
    }

    public List<SCClassInstance> getModuleInstances() {
        return this.original.getModuleInstances();
    }

    public boolean existChannel(SCKnownType mdl) {
        return this.original.existChannel(mdl);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WrappedSCPortInstance wrapped)) {
            return false;
        }
        return this.original.equals(wrapped.original);
    }

    @Override
    public String toString() {
        return this.original.toString();
    }

}

