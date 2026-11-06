@echo off
cd /d "%~dp0web"
echo Starting Weather App Web Server at http://localhost:3000 ...
start http://localhost:3000
node server.js
pause
