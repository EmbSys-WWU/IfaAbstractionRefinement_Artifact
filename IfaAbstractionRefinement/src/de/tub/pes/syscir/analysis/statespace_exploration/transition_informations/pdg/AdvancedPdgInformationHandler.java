package de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg;

import static de.tub.pes.syscir.analysis.util.CollectionUtil.nullSet;
import static de.tub.pes.syscir.analysis.util.WrapperUtil.wrap;

import de.tub.pes.syscir.analysis.dependencies.DgEdge.EdgeType;
import de.tub.pes.syscir.analysis.dependencies.DgNode.NodeType;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationLocation;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ExpressionCrawler.InsufficientValueTrackingException;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.GlobalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.StatementId;
import de.tub.pes.syscir.analysis.util.LockableObject;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCFunction;
import de.tub.pes.syscir.sc_model.expressions.EventNotificationExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Information handler providing an advanced program dependence graph (PDG) which, in addition to
 * the "simple" PDG captured by {@link PdgInformationHandler}, also captures information about the
 * triggering of code outside the current transition.
 *
 * These triggers are treated as variables (see
 * {@link GlobalVariable#blockTrigger(WrappedSCClassInstance, List)} and
 * {@link GlobalVariable#eventTrigger(Event)}) with in/out nodes where appropriate. Entry nodes are
 * considered to be reading their respective block trigger, while wait, notify and request_update
 * nodes write their respective block or event trigger. When an event is actually notified (i.e.,
 * after the time specified in the notification has elapsed), the event trigger is read and the
 * block trigger is written.
 * <p>
 * When combining the PDGs for several transitions into an SDG, it must be taken into account that
 * trigger variables should be treated differently to normal variables: writing a trigger variable
 * doesn't (necessarily) kill its other reaching definitions, whereas reading it does.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <ValueT> the type of abstracted value used in the exploration
 */
public class AdvancedPdgInformationHandler extends PdgInformationHandler {

    private ThreadLocal<Event> announcedEvent;

    public AdvancedPdgInformationHandler() {
        super();

        this.announcedEvent = new ThreadLocal<>();
    }

    @Override
    public PdgInformation handleStartOfCode(TransitionResult currentState, LocalState localState) {
        PdgInformation currentInformation = super.handleStartOfCode(currentState, localState);

        PdgNode entryNode = currentInformation.getCurrentEntryNode();
        List<EvaluationLocation> resumptionLocation = localState.getExecutionStack().stream()
                .map(EvaluationContext::toLocation).collect(Collectors.toCollection(ArrayList::new));
        List<Integer> resumptionIndices = resumptionLocation.getLast().getExpressionIndices();
        resumptionIndices.add(localState.getTopOfStack().getComingFrom() + 1);
        GlobalVariable<WrappedSCClassInstance, List<EvaluationLocation>> blockTrigger =
                GlobalVariable.blockTrigger(localState.getInitialThisValue(), resumptionLocation);
        PdgNode triggerInNode =
                currentInformation.getNodes().computeIfAbsent(new PdgNodeId(NodeType.IN, blockTrigger), PdgNode::new);
        new PdgEdge(EdgeType.CONTROL, triggerInNode, entryNode, null, true);

        return currentInformation;
    }

    @Override
    public void announceEvaluation(Expression expression, TransitionResult currentState, LocalState localState) {
        super.announceEvaluation(expression, currentState, localState);

        if (expression instanceof EventNotificationExpression ee
                && localState.getTopOfStack().getComingFrom() == expression.getNumOfChildren() - 1) {
            // the notified event will no longer be stored in the local state after the evaluation has finished.
            // store it to avoid recomputing what the expression crawler already computed

            AbstractedValue eventValue = localState.getTopOfStack().getExpressionValue(0, 0);
            if (!eventValue.isDetermined()) {
                throw new InsufficientValueTrackingException(eventValue);
            }
            this.announcedEvent.set((Event) eventValue.get());
        }
    }

    @Override
    public PdgInformation handleProcessWaitedForEvents(AnalyzedProcess process, ProcessState resultingState,
            Set<Event> notifiedEvents, EventBlocker effectedBlocker, TransitionInformation currentInfo) {
        // ignore irrelevant events
        notifiedEvents = new LinkedHashSet<>(notifiedEvents);
        notifiedEvents.retainAll(effectedBlocker.getEvents());

        PdgInformation currentPdgInfo = LockableObject.unlockedVersion((PdgInformation) currentInfo);
        PdgNode currentNode;

        /*
         * note that for immediate notifications, this method is called _before_ handleNotify(...). so,
         * immediate and non-immediate notifications can be distinguished by whether or not an announced
         * event is present. for immediate notifications, we skip the event trigger variable all together,
         * directly using the appropriate block triggers.
         */

        Event announcedEvent = this.announcedEvent.get();
        if (announcedEvent != null) {
            // immediate notification
            assert notifiedEvents.equals(Set.of(announcedEvent));
            currentNode = currentPdgInfo.getNodes().computeIfAbsent(
                    new PdgNodeId(NodeType.STATEMENT,
                            new StatementId(resultingState.getInitialThisValue(), getAnnouncedLocation())),
                    PdgNode::new);
        } else {
            // non-immediate notification
            currentNode =
                    currentPdgInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.STATEMENT, process), PdgNode::new);

            // read event triggers
            for (Event event : notifiedEvents) {
                GlobalVariable<Event, ?> triggerVar = GlobalVariable.eventTrigger(event);
                Set<PdgNode> triggers = currentPdgInfo.getReachingDefs().getOrDefault(triggerVar, nullSet());
                for (PdgNode trigger : triggers) {
                    if (trigger == null) {
                        trigger = currentPdgInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.IN, triggerVar),
                                PdgNode::new);
                    }
                    PdgEdge edge = new PdgEdge(EdgeType.DATA, trigger, currentNode, triggerVar, false);
                    edge.insert();
                }
            }
        }

        // write block trigger
        List<EvaluationLocation> resumptionLocation = resultingState.getExecutionStack().stream()
                .map(EvaluationContext::toLocation).collect(Collectors.toCollection(ArrayList::new));
        List<Integer> resumptionIndices = resumptionLocation.getLast().getExpressionIndices();
        resumptionIndices.add(resultingState.getTopOfStack().getComingFrom() + 1);
        GlobalVariable<WrappedSCClassInstance, List<EvaluationLocation>> blockTrigger =
                GlobalVariable.blockTrigger(resultingState.getInitialThisValue(), resumptionLocation);
        currentPdgInfo.getReachingDefs().computeIfAbsent(blockTrigger, v -> new LinkedHashSet<>()).add(currentNode);

        return currentPdgInfo;
    }

    @Override
    protected PdgNode handleNotify(PdgInformation currentInfo, PdgNode currentNode, Set<Variable<?, ?>> variablesRead,
            Set<Variable<?, ?>> variablesWritten, EventNotificationExpression ee, int comingFrom,
            TransitionResult resultingState, LocalState localState, List<EvaluationLocation> currentLocation) {
        if (ee.getParameters().isEmpty()) {
            // immediate notification, already handled by handleProcessWaitedForEvents(...). just reset
            // announcedEvent so that the distinction there remains functional
            this.announcedEvent.set(null);
            return currentNode;
        }

        if (currentNode == null) {
            currentNode = currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.STATEMENT,
                    new StatementId(localState.getInitialThisValue(), currentLocation)), PdgNode::new);
        }

        // write event trigger
        GlobalVariable<Event, ?> eventTrigger = GlobalVariable.eventTrigger(this.announcedEvent.get());
        variablesWritten.add(eventTrigger);

        // reset announcedEvent so that the distinction in handleProcessWaitedForEvents(...) remains
        // functional
        this.announcedEvent.set(null);
        return currentNode;
    }

    @Override
    protected PdgNode handleWait(PdgInformation currentInfo, PdgNode currentNode, Set<Variable<?, ?>> variablesRead,
            Set<Variable<?, ?>> variablesWritten, FunctionCallExpression fe, int comingFrom,
            TransitionResult resultingState, LocalState localState, List<EvaluationLocation> currentLocation) {
        if (currentNode == null) {
            currentNode = currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.STATEMENT,
                    new StatementId(localState.getInitialThisValue(), currentLocation)), PdgNode::new);
        }

        // write block trigger for following block
        List<EvaluationLocation> resumptionLocation = currentLocation.stream().map(EvaluationLocation::unlockedClone)
                .collect(Collectors.toCollection(ArrayList::new));
        List<Integer> resumptionIndices = resumptionLocation.getLast().getExpressionIndices();
        resumptionIndices.add(resumptionIndices.removeLast() + 1);
        GlobalVariable<WrappedSCClassInstance, List<EvaluationLocation>> blockTrigger =
                GlobalVariable.blockTrigger(localState.getInitialThisValue(), resumptionLocation);
        variablesWritten.add(blockTrigger);

        for (int parameterIndex = 0; parameterIndex < fe.getParameters().size(); parameterIndex++) {
            List<EvaluationLocation> parameterLocation = new ArrayList<>(currentLocation);
            int topIndex = parameterLocation.size() - 1;
            EvaluationLocation top = parameterLocation.get(topIndex).unlockedClone();
            top.getExpressionIndices().add(parameterIndex);
            parameterLocation.set(topIndex, top);
            PdgNode parameterNode = currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.STATEMENT,
                    new StatementId(localState.getInitialThisValue(), parameterLocation)), PdgNode::new);
            PdgNode outTrigger =
                    currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.OUT, blockTrigger), PdgNode::new);
            new PdgEdge(EdgeType.DATA, parameterNode, outTrigger, blockTrigger, false).insert();
        }

        return currentNode;
    }

    @Override
    protected PdgNode handleRequestUpdate(PdgInformation currentInfo, PdgNode currentNode,
            Set<Variable<?, ?>> variablesRead, Set<Variable<?, ?>> variablesWritten, FunctionCallExpression fe,
            int comingFrom, TransitionResult resultingState, LocalState localState,
            List<EvaluationLocation> currentLocation) {
        if (currentNode == null) {
            currentNode = currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.STATEMENT,
                    new StatementId(localState.getInitialThisValue(), currentLocation)), PdgNode::new);
        }

        // find the instance of the channel requesting the update
        AbstractedValue abstractValueOfThis = localState.getTopOfStack().getThisValue();
        if (!abstractValueOfThis.isDetermined()) {
            throw new InsufficientValueTrackingException(abstractValueOfThis);
        }
        Object valueOfThis = abstractValueOfThis.get();
        if (!(valueOfThis instanceof WrappedSCClassInstance instance)) {
            throw new ClassCastException(
                    "expected " + WrappedSCClassInstance.class + " but found " + valueOfThis.getClass());
        }

        // write block trigger for update block
        WrappedSCFunction updateFunction = wrap(instance.getSCClass().getMemberFunctionByName("update"));
        List<EvaluationLocation> resumptionLocation =
                List.of(new EvaluationLocation(updateFunction, new ArrayList<>()));
        GlobalVariable<WrappedSCClassInstance, List<EvaluationLocation>> blockTrigger =
                GlobalVariable.blockTrigger(instance, resumptionLocation);
        variablesWritten.add(blockTrigger);

        return currentNode;
    }

}
