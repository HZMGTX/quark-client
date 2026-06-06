#!/bin/bash
# ============================================================
#  Quark.cc Injector - Linux/macOS
#  Usage: ./inject.sh [pid]
#
#  Without PID: auto-detects Minecraft
#  With PID:    injects into specified process
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
AGENT_JAR="$SCRIPT_DIR/quark-agent.jar"
INJECTOR_JAR="$SCRIPT_DIR/quark-injector.jar"

if [ ! -f "$AGENT_JAR" ]; then
    echo "[inject.sh] quark-agent.jar not found. Building..."
    bash "$SCRIPT_DIR/build-agent.sh"
fi

if [ ! -f "$INJECTOR_JAR" ]; then
    echo "[inject.sh] quark-injector.jar not found."
    exit 1
fi

export QUARK_AGENT="$AGENT_JAR"

echo "[inject.sh] Injecting Quark.cc into Minecraft..."
java -cp "$INJECTOR_JAR:$JAVA_HOME/lib/tools.jar" \
     cc.quark.agent.Injector "$@"
