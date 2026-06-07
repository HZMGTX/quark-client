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
