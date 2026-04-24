from pathlib import Path

import matplotlib.pyplot as plt
import pandas as pd

ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / "data"

WORKLOAD_CSV = DATA_DIR / "workload.csv"
PREDICTIONS_CSV = DATA_DIR / "predictions.csv"
METRICS_CSV = DATA_DIR / "simulation_metrics.csv"
SUMMARY_CSV = DATA_DIR / "summary_metrics.csv"


def save_workload_vm_plot(metrics_df: pd.DataFrame, predictions_df: pd.DataFrame):
    dynamic_df = metrics_df[metrics_df["policy"] == "DYNAMIC_DRL"].copy()

    if dynamic_df.empty:
        print("No dynamic metrics found. Skipping workload vs VM graph.")
        return

    fig, ax1 = plt.subplots(figsize=(10, 5))
    ax2 = ax1.twinx()

    ax1.plot(dynamic_df["interval"], dynamic_df["allocated_vms"], label="Allocated VMs", color="#0B6E4F", linewidth=2)
    ax2.plot(predictions_df["timestep"] - 1, predictions_df["predicted_workload"], label="Predicted Workload", color="#E4572E", linewidth=2)

    ax1.set_title("Predicted Workload vs VM Allocation (Dynamic DRL-inspired)")
    ax1.set_xlabel("Time Interval")
    ax1.set_ylabel("Allocated VMs", color="#0B6E4F")
    ax2.set_ylabel("Predicted Workload", color="#E4572E")

    ax1.grid(alpha=0.25)

    lines_1, labels_1 = ax1.get_legend_handles_labels()
    lines_2, labels_2 = ax2.get_legend_handles_labels()
    ax1.legend(lines_1 + lines_2, labels_1 + labels_2, loc="upper left")

    out_path = DATA_DIR / "workload_vs_vm_allocation.png"
    fig.tight_layout()
    fig.savefig(out_path, dpi=200)
    plt.close(fig)
    print(f"Saved: {out_path}")


def save_execution_comparison_plot(summary_df: pd.DataFrame):
    if summary_df.empty:
        print("No summary metrics found. Skipping execution comparison graph.")
        return

    fig, ax = plt.subplots(figsize=(8, 5))

    policies = summary_df["policy"].tolist()
    execution_times = summary_df["avg_execution_time"].tolist()

    bars = ax.bar(policies, execution_times, color=["#0B6E4F", "#E4572E"])
    ax.set_title("Average Execution Time: Dynamic vs Static")
    ax.set_ylabel("Seconds")
    ax.grid(axis="y", alpha=0.25)

    for bar, value in zip(bars, execution_times):
        ax.text(bar.get_x() + bar.get_width() / 2, bar.get_height(), f"{value:.4f}", ha="center", va="bottom")

    out_path = DATA_DIR / "execution_time_comparison.png"
    fig.tight_layout()
    fig.savefig(out_path, dpi=200)
    plt.close(fig)
    print(f"Saved: {out_path}")


def main():
    missing = [p for p in [WORKLOAD_CSV, PREDICTIONS_CSV, METRICS_CSV, SUMMARY_CSV] if not p.exists()]
    if missing:
        print("Missing input files:")
        for m in missing:
            print(f" - {m}")
        print("Run Python LSTM script and Java simulation first.")
        return

    predictions_df = pd.read_csv(PREDICTIONS_CSV)
    metrics_df = pd.read_csv(METRICS_CSV)
    summary_df = pd.read_csv(SUMMARY_CSV)

    save_workload_vm_plot(metrics_df, predictions_df)
    save_execution_comparison_plot(summary_df)


if __name__ == "__main__":
    main()
