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

mkdir -p "$AOSP_DIR/packages/apps/ArinaHome"
rsync -a --delete "$REPO_ROOT/packages/apps/ArinaHome/" "$AOSP_DIR/packages/apps/ArinaHome/"

echo "ARINA source layers integrated into $AOSP_DIR"
