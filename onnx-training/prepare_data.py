from pathlib import Path
import numpy as np
from scipy.io import wavfile

SR = 16000
rng = np.random.default_rng(616)

# Split the general-purpose openWakeWord validation features so training and
# false-positive validation do not use the same examples.
src = np.load("validation_set_features.npy", mmap_mode="r")
print("validation features:", src.shape, src.dtype)
n = len(src)
cut = max(1, int(n * 0.75))
np.save("negative_train.npy", np.asarray(src[:cut]))
np.save("negative_val.npy", np.asarray(src[cut:]))
print("negative train/val:", cut, n - cut)

# Lightweight but varied synthetic background bank for augmentation.
bg_dir = Path("background_clips")
bg_dir.mkdir(exist_ok=True)
seconds = 8
N = SR * seconds
for i in range(64):
    t = np.arange(N, dtype=np.float32) / SR
    white = rng.normal(0, 1, N).astype(np.float32)
    # low-frequency colored component
    low = np.cumsum(rng.normal(0, 0.03, N)).astype(np.float32)
    low -= low.mean()
    low /= (np.max(np.abs(low)) + 1e-6)
    hum_freq = float(rng.choice([50, 60, 100, 120, 180, 240]))
    hum = np.sin(2 * np.pi * hum_freq * t + rng.uniform(0, 2*np.pi)).astype(np.float32)
    # intermittent appliance/road-like modulation
    mod = (0.35 + 0.65 * (np.sin(2*np.pi*rng.uniform(0.08, 0.8)*t) + 1) / 2).astype(np.float32)
    audio = (0.45*white + 0.35*low + 0.20*hum) * mod
    audio /= (np.max(np.abs(audio)) + 1e-6)
    audio *= rng.uniform(0.12, 0.45)
    wavfile.write(bg_dir / f"noise_{i:03d}.wav", SR, (audio * 32767).astype(np.int16))

# Synthetic room impulse responses with varied decay/reflections.
rir_dir = Path("rir_clips")
rir_dir.mkdir(exist_ok=True)
for i in range(48):
    length = int(SR * rng.uniform(0.18, 0.85))
    t = np.arange(length, dtype=np.float32) / SR
    decay = np.exp(-t * rng.uniform(5.0, 20.0)).astype(np.float32)
    rir = rng.normal(0, 0.05, length).astype(np.float32) * decay
    rir[0] += 1.0
    # add sparse early reflections
    for _ in range(rng.integers(3, 10)):
        pos = int(rng.uniform(0.003, min(0.12, length/SR)) * SR)
        if 0 < pos < length:
            rir[pos] += rng.uniform(-0.55, 0.55)
    rir /= (np.max(np.abs(rir)) + 1e-6)
    wavfile.write(rir_dir / f"rir_{i:03d}.wav", SR, (rir * 32767).astype(np.int16))

print("prepared background and RIR augmentation data")
