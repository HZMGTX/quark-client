#!/bin/bash
# ============================================================
#  Quark.cc Agent Builder
#  Compiles all agent Java files and packages quark-agent.jar
#  Requires: JDK 17+, ASM 9 (asm-9.x.jar) in ./lib/
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LIB_DIR="$SCRIPT_DIR/lib"
OUT_DIR="$SCRIPT_DIR/out"
JAR_OUT="$SCRIPT_DIR/quark-agent.jar"

echo "===================================================="
echo " Quark.cc Agent Builder"
echo "===================================================="

# Download ASM if not present
mkdir -p "$LIB_DIR"
ASM_JAR="$LIB_DIR/asm-9.6.jar"
ASM_COMMONS="$LIB_DIR/asm-commons-9.6.jar"
TOOLS_JAR=""

# Try to find tools.jar (JDK attach API)
if [ -f "$JAVA_HOME/lib/tools.jar" ]; then
    TOOLS_JAR="$JAVA_HOME/lib/tools.jar"
fi

if [ ! -f "$ASM_JAR" ]; then
    echo "[Builder] Downloading ASM 9.6..."
    curl -L "https://search.maven.org/remotecontent?filepath=org/ow2/asm/asm/9.6/asm-9.6.jar" -o "$ASM_JAR" 2>/dev/null
    curl -L "https://search.maven.org/remotecontent?filepath=org/ow2/asm/asm-commons/9.6/asm-commons-9.6.jar" -o "$ASM_COMMONS" 2>/dev/null
    echo "[Builder] ASM downloaded."
fi

CLASSPATH="$ASM_JAR:$ASM_COMMONS"
if [ -n "$TOOLS_JAR" ]; then
    CLASSPATH="$CLASSPATH:$TOOLS_JAR"
fi

# Compile
mkdir -p "$OUT_DIR"
echo "[Builder] Compiling agent sources..."
find "$SCRIPT_DIR" -name "*.java" | xargs javac \
    -cp "$CLASSPATH" \
    -d "$OUT_DIR" \
    --release 11 \
    -Xlint:none \
    2>&1

if [ $? -ne 0 ]; then
    echo "[Builder] Compilation FAILED."
    exit 1
fi

# Package
echo "[Builder] Packaging quark-agent.jar..."
cd "$OUT_DIR"
jar cfm "$JAR_OUT" "$SCRIPT_DIR/manifest.txt" \
    $(find . -name "*.class" | sort) \
    2>&1

if [ $? -eq 0 ]; then
    echo "[Builder] Built: $JAR_OUT"
    echo "[Builder] Done!"
else
    echo "[Builder] Packaging FAILED."
    exit 1
fi

# Build injector JAR (just Injector.java with Main-Class)
INJ_MANIFEST=$(mktemp)
echo "Manifest-Version: 1.0" > "$INJ_MANIFEST"
echo "Main-Class: cc.quark.agent.Injector" >> "$INJ_MANIFEST"
echo "" >> "$INJ_MANIFEST"

jar cfm "$SCRIPT_DIR/quark-injector.jar" "$INJ_MANIFEST" \
    -C "$OUT_DIR" cc/quark/agent/Injector.class \
    2>&1

rm -f "$INJ_MANIFEST"
echo "[Builder] Injector built: $SCRIPT_DIR/quark-injector.jar"
