package de.tub.pes.syscir.analysis.dependencies;

import java.util.Set;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;

public interface CfgNode<NodeType extends CfgNode<NodeType, EdgeType, VariableType, TransitionInformationType>, EdgeType extends CfgEdge<NodeType, EdgeType>, VariableType, TransitionInformationType extends TransitionInformation> {

    AbstractedValue isVariableRead(VariableType var);

    AbstractedValue isVariableWritten(VariableType var);

    Set<VariableType> getPossiblyWrittenVariables();

    Set<EdgeType> getIncomingEdges();

    Set<EdgeType> getOutgoingEdges();

    TransitionInformationType getTransitionInformation();

}
