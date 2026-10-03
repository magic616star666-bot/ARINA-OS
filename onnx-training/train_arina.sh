#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

python -m pip install --upgrade pip setuptools wheel
python -m pip install numpy==1.26.4 scipy==1.11.4 scikit-learn==1.3.2
python -m pip install torch==2.1.2 torchaudio==2.1.2 --index-url https://download.pytorch.org/whl/cpu
python -m pip install \
  onnx==1.15.0 onnxruntime==1.17.3 \
  torchinfo==1.8.0 torchmetrics==1.2.0 \
  speechbrain==0.5.16 \
  audiomentations==0.33.0 torch-audiomentations==0.11.0 \
  acoustics==0.2.6 mutagen==1.47.0 pronouncing==0.2.0 \
  PyYAML==6.0.1 tqdm requests webrtcvad piper-phonemize

rm -rf openwakeword piper-sample-generator arina_build background_clips rir_clips

git clone https://github.com/dscripka/openWakeWord.git openwakeword
git -C openwakeword checkout 368c03716d1e92591906a84949bc477f3a834455
python -m pip install -e ./openwakeword --no-deps

git clone --branch v2.0.0 --depth 1 https://github.com/rhasspy/piper-sample-generator.git piper-sample-generator
mkdir -p piper-sample-generator/models
wget -q --show-progress \
  https://github.com/rhasspy/piper-sample-generator/releases/download/v2.0.0/en_US-libritts_r-medium.pt \
  -O piper-sample-generator/models/en_US-libritts_r-medium.pt

mkdir -p openwakeword/openwakeword/resources/models
wget -q --show-progress \
  https://github.com/dscripka/openWakeWord/releases/download/v0.5.1/embedding_model.onnx \
  -O openwakeword/openwakeword/resources/models/embedding_model.onnx
wget -q --show-progress \
  https://github.com/dscripka/openWakeWord/releases/download/v0.5.1/melspectrogram.onnx \
  -O openwakeword/openwakeword/resources/models/melspectrogram.onnx

wget -q --show-progress \
  https://huggingface.co/datasets/davidscripka/openwakeword_features/resolve/main/validation_set_features.npy \
  -O validation_set_features.npy

python prepare_data.py

python openwakeword/openwakeword/train.py --training_config arina.yml --generate_clips
python openwakeword/openwakeword/train.py --training_config arina.yml --augment_clips --overwrite
python openwakeword/openwakeword/train.py --training_config arina.yml --train_model

MODEL_PATH="$(find arina_build -type f -name 'arina.onnx' -print -quit)"
if [[ -z "${MODEL_PATH}" ]]; then
  echo "ERROR: arina.onnx was not produced"
  find arina_build -maxdepth 4 -type f -print || true
  exit 1
fi

cp "${MODEL_PATH}" ./arina.onnx

python - <<'PY'
import os
import onnx
import onnxruntime as ort
p = 'arina.onnx'
m = onnx.load(p)
onnx.checker.check_model(m)
s = ort.InferenceSession(p, providers=['CPUExecutionProvider'])
print('ARINA ONNX VALID')
print('size_bytes=', os.path.getsize(p))
print('inputs=', [(i.name, i.shape, i.type) for i in s.get_inputs()])
print('outputs=', [(o.name, o.shape, o.type) for o in s.get_outputs()])
PY

sha256sum arina.onnx > arina.onnx.sha256
