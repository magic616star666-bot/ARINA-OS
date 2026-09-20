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

# ARINA Home is a shipping system app.
mkdir -p "$AOSP_DIR/packages/apps/ArinaHome"
rsync -a --delete "$REPO_ROOT/packages/apps/ArinaHome/" "$AOSP_DIR/packages/apps/ArinaHome/"

# ArinaLockScreen remains a visual preview/reference package only.
mkdir -p "$AOSP_DIR/packages/apps/ArinaLockScreen"
rsync -a --delete "$REPO_ROOT/packages/apps/ArinaLockScreen/" "$AOSP_DIR/packages/apps/ArinaLockScreen/"

# SystemUI source extensions used by real AOSP Keyguard.
mkdir -p "$AOSP_DIR/frameworks/base/packages/SystemUI/src/com/android/systemui/arina"
rsync -a --delete \
  "$REPO_ROOT/frameworks/base/packages/SystemUI/src/com/android/systemui/arina/" \
  "$AOSP_DIR/frameworks/base/packages/SystemUI/src/com/android/systemui/arina/"

# Wire ARINA state + visuals into the Android 16 real SystemUI Keyguard.
AOSP_DIR="$AOSP_DIR" python3 "$REPO_ROOT/scripts/patch-systemui-keyguard.py"
AOSP_DIR="$AOSP_DIR" python3 "$REPO_ROOT/scripts/patch-systemui-keyguard-visuals.py"
AOSP_DIR="$AOSP_DIR" python3 "$REPO_ROOT/scripts/patch-systemui-notifications.py"

echo "ARINA Home and real SystemUI/Keyguard auth + visual + notification integration applied to $AOSP_DIR"
