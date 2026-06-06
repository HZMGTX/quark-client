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
echo " Quark Ghost Client Agent Builder"
echo " Pure JVM-injection — no mods folder needed"
echo "===================================================="

mkdir -p "$LIB_DIR"
ASM_JAR="$LIB_DIR/asm-9.6.jar"
ASM_COMMONS="$LIB_DIR/asm-commons-9.6.jar"

# Locate tools.jar / attach API for the AttachShim
TOOLS_JAR=""
ATTACH_ARG=""
if [ -n "$JAVA_HOME" ]; then
    if [ -f "$JAVA_HOME/lib/tools.jar" ]; then
        TOOLS_JAR="$JAVA_HOME/lib/tools.jar"
    fi
fi
# JDK 9+ has the attach API built in — no tools.jar needed

if [ ! -f "$ASM_JAR" ]; then
    echo "[Builder] Downloading ASM 9.6…"
    curl -fsSL "https://search.maven.org/remotecontent?filepath=org/ow2/asm/asm/9.6/asm-9.6.jar" -o "$ASM_JAR"
    curl -fsSL "https://search.maven.org/remotecontent?filepath=org/ow2/asm/asm-commons/9.6/asm-commons-9.6.jar" -o "$ASM_COMMONS"
    echo "[Builder] ASM downloaded."
fi

CLASSPATH="$ASM_JAR:$ASM_COMMONS"
[ -n "$TOOLS_JAR" ] && CLASSPATH="$CLASSPATH:$TOOLS_JAR"

mkdir -p "$OUT_DIR"
echo "[Builder] Compiling agent sources…"

JAVA_FILES=$(find "$SCRIPT_DIR" -name "*.java" | sort)
javac \
    -cp "$CLASSPATH" \
    -d "$OUT_DIR" \
    --release 11 \
    -Xlint:none \
    $JAVA_FILES

if [ $? -ne 0 ]; then
    echo "[Builder] Compilation FAILED."
    exit 1
fi

echo "[Builder] Packaging quark-agent.jar…"
cd "$OUT_DIR"

jar cfm "$JAR_OUT" "$SCRIPT_DIR/manifest.txt" \
    $(find . -name "*.class" | sort)

if [ $? -ne 0 ]; then
    echo "[Builder] Packaging FAILED."
    exit 1
fi

echo "[Builder] Built: $JAR_OUT"

# Also build a standalone shim JAR that only contains AttachShim
SHIM_MF=$(mktemp)
cat > "$SHIM_MF" << 'MANIFEST'
Manifest-Version: 1.0
Main-Class: cc.quark.agent.AttachShim

MANIFEST

jar cfm "$SCRIPT_DIR/quark-attach.jar" "$SHIM_MF" \
    -C "$OUT_DIR" cc/quark/agent/AttachShim.class 2>/dev/null && \
    echo "[Builder] Attach shim built: $SCRIPT_DIR/quark-attach.jar"

rm -f "$SHIM_MF"

# Windows batch companion
cat > "$SCRIPT_DIR/inject.bat" << 'WINBAT'
@echo off
setlocal
set SCRIPT_DIR=%~dp0
set AGENT_JAR=%SCRIPT_DIR%quark-agent.jar

if "%1"=="" (
    echo Usage: inject.bat ^<pid^>
    echo.
    echo Running process scanner...
    jps -l 2>nul || java -cp "%AGENT_JAR%" cc.quark.agent.AttachShim --list
    exit /b 1
)

echo [Quark] Attaching to PID %1 ...
java -cp "%AGENT_JAR%" cc.quark.agent.AttachShim %1 "%AGENT_JAR%"
if %errorlevel% equ 0 (
    echo [Quark] Injection successful! Press Right-Shift in Minecraft.
) else (
    echo [Quark] Injection failed. Check console output above.
)
endlocal
WINBAT

# Unix shell companion
cat > "$SCRIPT_DIR/inject.sh" << 'SHELL'
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
SHELL
chmod +x "$SCRIPT_DIR/inject.sh"

echo "===================================================="
echo " Build complete!"
echo " Agent JAR:  $JAR_OUT"
echo " Attach shim: $SCRIPT_DIR/quark-attach.jar"
echo ""
echo " To inject manually:"
echo "   Linux/Mac: ./inject.sh <minecraft-pid>"
echo "   Windows:   inject.bat <minecraft-pid>"
echo "===================================================="
