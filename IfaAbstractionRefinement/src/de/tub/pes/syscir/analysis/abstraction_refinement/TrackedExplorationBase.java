package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedLogic;
import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.GlobalState;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesGlobalState;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation.SomeVariablesProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.EvaluationInterceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.GlobalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Interceptor;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.LocalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.VariableHolder;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.VariableInterceptor;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.List;

public class TrackedExplorationBase {
    
    public final RefinementConfig config;
    private long timeoutMillis;
    
    public final Sources trackedElements;
    private long lastExecutionDuration;
    
    protected final AbstractedLogic logic;
    protected final EvaluationInterceptor evaluationAccessTracker;
    protected final VariableInterceptor variableAccessTracker;
    
    private Expression currentEvaluatingExpression;
    
    public TrackedExplorationBase(RefinementConfig config, Sources trackedElements, ValueManagement valueManagement,
            long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        
        this.config = config;
        this.trackedElements = trackedElements;
        
        this.logic = valueManagement.makeLogic();
        
        this.evaluationAccessTracker = new EvaluationInterceptor() {
            
            @Override
            public void prepareEvaluation(Expression expression) {
                TrackedExplorationBase.this.currentEvaluatingExpression = expression;
            }
            
            @Override
            public AbstractedValue evaluated(Expression expression, StackTraceView stack, List<Integer> location,
                    AbstractedValue result) {
                config.log().evaluation(expression, result);
                TrackedExplorationBase.this.currentEvaluatingExpression = null;
                if (result == null) {
                    return null;
                }
                if (result.isDetermined() && result.get() instanceof Variable) {
                    return result;
                }
                if (trackedElements.tracks(expression, stack, location)) {
                    return result;
                }
                return valueManagement.abstractFromExpression(result, expression, stack, location);
            }
        };
        
        this.variableAccessTracker = new VariableInterceptor() {
            
            private AbstractedValue writeVariable(Variable<?, ?> variable, AbstractedValue value) {
                if (value == null) {
                    return null;
                }
                if (!trackedElements.retains(TrackedExplorationBase.this.currentEvaluatingExpression)) {
                    return valueManagement.unretainedValue(value,
                            TrackedExplorationBase.this.currentEvaluatingExpression);
                }
                if (trackedElements.tracks(variable)) {
                    return value;
                }
                return valueManagement.abstractValue(value);
            }
            
            private AbstractedValue readVariable(VariableHolder<Variable<?, ?>> store, Variable<?, ?> variable,
                    AbstractedValue value) {
                if (!trackedElements.retains(TrackedExplorationBase.this.currentEvaluatingExpression)) {
                    store.setVariableValue(variable, valueManagement.unretainedValue(value,
                            TrackedExplorationBase.this.currentEvaluatingExpression));
                }
                if (value.isDetermined() || trackedElements.tracks(variable)) {
                    return value;
                }
                return valueManagement.abstractFromVariable(value, variable);
            }
            
            @Override
            public AbstractedValue writeLocalVariable(LocalState localState, LocalVariable<?> variable,
                    AbstractedValue value) {
                return writeVariable(variable, value);
            }
            
            @Override
            public AbstractedValue writeGlobalVariable(GlobalState globalState, GlobalVariable<?, ?> variable,
                    AbstractedValue value) {
                return writeVariable(variable, value);
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public AbstractedValue readLocalVariable(LocalState localState, LocalVariable<?> variable,
                    AbstractedValue value) {
                return readVariable((VariableHolder<Variable<?, ?>>) localState, variable, value);
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public AbstractedValue readGlobalVariable(GlobalState globalState, GlobalVariable<?, ?> variable,
                    AbstractedValue value) {
                return readVariable((VariableHolder<Variable<?, ?>>) globalState, variable, value);
            }
        };
    }
    
    public ConsideredState initialState(Scheduler scheduler, Interceptor interceptor) {
        return ConsideredState.getInitialState(this.config.scSystem(),
                (eventStates, requestedUpdates, simulationStopped) -> {
                    SomeVariablesGlobalState globalState =
                            new SomeVariablesGlobalState(eventStates, requestedUpdates, simulationStopped,
                                    SomeVariablesGlobalState.initialVariableValues(this.config.scSystem(), this.logic));
                    return globalState;
                }, (s, p, i) -> {
                    return new SomeVariablesProcess(s, p, i, scheduler, this.logic, interceptor);
                }, SomeVariablesProcessState::new, (process, globalState) -> {
                    return ((SomeVariablesProcess) process).getSensitivities((SomeVariablesGlobalState) globalState);
                }, this.logic::value);
    }
    
    protected boolean runExploration(StateSpaceExploration exploration) {
        Runnable runner = () -> {
            long startTime = System.nanoTime();
            try {
                exploration.run();
            } finally {
                this.lastExecutionDuration = (System.nanoTime() - startTime) / 1000_000L;
            }
        };
        if (this.timeoutMillis < 0) {
            runner.run();
            return true;
        }
        Thread timeoutThread = new Thread(() -> {
            try {
                Thread.sleep(this.timeoutMillis);
            } catch (InterruptedException e) {
                return;
            }
            exploration.abort();
        });
        timeoutThread.setDaemon(true);
        timeoutThread.start();
        try {
            runner.run();
        } finally {
            timeoutThread.interrupt();
        }
        return exploration.isDone();
    }

    protected long getLastExecutionDuration() {
        return this.lastExecutionDuration;
    }
    
}
