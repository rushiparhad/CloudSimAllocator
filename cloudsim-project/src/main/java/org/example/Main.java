package org.example;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import ch.qos.logback.classic.Level;
import org.cloudsimplus.util.Log;

public class Main {
    public static void main(String[] args) {
        Log.setLevel(Level.ERROR);

        final Path projectRoot = Paths.get("");
        final Path dataDir = projectRoot.resolve("data");

        final WorkloadReader workloadReader = new WorkloadReader();
        final List<Double> predictedWorkload = workloadReader.loadPredictedWorkload(dataDir.resolve("predictions.csv"));

        if (predictedWorkload.isEmpty()) {
            System.out.println("No predictions found. Falling back to synthetic workload.");
        }

        final SimulationManager simulationManager = new SimulationManager();

        final List<Double> workloadSeries = predictedWorkload.isEmpty()
            ? workloadReader.generateFallbackWorkload(40)
            : predictedWorkload;

        System.out.println("\n=== Running Dynamic (LSTM + DRL-inspired) Allocation ===");
        final MetricsCollector.SimulationSummary dynamicSummary =
            simulationManager.runScenario(workloadSeries, SimulationManager.PolicyType.DYNAMIC_DRL, dataDir);

        System.out.println("\n=== Running Static Baseline Allocation ===");
        final MetricsCollector.SimulationSummary staticSummary =
            simulationManager.runScenario(workloadSeries, SimulationManager.PolicyType.STATIC_BASELINE, dataDir);

        MetricsCollector.printComparison(dynamicSummary, staticSummary);
        MetricsCollector.writeSummaryCsv(dataDir.resolve("summary_metrics.csv"), dynamicSummary, staticSummary);

        System.out.println("\nSimulation completed.");
        System.out.println("Interval metrics written to: " + dataDir.resolve("simulation_metrics.csv"));
        System.out.println("Summary metrics written to : " + dataDir.resolve("summary_metrics.csv"));
    }
}
