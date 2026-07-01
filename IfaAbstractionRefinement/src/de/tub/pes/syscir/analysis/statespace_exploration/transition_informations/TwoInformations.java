package de.tub.pes.syscir.analysis.statespace_exploration.transition_informations;

import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;

public record TwoInformations(TransitionInformation first, TransitionInformation second)
        implements TransitionInformation {
    
    public TwoInformations setFirst(TransitionInformation first) {
        return new TwoInformations(first, this.second);
    }
    
    public TwoInformations setSecond(TransitionInformation second) {
        return new TwoInformations(this.first, second);
    }
    
    @Override
    public TwoInformations clone() {
        return new TwoInformations(this.first.clone(), this.second.clone());
    }
    
    @Override
    public TwoInformations compose(TransitionInformation other) {
        TwoInformations info2 = (TwoInformations) other;
        return new TwoInformations(this.first.compose(info2.first), this.second.compose(info2.second));
    }
    
}
