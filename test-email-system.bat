@echo off
echo Testing Email System...

echo.
echo 1. Testing payment processing with email notification...
curl -X POST http://localhost:8080/api/payment/process ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\": 500000, \"currency\": \"VND\", \"account\": {\"accountId\": 1}}"

echo.
echo.
echo 2. Testing payment processing with failure (insufficient balance)...
curl -X POST http://localhost:8080/api/payment/process ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\": 2000000, \"currency\": \"VND\", \"account\": {\"accountId\": 1}}"

echo.
echo.
echo 3. Testing payment processing with invalid account...
curl -X POST http://localhost:8080/api/payment/process ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\": 100000, \"currency\": \"VND\", \"account\": {\"accountId\": 999}}"

echo.
echo.
echo Test completed. Check your email inbox for notifications!
echo Check console logs for SMS notifications.
pause
