@echo off
echo ========================================
echo    Banking System with Embedded ActiveMQ
echo ========================================
echo.

echo Starting Banking Application with Embedded ActiveMQ...
echo This will use embedded ActiveMQ for testing purposes.
echo.

mvn spring-boot:run -Dspring-boot.run.arguments="--spring.activemq.broker-url=vm://embedded-broker"

echo.
echo System Status:
echo - Banking Application: http://localhost:8080
echo - Login: http://localhost:8080/auth/login
echo - Admin Dashboard: http://localhost:8080/admin/dashboard
echo - Create Payment: http://localhost:8080/payment/createPayment
echo.
echo Press any key to exit...
pause >nul
