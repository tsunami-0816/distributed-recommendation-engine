@echo off
title One-Click Start - Recommendation Engine

echo ========================================
echo    Distributed Recommendation Engine
echo    One-Click Start
echo ========================================
echo.
echo This script will start:
echo   [1] Backend service (port 8084)
echo   [2] Frontend service (port 5174)
echo.
echo Two black windows will pop up. Do NOT close them!
echo Browser will open automatically after frontend starts.
echo.
pause

cd /d "%~dp0"

echo.
echo [1/2] Starting backend service (new window)...
start "Backend-8084" cmd /k "02-start-backend.bat"

echo [WAIT] Waiting 5 seconds for backend to initialize...
timeout /t 5 /nobreak >nul

echo.
echo [2/2] Starting frontend service (new window)...
start "Frontend-5174" cmd /k "03-start-frontend.bat"

echo.
echo ========================================
echo    Startup Complete!
echo ========================================
echo.
echo Important:
echo   - Two black windows are running. Do NOT close them.
echo   - Backend first run takes 10-30 seconds to compile.
echo   - Browser opens at http://localhost:5174 automatically.
echo   - If page is blank, wait until backend window shows
echo     "Started RecommendationEngineApplication", then refresh.
echo.
echo To stop: double-click 05-stop-all.bat
echo.
echo Press any key to close this window (backend and frontend keep running).
pause >nul
