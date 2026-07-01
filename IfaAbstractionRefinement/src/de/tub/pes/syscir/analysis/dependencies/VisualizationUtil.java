package de.tub.pes.syscir.analysis.dependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.tomatengames.util.StringUtil;
import de.tomatengames.util.linked.LinkedLong;
import de.tub.pes.syscir.analysis.dependencies.DgEdge.EdgeType;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;

public class VisualizationUtil {

    public static String toMermaidGraph(Collection<String> nodes, Collection<Edge> edges) {
        StringBuilder out = new StringBuilder();
        out.append("stateDiagram-v2\n");
        for (String node : nodes)
            out.append("\t" + mermaidSanitizeNodeName(node) + "\n");
        for (Edge edge : edges) {
            out.append("\t" + mermaidSanitizeNodeName(edge.sourceNode) + " --> "
                    + mermaidSanitizeNodeName(edge.targetNode));
            if (edge.edgeTitle != null)
                out.append(": " + edge.edgeTitle);
            out.append('\n');
        }
        return out.toString();
    }

    public static class Edge {
        public String sourceNode;
        public String targetNode;
        public String edgeTitle;

        public Edge(String sourceNode, String targetNode, String title) {
            this.sourceNode = sourceNode;
            this.targetNode = targetNode;
            this.edgeTitle = title;
        }

        public Edge(String sourceNode, String targetNode) {
            this(sourceNode, targetNode, null);
        }
    }

    public static String nodeName(SdgNode node) {
        return node.getId().toString().replace(PdgInformation.class.getName(), "");
    }

    public static String mermaidSanitizeNodeName(String nodeName) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < nodeName.length(); i++) {
            char c = nodeName.charAt(i);
            if (('0' <= c && c <= '9') || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')
                    || "()[]_.,;".indexOf(c) >= 0)
                out.append(c);
            else if (c == ' ')
                out.append('_');
        }
        return out.toString();
    }

    public static String toMermaidGraph(Sdg sdg) {
        return toMermaidGraph(
                sdg.getNodes().values().stream().map(VisualizationUtil::nodeName).toList(),
                sdg.getEdges().stream().map(edge -> new Edge(nodeName(edge.getSource()),
                        nodeName(edge.getTarget()), edge.getType().name())).toList());
    }

    public static String toReducedMermaidGraph(Sdg sdg) {
        LinkedLong nextBlockId = new LinkedLong(1);
        Map<PdgInformation, Long> blockIds = new IdentityHashMap<>();
        Map<PdgNodeId, ReducedNode> nodes = new LinkedHashMap<>();
        Map<ReducedEdgeId, ReducedEdge> edges = new LinkedHashMap<>();
        for (SdgNode node : sdg.getNodes().values()) {
            blockIds.computeIfAbsent(node.getId().pdg(), pdginfo -> {
                long blockId = nextBlockId.get();
                nextBlockId.set(blockId + 1);
                return blockId;
            });
            nodes.computeIfAbsent(node.getId().id(), ReducedNode::new).occurrences.add(node);
        }
        for (SdgEdge edge : sdg.getEdges()) {
            edges.computeIfAbsent(new ReducedEdgeId(
                    edge.getSource().getId().id(),
                    edge.getTarget().getId().id(),
                    edge.getType()), ReducedEdge::new).occurrences.add(edge);
        }
        Collection<String> outputNodes = nodes.values().stream().map(node -> node.title(blockIds)).toList();
        Collection<Edge> outputEdges = new ArrayList<>();
        for (ReducedEdge edge : edges.values()) {
            outputEdges.add(new Edge(
                    nodes.get(edge.id.sourceId).title(blockIds), nodes.get(edge.id.targetId).title(blockIds),
                    edge.title(blockIds)));
        }
        return toMermaidGraph(outputNodes, outputEdges);
    }

    private static class ReducedNode {
        private final PdgNodeId id;
        private final List<SdgNode> occurrences;

        public ReducedNode(PdgNodeId id) {
            this.id = id;
            this.occurrences = new ArrayList<>();
        }

        public String title(Map<PdgInformation, Long> blockIds) {
            return this.id.toString() + " ["
                    + StringUtil.join(this.occurrences, node -> blockIds.get(node.getId().pdg()).toString(), ",") + "]";
        }
    }

    private static class ReducedEdgeId {
        private final PdgNodeId sourceId, targetId;
        private final EdgeType type;

        public ReducedEdgeId(PdgNodeId sourceId, PdgNodeId targetId, EdgeType type) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.type = type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceId, targetId, type);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ReducedEdgeId other = (ReducedEdgeId) obj;
            return Objects.equals(sourceId, other.sourceId) && Objects.equals(targetId, other.targetId)
                    && type == other.type;
        }
    }

    private static class ReducedEdge {
        private final ReducedEdgeId id;
        private final List<SdgEdge> occurrences;

        public ReducedEdge(ReducedEdgeId id) {
            this.id = id;
            this.occurrences = new ArrayList<>();
        }

        public String title(Map<PdgInformation, Long> blockIds) {
            return this.id.type.name() + " ["
                    + StringUtil.join(this.occurrences, edge -> blockIds.get(edge.getSource().getId().pdg()) + "->"
                            + blockIds.get(edge.getTarget().getId().pdg()), ",")
                    + "]";
        }
    }

}
