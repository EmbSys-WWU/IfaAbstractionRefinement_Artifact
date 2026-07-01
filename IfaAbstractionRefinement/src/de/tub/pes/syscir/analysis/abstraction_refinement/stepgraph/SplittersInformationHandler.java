package de.tub.pes.syscir.analysis.abstraction_refinement.stepgraph;

import de.tub.pes.syscir.sc_model.SCFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class SplittersInformationHandler extends StepGraphInformationHandler {
    
    @Override
    protected void finishStep(ReconstructionContext stepGraph) {
        Collection<ReconstructionNode> nodes = stepGraph.getNodes();
        List<ReconstructionNode> splitters = new ArrayList<>();
        for (ReconstructionNode node : nodes) {
            Collection<ReconstructionNode> branches = node.getSuccessors();
            if (branches.size() < 2) {
                continue;
            }
            SCFunction containingFunction = node.getInfo().getExpression().getContainingFunction();
            if (containingFunction != null && containingFunction.getSCClass().isPrimitiveChannel()
                    && !containingFunction.getSCClass().getName().contains("fifo")) {
                continue;
            }
            Set<ReconstructionNode> leaves = null;
            for (ReconstructionNode branch : branches) {
                if (leaves == null) {
                    leaves = branch.reachableLeaves();
                } else if (!leaves.equals(branch.reachableLeaves())) {
                    splitters.add(node);
                    break;
                }
            }
        }
        handleSplitters(splitters);
    }
    
    protected abstract void handleSplitters(Collection<ReconstructionNode> splitters);
}
