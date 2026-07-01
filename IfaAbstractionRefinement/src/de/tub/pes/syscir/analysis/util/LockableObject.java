package de.tub.pes.syscir.analysis.util;

/**
 * Abstract class representing some object that is mutable after construction but can be locked to
 * be immutable.
 * <p>
 * Any subclass must ensure that no visible change to the internal state of an object can occur
 * after {@link #lock()} has first been invoked on that object.
 * <p>
 * Unless otherwise specified, instances of this class are thread-safe if and only if they have been
 * locked (the lock must have happened-before any non-synchronized access as defined by § 17.4.5 of
 * the Java SE 17 specification).
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <T> The type of this object (to specify the return types of {@link #unlockedClone()} and
 *        {@link #unlockedVersion()}). It must always hold that <code>this instanceof T</code>.
 */
public abstract class LockableObject {

    private volatile boolean locked;

    /**
     * Constructs a new, unlocked object.
     */
    public LockableObject() {
        this.locked = false;
    }

    /**
     * Returns whether or not this object is currently locked.
     *
     * @return whether the object is locked
     */
    public final boolean isLocked() {
        return this.locked;
    }

    /**
     * Locks this object, preventing any future change to its internal state.
     * 
     * @return true if the object was unlocked before, false if it was already locked
     */
    protected boolean lock() {
        if (this.locked) {
            return false;
        }

        this.locked = true;
        return true;
    }

    /**
     * Throws an {@link IllegalStateException} iff this object is locked. Returns normally otherwise.
     * 
     * @throws IllegalStateException if the object is locked
     */
    protected final void requireNotLocked() throws IllegalStateException {
        if (this.locked) {
            throw new IllegalStateException("locked state cannot be modified");
        }
    }

    /**
     * Returns an unlocked version of this object, making an {@link #unlockedClone()} if necessary.
     * 
     * @return this object if it is unlocked, an {@link #unlockedClone()} otherwise
     */
    @SuppressWarnings("unchecked")
    public static <T extends LockableObject> T unlockedVersion(T object) {
        return object.isLocked() ? (T) object.unlockedClone() : object;
    }

    /**
     * Returns a clone of this object hat has the same visible state but has not yet been locked.
     * 
     * @return an unlocked clone of this object
     */
    public abstract LockableObject unlockedClone();

}
