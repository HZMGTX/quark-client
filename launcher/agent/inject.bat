@echo off
:: ============================================================
::  Quark.cc Injector - Windows
::  Usage: inject.bat [pid]
::
::  Run Minecraft first, then double-click inject.bat
::  OR: inject.bat <pid>
:: ============================================================

title Quark.cc Ghost Client Injector

set SCRIPT_DIR=%~dp0
set AGENT_JAR=%SCRIPT_DIR%quark-agent.jar
set INJECTOR_JAR=%SCRIPT_DIR%quark-injector.jar

if not exist "%AGENT_JAR%" (
    echo [inject.bat] quark-agent.jar not found. Please build first.
    echo [inject.bat] Run: build-agent.bat
    pause
    exit /b 1
)

if not exist "%INJECTOR_JAR%" (
    echo [inject.bat] quark-injector.jar not found.
    pause
    exit /b 1
)

set QUARK_AGENT=%AGENT_JAR%

echo [inject.bat] Injecting Quark.cc into Minecraft...
java -cp "%INJECTOR_JAR%;%JAVA_HOME%\lib\tools.jar" cc.quark.agent.Injector %*

if %ERRORLEVEL% neq 0 (
    echo.
    echo [inject.bat] Injection failed. Try:
    echo  - Run as Administrator
    echo  - Make sure Minecraft is fully loaded
    echo  - Check Java version (requires JDK 17+)
    pause
)
