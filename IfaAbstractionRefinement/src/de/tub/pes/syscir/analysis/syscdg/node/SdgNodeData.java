package de.tub.pes.syscir.analysis.syscdg.node;

import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.CfgLikeRecord.Node;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class, that holds a node's data in the SysCDG.
 *
 * @author twierbru
 */
public abstract class SdgNodeData<IdentificationObject> implements NodeData {

    protected final ConsideredState fromState;
    protected final ConsideredState toState;
    protected final TransitionInformation information;
    protected final int hashCode;
    protected final IdentificationObject identifier;

    /**
     * The constructor.
     */
    protected SdgNodeData(Node node, IdentificationObject identifier) {
        this.fromState = node.getFromState();
        this.toState = node.getToState();
        this.information = node.getTransitionInformation();
        this.identifier = identifier;
        hashCode = Objects.hash(this.fromState, this.toState, this.information, this.identifier);
    }

    public ConsideredState getFromState() {
        return this.fromState;
    }

    public ConsideredState getToState() {
        return this.toState;
    }

    public TransitionInformation getTransitionInformation() {
        return this.information;
    }

    public IdentificationObject getIdentifier() {
        return this.identifier;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Compares CDGNodes based on their ID and the Pdg, they appear in.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof SdgNodeData<?> other) {
            if (fromState == other.getFromState() && toState == other.getToState()
                    && information == other.getTransitionInformation()
                    && this.identifier.equals(other.getIdentifier())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Presents information about the saved pdg, which this NodeData is identified by.
     */
    @Override
    public String toString() {
        return Stream.of(Objects.toIdentityString(this.information), this.identifier).filter(Objects::nonNull)
                .map(String::valueOf).collect(Collectors.joining(" ", "(", ")"));
    }
}
