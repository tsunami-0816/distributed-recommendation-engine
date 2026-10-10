@echo off
title Stop Services - Recommendation Engine

echo ========================================
echo    Distributed Recommendation Engine
echo    Stop All Services
echo ========================================
echo.

echo [1/2] Stopping backend service (port 8084)...
set FOUND1=0
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8084 ^| findstr LISTENING') do (
    echo   Killing process PID: %%a
    taskkill /F /PID %%a
    set FOUND1=1
)
if "%FOUND1%"=="0" echo   [INFO] No running backend service detected.

echo.
echo [2/2] Stopping frontend service (port 5174)...
set FOUND2=0
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5174 ^| findstr LISTENING') do (
    echo   Killing process PID: %%a
    taskkill /F /PID %%a
    set FOUND2=1
)
if "%FOUND2%"=="0" echo   [INFO] No running frontend service detected.

echo.
echo ========================================
echo    Done!
echo ========================================
echo.
echo Tip: If any black windows remain, you can close them manually.
echo.
pause
