# 📧 Email Notification System - Hướng dẫn sử dụng

## 🎯 Tổng quan

Hệ thống Banking đã được tích hợp hoàn chỉnh với **Email Notification System** sử dụng ActiveMQ để gửi thông báo tự động cho khách hàng và admin.

## ✨ Tính năng Email

### 📬 Loại email được gửi:

1. **Payment Confirmation Email** - Xác nhận thanh toán thành công
2. **Payment Failure Email** - Thông báo thanh toán thất bại  
3. **Admin Notification Email** - Thông báo cho admin về giao dịch mới

### 🎨 Template Email HTML đẹp mắt:
- **Responsive Design** - Tối ưu cho mọi thiết bị
- **Modern UI** - Giao diện hiện đại với gradient và hiệu ứng
- **Thông tin chi tiết** - Đầy đủ thông tin giao dịch
- **Branding** - Logo và màu sắc thương hiệu

## 🚀 Cách khởi động

### 1. Khởi động với Embedded ActiveMQ (Khuyến nghị)
```bash
# Windows
start-with-embedded-activemq.bat

# Hoặc chạy trực tiếp
mvn spring-boot:run
```

### 2. Khởi động với ActiveMQ thật
```bash
# Khởi động ActiveMQ trước
start-activemq.bat

# Sau đó khởi động ứng dụng
mvn spring-boot:run
```

## 📧 Cấu hình Email

### Gmail Configuration (Đã cấu hình sẵn):
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=languages.center25@gmail.com
spring.mail.password=ftmiukztsqbabbbt
```

### Cấu hình khác:
```properties
app.email.from=languages.center25@gmail.com
app.email.admin=languages.center25@gmail.com
```

## 🧪 Test Email System

### 1. Test tự động:
```bash
# Windows
test-email-system.bat
```

### 2. Test thủ công:

#### Test thanh toán thành công:
```bash
curl -X POST http://localhost:8080/api/payment/process \
  -H "Content-Type: application/json" \
  -d '{"amount": 500000, "currency": "VND", "account": {"accountId": 1}}'
```

#### Test thanh toán thất bại (số dư không đủ):
```bash
curl -X POST http://localhost:8080/api/payment/process \
  -H "Content-Type: application/json" \
  -d '{"amount": 2000000, "currency": "VND", "account": {"accountId": 1}}'
```

#### Test tài khoản không tồn tại:
```bash
curl -X POST http://localhost:8080/api/payment/process \
  -H "Content-Type: application/json" \
  -d '{"amount": 100000, "currency": "VND", "account": {"accountId": 999}}'
```

## 📱 Luồng xử lý Email

### 1. Thanh toán thành công:
```
Payment Request → PaymentService → ActiveMQ → NotificationService → EmailService
                                                                    ↓
                                                              Customer Email
                                                              Admin Email
```

### 2. Thanh toán thất bại:
```
Payment Request → PaymentService → Validation Error → NotificationService → EmailService
                                                                           ↓
                                                                     Customer Email (Failure)
                                                                     Admin Email (Failed)
```

## 📧 Template Email

### 1. Payment Confirmation Template:
- **File**: `templates/email/payment-confirmation.html`
- **Màu sắc**: Xanh dương gradient
- **Nội dung**: Thông tin giao dịch thành công
- **Hiệu ứng**: Icon check, amount highlight

### 2. Payment Failure Template:
- **File**: `templates/email/payment-failure.html`
- **Màu sắc**: Đỏ gradient
- **Nội dung**: Lý do thất bại, hướng dẫn
- **Hiệu ứng**: Icon warning, action buttons

### 3. Admin Notification Template:
- **File**: `templates/email/admin-notification.html`
- **Màu sắc**: Tím gradient
- **Nội dung**: Thông tin giao dịch cho admin
- **Hiệu ứng**: Status badge, admin actions

## 🔧 Cấu hình nâng cao

### Thay đổi email provider:
```properties
# Outlook/Hotmail
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587

# Yahoo
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587

# Custom SMTP
spring.mail.host=your-smtp-server.com
spring.mail.port=587
spring.mail.username=your-email@domain.com
spring.mail.password=your-password
```

### Thay đổi template:
1. Chỉnh sửa file HTML trong `templates/email/`
2. Sử dụng Thymeleaf syntax: `th:text="${variable}"`
3. Restart ứng dụng

## 🐛 Troubleshooting

### 1. Email không được gửi:
- Kiểm tra cấu hình SMTP
- Kiểm tra username/password
- Kiểm tra firewall/antivirus
- Xem log console để tìm lỗi

### 2. Template không hiển thị đúng:
- Kiểm tra file template có tồn tại không
- Kiểm tra Thymeleaf syntax
- Clear cache browser

### 3. ActiveMQ connection error:
- Sử dụng embedded ActiveMQ: `vm://embedded-broker`
- Hoặc cài đặt ActiveMQ thật

## 📊 Monitoring

### Console Logs:
```
=== PAYMENT NOTIFICATION ===
Payment confirmed for paymentId: 123
Account ID: 1
Amount: 500000.0 VND
=============================
📧 Payment confirmation email sent to: customer@example.com
📧 Admin notification email sent to: admin@example.com
📱 SMS SENT: Payment of 500000.0 VND confirmed. ID: 123
```

### Email Status:
- ✅ **Success**: Email gửi thành công
- ❌ **Error**: Có lỗi khi gửi email
- 📧 **Sent**: Email đã được gửi

## 🎉 Kết luận

Hệ thống Email Notification đã được tích hợp hoàn chỉnh với:
- ✅ 3 loại email template đẹp mắt
- ✅ Tích hợp với ActiveMQ
- ✅ Responsive design
- ✅ Error handling
- ✅ Admin notifications
- ✅ SMS simulation

**Chúc bạn sử dụng hệ thống email thành công! 📧✨**
