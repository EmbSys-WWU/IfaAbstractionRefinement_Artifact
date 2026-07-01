package de.tub.pes.syscir.analysis.dependencies;

import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;

public interface CfgCallNode<NodeType extends CfgNode<NodeType, EdgeType, VariableType, TransitionInformationType>, EdgeType extends CfgEdge<NodeType, EdgeType>, VariableType, TransitionInformationType extends TransitionInformation, CallTargetType extends Callable>
extends CfgNode<NodeType, EdgeType, VariableType, TransitionInformationType> {

    CallTargetType getCalled();

}
