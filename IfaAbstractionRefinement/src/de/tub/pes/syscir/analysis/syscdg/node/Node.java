package de.tub.pes.syscir.analysis.syscdg.node;

import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeType;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;
import de.tub.pes.syscir.analysis.syscdg.SecurityLevel;
import java.util.Objects;

/**
 * The abstract implementation of node.
 *
 * @param <N> is a node type.
 * @param <S> is a security level type.
 * @param <ND> is a node data type.
 *
 * @author Jan Kirchner
 */
public abstract class Node<S extends SecurityLevel<S>, ND extends NodeData> {

    private final ND nodeData;
    private final S providedSecurityLevel;
    private final S requiredSecurityLevel;
    private final boolean declassification;
    private final int hashCode;

    /**
     * The constructor of the node.
     *
     * @param nodeData The node data of the node
     * @param providedSecurityLevel The provided security level of the node
     * @param requiredSecurityLevel The required security level of the node
     * @param declassification whether the node is a declassification node
     */
    public Node(ND nodeData, S providedSecurityLevel, S requiredSecurityLevel, boolean declassification) {
        this.nodeData = nodeData;
        this.providedSecurityLevel = providedSecurityLevel;
        this.requiredSecurityLevel = requiredSecurityLevel;
        this.declassification = declassification;

        this.hashCode = Objects.hash(nodeData, providedSecurityLevel, requiredSecurityLevel, declassification, this.getType());
    }

    /**
     * This method retrieves the type of this node according to the usual NodeTypes used in Pdgs.
     *
     * @return type of this node
     */
    public abstract NodeType getType();

    /**
     * This method reconstructs the PdgId corresponding to this node if possible.
     *
     * @return PdgNodeId of the represented PdgNode
     */
    public PdgNodeId getPdgNodeId() {
        if (this.nodeData instanceof SdgNodeData nd) {
            return new PdgNodeId(this.getType(), nd.getIdentifier());
        }
        return null;
    }

    /**
     * The getter for the node data.
     *
     * @return nodeData the node data of the node.
     */
    public ND getNodeData() {
        return nodeData;
    }

    public boolean isDeclassification() {
        return declassification;
    }

    /**
     * The getter for the provided security level.
     *
     * @return providedSecurityLevel the provided security level of the node.
     */
    public S getProvidedSecurityLevel() {
        return providedSecurityLevel;
    }

    /**
     * The getter for the required security level.
     *
     * @return requiredSecurityLevel the required security level of the node.
     */
    public S getRequiredSecurityLevel() {
        return requiredSecurityLevel;
    }

    /**
     * The equals functionality for the node. It compares the instance of the object with a given
     * instance. If the given instance is null or the class of the given instance is not equal to the
     * class of the instance, the method returns false. Otherwise it compares the node data, the
     * expression, the provided and required security level of the instance in combination with the
     * given instance.
     *
     * @param comparedObject the object which is compared with the instance.
     * @return true if the instance is equal to the given object, otherwise false.
     */
    @Override
    public boolean equals(Object comparedObject) {
        if (this == comparedObject)
            return true;
        if (comparedObject == null || getClass() != comparedObject.getClass())
            return false;
        Node<?, ?> comparedNode = (Node<?, ?>) comparedObject;
        return nodeData.equals(comparedNode.nodeData)
                && providedSecurityLevel.equals(comparedNode.providedSecurityLevel)
                && requiredSecurityLevel.equals(comparedNode.requiredSecurityLevel);
    }

    /**
     * The hashCode functionality for the node. It returns the hashCode of the node data, the
     * expression, the provided security level and the required security level. If the sdg is not
     * immutable, the hashCode is calculated every time. Otherwise it is calculated only once and stored
     * in the finalHashCode variable.
     *
     * @return the hashCode of the node.
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return switch (this.getType()) {
            case NodeType.ENTRY -> "Entry-Node: [" + this.nodeData.toString() + "]";
            case NodeType.IN -> "In-Node: [" + this.nodeData.toString() + "]";
            case NodeType.OUT -> "Out-Node: [" + this.nodeData.toString() + "]";
            case NodeType.STATEMENT -> "Statement-Node: [" + this.nodeData.toString() + "]";
            case null, default -> "Node: [" + this.nodeData.toString() + "]";
        };
    }
}
