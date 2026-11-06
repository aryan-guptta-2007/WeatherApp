@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ========================================================
echo    Launching Weather App (Java Desktop GUI Version)
echo ========================================================

set "JAVA_EXE="

:: 1. Check JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVA_EXE=%JAVA_HOME%\bin\javaw.exe"
    if not defined JAVA_EXE if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

:: 2. Check installed Program Files JDKs
if not defined JAVA_EXE (
    for /d %%D in ("C:\Program Files\Java\jdk*") do (
        if exist "%%D\bin\javaw.exe" set "JAVA_EXE=%%D\bin\javaw.exe"
    )
)

:: 3. Check system javaw in PATH
if not defined JAVA_EXE (
    where javaw >nul 2>nul
    if %errorlevel% equ 0 set "JAVA_EXE=javaw"
)

:: 4. Check system java in PATH
if not defined JAVA_EXE (
    where java >nul 2>nul
    if %errorlevel% equ 0 set "JAVA_EXE=java"
)

if not defined JAVA_EXE (
    echo [ERROR] Java Runtime not found! Please install Java 17+ or set JAVA_HOME.
    pause
    exit /b 1
)

echo Using Java executable: !JAVA_EXE!
start "" "!JAVA_EXE!" -jar WeatherApp.jar
exit /b 0
