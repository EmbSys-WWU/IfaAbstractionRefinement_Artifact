package de.tub.pes.syscir.analysis.util;

/**
 * Subclass of LockableObject that caches the hashCode of the object.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <T> The type of this object (to specify the return types of {@link #unlockedClone()} and
 *        {@link #unlockedVersion()}). It must always hold that <code>this instanceof T</code>.
 */
public abstract class HashCachingLockableObject extends LockableObject {

    private int hashCode;

    private boolean hashCodePrecomputed;

    /**
     * Constructs a new HashCachingLockableObject without a precomputed hash.
     */
    public HashCachingLockableObject() {
        this.hashCodePrecomputed = false;
    }

    /**
     * Constructs a new HashCachingLockableObject with the given precomputed hash.
     */
    public HashCachingLockableObject(int initialHashCode) {
        this.hashCode = initialHashCode;
        this.hashCodePrecomputed = true;
    }

    /**
     * Constructs a new HashCachingLockableObject, copying the precomputed hash (if present) of the
     * given object.
     */
    public HashCachingLockableObject(HashCachingLockableObject copyOf) {
        if (!copyOf.hashCodePrecomputed) {
            return;
        }

        this.hashCode = copyOf.hashCode;
        this.hashCodePrecomputed = true;
    }

    /**
     * Returns the synchronization lock that shall be used to synchronize accesses to {@link #hashCode}.
     * 
     * By default, this method returns {@code this}.
     * 
     * @return synchronization lock für {@link #hashCode}
     */
    protected Object hashSynchronizationLock() {
        return this;
    }

    /**
     * Internally computes the hashCode for this object.
     * 
     * This method is invoked once every time {@link #hashCode()} is invoked before {@link #lock()} and
     * finally once when {@link #lock()} is invoked to cache the result.
     * 
     * @return hashCode
     */
    protected abstract int hashCodeInternal();

    /**
     * Resets the hashCode if it has been precomputed and this object has not yet been locked.
     * <p>
     * This method must be called whenever the internal hashCode might have changed.
     */
    protected void resetHashCode() {
        synchronized (hashSynchronizationLock()) {
            if (!isLocked()) {
                this.hashCodePrecomputed = false;
            }
        }
    }

    @Override
    public final int hashCode() {
        synchronized (hashSynchronizationLock()) {
            if (!this.hashCodePrecomputed) {
                this.hashCodePrecomputed = true;
                this.hashCode = hashCodeInternal();
            }
            return this.hashCode;
        }
    }

}
