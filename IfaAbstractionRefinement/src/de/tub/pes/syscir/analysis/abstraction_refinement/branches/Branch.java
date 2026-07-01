package de.tub.pes.syscir.analysis.abstraction_refinement.branches;

public class Branch {

    private final Branching origin;

    public Branch(Branching origin) {
        this.origin = origin;
    }

    public Branching getOrigin() {
        return origin;
    }

}
