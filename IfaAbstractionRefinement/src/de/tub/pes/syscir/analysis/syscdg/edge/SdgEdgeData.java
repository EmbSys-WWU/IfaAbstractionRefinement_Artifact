package de.tub.pes.syscir.analysis.syscdg.edge;

/**
 * Class, that represents the former Cfg-Edge's data in a SysCDG-compliant way.
 *
 * @author twierbru
 */
public class SdgEdgeData implements EdgeData {

    /**
     * The constructor.
     */
    public SdgEdgeData() {}

    /**
     * Compares to other objects and only returns true, if the other object is CDGEdgeData with equal
     * in- and out-Nodes.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof SdgEdgeData) {
            return true;
        }
        return false;
    }

}
