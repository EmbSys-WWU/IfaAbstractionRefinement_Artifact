package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.abstraction_refinement.SourceExpressions.ExpressionLocation;
import de.tub.pes.syscir.analysis.dependencies.DgEdge.EdgeType;
import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeType;
import de.tub.pes.syscir.analysis.dependencies.Sdg;
import de.tub.pes.syscir.analysis.dependencies.SdgEdge;
import de.tub.pes.syscir.analysis.dependencies.SdgNode;
import de.tub.pes.syscir.analysis.dependencies.SdgNode.SdgNodeId;
import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationLocation;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.GlobalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgInformationHandler;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.StatementId;
import de.tub.pes.syscir.analysis.util.WrappedSCFunction;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.IfElseExpression;
import de.tub.pes.syscir.sc_model.expressions.LoopExpression;
import de.tub.pes.syscir.sc_model.expressions.SwitchExpression;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Static utility methods used for abstraction refinement.
 * 
 * @author Lukas Ernst
 */
public class RefinementUtil {
    
    record AbstractionExplorationResult(Sources trackedElements, long successfulExecutionTime) {}
    
    public static AbstractionExplorationResult findExplorableAbstraction(RefinementConfig config) {
        return findExplorableAbstraction(config, config.initialAbstraction(), -1);
    }
    
    /**
     * Uses exploration refinement to find an explorable abstraction that tracks at least the specified
     * sources.
     */
    public static AbstractionExplorationResult findExplorableAbstraction(RefinementConfig config, Sources track,
            long timeoutMillis) {
        ExplorationRefinement r = new ExplorationRefinement(config, track, timeoutMillis);
        if (r.run()) {
            return new AbstractionExplorationResult(r.trackedElements, r.getLastExecutionDuration());
        }
        return null;
    }
    
    /**
     * Generates an SDG based on the specified abstraction / tracked sources.
     */
    public static Sdg informationFlow(RefinementConfig config, Sources trackedElements) {
        return new InformationFlowExploration(config, trackedElements).run();
    }
    
    public static Expression getNodeExpression(PdgNodeId pdgNodeId) {
        if (pdgNodeId.type() != NodeType.STATEMENT || !(pdgNodeId.identifier() instanceof StatementId statement)) {
            return null;
        }
        return statement.callStack().getLast().getNextExpression();
    }
    
    public static Expression getNodeExpression(SdgNode node) {
        return getNodeExpression(node.getId().id());
    }
    
    public static Set<ExpressionLocation> getNodeExpressionLocations(SdgNode node) {
        return getNodeExpressionLocations(node.getId().id());
    }
    
    public static Set<ExpressionLocation> getNodeExpressionLocations(PdgNodeId pdgNodeId) {
        if (pdgNodeId.type() != NodeType.STATEMENT || !(pdgNodeId.identifier() instanceof StatementId statement)) {
            return Set.of();
        }
        WrappedSCFunction base = statement.callStack().getFirst().getFunction();
        List<EvaluationLocation> calls = statement.callStack();
        calls = calls.subList(0, calls.size() - 1);
        List<Integer> indices = new ArrayList<>(statement.callStack().getLast().getExpressionIndices());
        Set<ExpressionLocation> result = new LinkedHashSet<>();
        ExpressionLocation current = new ExpressionLocation(new StackTraceView.StackTrace(base, calls), indices);
        gatherChildLocations(current.getExpression(), current, result);
        return result;
    }
    
    private static void gatherChildLocations(Expression current, ExpressionLocation location,
            Set<ExpressionLocation> output) {
        output.add(ExpressionLocation.copyOf(location.stack(), location.location()));
        for (int i = 0; i < current.getNumOfChildren(); i++) {
            Expression child = current.getChild(i);
            if (PdgInformationHandler.formsOwnNode(child)) {
                continue;
            }
            location.location().add(i);
            gatherChildLocations(child, location, output);
            location.location().removeLast();
        }
    }
    
    public static Set<Variable<?, ?>> getNodeWrittenVariables(SdgNode node) {
        if (node.getType() != NodeType.STATEMENT) {
            return Set.of();
        }
        return node.getOutgoing().stream().filter(edge -> edge.getType() == EdgeType.DATA)
                .map(e -> (Variable<?, ?>) e.getData()).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    public static void addSourcesFromNode(SdgNode node, Sources to) {
        Set<ExpressionLocation> locations = getNodeExpressionLocations(node);
        for (ExpressionLocation location : locations) {
            to.add(location.stack(), location.location());
            to.addRetains(location.getExpression()); // TODO correct?
        }
        Set<Variable<?, ?>> variables = getNodeWrittenVariables(node);
        for (Variable<?, ?> variable : variables) {
            to.add(variable);
        }
    }
    
    /**
     * Returns the amount of branches the specified expression is expected to spawn when evaluating to
     * unknown. It returns 1 for any non-control-condition but possibly more for control conditions.
     */
    public static int getPotentialBranchCount(Expression expression) {
        if (expression == null) {
            return 1;
        }
        Expression parent = expression.getParent();
        if (parent instanceof IfElseExpression ie) {
            return ie.getCondition() == expression ? 2 : 1;
        }
        if (parent instanceof LoopExpression le) {
            return le.getCondition() == expression ? 2 : 1;
        }
        if (parent instanceof SwitchExpression se) {
            return se.getSwitchExpression() == expression ? se.getCases().size() : 1;
        }
        return 1;
    }
    
    public static boolean isControlCondition(Expression expression) {
        if (expression == null) {
            return false;
        }
        Expression parent = expression.getParent();
        if (parent instanceof IfElseExpression ie) {
            return ie.getCondition() == expression;
        }
        if (parent instanceof LoopExpression le) {
            return le.getCondition() == expression;
        }
        if (parent instanceof SwitchExpression se) {
            return se.getSwitchExpression() == expression;
        }
        return false;
    }
    
    public static boolean isControlCondition(PdgNodeId pdgNodeId) {
        return isControlCondition(getNodeExpression(pdgNodeId));
    }
    
    public static boolean isControlCondition(SdgNode node) {
        return isControlCondition(getNodeExpression(node));
    }
    
    public static boolean isWaitStatement(Expression expression) {
        if (!(expression instanceof FunctionCallExpression fe)) {
            return false;
        }
        return fe.getFunction().getName().equals("wait");
    }
    
    public static boolean isWaitStatement(PdgNodeId pdgNodeId) {
        return isWaitStatement(getNodeExpression(pdgNodeId));
    }
    
    public static boolean isWaitStatement(SdgNode node) {
        return isWaitStatement(getNodeExpression(node));
    }
    
    public static Collection<SdgNode> listMembers(SdgNode node) {
        return node.getOutgoing().stream().filter(edge -> edge.getType() == EdgeType.MEMBER).map(SdgEdge::getTarget)
                .toList();
    }
    
    /**
     * Recursively finds control dependences of the specified SDG node, which are all nodes that have a
     * path to the specified SDG node that only consists of CONTROL edges or block-/event-triggers.
     */
    public static Collection<SdgNode> controlDependencies(SdgNode node) {
        Set<SdgNodeId> visited = new LinkedHashSet<>();
        Collection<SdgNode> output = new ArrayList<>();
        gatherControlDependencies(node, visited, output);
        return output;
    }
    
    private static void gatherControlDependencies(SdgNode node, Set<SdgNodeId> visited, Collection<SdgNode> output) {
        Object identifier = node.getId().id().identifier();
        boolean isTrigger = (identifier instanceof AnalyzedProcess)
                || (identifier instanceof GlobalVariable<?, ?> gv && (gv.isBlockTrigger() || gv.isEventTrigger()));
        for (SdgEdge edge : node.getIncoming()) {
            if (edge.getType() != EdgeType.CONTROL && !isTrigger) {
                continue;
            }
            SdgNode source = edge.getSource();
            if (!visited.add(source.getId())) {
                continue;
            }
            output.add(source);
            gatherControlDependencies(source, visited, output);
        }
    }
    
}
