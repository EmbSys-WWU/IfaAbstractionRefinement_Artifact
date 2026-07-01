package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementConfig.ConditionTracking;
import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementConfig.InformationFlowRefinementMode;
import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementConfig.PartialDescendantsTracking;
import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementConfig.SimpleConditionTracking;
import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementConfig.SplittersAndPartialDescendantsTracking;
import de.tub.pes.syscir.analysis.abstraction_refinement.evaluation.EvaluationEngine;
import de.tub.pes.syscir.analysis.abstraction_refinement.evaluation.EvaluationEngine.EvaluationTask;
import de.tub.pes.syscir.analysis.abstraction_refinement.evaluation.EvaluationEngine.Measure;
import de.tub.pes.syscir.analysis.util.TeeOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

/**
 * Extracts user parameters and runs the analysis.
 * 
 * @author Jonas Becker-Kupczok, Lukas Ernst
 *
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Options options = new Options();

        Option basePath = new Option("b", "base-path", true, """
                prefix of the paths to the model and (optionally) the policy and predefined abstraction files.\
                 \
                paths to the individual files are created by appending their respective extension.""");
        basePath.setRequired(false);
        options.addOption(basePath);

        Option model = new Option("m", "model", true, "path to ast-file of the system model");
        model.setRequired(false);
        options.addOption(model);

        Option policy = new Option("p", "policy", true, "path to the policy-file to check");
        policy.setRequired(false);
        options.addOption(policy);

        Option predefinedAbstraction =
                new Option("a", "abstraction", true, "path to the predefined abstraction to use");
        predefinedAbstraction.setRequired(false);
        options.addOption(predefinedAbstraction);

        Option splitters = new Option("s", "splitters", false, "use splitters heuristic");
        splitters.setRequired(false);
        options.addOption(splitters);

        Option partialDescendants = new Option("d", "partial-descendants", true,
                "use partial descendants heuristic with the given threshold");
        partialDescendants.setRequired(false);
        options.addOption(partialDescendants);

        Option cumulativeIFR = new Option("ic", "cumulative-ifr", false, "use cumulative information flow refinement");
        cumulativeIFR.setRequired(false);
        options.addOption(cumulativeIFR);

        Option nonCumulativeIFR =
                new Option("in", "non-cumulative-ifr", false, "use non-cumulative information flow refinement");
        nonCumulativeIFR.setRequired(false);
        options.addOption(nonCumulativeIFR);

        Option eval = new Option("e", "eval", false, "evaluate performance");
        eval.setRequired(false);
        options.addOption(eval);

        Option out = new Option("o", "out", true, "path to the output log file");
        out.setRequired(false);
        options.addOption(out);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = HelpFormatter.builder().get();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp(e.getMessage(), null, options, null, false);
            System.exit(1);
            return;
        }

        String modelPath;
        String policyPath;
        String abstractionPath;

        if (cmd.hasOption(basePath)) {
            String prefix = cmd.getOptionValue(basePath);
            modelPath = prefix + ".ast.xml";
            policyPath = prefix + ".policy.txt";
            abstractionPath = prefix + ".abstraction.txt";
        } else {
            if (!cmd.hasOption(model)) {
                formatter.printHelp("Either model or base path must be specified.", null, options, null, false);
                System.exit(1);
                return;
            }

            modelPath = cmd.getOptionValue(model);
            policyPath = cmd.hasOption(policy) ? cmd.getOptionValue(policy) : null;
            abstractionPath = cmd.hasOption(predefinedAbstraction) ? cmd.getOptionValue(predefinedAbstraction) : null;

            if (policyPath != null && !new File(policyPath).exists()) {
                formatter.printHelp("Policy file does not exist: " + policyPath, null, options, null, false);
                System.exit(1);
                return;
            }
            if (abstractionPath != null && !new File(abstractionPath).exists()) {
                formatter.printHelp("Abstraction file does not exist: " + abstractionPath, null, options, null, false);
                System.exit(1);
                return;
            }
        }

        if (!new File(modelPath).exists()) {
            formatter.printHelp("Model file does not exist: " + modelPath, null, options, null, false);
            System.exit(1);
            return;
        }

        // Check refinement flags
        boolean hasSplitters = cmd.hasOption(splitters);
        boolean hasPartialDescendants = cmd.hasOption(partialDescendants);
        boolean hasCumulativeIFR = cmd.hasOption(cumulativeIFR);
        boolean hasNonCumulativeIFR = cmd.hasOption(nonCumulativeIFR);

        // Validate mutual exclusivity of -ic and -in
        if (hasCumulativeIFR && hasNonCumulativeIFR) {
            formatter.printHelp("Options -ic and -in are mutually exclusive.", null, options, null, false);
            System.exit(1);
            return;
        }

        // Determine if any refinement flag is set
        boolean useRefinement = hasSplitters || hasPartialDescendants || hasCumulativeIFR || hasNonCumulativeIFR;

        // Validate that -ic or -in requires -p (or -b with policy)
        if ((hasCumulativeIFR || hasNonCumulativeIFR) && policyPath == null) {
            formatter.printHelp("Options -ic and -in require a policy (via -p or -b).", null, options, null, false);
            System.exit(1);
            return;
        }

        // Determine condition tracking mode
        ConditionTracking conditionTracking;
        if (hasSplitters && hasPartialDescendants) {
            int threshold = Integer.parseInt(cmd.getOptionValue(partialDescendants));
            conditionTracking = new SplittersAndPartialDescendantsTracking(threshold);
        } else if (hasSplitters) {
            conditionTracking = SimpleConditionTracking.BLOCK_SPLITTERS;
        } else if (hasPartialDescendants) {
            int threshold = Integer.parseInt(cmd.getOptionValue(partialDescendants));
            conditionTracking = new PartialDescendantsTracking(threshold);
        } else {
            conditionTracking = SimpleConditionTracking.NONE;
        }

        // Determine information flow refinement mode
        InformationFlowRefinementMode ifrMode;
        if (hasCumulativeIFR) {
            ifrMode = InformationFlowRefinementMode.CUMULATIVE;
        } else if (hasNonCumulativeIFR) {
            ifrMode = InformationFlowRefinementMode.NON_CUMULATIVE;
        } else {
            ifrMode = InformationFlowRefinementMode.NONE;
        }


        if (cmd.hasOption(out)) {
            File file = new File(cmd.getOptionValue(out));
            file.delete();
        }

        RefinementLog log;
        if (cmd.hasOption(eval)) {
            log = NoRefinementLog.INSTANCE;
        } else if (cmd.hasOption(out)) {
            String logPath = cmd.getOptionValue(out);
            log = new FilePrintRefinementLog(PrintRefinementLog::new, new File(logPath));
        } else {
            log = PrintRefinementLog.INSTANCE;
        }

        // Create RefinementConfig
        RefinementConfig config = new RefinementConfig(log, modelPath, policyPath, abstractionPath, useRefinement,
                conditionTracking, ifrMode);

        // Run or evaluate
        if (cmd.hasOption(eval)) {
            PrintStream outStream;
            if (cmd.hasOption(out)) {
                String logPath = cmd.getOptionValue(out);
                outStream = new PrintStream(new TeeOutputStream(System.out, new PrintStream(new File(logPath))));
            } else {
                outStream = System.out;
            }
            eval(config, outStream);
        } else {
            run(config);
        }
    }

    public static void run(RefinementConfig config) {
        System.out.println(config.policy() == null ? "Checking no policy" : "Checking policy: " + config.policy());

        if (config.initialAbstraction() != null) {
            System.out.println("Using initial abstraction: " + config.initialAbstraction());
        }
        config.run();
    }

    public static void eval(RefinementConfig config, PrintStream out) {
        EvaluationEngine engine = new EvaluationEngine(new EvaluationTask(config, out));
        engine.measureIterations = 5;
        engine.slowMode = false;

        Measure[] measures = engine.run();
        Arrays.sort(measures, (m1, m2) -> Long.compare(m1.timeNanos, m2.timeNanos));
        System.out.println("Median: " + measures[measures.length / 2]);
    }

}
