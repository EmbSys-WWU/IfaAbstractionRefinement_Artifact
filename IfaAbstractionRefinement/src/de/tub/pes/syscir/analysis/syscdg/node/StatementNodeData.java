package de.tub.pes.syscir.analysis.syscdg.node;

import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationLocation;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.CfgLikeRecord.Node;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.StatementId;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.List;

/**
 * Class, that holds the data of a transition in the SysCDG.
 *
 * @author twierbru
 */
public class StatementNodeData extends SdgNodeData<StatementId> {

    /**
     * The constructor.
     */
    public StatementNodeData(Node node, StatementId sid) {
        super(node, sid);
    }

    /**
     * Returns the callstack contained in the StatementId used as an identifier.
     *
     * @return callstack for the represented Node
     */
    public List<EvaluationLocation> getCallStack() {
        return this.identifier.callStack();
    }

    /**
     * Returns the expression that is targeted next in the current evaluation location.
     *
     * @return Next expression at the current node.
     */
    public Expression getExpression() {
        return this.identifier.callStack().getLast().getNextExpression();
    }

    /**
     * Returns the initial value of 'this' for the represented node.
     *
     * @return Wrapper for 'this' of the represented node.
     */
    public WrappedSCClassInstance getInitialValueOfThis() {
        return this.identifier.initialThis();
    }
}
