package org.example;

public class ResourceAllocator {
    private final int minVms;
    private final int maxVms;
    private final int staticVmCount;

    public ResourceAllocator(int minVms, int maxVms, int staticVmCount) {
        this.minVms = minVms;
        this.maxVms = maxVms;
        this.staticVmCount = staticVmCount;
    }

    public int getStaticVmCount() {
        return staticVmCount;
    }

    public int dynamicPolicy(double predictedWorkload, int previousVmCount) {
        final int workloadState = toState(predictedWorkload);

        final int actionScaleOut = +1;
        final int actionHold = 0;
        final int actionScaleIn = -1;

        final double rewardScaleOut = estimateReward(workloadState, previousVmCount, actionScaleOut);
        final double rewardHold = estimateReward(workloadState, previousVmCount, actionHold);
        final double rewardScaleIn = estimateReward(workloadState, previousVmCount, actionScaleIn);

        int selectedAction = actionHold;
        double maxReward = rewardHold;

        if (rewardScaleOut > maxReward) {
            maxReward = rewardScaleOut;
            selectedAction = actionScaleOut;
        }

        if (rewardScaleIn > maxReward) {
            selectedAction = actionScaleIn;
        }

        final int targetVmCount = previousVmCount + selectedAction;
        return clamp(targetVmCount, minVms, maxVms);
    }

    private int toState(double predictedWorkload) {
        if (predictedWorkload < 45) {
            return 0; // low
        }
        if (predictedWorkload < 85) {
            return 1; // medium
        }
        return 2; // high
    }

    private double estimateReward(int state, int currentVmCount, int action) {
        final int candidateVms = clamp(currentVmCount + action, minVms, maxVms);

        final double targetVm;
        if (state == 0) {
            targetVm = minVms + 1;
        } else if (state == 1) {
            targetVm = (minVms + maxVms) / 2.0;
        } else {
            targetVm = maxVms - 1;
        }

        final double performanceReward = -Math.abs(candidateVms - targetVm);
        final double scalingPenalty = Math.abs(action) * 0.25;
        final double underProvisionPenalty = (state == 2 && candidateVms < targetVm) ? 1.25 : 0.0;

        return performanceReward - scalingPenalty - underProvisionPenalty;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
