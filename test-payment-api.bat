@echo off
echo Testing Payment API...

echo.
echo 1. Testing payment processing...
curl -X POST http://localhost:8080/api/payment/process ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\": 100000, \"currency\": \"VND\", \"account\": {\"accountId\": 1}}"

echo.
echo.
echo 2. Testing payment processing with USD...
curl -X POST http://localhost:8080/api/payment/process ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\": 50, \"currency\": \"USD\", \"account\": {\"accountId\": 1}}"

echo.
echo.
echo 3. Testing invalid payment (negative amount)...
curl -X POST http://localhost:8080/api/payment/process ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\": -100, \"currency\": \"VND\", \"account\": {\"accountId\": 1}}"

echo.
echo.
echo Test completed. Check the console logs for notification messages.
pause
