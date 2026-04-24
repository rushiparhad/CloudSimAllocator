package org.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MetricsCollector {

    public static class IntervalMetrics {
        public int interval;
        public String policy;
        public double predictedWorkload;
        public int allocatedVms;
        public int cloudlets;
        public double avgExecutionTime;
        public double avgResponseTime;
        public double makespan;
        public double resourceUtilization;
    }

    public static class SimulationSummary {
        public String policy;
        public int totalCloudlets;
        public int averageVmCount;
        public double averageExecutionTime;
        public double averageResponseTime;
        public double averageUtilization;
        public double totalMakespan;
    }

    public static SimulationSummary summarize(String policy, List<IntervalMetrics> intervals) {
        final SimulationSummary summary = new SimulationSummary();
        summary.policy = policy;

        if (intervals.isEmpty()) {
            return summary;
        }

        double vmCountSum = 0;
        double execSum = 0;
        double responseSum = 0;
        double utilSum = 0;
        double makespanSum = 0;
        int cloudletSum = 0;

        for (IntervalMetrics metric : intervals) {
            vmCountSum += metric.allocatedVms;
            execSum += metric.avgExecutionTime;
            responseSum += metric.avgResponseTime;
            utilSum += metric.resourceUtilization;
            makespanSum += metric.makespan;
            cloudletSum += metric.cloudlets;
        }

        summary.totalCloudlets = cloudletSum;
        summary.averageVmCount = (int) Math.round(vmCountSum / intervals.size());
        summary.averageExecutionTime = execSum / intervals.size();
        summary.averageResponseTime = responseSum / intervals.size();
        summary.averageUtilization = utilSum / intervals.size();
        summary.totalMakespan = makespanSum;
        return summary;
    }

    public static void writeIntervalCsv(Path csvPath, List<IntervalMetrics> metrics) {
        try {
            Files.createDirectories(csvPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create metrics directory", e);
        }

        final boolean append = Files.exists(csvPath);

        try (Writer writer = Files.newBufferedWriter(csvPath,
                append ? java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            if (!append) {
                printer.printRecord("interval", "policy", "predicted_workload", "allocated_vms", "cloudlets",
                    "avg_execution_time", "avg_response_time", "makespan", "resource_utilization");
            }

            for (IntervalMetrics m : metrics) {
                printer.printRecord(
                    m.interval,
                    m.policy,
                    format(m.predictedWorkload),
                    m.allocatedVms,
                    m.cloudlets,
                    format(m.avgExecutionTime),
                    format(m.avgResponseTime),
                    format(m.makespan),
                    format(m.resourceUtilization)
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed writing interval metrics CSV", e);
        }
    }

    public static void writeSummaryCsv(Path csvPath, SimulationSummary dynamicSummary, SimulationSummary staticSummary) {
        try {
            Files.createDirectories(csvPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create summary directory", e);
        }

        try (Writer writer = Files.newBufferedWriter(csvPath);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                 "policy", "total_cloudlets", "avg_vm_count", "avg_execution_time",
                 "avg_response_time", "avg_resource_utilization", "total_makespan"
             ))) {
            printer.printRecord(
                dynamicSummary.policy,
                dynamicSummary.totalCloudlets,
                dynamicSummary.averageVmCount,
                format(dynamicSummary.averageExecutionTime),
                format(dynamicSummary.averageResponseTime),
                format(dynamicSummary.averageUtilization),
                format(dynamicSummary.totalMakespan)
            );
            printer.printRecord(
                staticSummary.policy,
                staticSummary.totalCloudlets,
                staticSummary.averageVmCount,
                format(staticSummary.averageExecutionTime),
                format(staticSummary.averageResponseTime),
                format(staticSummary.averageUtilization),
                format(staticSummary.totalMakespan)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed writing summary metrics CSV", e);
        }
    }

    public static void printInterval(IntervalMetrics m) {
        System.out.printf(Locale.US,
            "[t=%02d][%s] workload=%.2f -> VMs=%d, cloudlets=%d, avgExec=%.4fs, avgResp=%.4fs, util=%.2f%%%n",
            m.interval,
            m.policy,
            m.predictedWorkload,
            m.allocatedVms,
            m.cloudlets,
            m.avgExecutionTime,
            m.avgResponseTime,
            m.resourceUtilization * 100.0
        );
    }

    public static void printComparison(SimulationSummary dynamicSummary, SimulationSummary staticSummary) {
        System.out.println("\n=== Comparison: Dynamic vs Static ===");
        System.out.printf(Locale.US, "Dynamic avg execution time: %.4f s%n", dynamicSummary.averageExecutionTime);
        System.out.printf(Locale.US, "Static  avg execution time: %.4f s%n", staticSummary.averageExecutionTime);
        System.out.printf(Locale.US, "Dynamic avg response time : %.4f s%n", dynamicSummary.averageResponseTime);
        System.out.printf(Locale.US, "Static  avg response time : %.4f s%n", staticSummary.averageResponseTime);
        System.out.printf(Locale.US, "Dynamic avg utilization   : %.2f%%%n", dynamicSummary.averageUtilization * 100.0);
        System.out.printf(Locale.US, "Static  avg utilization   : %.2f%%%n", staticSummary.averageUtilization * 100.0);
    }

    private static String format(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    public static List<IntervalMetrics> copyOf(List<IntervalMetrics> source) {
        return new ArrayList<>(source);
    }
}
