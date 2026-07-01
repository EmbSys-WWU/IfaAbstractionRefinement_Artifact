package de.tub.pes.syscir.analysis.statespace_exploration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrent implementation of the {@link StateSpaceExploration}.
 *
 * The number of threads used can be specified in the constructor.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of {@link GlobalState} used in the exploration
 * @param <ProcessStateT> the type of {@link ProcessState} used in the exploration
 * @param <InfoT> the type of {@link TransitionInformation} supplied by the {@link AnalyzedProcess}
 * @param <ProcessT> the type of {@link AnalyzedProcess} used in the exploration
 */
// TODO: how much of the syscir is thread safe?
public class ConcurrentStateSpaceExploration extends StateSpaceExploration {

    private int numOfThreads;

    private Set<ConsideredState> exploredStates;
    private BlockingQueue<ConsideredState> worklist;
    private AtomicInteger worklistSize;
    private ThreadLocal<List<ConsideredState>> worklistCache;

    private volatile Throwable thrown;

    /**
     * Constructs a new ConcurrentStateSpaceExploration using the given scheduler, recording all state
     * transitions in the given record and beginning its exploration at the given initial states.
     * <p>
     * The number of used threads defaults to the number of available processors. All initial states are
     * locked by this constructor.
     *
     * @param scheduler the {@link Scheduler} implementation used for this exploration
     * @param record the {@link ExplorationRecord} informed about every possible state transition
     * @param initialStates the initial state from where the exploration shall be performed
     */
    public ConcurrentStateSpaceExploration(Scheduler scheduler, ExplorationRecord record,
            Set<ConsideredState> initialStates) {
        this(Runtime.getRuntime().availableProcessors(), scheduler, record, initialStates);
    }

    /**
     * Constructs a new ConcurrentStateSpaceExploration using the given scheduler, recording all state
     * transitions in the given record and beginning its exploration at the given initial states.
     * <p>
     * All initial states are locked by this constructor.
     *
     * @param numOfThreads the number of concurrent threads used for the exploration
     * @param scheduler the {@link Scheduler} implementation used for this exploration
     * @param record the {@link ExplorationRecord} informed about every possible state transition
     * @param initialStates the initial state from where the exploration shall be performed
     * @throws IllegalArgumentException if numOfThreads is less than 1
     */
    public ConcurrentStateSpaceExploration(int numOfThreads, Scheduler scheduler, ExplorationRecord record,
            Set<ConsideredState> initialStates) {
        super(scheduler, record);

        if (numOfThreads < 1) {
            throw new IllegalArgumentException("needs at least 1 thread");
        }
        this.numOfThreads = numOfThreads;

        this.exploredStates = ConcurrentHashMap.newKeySet();
        this.worklist = new LinkedBlockingQueue<>();
        this.worklistSize = new AtomicInteger(initialStates.size());

        for (ConsideredState state : initialStates) {
            this.worklist.add(state);
            state.lock();
        }

        // worklistCache is initialized in #explore() to catch illegal additional invocations
    }

    /**
     * {@inheritDoc}
     *
     * If this method is invoked again while the exploration is already running, an
     * {@link IllegalStateException} is thrown.
     *
     * @throws IllegalStateException if the exploration is already running
     */
    @Override
    public void run() throws IllegalStateException {
        // check for additional invocations
        synchronized (this) {
            if (this.worklistCache != null) {
                throw new IllegalStateException("exploration is already running");
            }
            this.worklistCache = new ThreadLocal<>();
        }

        // create desired number of threads
        Thread[] threads = new Thread[this.numOfThreads];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread() {

                @Override
                public void run() {
                    runExplorationThread(threads);
                }
            };
        }

        // start threads
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        // join on threads to wait for exploration to finish
        for (int i = 0; i < threads.length; i++) {
            while (true) {
                try {
                    threads[i].join();
                    break;
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        // if an exception was thrown during the exploration, rethrow it. otherwise, return normally.
        if (this.thrown != null) {
            if (this.thrown instanceof RuntimeException re) {
                throw re;
            } else if (this.thrown instanceof Error er) {
                throw er;
            }
            throw new RuntimeException(this.thrown);
        }

        done();
    }

    private void runExplorationThread(Thread[] allThreads) {
        /*
         * This exploration works just like the sequential one, except for additional bookkeeping requried
         * by its concurrent nature.
         *
         * To ensure that the thread terminates once all state have been explored, but not any sooner, the
         * worklistSize is atomically updated after one state has been dealt with. If the resulting size
         * becomes zero, this thread interrupts all others (so that they immediately query the new size and
         * terminate themselves) and then terminates. Otherwise, it continues its exploration normally.
         *
         * If at any point an uncaught exception is thrown, this thread stores it in the field thrown,
         * abortin the exploration and causing #explore() to terminate by rethrowing the exception.
         */

        // store the current explorer such that internally used classes can access it without an explicit
        // reference. there is no need to reset the value because this thread dies at the same time the
        // value is no longer needed.
        setCurrentExplorer(this);

        // create a cache in which to collect newly reached states. if states were to be added to the
        // worklist directly (before the worklistSize i updated), other threads might already work on
        // them and reduce the worklistSize (potentially to zero, terminating prematurely)
        List<ConsideredState> worklistCache = new ArrayList<>();
        this.worklistCache.set(worklistCache);

        int leftInWorklist;
        do {
            try {
                // if aborted, return immediately
                if (isAborted()) {
                    return;
                }

                // poll the worklist for a state to consider, waiting if necessary
                ConsideredState currentState = ConcurrentStateSpaceExploration.this.worklist.poll(1, TimeUnit.SECONDS);
                // if the poll timed out, update the worklistSize and repeat (if still > 0)
                if (currentState == null) {
                    leftInWorklist = ConcurrentStateSpaceExploration.this.worklistSize.get();
                    continue;
                }

                // attempt to add the state to the set of explored states
                if (ConcurrentStateSpaceExploration.this.exploredStates.add(currentState)) {
                    // store to detect possible timing loops
                    // this.possibleTimingLoops.putIfAbsent(currentState, currentState);

                    // if the state was successfully added, explore all followup states (adding them to the
                    // worklistCache)
                    explorationStep(currentState);

                    // first update the worklistSize, then actually add the newly reached states (see above)
                    leftInWorklist =
                            ConcurrentStateSpaceExploration.this.worklistSize.addAndGet(worklistCache.size() - 1);
                    this.worklist.addAll(worklistCache);

                    // clear the worklistCache
                    worklistCache = new ArrayList<>();
                    this.worklistCache.set(worklistCache);
                } else {
                    // otherwise, no states are added to the worklist, but one was removed
                    leftInWorklist = ConcurrentStateSpaceExploration.this.worklistSize.decrementAndGet();
                }
            } catch (InterruptedException e) {
                // if interrupted, update the worklistSize
                leftInWorklist = ConcurrentStateSpaceExploration.this.worklistSize.get();
            } catch (ExplorationAbortedException e) {
                // if aborted, return immediately
                return;
            } catch (Throwable t) {
                // catch any uncaught exception, store it (signaling abortion of the exploration) and interrupt all
                // other threads (so that they terminate), then terminate this thread
                abort();
                this.thrown = t;
                for (Thread other : allThreads) {
                    other.interrupt();
                }
                return;
            }
        } while (leftInWorklist > 0);

        // after exhausting the worklist, interrupt all other threads so that they update the worklistSize
        // and terminate as well
        for (Thread t : allThreads) {
            t.interrupt();
        }

    }

    @Override
    protected void handleExploration(ConsideredState from, Collection<ProcessTransitionResult> transitions) {
        List<ConsideredState> worklistCache = this.worklistCache.get();
        for (ProcessTransitionResult transition : transitions) {
            worklistCache.add(transition.resultingState());

            getRecord().explorationMade(from, transition.resultingState(), transition.transitionInformation());
        }
    }

    @Override
    public int getNumPendingStates() {
        return this.worklistSize.get();
    }

    @Override
    public int getNumExploredStates() {
        return this.exploredStates.size();
    }

    @Override
    public Set<ConsideredState> getExploredStates() {
        return Collections.unmodifiableSet(this.exploredStates);
    }

}
