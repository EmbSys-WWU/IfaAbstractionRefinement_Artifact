package de.tub.pes.syscir.analysis.abstraction_refinement.stepgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.tub.pes.syscir.analysis.dependencies.VisualizationUtil;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.sc_model.expressions.Expression;

public class ReconstructionContext {

    private final Collection<ReconstructionNode> nodes;
    Expression currentConditionExpression = null;
    AbstractedValue currentConditionValue = null;

    public ReconstructionContext() {
        this.nodes = new LinkedHashSet<>();
    }

    public Collection<ReconstructionNode> getNodes() {
        return nodes;
    }

    public SmallStepInformation wrap(ReconstructionNode node) {
        return new SmallStepInformation(this, node);
    }

    private ReconstructionNode addNode(NodeInfo info) {
        ReconstructionNode node = new ReconstructionNode(info);
        this.nodes.add(node);
        return node;
    }

    public ReconstructionNode addRoot() {
        return addNode(null);
    }

    public void addEdge(ReconstructionNode source, ReconstructionNode target) {
        source.successors.add(target);
        target.predecessors.add(source);
    }

    public ReconstructionNode append(ReconstructionNode node, NodeInfo info) {
        ReconstructionNode newNode = addNode(info);
        addEdge(node, newNode);
        return newNode;
    }

    public ReconstructionNode merge(ReconstructionNode node1, ReconstructionNode node2) {
        ReconstructionNode mergeNode = addNode(null);
        addEdge(node1, mergeNode);
        addEdge(node2, mergeNode);
        return mergeNode;
    }

    public void alternative(ReconstructionNode alternative1, ReconstructionNode alternative2) {
        alternative1.alternatives.add(alternative2);
        alternative2.alternatives.add(alternative1);
    }

    public void resolveAlternatives() {
        Collection<ReconstructionNode> oldNodes = new ArrayList<>(this.nodes);
        this.nodes.clear();
        Map<ReconstructionNode, ReconstructionNode> oldToNew = new HashMap<>();
        Set<ReconstructionNode> seen = new HashSet<>();
        for (ReconstructionNode oldNode : oldNodes) {
            if (!seen.add(oldNode))
                continue;
            ReconstructionNode newNode = new ReconstructionNode(oldNode.getInfo());
            // selects the info first found for the state during exploration -^
            // which is also the first alternative in the nodes list here
            Stack<ReconstructionNode> alternativesStack = new Stack<>();
            alternativesStack.push(oldNode);
            while (!alternativesStack.isEmpty()) {
                ReconstructionNode alternative = alternativesStack.pop();
                oldToNew.put(alternative, newNode);
                for (ReconstructionNode alt : alternative.alternatives) {
                    if (seen.add(alt))
                        alternativesStack.push(alt);
                }
            }
            this.nodes.add(newNode);
        }
        for (ReconstructionNode oldNode : oldNodes) {
            for (ReconstructionNode predecessor : oldNode.predecessors)
                oldToNew.get(oldNode).predecessors.add(oldToNew.get(predecessor));
            for (ReconstructionNode successor : oldNode.successors)
                oldToNew.get(oldNode).successors.add(oldToNew.get(successor));
        }
    }

    public String toMermaidGraph() {
        Collection<String> nodes = new ArrayList<String>();
        Collection<VisualizationUtil.Edge> edges = new ArrayList<>();
        for (ReconstructionNode node : this.nodes) {
            nodes.add(nodeNameAndId(node));
            for (ReconstructionNode successor : node.getSuccessors())
                edges.add(new VisualizationUtil.Edge(nodeNameAndId(node), nodeNameAndId(successor)));
        }
        return VisualizationUtil.toMermaidGraph(nodes, edges);
    }

    private static String nodeNameAndId(ReconstructionNode node) {
        return node.toString() + " " + System.identityHashCode(node);
    }

}
