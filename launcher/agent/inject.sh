#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
AGENT_JAR="$SCRIPT_DIR/quark-agent.jar"

if [ -z "$1" ]; then
    echo "Usage: inject.sh <pid>"
    echo ""
    echo "Running JVM process scanner..."
    jps -l 2>/dev/null || ps aux | grep java | grep -v grep
    exit 1
fi

echo "[Quark] Attaching to PID $1..."
java -cp "$AGENT_JAR" cc.quark.agent.AttachShim "$1" "$AGENT_JAR"
if [ $? -eq 0 ]; then
    echo "[Quark] Injection successful! Press Right-Shift in Minecraft."
else
    echo "[Quark] Injection failed. Check output above."
    echo "[Quark] Hint: Try running as the same user as Minecraft."
    exit 1
fi
