package org.example;

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SimulationManager {

    public enum PolicyType {
        DYNAMIC_DRL,
        STATIC_BASELINE
    }

    private static final int HOSTS = 4;
    private static final int HOST_PES = 16;
    private static final long HOST_RAM_MB = 32768;
    private static final long HOST_BW = 100000;
    private static final long HOST_STORAGE = 1_000_000;
    private static final int HOST_MIPS = 2500;

    private static final int VM_PES = 2;
    private static final long VM_RAM_MB = 2048;
    private static final long VM_BW = 1000;
    private static final long VM_SIZE = 10000;
    private static final int VM_MIPS = 1000;

    private static final long CLOUDLET_LENGTH = 12_000;
    private static final int CLOUDLET_PES = 1;

    private static final int MIN_VMS = 2;
    private static final int MAX_VMS = 12;
    private static final int STATIC_VMS = 4;

    private final ResourceAllocator allocator;

    public SimulationManager() {
        this.allocator = new ResourceAllocator(MIN_VMS, MAX_VMS, STATIC_VMS);
    }

    public MetricsCollector.SimulationSummary runScenario(List<Double> workloadSeries, PolicyType policyType, Path dataDir) {
        final List<MetricsCollector.IntervalMetrics> intervalMetrics = new ArrayList<>();

        int previousVmCount = STATIC_VMS;

        for (int i = 0; i < workloadSeries.size(); i++) {
            final double workload = workloadSeries.get(i);

            final int targetVmCount = policyType == PolicyType.DYNAMIC_DRL
                ? allocator.dynamicPolicy(workload, previousVmCount)
                : allocator.getStaticVmCount();

            final int cloudletCount = estimateCloudletCount(workload);
            final MetricsCollector.IntervalMetrics metrics =
                runInterval(i, policyType.name(), workload, targetVmCount, cloudletCount);

            intervalMetrics.add(metrics);
            MetricsCollector.printInterval(metrics);
            previousVmCount = targetVmCount;
        }

        final Path intervalCsv = dataDir.resolve("simulation_metrics.csv");
        if (policyType == PolicyType.DYNAMIC_DRL) {
            try {
                Files.deleteIfExists(intervalCsv);
            } catch (Exception ignored) {
                // Not critical; file will be appended anyway.
            }
        }
        MetricsCollector.writeIntervalCsv(intervalCsv, intervalMetrics);

        return MetricsCollector.summarize(policyType.name(), intervalMetrics);
    }

    private MetricsCollector.IntervalMetrics runInterval(int interval, String policy, double workload,
                                                         int vmCount, int cloudletCount) {
        final CloudSim simulation = new CloudSim();
        final Datacenter datacenter = createDatacenter(simulation);
        final DatacenterBrokerSimple broker = new DatacenterBrokerSimple(simulation);

        final List<Vm> vmList = createVms(vmCount);
        broker.submitVmList(vmList);

        final List<Cloudlet> cloudlets = createCloudlets(cloudletCount);
        broker.submitCloudletList(cloudlets);

        simulation.start();
        final List<Cloudlet> finished = broker.getCloudletFinishedList();

        final double avgExecution = finished.stream().mapToDouble(Cloudlet::getActualCpuTime).average().orElse(0.0);
        final double avgResponse = finished.stream().mapToDouble(c -> c.getFinishTime() - c.getSubmissionDelay()).average().orElse(0.0);
        final double makespan = finished.stream().mapToDouble(Cloudlet::getFinishTime).max().orElse(0.0);

        final double totalRequiredMi = finished.stream().mapToDouble(Cloudlet::getLength).sum();
        final double totalCapacityMi = Math.max(1.0, vmCount * (double) VM_MIPS * Math.max(makespan, 1e-6));
        final double utilization = Math.min(1.0, totalRequiredMi / totalCapacityMi);

        final MetricsCollector.IntervalMetrics intervalMetrics = new MetricsCollector.IntervalMetrics();
        intervalMetrics.interval = interval;
        intervalMetrics.policy = policy;
        intervalMetrics.predictedWorkload = workload;
        intervalMetrics.allocatedVms = vmCount;
        intervalMetrics.cloudlets = finished.size();
        intervalMetrics.avgExecutionTime = avgExecution;
        intervalMetrics.avgResponseTime = avgResponse;
        intervalMetrics.makespan = makespan;
        intervalMetrics.resourceUtilization = utilization;

        return intervalMetrics;
    }

    private Datacenter createDatacenter(CloudSim simulation) {
        final List<Host> hostList = new ArrayList<>(HOSTS);

        for (int i = 0; i < HOSTS; i++) {
            final List<Pe> peList = new ArrayList<>(HOST_PES);
            for (int p = 0; p < HOST_PES; p++) {
                peList.add(new PeSimple(HOST_MIPS));
            }

            final Host host = new HostSimple(HOST_RAM_MB, HOST_BW, HOST_STORAGE, peList);
            host.setVmScheduler(new VmSchedulerTimeShared());
            hostList.add(host);
        }

        return new DatacenterSimple(simulation, hostList);
    }

    private List<Vm> createVms(int count) {
        final List<Vm> vmList = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            final Vm vm = new VmSimple(VM_MIPS, VM_PES)
                .setRam(VM_RAM_MB)
                .setBw(VM_BW)
                .setSize(VM_SIZE)
                .setCloudletScheduler(new CloudletSchedulerSpaceShared());
            vmList.add(vm);
        }

        return vmList;
    }

    private List<Cloudlet> createCloudlets(int count) {
        final List<Cloudlet> cloudletList = new ArrayList<>(count);
        final UtilizationModelDynamic cpuUtilization = new UtilizationModelDynamic(0.8);
        final UtilizationModelDynamic ramUtilization = new UtilizationModelDynamic(0.3);
        final UtilizationModelDynamic bwUtilization = new UtilizationModelDynamic(0.3);

        for (int i = 0; i < count; i++) {
            final Cloudlet cloudlet = new CloudletSimple(CLOUDLET_LENGTH + (i % 4) * 1500L, CLOUDLET_PES, cpuUtilization)
                .setUtilizationModelRam(ramUtilization)
                .setUtilizationModelBw(bwUtilization)
                .setSizes(1024);
            cloudletList.add(cloudlet);
        }

        return cloudletList;
    }

    private int estimateCloudletCount(double workload) {
        final int cloudlets = (int) Math.round(workload / 4.0);
        return Math.max(6, Math.min(80, cloudlets));
    }
}
