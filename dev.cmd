@echo off
setlocal

if not defined JAVA_HOME (
  if exist "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.1"
)

start "notehub-backend" /D "%~dp0backend" cmd /c "mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=18080"
start "notehub-frontend" /D "%~dp0frontend" cmd /c "npm.cmd run dev -- --host 127.0.0.1"

echo Frontend: http://127.0.0.1:3000
echo Backend:  http://127.0.0.1:18080
