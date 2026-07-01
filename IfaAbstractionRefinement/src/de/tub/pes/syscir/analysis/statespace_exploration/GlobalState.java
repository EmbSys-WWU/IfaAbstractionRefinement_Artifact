package de.tub.pes.syscir.analysis.statespace_exploration;

import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.util.HashCachingLockableObject;
import de.tub.pes.syscir.analysis.util.HashCasher;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class representing an abstraction of the global state of a SystemC model.
 * 
 * This includes the scheduler states of all events (are they pending and if so, for when).
 * <p>
 * This state starts out as mutable but can be locked to be immutable by invoking the method
 * {@link #lock()}. When locked, any attempt to modify a state results in an
 * {@link IllegalStateException}.
 * 
 * @author Jonas Becker-Kupczok
 *
 */
public abstract class GlobalState extends HashCachingLockableObject {
    
    private Map<Event, TimedBlocker> eventStates; // not contained means not pending, null mapping is not allowed
    private Set<WrappedSCClassInstance> requestedUpdates;
    private boolean simulationStopped;
    
    private HashCasher<GlobalState> timingAgnosticHashCasher;
    
    /**
     * Constructs a new, mutable GlobalState with the given event scheduler states, requested updates
     * and simulation stoppage.
     * <p>
     * The map and set parameters are stored in the newly created object as is, without being copied.
     * They must be modifiable. Care must be taken not to modify them externally, especially after this
     * state has been locked.
     * 
     * @param eventStates map of each event to its scheduler state (no mapping means not pending, null
     *        values are not allowed)
     * @param requestedUpdates set of ports for which updates have been requested
     * @param simulationStopped whether or not sc_stop() has been called
     */
    public GlobalState(Map<Event, TimedBlocker> eventStates, Set<WrappedSCClassInstance> requestedUpdates,
            boolean simulationStopped) {
        this.eventStates = eventStates;
        this.requestedUpdates = requestedUpdates;
        this.simulationStopped = simulationStopped;
        
        this.timingAgnosticHashCasher = new HashCasher<>(GlobalState::timingAgnosticHashCodeInternal, this);
    }
    
    /**
     * Constructs a new, mutable copy of the given GlobalState.
     * 
     * @param copyOf the state to copy
     */
    protected GlobalState(GlobalState copyOf) {
        super(copyOf);
        
        this.eventStates = new LinkedHashMap<>(copyOf.eventStates);
        this.requestedUpdates = new LinkedHashSet<>(copyOf.requestedUpdates);
        this.timingAgnosticHashCasher = new HashCasher<>(copyOf.timingAgnosticHashCasher, this);
    }
    
    @Override
    protected int hashCodeInternal() {
        int result = this.eventStates.hashCode();
        result = result * 31 + this.requestedUpdates.hashCode();
        if (this.simulationStopped) {
            result = ~result;
        }
        return result;
    }
    
    /**
     * Returns a {@link Object#hashCode()} for this state that ignores the timed (non-delta) duration
     * for which any event may be waiting.
     * 
     * Consistent with {@link #timingAgnosticEquals(GlobalState)}.
     * 
     * @implNote Subclasses should override {@link #timingAgnosticHashCodeInternal()} instead.
     * 
     * @return timing agnostic hashCode
     */
    public final int timingAgnosticHashCode() {
        return this.timingAgnosticHashCasher.hashCode();
    }
    
    /**
     * Internally computes the {@link #timingAgnosticHashCode()} (cashed after computation).
     *
     * @return timing agnostic hashCode
     */
    protected int timingAgnosticHashCodeInternal() {
        int result = 0;
        for (Entry<Event, TimedBlocker> eventEntry : this.eventStates.entrySet()) {
            if (eventEntry.getValue() instanceof RealTimedBlocker) {
                // only consider the event for RealTimedBlocker, not the blocker
                result += eventEntry.getKey().hashCode();
            } else {
                result += eventEntry.hashCode();
            }
        }
        
        result = result * 31 + this.requestedUpdates.hashCode();
        if (this.simulationStopped) {
            result = ~result;
        }
        return result;
    }
    
    /**
     * Returns the smallest wait time of any event in other that is different from its wait time in this
     * state.
     * 
     * @param other another (compatible) global state
     * @return the smallest changed wait time in other
     */
    public RealTimedBlocker getSmallestChangedWait(GlobalState other) {
        GlobalState ogs = other;
        RealTimedBlocker minChanged = null;
        for (Entry<Event, TimedBlocker> eventEntry : this.eventStates.entrySet()) {
            if (!(eventEntry.getValue() instanceof RealTimedBlocker currentTime)) {
                continue;
            }
            
            RealTimedBlocker otherTime = (RealTimedBlocker) ogs.eventStates.get(eventEntry.getKey());
            if (currentTime.equals(otherTime)) {
                continue;
            }
            
            if (minChanged == null || otherTime.compareTo(minChanged) < 0) {
                minChanged = otherTime;
            }
        }
        return minChanged;
    }
    
    /**
     * Returns an view of the scheduler states of events that is modifiable iff this state is not
     * locked.
     * 
     * If the event states are overwritten by {@link #setEventStates(Map)}, a previously returned view
     * will no longer be up to date.
     * 
     * @return view of the event states
     */
    public Map<Event, TimedBlocker> getEventStates() {
        resetHashCode();
        return isLocked() ? Collections.unmodifiableMap(this.eventStates) : this.eventStates;
    }
    
    /**
     * Returns an view of the events with their respective states that is modifiable iff this state is
     * not locked.
     * 
     * If the event states are overwritten by {@link #setEventStates(Map)}, a previously returned view
     * will no longer be up to date.
     * 
     * @return unmodifiable view of the events with their states
     */
    public Set<Entry<Event, TimedBlocker>> getEventsWithStates() {
        resetHashCode();
        return isLocked() ? Collections.unmodifiableSet(this.eventStates.entrySet()) : this.eventStates.entrySet();
    }
    
    /**
     * Replaces the event states of this state by the given parameter.
     * 
     * As at construction, the parameter is stored as is, without being copied. Care must be taken not
     * to modify it, especially after this state has been locked.
     * 
     * @param eventStates the new event states
     */
    public void setEventStates(Map<Event, TimedBlocker> eventStates) {
        requireNotLocked();
        resetHashCode();
        this.eventStates = eventStates;
    }
    
    
    /**
     * Returns an view of the requested updates that is modifiable iff this state is not locked.
     * 
     * If the requested updates are overwritten by {@link #setRequestedUpdates(Set)}, a previously
     * returned view will no longer be up to date.
     * 
     * @return view of the requested updates
     */
    public Set<WrappedSCClassInstance> getRequestedUpdates() {
        resetHashCode();
        return isLocked() ? Collections.unmodifiableSet(this.requestedUpdates) : this.requestedUpdates;
    }
    
    /**
     * Replaces the requested updates of this state by the given parameter.
     * 
     * As at construction, the parameter is stored as is, without being copied. Care must be taken not
     * to modify it, especially after this state has been locked.
     * 
     * @param eventStates the new requested updates
     */
    public void setRequestedUpdates(Set<WrappedSCClassInstance> requestedUpdates) {
        requireNotLocked();
        resetHashCode();
        this.requestedUpdates = requestedUpdates;
    }
    
    /**
     * Returns whether or not sc_stop() has been called for this state.
     *
     * @return whether or not sc_stop() has been called
     */
    public boolean isSimulationStopped() {
        return this.simulationStopped;
    }
    
    
    /**
     * Sets whether or not sc_stop() has been called for this state.
     *
     * @param value whether or not sc_stop() has been called
     */
    public void setSimulationStopped(boolean value) {
        requireNotLocked();
        resetHashCode();
        this.simulationStopped = value;
    }
    
    @Override
    protected void resetHashCode() {
        super.resetHashCode();
        this.timingAgnosticHashCasher.resetHashCode();
    }
    
    // increase visibility
    @Override
    public boolean lock() {
        if (super.lock()) {
            this.timingAgnosticHashCasher.lock();
            return true;
        }
        return false;
    }
    
    @Override
    public abstract GlobalState unlockedClone();
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        GlobalState g = (GlobalState) other;
        return this.eventStates.equals(g.eventStates) && this.requestedUpdates.equals(g.requestedUpdates)
                && this.simulationStopped == g.simulationStopped;
    }
    
    @Override
    public String toString() {
        return this.eventStates.toString() + ", updates " + this.requestedUpdates.toString() + " ("
                + (this.simulationStopped ? "stopped" : "running") + ")";
    }
    
}
