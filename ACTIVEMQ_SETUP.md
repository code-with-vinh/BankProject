# Hướng dẫn cài đặt ActiveMQ

## 1. Tải ActiveMQ

### Windows
1. Tải ActiveMQ từ: https://activemq.apache.org/downloads
2. Giải nén vào thư mục (ví dụ: C:\apache-activemq-5.17.0)

### Linux/Mac
```bash
# Sử dụng wget
wget https://archive.apache.org/dist/activemq/5.17.0/apache-activemq-5.17.0-bin.tar.gz

# Giải nén
tar -xzf apache-activemq-5.17.0-bin.tar.gz
cd apache-activemq-5.17.0
```

## 2. Khởi động ActiveMQ

### Windows
```cmd
cd C:\apache-activemq-5.17.0\bin\win64
activemq.bat start
```

### Linux/Mac
```bash
cd apache-activemq-5.17.0/bin
./activemq start
```

## 3. Kiểm tra ActiveMQ

### Web Console
- Mở trình duyệt và truy cập: http://localhost:8161/admin
- Username: admin
- Password: admin

### Kiểm tra Queue
1. Vào tab "Queues"
2. Tìm queue "payment.queue" (sẽ được tạo khi ứng dụng chạy)

## 4. Cấu hình (nếu cần)

### Thay đổi port (mặc định: 61616)
Chỉnh sửa file `conf/activemq.xml`:
```xml
<transportConnector name="openwire" uri="tcp://0.0.0.0:61616"/>
```

### Thay đổi username/password
Chỉnh sửa file `conf/users.properties`:
```
admin=admin
user=password
```

## 5. Troubleshooting

### Port đã được sử dụng
```bash
# Windows
netstat -ano | findstr :61616

# Linux/Mac
lsof -i :61616
```

### Không thể kết nối
1. Kiểm tra firewall
2. Kiểm tra ActiveMQ đã khởi động chưa
3. Kiểm tra log trong thư mục `data/activemq.log`

### Dừng ActiveMQ
```bash
# Windows
activemq.bat stop

# Linux/Mac
./activemq stop
```
