@echo off
echo ========================================
echo Testing Payment Request System
echo ========================================

echo.
echo 1. Testing Admin Create Payment Request Page...
curl -s -o nul -w "Status: %%{http_code}\n" http://localhost:8080/admin/payment/create-request

echo.
echo 2. Testing Customer Payment Requests Page...
curl -s -o nul -w "Status: %%{http_code}\n" "http://localhost:8080/customer/payment/my-requests?accountId=1"

echo.
echo 3. Testing API - Create Payment Request...
curl -X POST http://localhost:8080/admin/payment/create-request ^
  -H "Content-Type: application/json" ^
  -d "{\"accountId\": 1, \"amount\": 500000, \"currency\": \"VND\", \"description\": \"Test payment request\"}"

echo.
echo 4. Testing API - Get All Payment Requests...
curl -s http://localhost:8080/admin/payment/api/all

echo.
echo ========================================
echo Test completed!
echo ========================================
pause
