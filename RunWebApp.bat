@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0web"

echo ========================================================
echo    Launching Weather App (Web Version) on Port 3000
echo ========================================================

:: Check for Node.js
where node >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] Detected Node.js runtime.
    start "" http://localhost:3000
    node server.js
    goto :end
)

:: Check for Python
where python >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] Detected Python runtime.
    start "" http://localhost:3000
    python server.py
    goto :end
)

:: Check for Java 18+ jwebserver
where jwebserver >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] Detected Java jwebserver runtime.
    start "" http://localhost:3000
    jwebserver -p 3000 -d .
    goto :end
)

:: Fallback: Open index.html directly in default browser
echo [INFO] No local web runtime detected. Opening index.html directly in browser...
start "" index.html

:end
pause
