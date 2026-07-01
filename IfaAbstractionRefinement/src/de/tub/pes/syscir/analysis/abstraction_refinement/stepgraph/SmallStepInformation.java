package de.tub.pes.syscir.analysis.abstraction_refinement.stepgraph;

import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;

public class SmallStepInformation implements TransitionInformation {

    private final ReconstructionContext context;
    private final ReconstructionNode node;

    public SmallStepInformation(ReconstructionContext context, ReconstructionNode node) {
        this.context = context;
        this.node = node;
    }

    public ReconstructionNode getNode() {
        return node;
    }

    @Override
    public SmallStepInformation clone() {
        return this; // no use in shallow or deep copying
    }

    @Override
    public TransitionInformation compose(TransitionInformation other) {
        this.context.alternative(this.getNode(), ((SmallStepInformation) other).getNode());
        return this;
    }

}
