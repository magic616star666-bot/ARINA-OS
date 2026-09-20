#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
AOSP_DIR="${AOSP_DIR:-$HOME/aosp}"

if [ ! -d "$AOSP_DIR/.repo" ]; then
  echo "AOSP tree not found at $AOSP_DIR"
  exit 1
fi

mkdir -p "$AOSP_DIR/vendor/arina"
rsync -a --delete "$REPO_ROOT/vendor/arina/" "$AOSP_DIR/vendor/arina/"

for app in ArinaHome ArinaLockScreen; do
  mkdir -p "$AOSP_DIR/packages/apps/$app"
  rsync -a --delete "$REPO_ROOT/packages/apps/$app/" "$AOSP_DIR/packages/apps/$app/"
done

mkdir -p "$AOSP_DIR/frameworks/base/packages/SystemUI/src/com/android/systemui/arina"
rsync -a --delete \
  "$REPO_ROOT/frameworks/base/packages/SystemUI/src/com/android/systemui/arina/" \
  "$AOSP_DIR/frameworks/base/packages/SystemUI/src/com/android/systemui/arina/"

echo "ARINA Home, Lock Screen foundation and SystemUI Keyguard contract integrated into $AOSP_DIR"
