@echo off
setlocal

cd /d "%~dp0frontend"
call npm.cmd run check
if errorlevel 1 exit /b %errorlevel%

cd /d "%~dp0backend"
if not defined JAVA_HOME (
  if exist "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.1"
)
call mvnw.cmd test
exit /b %errorlevel%
