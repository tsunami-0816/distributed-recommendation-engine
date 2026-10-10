@echo off
title Frontend Service - Port 5174

cd /d "%~dp0.."
cd apps\recommend-admin

echo ========================================
echo    Starting Frontend Service...
echo    Port: 5174
echo ========================================
echo.

if not exist "node_modules" goto install_deps
goto start_dev

:install_deps
echo [INFO] First run detected. Installing frontend dependencies...
echo [INFO] This may take 1-3 minutes, please wait...
echo.
call npm install
if errorlevel 1 goto install_fail
echo.
echo [DONE] Frontend dependencies installed successfully!
echo.
goto start_dev

:install_fail
echo.
echo [ERROR] Failed to install dependencies. Please check your network and retry.
pause
exit /b 1

:start_dev
echo [STARTING] Frontend dev server starting. Browser will open automatically...
echo.
call npm run dev

echo.
echo Frontend service stopped.
pause
