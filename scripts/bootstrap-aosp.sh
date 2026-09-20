#!/usr/bin/env bash
set -euo pipefail

AOSP_DIR="${AOSP_DIR:-$HOME/aosp}"
AOSP_BRANCH="${AOSP_BRANCH:-android16-release}"

mkdir -p "$AOSP_DIR"
cd "$AOSP_DIR"

if ! command -v repo >/dev/null 2>&1; then
  mkdir -p "$HOME/bin"
  curl -fsSL https://storage.googleapis.com/git-repo-downloads/repo -o "$HOME/bin/repo"
  chmod +x "$HOME/bin/repo"
  export PATH="$HOME/bin:$PATH"
fi

if [ ! -d .repo ]; then
  repo init -u https://android.googlesource.com/platform/manifest -b "$AOSP_BRANCH"
fi

repo sync -c -j"$(nproc)" --force-sync --no-tags --no-clone-bundle

echo "AOSP sync complete: $AOSP_DIR"
