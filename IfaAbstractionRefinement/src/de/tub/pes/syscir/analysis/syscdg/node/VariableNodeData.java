package de.tub.pes.syscir.analysis.syscdg.node;

import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.CfgLikeRecord.Node;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;

/**
 * Class, that holds the data of a variable in the SysCDG.
 *
 * @author twierbru
 */
public class VariableNodeData extends SdgNodeData<Variable<?, ?>> {

    /**
     * The constructor.
     */
    public VariableNodeData(Node node, Variable<?, ?> variable) {
        super(node, variable);
    }
}
