@echo off
title Env Check - Recommendation Engine

echo ========================================
echo    Distributed Recommendation Engine
echo    Environment Check
echo ========================================
echo.

set ALL_OK=1
set JAVA_VER=
set MAVEN_VER=
set NODE_VER=
set NPM_VER=
set TMPFILE=%temp%\rec_env_check.txt

echo [1/3] Checking Java (need 17 or higher)...
java -version >nul 2>&1
if errorlevel 1 goto java_fail
java -version > "%TMPFILE%" 2>&1
for /f "tokens=3" %%v in ('findstr /i "version" "%TMPFILE%"') do set JAVA_VER=%%v
echo   [OK] Java version: %JAVA_VER%
goto java_done
:java_fail
echo   [FAIL] Java not found. Please install JDK 17.
echo   Download: https://adoptium.net/ (choose Temurin 17 LTS)
set ALL_OK=0
:java_done
echo.

echo [2/3] Checking Maven (need 3.6 or higher)...
call mvn -version >nul 2>&1
if errorlevel 1 goto maven_fail
call mvn -version > "%TMPFILE%" 2>&1
for /f "tokens=3" %%v in ('findstr /b /i /c:"Apache Maven" "%TMPFILE%"') do set MAVEN_VER=%%v
echo   [OK] Maven version: %MAVEN_VER%
goto maven_done
:maven_fail
echo   [FAIL] Maven not found. Please install Maven.
echo   Download: https://maven.apache.org/download.cgi
set ALL_OK=0
:maven_done
echo.

echo [3/3] Checking Node.js (need 16 or higher)...
node -v >nul 2>&1
if errorlevel 1 goto node_fail
node -v > "%TMPFILE%" 2>&1
for /f "delims=" %%v in ('type "%TMPFILE%"') do set NODE_VER=%%v
call npm -v > "%TMPFILE%" 2>&1
for /f "delims=" %%v in ('type "%TMPFILE%"') do set NPM_VER=%%v
echo   [OK] Node.js version: %NODE_VER%
echo   [OK] npm version: %NPM_VER%
goto node_done
:node_fail
echo   [FAIL] Node.js not found. Please install Node.js.
echo   Download: https://nodejs.org/ (choose LTS version)
set ALL_OK=0
:node_done
echo.

del "%TMPFILE%" >nul 2>&1

echo ========================================
if "%ALL_OK%"=="1" goto all_pass
echo   Some checks failed. Please install missing software.
echo   Then run this script again to confirm.
goto end
:all_pass
echo   All checks passed! You can start the project now.
echo   Please double-click: 04-start-all.bat
:end
echo ========================================
echo.
pause
