package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tomatengames.util.function.LongToLongFunction;
import de.tub.pes.syscir.analysis.abstraction_refinement.Sources.SourcesType;
import de.tub.pes.syscir.engine.Engine;
import de.tub.pes.syscir.sc_model.SCSystem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration bundle for a refinement process.
 * 
 * @author Lukas Ernst
 */
public record RefinementConfig(RefinementLog log, SCSystem scSystem, SourcesType trackingType,
        boolean refinementEnabled, ConditionTracking conditionTrackingMode, Sources initialAbstraction,
        InformationFlowPolicy policy, InformationFlowRefinementMode informationFlowRefinementMode,
        LongToLongFunction timeout) {

    public RefinementConfig(RefinementLog log, SCSystem scSystem, SourcesType trackingType, boolean refinementEnabled,
            ConditionTracking conditionTrackingMode, Sources initialAbstraction, InformationFlowPolicy policy,
            InformationFlowRefinementMode informationFlowRefinementMode, LongToLongFunction timeout) {
        if (scSystem == null) {
            throw new IllegalArgumentException("SCSystem cannot be null");
        }
        if (log == null) {
            throw new IllegalArgumentException("RefinementLog cannot be null");
        }
        if (trackingType == null) {
            throw new IllegalArgumentException("Tracking type supplier cannot be null");
        }

        this.trackingType = trackingType;
        if (initialAbstraction == null) {
            initialAbstraction = newEmptySources();
        } else if (!initialAbstraction.getType().equals(trackingType)) {
            throw new IllegalArgumentException("Initial abstraction type does not match tracking type");
        }

        this.scSystem = scSystem;
        this.initialAbstraction = initialAbstraction;
        this.log = log;
        this.policy = policy;
        this.refinementEnabled = refinementEnabled;
        this.conditionTrackingMode = conditionTrackingMode;
        this.informationFlowRefinementMode = informationFlowRefinementMode;
        this.timeout = timeout;
    }

    /**
     * Constructs a RefinementConfig from file paths with default values from Main#run.
     * 
     * @param astFilePath the path to the AST XML file
     * @param policyFilePath the path to the policy file (may be null)
     * @param initialAbstractionFilePath the path to the initial abstraction file (may be null)
     * @param conditionTrackingMode the condition tracking mode to use
     * @param informationFlowRefinementMode the information flow refinement mode
     */
    public RefinementConfig(RefinementLog log, String astFilePath, String policyFilePath,
            String initialAbstractionFilePath, boolean refinementEnabled, ConditionTracking conditionTrackingMode,
            InformationFlowRefinementMode informationFlowRefinementMode) throws IOException {
        SCSystem system = Engine.buildModelFromFile(astFilePath);
        Sources initialAbstraction = parseCustomAbstraction(system, initialAbstractionFilePath);
        SourcesType trackingType = initialAbstraction != null ? initialAbstraction.getType() : SourceAssignments.TYPE;
        InformationFlowPolicy policy = parsePolicyIfExists(system, policyFilePath);

        this(log, system, trackingType, refinementEnabled, conditionTrackingMode, initialAbstraction, policy,
                informationFlowRefinementMode, lastDuration -> 5 * lastDuration + 10000L);
    }

    private static Sources parseCustomAbstraction(SCSystem system, String filePath) throws IOException {
        if (filePath == null || !Files.exists(Paths.get(filePath))) {
            return null;
        }
        return SourcesParser.parse(system, Paths.get(filePath));
    }

    private static InformationFlowPolicy parsePolicyIfExists(SCSystem system, String filePath) throws IOException {
        if (filePath == null || !Files.exists(Paths.get(filePath))) {
            return null;
        }
        return readPolicy(system, Paths.get(filePath));
    }

    private static InformationFlowPolicy readPolicy(SCSystem scSystem, Path policyFile) throws IOException {
        InformationFlowPolicy policy = new InformationFlowPolicy();
        policy.readPolicy(policyFile);
        return policy;
    }

    public Sources newEmptySources() {
        return this.trackingType.makeEmptySources();
    }

    public long computeTimeout(long baseDuration) {
        return this.timeout.apply(baseDuration);
    }

    // ── With-methods (setters that return new copies) ───────────────────────

    public RefinementConfig withScSystem(SCSystem scSystem) {
        return new RefinementConfig(this.log, scSystem, this.trackingType, this.refinementEnabled,
                this.conditionTrackingMode, this.initialAbstraction, this.policy, this.informationFlowRefinementMode,
                this.timeout);
    }

    public RefinementConfig withCustomAbstraction(Sources customAbstraction) {
        return new RefinementConfig(this.log, this.scSystem, this.trackingType, this.refinementEnabled,
                this.conditionTrackingMode, customAbstraction, this.policy, this.informationFlowRefinementMode,
                this.timeout);
    }

    public RefinementConfig withLog(RefinementLog log) {
        return new RefinementConfig(log, this.scSystem, this.trackingType, this.refinementEnabled,
                this.conditionTrackingMode, this.initialAbstraction, this.policy, this.informationFlowRefinementMode,
                this.timeout);
    }

    public RefinementConfig withPolicy(InformationFlowPolicy policy) {
        return new RefinementConfig(this.log, this.scSystem, this.trackingType, this.refinementEnabled,
                this.conditionTrackingMode, this.initialAbstraction, policy, this.informationFlowRefinementMode,
                this.timeout);
    }

    public RefinementConfig withTrackingType(SourcesType trackingType) {
        return new RefinementConfig(this.log, this.scSystem, trackingType, this.refinementEnabled,
                this.conditionTrackingMode, this.initialAbstraction, this.policy, this.informationFlowRefinementMode,
                this.timeout);
    }

    public RefinementConfig withConditionTrackingMode(ConditionTracking conditionTrackingMode) {
        return new RefinementConfig(this.log, this.scSystem, this.trackingType, this.refinementEnabled,
                conditionTrackingMode, this.initialAbstraction, this.policy, this.informationFlowRefinementMode,
                this.timeout);
    }

    public RefinementConfig withInformationFlowRefinementMode(
            InformationFlowRefinementMode informationFlowRefinementMode) {
        return new RefinementConfig(this.log, this.scSystem, this.trackingType, this.refinementEnabled,
                this.conditionTrackingMode, this.initialAbstraction, this.policy, informationFlowRefinementMode,
                this.timeout);
    }

    public RefinementConfig withTimeout(LongToLongFunction timeout) {
        return new RefinementConfig(this.log, this.scSystem, this.trackingType, this.refinementEnabled,
                this.conditionTrackingMode, this.initialAbstraction, this.policy, this.informationFlowRefinementMode,
                timeout);
    }

    // ── Nested types ─────────────────────────────────────────────────────────

    public static enum InformationFlowRefinementMode {
        NONE, CUMULATIVE, NON_CUMULATIVE
    }

    public static interface ConditionTracking {
    }

    public static enum SimpleConditionTracking implements ConditionTracking {
        NONE, BLOCK_SPLITTERS, ALL
    }

    public static class PartialDescendantsTracking implements ConditionTracking {

        public int threshold;

        public PartialDescendantsTracking(int threshold) {
            this.threshold = threshold;
        }
    }

    public static class SplittersAndPartialDescendantsTracking implements ConditionTracking {

        public int threshold;

        public SplittersAndPartialDescendantsTracking(int threshold) {
            this.threshold = threshold;
        }
    }

    /**
     * Run the process as configured.
     */
    public void run() {
        if (!this.refinementEnabled) {
            if (this.policy == null) {
                new CustomAbstractedExploration(this, this.initialAbstraction).run();
            } else {
                new CustomAbstractedPolicyChecker(this, this.initialAbstraction).run();
            }
        } else {
            if (this.informationFlowRefinementMode == InformationFlowRefinementMode.NONE) {
                if (this.policy == null) {
                    RefinementUtil.findExplorableAbstraction(this, this.initialAbstraction, -1);
                } else {
                    new CustomAbstractedPolicyChecker(this, RefinementUtil
                            .findExplorableAbstraction(this, this.initialAbstraction, -1).trackedElements()).run();
                }
            } else {
                new InformationFlowRefinement(this).run();
            }
        }
    }

}
