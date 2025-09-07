# Hệ thống Thanh toán với ActiveMQ

## Tổng quan
Hệ thống thanh toán sử dụng Message Queue (ActiveMQ) để xử lý các giao dịch thanh toán một cách bất đồng bộ.

## Kiến trúc hệ thống

### 1. Payment Service
- **Chức năng**: Nhận yêu cầu thanh toán từ người dùng và gửi đến Message Queue
- **API Endpoints**:
  - `POST /api/payment/process` - Xử lý thanh toán
  - `GET /api/payment/{paymentId}` - Lấy thông tin thanh toán
  - `GET /payment/createPayment` - Form tạo thanh toán (Web UI)

### 2. Notification Service
- **Chức năng**: Lắng nghe message từ Queue và gửi thông báo
- **Xử lý**: Tự động gửi email và SMS khi thanh toán hoàn tất

### 3. Message Queue (ActiveMQ)
- **Chức năng**: Lưu trữ và chuyển tiếp message giữa các service
- **Queue**: `payment.queue`

## Cấu hình

### 1. ActiveMQ Configuration
```properties
# ActiveMQ Configuration
spring.activemq.broker-url=tcp://localhost:61616
spring.activemq.user=admin
spring.activemq.password=admin
spring.activemq.pool.enabled=true
spring.activemq.pool.max-connections=10

# JMS Configuration
spring.jms.cache.enabled=true
spring.jms.cache.connection-cache-size=10
```

### 2. Dependencies (pom.xml)
```xml
<!-- ActiveMQ -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-activemq</artifactId>
</dependency>

<!-- ActiveMQ Pool -->
<dependency>
    <groupId>org.apache.activemq</groupId>
    <artifactId>activemq-pool</artifactId>
</dependency>
```

## Cách sử dụng

### 1. Khởi động ActiveMQ
```bash
# Tải và cài đặt ActiveMQ
# Khởi động broker
bin/activemq start
```

### 2. Khởi động ứng dụng
```bash
mvn spring-boot:run
```

### 3. Test API

#### Tạo thanh toán
```bash
curl -X POST http://localhost:8080/api/payment/process \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100000,
    "currency": "VND",
    "account": {
      "accountId": 1
    }
  }'
```

#### Lấy thông tin thanh toán
```bash
curl -X GET http://localhost:8080/api/payment/1
```

## Luồng xử lý

1. **Client gửi yêu cầu thanh toán** → PaymentController
2. **PaymentController** → PaymentService.processPayment()
3. **PaymentService** → Validate dữ liệu → Gửi message đến ActiveMQ
4. **ActiveMQ** → Lưu trữ message trong queue `payment.queue`
5. **NotificationService** → Lắng nghe queue → Xử lý thông báo
6. **NotificationService** → Gửi email/SMS (giả lập)

## Monitoring

### Console Logs
- Payment Service: `Payment request sent to queue: ...`
- Notification Service: 
  ```
  === PAYMENT NOTIFICATION ===
  Payment confirmed for paymentId: 12345
  Account ID: 1
  Amount: 100000.0 VND
  =============================
  📧 EMAIL SENT: ...
  📱 SMS SENT: ...
  ```

### ActiveMQ Web Console
- URL: http://localhost:8161/admin
- Username: admin
- Password: admin

## Troubleshooting

### 1. ActiveMQ không kết nối được
- Kiểm tra ActiveMQ đã khởi động chưa
- Kiểm tra port 61616 có bị block không
- Kiểm tra cấu hình trong application.properties

### 2. Message không được xử lý
- Kiểm tra NotificationService có @JmsListener đúng không
- Kiểm tra queue name có khớp không
- Kiểm tra log để xem có lỗi gì không

### 3. Test không chạy được
- Đảm bảo có profile "test" trong application-test.properties
- Kiểm tra test database đã cấu hình chưa
