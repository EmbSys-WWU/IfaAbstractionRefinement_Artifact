package de.tub.pes.syscir.analysis.statespace_exploration;

import de.tub.pes.syscir.sc_model.variables.SCEvent;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class indicating that a {@link AnalyzedProcess} is waiting for an event to be called.
 * 
 * @author Jonas Becker-Kupczok
 *
 */
public final class EventBlocker extends ProcessBlocker {

    /**
     * Class modeling an event in SystemC. Equality of events is determined by identity.
     * 
     * Note that this is different from {@link SCEvent} which merely models a variable holding an event.
     * 
     * @author Jonas Becker-Kupczok
     *
     */
    public static class Event {

        private String name;

        /**
         * Constructs a new event for the given name.
         * 
         * The name is merely used for debugging purposes.
         *
         * @param name the name of this event
         */
        public Event(String name) {
            this.name = name;
        }

        /**
         * Constructs a new event for the given variable.
         * 
         * The name defaults to the name of the variable. It is merely used for debugging purposes.
         *
         * @param variable the (somewhat arbitrary) owning variable of this event
         */
        public Event(SCEvent variable) {
            this(variable.getName());
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    private final Set<Event> events;

    // true means one event is enough (e1 | e1 | ...),
    // false means all events are necessary (e1 & e2 & ...)
    private final boolean choice;

    private final TimedBlocker timeout;

    /**
     * Creates a new EventBlocker waiting for all or one of the specified events (if choice is false or
     * true resp.), or until the timeout (if not null).
     * 
     * The parameters are stored in the newly created object as is, without being copied. Modifying the
     * list of events afterwards leads to undefined behavior.
     *
     * @param events the events to wait for (may not be null or empty)
     * @param whether all events (false) or one of them (true) must be triggered to resolve this block
     * @param when to timeout this block (may be null)
     */
    public EventBlocker(Set<Event> events, boolean choice, TimedBlocker timeout) {
        this.events = Objects.requireNonNull(events);
        this.choice = choice;
        this.timeout = timeout;

        if (events.isEmpty()) {
            throw new IllegalArgumentException("events may not be empty");
        }
    }

    /**
     * Returns the events to wait for.
     * 
     * @return the events
     */
    public Set<Event> getEvents() {
        return Collections.unmodifiableSet(this.events);
    }

    /**
     * Returns a new EventBlocker waiting for the given events, with all else remaining equal.
     *
     * @param events the new events to wait for
     * @return a new EventBlocker waiting for the given events
     */
    public EventBlocker replaceEvents(Set<Event> events) {
        return new EventBlocker(events, this.choice, this.timeout);
    }

    /**
     * Returns whether or not one of the given events being triggered is enough to resolve this block.
     *
     * @return whether all events (false) or one of them (true) must be triggered to resolve this block
     */
    public boolean isChoice() {
        return this.choice;
    }

    /**
     * Returns when this block will timeout, or null if it doesn't.
     *
     * @return timeout
     */
    public TimedBlocker getTimeout() {
        return this.timeout;
    }


    /**
     * Returns a new EventBlocker waiting for the given timeout, with all else remaining equal.
     *
     * @param timeout the new timeout to wait for
     * @return a new EventBlocker waiting for the given timeout
     */
    public EventBlocker replaceTimeout(TimedBlocker timeout) {
        return new EventBlocker(this.events, this.choice, timeout);
    }

    public int timingAgnosticHashCode() {
        int result = this.events.hashCode();
        return (this.events.size() <= 1 || this.choice) ? result : ~result;
    }

    public boolean timingAgnosticEquals(EventBlocker other) {
        if (this.choice != other.choice) {
            return false;
        }
        if (!this.events.equals(other.events)) {
            return false;
        }
        if (!(this.timeout instanceof RealTimedBlocker && other.timeout instanceof RealTimedBlocker)) {
            if (!Objects.equals(this.timeout, other.timeout)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = this.events.hashCode() * 31 + Objects.hashCode(this.timeout);
        return (this.events.size() <= 1 || this.choice) ? result : ~result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EventBlocker e)) {
            return false;
        }
        return this.events.equals(e.events) && (this.events.size() <= 1 || this.choice == e.choice)
                && Objects.equals(this.timeout, e.timeout);
    }

    @Override
    public String toString() {
        String result = ""; // this.events.size() == 1 ? "Event " : "Events ";
        result += this.events.stream().map(Event::getName).collect(Collectors.joining(this.choice ? "|" : "&"));
        if (this.timeout != null) {
            result += " Timeout " + this.timeout.toString();
        }
        return result;
    }

}
