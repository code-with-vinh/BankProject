#!/bin/bash

echo "========================================"
echo "   Banking System Startup Script"
echo "========================================"
echo

echo "[1/3] Starting ActiveMQ..."
echo "Checking if ActiveMQ is already running..."
if lsof -i :61616 >/dev/null 2>&1; then
    echo "ActiveMQ is already running on port 61616"
else
    echo "Starting ActiveMQ..."
    if [ -f "apache-activemq-5.17.0/bin/activemq" ]; then
        echo "Starting ActiveMQ from current directory..."
        ./apache-activemq-5.17.0/bin/activemq start
    else
        echo "ActiveMQ not found. Please install ActiveMQ first."
        echo "Download from: https://activemq.apache.org/downloads"
        echo "Extract to current directory"
        exit 1
    fi
    
    echo "Waiting for ActiveMQ to start..."
    sleep 10
fi

echo
echo "[2/3] Starting Banking Application..."
echo "Compiling and starting Spring Boot application..."
mvn spring-boot:run

echo
echo "[3/3] System Status:"
echo "- ActiveMQ Web Console: http://localhost:8161/admin"
echo "- Banking Application: http://localhost:8080"
echo "- Username: admin"
echo "- Password: admin"
echo
echo "Press any key to exit..."
read -n 1
