#!/usr/bin/env bash
set -euo pipefail

# Downloads adb (Android platform-tools) and scrcpy prebuilt for macOS arm64,
# and places the binaries into src/main/resources/bin/darwin-arm64/

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
OUT_DIR="$ROOT_DIR/src/main/resources/bin/darwin-arm64"
TMP_DIR=$(mktemp -d)
mkdir -p "$OUT_DIR"

echo "Downloading platform-tools (adb)…"
PT_ZIP="$TMP_DIR/platform-tools.zip"
curl -L -o "$PT_ZIP" "https://dl.google.com/android/repository/platform-tools-latest-darwin.zip"
unzip -q -o "$PT_ZIP" -d "$TMP_DIR"
cp "$TMP_DIR/platform-tools/adb" "$OUT_DIR/adb"
chmod +x "$OUT_DIR/adb"

echo "Downloading scrcpy (macOS arm64)…"
# Use Genymobile official releases
SCRCPY_TAR="$TMP_DIR/scrcpy-macos.tar.gz"
curl -L -o "$SCRCPY_TAR" "https://github.com/Genymobile/scrcpy/releases/latest/download/scrcpy-macos-aarch64-v3.3.3.tar.gz"
tar -xzf "$SCRCPY_TAR" -C "$TMP_DIR"
# Find scrcpy binary
SCRCPY_BIN=$(find "$TMP_DIR" -type f -name scrcpy -perm +111 | head -n 1)
if [[ -z "$SCRCPY_BIN" ]]; then
  echo "scrcpy binary not found in archive" >&2
  exit 1
fi
cp "$SCRCPY_BIN" "$OUT_DIR/scrcpy"
chmod +x "$OUT_DIR/scrcpy"

# Find and copy scrcpy-server (required by scrcpy)
SCRCPY_SERVER=$(find "$TMP_DIR" -type f -name "scrcpy-server" | head -n 1)
if [[ -n "$SCRCPY_SERVER" ]]; then
  echo "Found scrcpy-server, copying..."
  cp "$SCRCPY_SERVER" "$OUT_DIR/scrcpy-server"
  chmod +x "$OUT_DIR/scrcpy-server"
else
  echo "Warning: scrcpy-server not found in archive, scrcpy may not work properly"
fi

echo "Done. Binaries placed in $OUT_DIR"

