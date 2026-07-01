package de.tub.pes.syscir.analysis.abstraction_refinement.evaluation;

import de.tub.pes.syscir.analysis.abstraction_refinement.RefinementConfig;
import java.io.PrintStream;

public class EvaluationEngine {

    public long warmupTime = 10000;
    public long calmDownTime = 2000;
    public boolean slowMode = false;
    public int measureIterations = 5;
    public EvaluationTask task;

    public EvaluationEngine(EvaluationTask task) {
        this.task = task;
    }

    public static class EvaluationTask {

        public RefinementConfig config;
        public PrintStream out;

        public RefinementStatistics stats;
        public Throwable error;

        public EvaluationTask(RefinementConfig config, PrintStream out) {
            this.config = config;
            this.out = out;
        }
    }

    public static class Measure {

        public long timeNanos;
        public long memoryUsage;

        @Override
        public String toString() {
            return "Runtime is " + this.timeNanos + " ns using " + this.memoryUsage + " bytes";
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static long currentMemoryUsage() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    private static Measure runTask(EvaluationTask task, boolean slow) {
        RefinementStatistics stats = new RefinementStatistics();
        task.config = task.config.withLog(stats);
        long maxMemoryUsage = currentMemoryUsage();

        Measure measure = new Measure();

        Thread worker = new Thread(() -> {
            long starttime = System.nanoTime();
            try {
                task.config.run();
            } catch (Throwable e) {
                task.error = e; // e.g. OOM
                e.printStackTrace();
            }
            long timeNanos = System.nanoTime() - starttime;

            measure.timeNanos = timeNanos;
        });
        worker.start();

        do {
            if (slow) {
                System.gc();
            }
            sleep(100);
            long memory = currentMemoryUsage();
            if (maxMemoryUsage < memory) {
                maxMemoryUsage = memory;
            }
        } while (worker.isAlive());

        measure.memoryUsage = maxMemoryUsage;

        if (task.stats == null) {
            task.stats = stats;
        } else if (!stats.toString().equals(task.stats.toString())) {
            throw new IllegalStateException("Unequal statistics between memory and time run");
        }

        return measure;
    }

    public Measure[] run() {
        // Warm up
        this.task.out.println("Warming up ...");
        runTask(this.task, false);

        Measure[] measures = new Measure[this.measureIterations];
        for (int i = 0; i < measures.length; i++) {
            // Calm down
            this.task.out.println("Calming before the run ...");
            System.gc();
            sleep(this.calmDownTime);
            // Measure
            Measure m = runTask(this.task, this.slowMode);
            measures[i] = m;
            this.task.out.println(m);
            this.task.out.println(this.task.stats);
        }

        return measures;

    }
}
