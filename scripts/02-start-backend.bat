@echo off
title Backend Service - Port 8084

cd /d "%~dp0.."
cd apps\recommendation-engine

echo ========================================
echo    Starting Backend Service...
echo    Port: 8084
echo    First run may download dependencies, please wait
echo ========================================
echo.

call mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Xmx512m

echo.
echo Backend service stopped.
pause
