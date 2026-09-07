#!/usr/bin/env bash

# Resolve project root directory whether script is run from project root or journey_generation directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=========================================================="
echo "  Sahaba Companions - Sahaba Journey Generation Tool     "
echo "=========================================================="

if [ "$#" -eq 0 ]; then
    echo "🚀 Running default automated 30-story extraction (Arabic) and syncing directly to app..."
    "$ROOT_DIR/gradlew" -p "$ROOT_DIR" :journey_generation:run --quiet --console=plain --args="--target-count 30 --lang ar --sync-app"
else
    ARGS="$*"
    "$ROOT_DIR/gradlew" -p "$ROOT_DIR" :journey_generation:run --quiet --console=plain --args="$ARGS"
fi
