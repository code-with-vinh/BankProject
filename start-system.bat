@echo off
echo ========================================
echo    Banking System Startup Script
echo ========================================
echo.

echo [1/3] Starting ActiveMQ...
echo Checking if ActiveMQ is already running...
netstat -an | findstr :61616 >nul
if %errorlevel% == 0 (
    echo ActiveMQ is already running on port 61616
    goto :startapp
)

echo Starting ActiveMQ...
if exist "C:\apache-activemq-5.17.0\bin\win64\activemq.bat" (
    echo Starting ActiveMQ from C:\apache-activemq-5.17.0...
    start "ActiveMQ" "C:\apache-activemq-5.17.0\bin\win64\activemq.bat"
) else if exist "apache-activemq-5.17.0\bin\win64\activemq.bat" (
    echo Starting ActiveMQ from current directory...
    start "ActiveMQ" "apache-activemq-5.17.0\bin\win64\activemq.bat"
) else (
    echo ActiveMQ not found. Please install ActiveMQ first.
    echo Download from: https://activemq.apache.org/downloads
    echo Extract to C:\apache-activemq-5.17.0\
    pause
    exit
)

echo Waiting for ActiveMQ to start...
timeout /t 10 /nobreak >nul

:startapp
echo.
echo [2/3] Starting Banking Application...
echo Compiling and starting Spring Boot application...
mvn spring-boot:run

echo.
echo [3/3] System Status:
echo - ActiveMQ Web Console: http://localhost:8161/admin
echo - Banking Application: http://localhost:8080
echo - Username: admin
echo - Password: admin
echo.
echo Press any key to exit...
pause >nul
