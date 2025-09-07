@echo off
echo Testing Payment Request System...

echo.
echo 1. Testing Admin Create Payment Request Page...
curl -s -o nul -w "Status: %%{http_code}\n" http://localhost:8080/admin/payment/create-request

echo.
echo 2. Testing Customer Payment Requests Page...
curl -s -o nul -w "Status: %%{http_code}\n" "http://localhost:8080/customer/payment/my-requests?accountId=1"

echo.
echo 3. Testing API - Get All Payment Requests...
curl -s http://localhost:8080/admin/payment/api/all

echo.
echo Test completed!
pause
