package de.tub.pes.syscir.analysis.statespace_exploration.transition_informations;

import java.util.Set;

import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.InformationHandler;
import de.tub.pes.syscir.sc_model.expressions.Expression;

/**
 * A trivial implementation of {@link TransitionInformation} representing no information at all.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public class NoInformation implements TransitionInformation {

    public static final NoInformation INSTANCE = new NoInformation();

    public static final InformationHandler HANDLER = new NoInformationHandler();

    public static class NoInformationHandler implements InformationHandler {

        @Override
        public NoInformation getInitialInformation(ConsideredState state) {
            return INSTANCE;
        }

        @Override
        public NoInformation handleExpressionEvaluation(Expression evaluated, int comingFrom,
                TransitionResult resultingState, LocalState localState) {
            return INSTANCE;
        }

        @Override
        public NoInformation handleProcessWaitedForDelta(AnalyzedProcess process,
                ProcessState resultingState, TransitionInformation currentInformation) {
            return INSTANCE;
        }

        @Override
        public NoInformation handleProcessWaitedForTime(AnalyzedProcess process,
                ProcessState resultingState, TransitionInformation currentInformation) {
            return INSTANCE;
        }

        @Override
        public NoInformation handleProcessWaitedForEvents(AnalyzedProcess process,
                ProcessState resultingState, Set<Event> events, EventBlocker blockerBefore,
                TransitionInformation currentInformation) {
            return INSTANCE;
        }

        @Override
        public NoInformation handleStartOfCode(TransitionResult currentState, LocalState localState) {
            return INSTANCE;
        }

    };

    private NoInformation() {}

    @Override
    public NoInformation clone() {
        return this;
    }

    @Override
    public TransitionInformation compose(TransitionInformation other) {
        return this;
    }

    @Override
    public String toString() {
        return "";
    }

}
