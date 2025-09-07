# 🏦 Banking System - Hướng dẫn sử dụng

## 🎯 Tổng quan hệ thống

Hệ thống Banking được thiết kế với giao diện hiện đại sử dụng **Glassmorphism** và hiệu ứng **bong bóng** tạo trải nghiệm người dùng tuyệt vời.

### ✨ Tính năng chính
- **Payment Service**: Xử lý thanh toán với ActiveMQ
- **Notification Service**: Gửi thông báo tự động
- **Admin Dashboard**: Quản lý hệ thống với UI đẹp mắt
- **Modern UI**: Hiệu ứng trong suốt và bong bóng

## 🚀 Cách khởi động

### 1. Khởi động nhanh (Windows)
```bash
# Chạy script tự động
start-system.bat
```

### 2. Khởi động thủ công

#### Bước 1: Khởi động ActiveMQ
```bash
# Tải ActiveMQ từ: https://activemq.apache.org/downloads
# Giải nén và chạy:
C:\apache-activemq-5.17.0\bin\win64\activemq.bat
```

#### Bước 2: Khởi động ứng dụng
```bash
mvn spring-boot:run
```

## 🌐 Truy cập hệ thống

### URLs chính:
- **Ứng dụng chính**: http://localhost:8080
- **ActiveMQ Console**: http://localhost:8161/admin
  - Username: `admin`
  - Password: `admin`

### Trang chính:
- **Login**: http://localhost:8080/auth/login
- **Admin Dashboard**: http://localhost:8080/admin/dashboard
- **Create Payment**: http://localhost:8080/payment/createPayment
- **View Payments**: http://localhost:8080/payment/viewAllPayment

## 🎨 Giao diện người dùng

### Hiệu ứng đặc biệt:
- **Glassmorphism**: Hiệu ứng kính mờ với backdrop-filter
- **Floating Bubbles**: Bong bóng bay lơ lửng
- **Gradient Backgrounds**: Nền gradient động
- **Smooth Animations**: Chuyển động mượt mà
- **Hover Effects**: Hiệu ứng khi di chuột

### Màu sắc chủ đạo:
- **Primary**: #667eea (Xanh dương)
- **Secondary**: #764ba2 (Tím)
- **Accent**: #f093fb (Hồng)
- **Success**: #4facfe (Xanh lá)
- **Warning**: #f093fb (Cam)
- **Danger**: #ff6b6b (Đỏ)

## 🔧 API Endpoints

### Payment API:
```bash
# Tạo thanh toán
POST /api/payment/process
Content-Type: application/json

{
  "amount": 100000,
  "currency": "VND",
  "account": {
    "accountId": 1
  }
}

# Lấy thông tin thanh toán
GET /api/payment/{paymentId}
```

### Test API:
```bash
# Windows
test-payment-api.bat

# Linux/Mac
./test-payment-api.sh
```

## 📱 Responsive Design

Hệ thống được thiết kế responsive hoàn toàn:
- **Desktop**: Giao diện đầy đủ với sidebar
- **Tablet**: Layout tối ưu cho màn hình vừa
- **Mobile**: Giao diện tối giản, dễ sử dụng

## 🎭 Hiệu ứng đặc biệt

### 1. Floating Bubbles
- Bong bóng bay từ dưới lên trên
- Tự động tạo và xóa
- Tạo cảm giác sống động

### 2. Glassmorphism
- Hiệu ứng kính mờ
- Backdrop blur
- Border trong suốt

### 3. Gradient Animations
- Nền gradient chuyển động
- Màu sắc thay đổi liên tục
- Tạo cảm giác hiện đại

### 4. Hover Effects
- Transform scale
- Box shadow
- Color transitions

## 🛠️ Cấu hình

### ActiveMQ Configuration:
```properties
spring.activemq.broker-url=tcp://localhost:61616
spring.activemq.user=admin
spring.activemq.password=admin
```

### Database Configuration:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=BankData
spring.datasource.username=sa
spring.datasource.password=1234
```

## 🐛 Troubleshooting

### 1. ActiveMQ không kết nối được
- Kiểm tra ActiveMQ đã khởi động chưa
- Kiểm tra port 61616 có bị block không
- Kiểm tra firewall

### 2. Ứng dụng không khởi động được
- Kiểm tra database connection
- Kiểm tra port 8080 có bị sử dụng không
- Xem log để tìm lỗi

### 3. Giao diện không hiển thị đúng
- Kiểm tra CSS files đã load chưa
- Kiểm tra browser có hỗ trợ backdrop-filter không
- Clear cache browser

## 📊 Monitoring

### ActiveMQ Web Console:
- URL: http://localhost:8161/admin
- Monitor queues và messages
- Xem statistics

### Application Logs:
- Console output
- Log files trong target/logs
- Debug mode: `logging.level.com.banking=DEBUG`

## 🎉 Kết luận

Hệ thống Banking đã được hoàn thiện với:
- ✅ Payment Service với ActiveMQ
- ✅ Notification Service
- ✅ Giao diện hiện đại với hiệu ứng đẹp
- ✅ Responsive design
- ✅ API đầy đủ
- ✅ Documentation chi tiết

**Chúc bạn sử dụng hệ thống vui vẻ! 🎊**
