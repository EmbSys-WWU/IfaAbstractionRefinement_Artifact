package de.tub.pes.syscir.analysis.dependencies;

import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeId;
import de.tub.pes.syscir.analysis.dependencies.SdgNode.SdgNodeId;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SdgNode extends DgNode<SdgNode, SdgNodeId, SdgEdge> {
    
    public static class SdgNodeId implements NodeId {
        
        private static final int PDG_CLASS_NAME_LENGTH = PdgInformation.class.getName().length() + 1;
        
        // TODO: using the pdg identity is kind of ugly, better ideas?
        private final PdgInformation pdg;
        private final PdgNodeId id;
        
        public SdgNodeId(PdgInformation pdg, PdgNodeId id) {
            this.pdg = pdg;
            this.id = id;
        }
        
        public SdgNodeId(PdgInformation pdg, NodeType type, Object identifier) {
            this(pdg, new PdgNodeId(type, identifier));
        }
        
        public PdgInformation pdg() {
            return this.pdg;
        }
        
        public PdgNodeId id() {
            return this.id;
        }
        
        @Override
        public NodeType type() {
            return this.id.type();
        }
        
        @Override
        public int hashCode() {
            return System.identityHashCode(this.pdg) ^ this.id.hashCode();
        }
        
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof SdgNodeId snid)) {
                return false;
            }
            return this.pdg == snid.pdg && Objects.equals(this.id, snid.id);
        }
        
        @Override
        public String toString() {
            return Stream
                    .of(this.id.type(), Objects.toIdentityString(this.pdg).substring(PDG_CLASS_NAME_LENGTH),
                            this.id.identifier())
                    .filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(" ", "(", ")"));
        }
    }
    
    public SdgNode(SdgNode copyOf) {
        super(copyOf);
    }
    
    public SdgNode(SdgNodeId id) {
        super(id);
    }
    
    public Set<SdgNode> backwardsSlice() {
        Set<SdgNode> result = new LinkedHashSet<>(Set.of(this));
        Deque<SdgNode> worklist = new ArrayDeque<>(List.of(this));
        
        while (!worklist.isEmpty()) {
            SdgNode current = worklist.poll();
            for (SdgEdge edge : current.getIncoming()) {
                if (result.add(edge.getSource())) {
                    worklist.add(edge.getSource());
                }
            }
        }
        
        return result;
    }
    
    @Override
    public SdgNode unlockedClone() {
        return new SdgNode(this);
    }
    
}
