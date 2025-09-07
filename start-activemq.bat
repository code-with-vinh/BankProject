@echo off
echo Starting ActiveMQ...

REM Check if ActiveMQ is already running
netstat -an | findstr :61616 >nul
if %errorlevel% == 0 (
    echo ActiveMQ is already running on port 61616
    goto :webconsole
)

REM Try to start ActiveMQ from common locations
if exist "C:\apache-activemq-5.17.0\bin\win64\activemq.bat" (
    echo Starting ActiveMQ from C:\apache-activemq-5.17.0...
    start "ActiveMQ" "C:\apache-activemq-5.17.0\bin\win64\activemq.bat"
    goto :wait
)

if exist "apache-activemq-5.17.0\bin\win64\activemq.bat" (
    echo Starting ActiveMQ from current directory...
    start "ActiveMQ" "apache-activemq-5.17.0\bin\win64\activemq.bat"
    goto :wait
)

echo ActiveMQ not found. Please install ActiveMQ first.
echo Download from: https://activemq.apache.org/downloads
echo Extract to C:\apache-activemq-5.17.0\
pause
exit

:wait
echo Waiting for ActiveMQ to start...
timeout /t 10 /nobreak >nul

:webconsole
echo.
echo ActiveMQ Web Console: http://localhost:8161/admin
echo Username: admin
echo Password: admin
echo.
echo Press any key to continue...
pause >nul
