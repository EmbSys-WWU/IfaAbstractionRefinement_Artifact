package de.tub.pes.syscir.analysis.statespace_exploration;

import java.util.function.Function;

import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.TwoInformations;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.TwoInformationsHandler;

/**
 * Interface describing the result of taking a transition, consisting of the resulting state as well
 * as potentially some additional information provided by the {@link AnalyzedProcess} or
 * {@link Scheduler}.
 * 
 * @author Jonas Becker-Kupczok
 *
 */
public interface TransitionResult {

    /**
     * Returns the state in which the transition results.
     *
     * @return resulting state
     */
    ConsideredState resultingState();

    /**
     * Returns the global portion of the state in which the transition results.
     *
     * @return resulting global state
     */
    default GlobalState globalState() {
        return resultingState().getGlobalState();
    }

    /**
     * Returns the additional information provided for this transition.
     *
     * @return transition information
     */
    TransitionInformation transitionInformation();

    /**
     * Returns a (shallow) copy of this result with the given resulting state and all else being equal.
     *
     * @param state the new resulting state
     * @return copy of this result with the given resulting state
     */
    TransitionResult replaceResultingState(ConsideredState state);

    /**
     * Returns a (shallow) copy of this result with the given transition information and all else being
     * equal.
     *
     * @param state the new resulting state
     * @return copy of this result with the given resulting state
     */
    TransitionResult replaceTransitionInformation(TransitionInformation transitionInformation);

    /**
     * Returns a deep copy of this result. The resulting state of the copy will be unlocked.
     *
     * @return deep copy
     */
    TransitionResult clone();

    /**
     * Returns a view on this TransitionResult with a masked TransitionInformation. All interface
     * methods are supported by the result.
     * <p>
     * The result of any call to {@link #transitionInformation()} is replaced by invocing the given
     * maskings function. For any call to {@link #replaceTransitionInformation(TransitionInformation)},
     * the parameter is first replaced by invocing the reverse masking function.
     *
     * @see TwoInformations
     * @see TwoInformationsHandler
     *
     * @param <X> the type of the result
     * @param <OtherInfoT> some type of transition information
     * @param mask a function replacing the original type of transition information (InfoT) with the the
     *        new type (OtherInfoT)
     * @param reverseMask the reverse of the mask function
     * @return a view on this TransitionResult with masked information
     */
    default TransitionResult maskTransitionInformation(Function<TransitionInformation, TransitionInformation> mask,
            Function<TransitionInformation, TransitionInformation> reverseMask) {
        return new TransitionResult() {

            @Override
            public ConsideredState resultingState() {
                return TransitionResult.this.resultingState();
            }

            @Override
            public TransitionInformation transitionInformation() {
                return mask.apply(TransitionResult.this.transitionInformation());
            }

            @Override
            public TransitionResult replaceResultingState(ConsideredState state) {
                return TransitionResult.this.replaceResultingState(state).maskTransitionInformation(mask, reverseMask);
            }

            @Override
            public TransitionResult replaceTransitionInformation(TransitionInformation transitionInformation) {
                return TransitionResult.this.replaceTransitionInformation(reverseMask.apply(transitionInformation))
                        .maskTransitionInformation(mask, reverseMask);
            }

            @Override
            public TransitionResult clone() {
                return TransitionResult.this.clone().maskTransitionInformation(mask, reverseMask);
            }
        };
    }

}
