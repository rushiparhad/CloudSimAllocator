# Resource Allocation in Cloud Computing using Deep Learning (LSTM + DRL-inspired Policy) with CloudSim Plus

This project simulates an intelligent cloud resource allocation system that combines:
- **LSTM workload forecasting (Python/TensorFlow)**
- **DRL-inspired VM scaling policy (Java)**
- **Cloud simulation and evaluation (CloudSim Plus)**

## 1. Project Goal

- Predict future workload from time-series data
- Dynamically allocate VMs based on predicted load
- Simulate cloud infrastructure behavior using CloudSim Plus
- Measure execution time, response time, and resource utilization
- Compare **dynamic** vs **static** allocation

## 2. System Architecture

```text
+----------------------+        predictions.csv        +-----------------------------+
|  LSTM Prediction     | --------------------------->  | Resource Allocation Engine  |
|  Module (Python)     |                               | (DRL-inspired policy, Java) |
+----------------------+                               +--------------+--------------+
        ^                                                                  |
        | workload.csv                                                      v
+----------------------+                               +-----------------------------+
| Synthetic Workload   |                               | CloudSim Simulation Layer   |
| Generator (Python)   |                               | Datacenter/VM/Cloudlets     |
+----------------------+                               +--------------+--------------+
                                                                      |
                                                                      v
                                                       +-----------------------------+
                                                       | Metrics & Evaluation Module |
                                                       | CSV + console + graphs      |
                                                       +-----------------------------+
```

## 3. Project Structure

```text
cloudsim-project/
├── src/main/java/org/example/
│   ├── Main.java
│   ├── ResourceAllocator.java
│   ├── WorkloadReader.java
│   ├── MetricsCollector.java
│   └── SimulationManager.java
├── src/test/java/org/example/
│   └── AppTest.java
├── python/
│   ├── lstm_model.py
│   └── generate_graphs.py
├── data/
│   ├── workload.csv
│   ├── predictions.csv
│   ├── simulation_metrics.csv
│   └── summary_metrics.csv
├── pom.xml
└── README.md
```

## 4. Module Responsibilities

- `python/lstm_model.py`
  - Generates synthetic workload dataset
  - Trains LSTM model using TensorFlow/Keras
  - Forecasts future workload
  - Exports `data/predictions.csv`

- `ResourceAllocator.java`
  - Implements a DRL-inspired policy (action-value style heuristic)
  - Chooses scale-in / hold / scale-out action based on predicted workload state

- `SimulationManager.java`
  - Creates CloudSim datacenter, hosts, VMs, and cloudlets
  - Runs interval-wise simulation loop
  - Applies dynamic or static policy

- `MetricsCollector.java`
  - Collects interval and aggregate metrics
  - Writes `simulation_metrics.csv` and `summary_metrics.csv`
  - Prints side-by-side dynamic vs static comparison

## 5. DRL-inspired Allocation Logic

The policy approximates a DQN/PPO-style decision layer with three actions:
- `-1`: scale in one VM
- `0`: hold
- `+1`: scale out one VM

State buckets from predicted workload:
- Low workload
- Medium workload
- High workload

A reward proxy balances:
- closeness to target VM level for each state
- scaling stability (penalize frequent changes)
- under-provisioning penalty under high load

## 6. Setup (macOS M2)

### Prerequisites

- Java 17+
- Maven 3.9+
- Python 3.10+ (or 3.11)

### Install Python dependencies

```bash
cd cloudsim-project
python3 -m venv .venv
source .venv/bin/activate
pip install --upgrade pip
pip install tensorflow pandas numpy matplotlib
```

> On Apple Silicon, if TensorFlow installation differs, use the official Apple TensorFlow packages matching your Python version.

## 7. Run Pipeline

### Step 1: Generate workload predictions (Python)

```bash
cd cloudsim-project
source .venv/bin/activate
python python/lstm_model.py
```

This creates/updates:
- `data/workload.csv`
- `data/predictions.csv`

### Step 2: Run CloudSim simulation (Java)

```bash
cd cloudsim-project
mvn clean test
mvn exec:java
```

This creates/updates:
- `data/simulation_metrics.csv`
- `data/summary_metrics.csv`

### Step 3: Generate graphs

```bash
cd cloudsim-project
source .venv/bin/activate
python python/generate_graphs.py
```

Graph outputs:
- `data/workload_vs_vm_allocation.png`
- `data/execution_time_comparison.png`

## 8. Output Metrics

The simulation prints interval-level details such as:
- VM allocation decisions
- cloudlet counts and execution times
- response times
- resource utilization

At the end it reports dynamic vs static comparison:
- average execution time
- average response time
- average utilization

## 9. Static vs Dynamic Comparison (Bonus)

Two scenarios are run automatically in `Main.java`:
- `DYNAMIC_DRL`: prediction-driven scaling
- `STATIC_BASELINE`: fixed VM count baseline

Use `data/summary_metrics.csv` and `execution_time_comparison.png` to show improvement from intelligent allocation.

## 10. Notes for Academic Demonstration

- The DRL block is intentionally simplified for mini-project scope but follows RL decision concepts (state-action-reward).
- For extension, you can replace `ResourceAllocator` with a true DQN/PPO model policy loaded from Python.
- You can plug real workload traces into `data/workload.csv` without changing the architecture.
