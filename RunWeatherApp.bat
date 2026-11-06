@echo off
cd /d "%~dp0"
if exist "C:\Program Files\Java\jdk-25.0.2\bin\javaw.exe" (
    start "" "C:\Program Files\Java\jdk-25.0.2\bin\javaw.exe" -jar WeatherApp.jar
) else (
    start "" javaw -jar WeatherApp.jar
)
