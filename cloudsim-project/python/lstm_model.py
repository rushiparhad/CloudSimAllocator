import math
import random
from pathlib import Path

import numpy as np
import pandas as pd
import tensorflow as tf
from tensorflow.keras import Sequential
from tensorflow.keras.layers import LSTM, Dense, Dropout

SEED = 42
random.seed(SEED)
np.random.seed(SEED)
tf.random.set_seed(SEED)

ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / "data"
WORKLOAD_CSV = DATA_DIR / "workload.csv"
PREDICTIONS_CSV = DATA_DIR / "predictions.csv"


def build_synthetic_workload(length: int = 240) -> pd.DataFrame:
    values = []
    for t in range(length):
        seasonal = 70 + 28 * math.sin((2 * math.pi * t) / 24)
        trend = 0.12 * t
        burst = 16 if t % 35 in (0, 1, 2) else 0
        noise = np.random.normal(0, 3)
        workload = max(15, seasonal + trend + burst + noise)
        values.append(workload)

    df = pd.DataFrame({"timestep": np.arange(1, length + 1), "workload": np.round(values, 4)})
    return df


def min_max_scale(series: np.ndarray):
    min_v = float(np.min(series))
    max_v = float(np.max(series))
    denom = max(max_v - min_v, 1e-8)
    scaled = (series - min_v) / denom
    return scaled, min_v, max_v


def inverse_min_max(values: np.ndarray, min_v: float, max_v: float):
    return values * (max_v - min_v) + min_v


def make_sequences(data: np.ndarray, look_back: int):
    x, y = [], []
    for i in range(len(data) - look_back):
        x.append(data[i : i + look_back])
        y.append(data[i + look_back])
    x = np.array(x)
    y = np.array(y)
    return x.reshape((x.shape[0], x.shape[1], 1)), y


def train_and_predict(horizon: int = 40, look_back: int = 12):
    DATA_DIR.mkdir(parents=True, exist_ok=True)

    workload_df = build_synthetic_workload()
    workload_df.to_csv(WORKLOAD_CSV, index=False)

    series = workload_df["workload"].values.astype(np.float32)
    scaled, min_v, max_v = min_max_scale(series)

    x_train, y_train = make_sequences(scaled, look_back)

    model = Sequential(
        [
            LSTM(64, return_sequences=True, input_shape=(look_back, 1)),
            Dropout(0.15),
            LSTM(32),
            Dense(16, activation="relu"),
            Dense(1),
        ]
    )

    model.compile(optimizer="adam", loss="mse")
    model.fit(x_train, y_train, epochs=35, batch_size=16, verbose=0)

    context = scaled[-look_back:].tolist()
    predictions_scaled = []

    for _ in range(horizon):
        x_input = np.array(context[-look_back:], dtype=np.float32).reshape(1, look_back, 1)
        pred_scaled = float(model.predict(x_input, verbose=0)[0][0])
        pred_scaled = max(0.0, min(1.0, pred_scaled))
        predictions_scaled.append(pred_scaled)
        context.append(pred_scaled)

    predictions = inverse_min_max(np.array(predictions_scaled), min_v, max_v)
    prediction_df = pd.DataFrame(
        {
            "timestep": np.arange(1, horizon + 1),
            "predicted_workload": np.round(predictions, 4),
        }
    )
    prediction_df.to_csv(PREDICTIONS_CSV, index=False)

    print(f"Saved synthetic workload to: {WORKLOAD_CSV}")
    print(f"Saved LSTM predictions to:   {PREDICTIONS_CSV}")


if __name__ == "__main__":
    train_and_predict(horizon=40, look_back=12)
