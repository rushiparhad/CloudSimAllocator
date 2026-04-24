# 🚀 Intelligent Cloud Resource Allocation using LSTM + Deep Reinforcement Learning (DRL)

## 📌 Overview
This project presents an intelligent cloud resource allocation system that combines LSTM (Long Short-Term Memory) for workload prediction and Deep Reinforcement Learning (DRL) for dynamic resource allocation. The system is implemented using CloudSim, enabling simulation-based evaluation of cloud environments.

The goal is to overcome limitations of static allocation strategies by adapting resources dynamically based on workload patterns, thereby improving performance, reducing response time, and optimizing resource utilization.

---

## 🎯 Objectives
- Predict future workloads using LSTM
- Dynamically allocate virtual machines using DRL
- Reduce response time and makespan
- Improve resource utilization
- Compare with traditional static allocation methods

---

## 🧠 Methodology
1. Workload Input
   - Historical workload data is used as input

2. LSTM Model
   - Predicts future workload trends
   - Helps in proactive resource allocation

3. DRL Agent
   - Learns optimal VM allocation policy
   - Uses reward function based on performance metrics

4. CloudSim Simulation
   - Simulates cloud environment
   - Executes allocation strategies

5. Performance Evaluation
   - Compares Dynamic (LSTM + DRL) vs Static Baseline

---

## 📊 Results Summary

| Metric                     | Dynamic (LSTM + DRL) | Static Baseline |
|--------------------------|---------------------|-----------------|
| Response Time            | 21.49 s             | 36.03 s         |
| Execution Time           | 17.80 s             | 17.80 s         |
| Resource Utilization     | 96.35%              | 100%            |
| Total Makespan           | 1441.55             | 2698.73         |
| Average VM Count         | 9                   | 4               |

---

## 📈 Key Insights
- ✅ 40% reduction in response time
- ✅ ~46% reduction in makespan
- ✅ Better resource distribution across VMs
- ✅ Dynamic scaling improves system responsiveness
- ⚠ Slightly lower utilization due to optimized load balancing (not overloading resources)

---

## 📊 Visualizations
- Workload vs VM Allocation Graph
- Execution Time Comparison Graph
- Interactive Dashboard (HTML)

---

## 🖥️ Project Structure

cloudsim-project/ │ ├── data/ │   ├── workload.csv │   ├── predictions.csv │   ├── simulation_metrics.csv │   ├── summary_metrics.csv │   ├── workload_vs_vm_allocation.png │   └── execution_time_comparison.png │ ├── python/ │   ├── lstm_model.py │   └── generate_graphs.py │ ├── src/main/java/org/example/ │   ├── Main.java │   ├── SimulationManager.java │   ├── ResourceAllocator.java │   ├── MetricsCollector.java │   └── WorkloadReader.java │ ├── dashboard/ │   └── index.html │ ├── pom.xml └── README.md

---

## ⚙️ Technologies Used
- Java (CloudSim)
- Python (LSTM Model)
- TensorFlow / Keras
- Deep Reinforcement Learning
- HTML Dashboard Visualization

---

## ▶️ How to Run

### 1️⃣ Clone Repository
bash git clone https://github.com/rushiparhad/CloudSimAllocator.git cd CloudSimAllocator 

### 2️⃣ Setup Python Environment
bash pip install -r requirements.txt 

### 3️⃣ Run LSTM Model
bash python python/lstm_model.py 

### 4️⃣ Run Cloud Simulation
bash mvn clean install java -cp target/classes org.example.Main 

### 5️⃣ View Dashboard
Open:
dashboard/index.html

---

## 📌 Future Enhancements
- Multi-cloud environment support
- Energy-aware resource allocation
- Real-time deployment on AWS
- Advanced DRL models (PPO, A3C)
- Integration with Kubernetes

---

## 🤝 Contribution
Contributions are welcome! Feel free to fork the repository and submit pull requests.

---

## 📎 GitHub Repository
👉 https://github.com/rushiparhad/CloudSimAllocator.git

---

## 📜 License
This project is for academic and research purposes.

---

## 👨‍💻 Author
** Rushi Parhad
