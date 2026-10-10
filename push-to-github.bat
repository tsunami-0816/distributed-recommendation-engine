@echo off
chcp 65001 >nul
echo ========================================
echo   Push to GitHub - distributed-recommendation-engine
echo ========================================
echo.

cd /d "D:\lg\project\distributed-recommendation-engine"

echo [1/6] Cleaning old .git directory...
if exist .git (
    rmdir /s /q .git
)

echo [2/6] Initializing git repository...
git init
if errorlevel 1 (
    echo ERROR: git init failed
    pause
    exit /b 1
)

echo [3/6] Adding files...
git add .
if errorlevel 1 (
    echo ERROR: git add failed
    pause
    exit /b 1
)

echo [4/6] Committing...
git commit -m "Initial commit"
if errorlevel 1 (
    echo ERROR: git commit failed
    pause
    exit /b 1
)

echo [5/6] Renaming branch to main...
git branch -M main

echo [6/6] Adding remote and pushing...
git remote add origin https://github.com/tsunami-0816/distributed-recommendation-engine.git
git push -u origin main
if errorlevel 1 (
    echo.
    echo ERROR: git push failed. You may need to authenticate.
    echo If a browser login window appears, please complete it.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   SUCCESS! Project pushed to GitHub.
echo   https://github.com/tsunami-0816/distributed-recommendation-engine
echo ========================================
echo.
pause
