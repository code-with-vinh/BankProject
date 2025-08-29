# Hướng dẫn sử dụng chức năng Admin

## Tổng quan
Hệ thống banking đã được cập nhật với các chức năng quản lý toàn diện cho Admin, bao gồm:

## 1. Dashboard Admin (`/admin/dashboard`)
- **Mô tả**: Trang tổng quan với thống kê hệ thống
- **Chức năng**:
  - Hiển thị tổng số tài khoản, thẻ, giao dịch
  - Thao tác nhanh đến các chức năng chính
  - Giao diện hiện đại với sidebar navigation

## 2. Quản lý tài khoản (`/admin/accounts`)
- **Mô tả**: Quản lý danh sách tất cả tài khoản trong hệ thống
- **Chức năng**:
  - Xem danh sách tài khoản với thông tin chi tiết
  - Tìm kiếm theo email hoặc tên
  - Lọc theo role (Customer/Admin) và level (Silver/Gold/Platinum)
  - Cập nhật role của tài khoản
  - Xóa tài khoản (chỉ khi không có thẻ)
  - Xem chi tiết tài khoản

## 3. Quản lý thẻ (`/admin/cards`)
- **Mô tả**: Quản lý danh sách tất cả thẻ trong hệ thống
- **Chức năng**:
  - Xem danh sách thẻ với thông tin chi tiết
  - Tìm kiếm theo số thẻ
  - Lọc theo loại thẻ (Debit/Credit) và trạng thái (Active/Inactive/Expired)
  - Cập nhật trạng thái thẻ
  - Xóa thẻ
  - Xem chi tiết thẻ và thông tin chủ thẻ

## 4. Lịch sử giao dịch (`/admin/transactions`)
- **Mô tả**: Quản lý danh sách tất cả giao dịch trong hệ thống
- **Chức năng**:
  - Xem danh sách giao dịch với thông tin chi tiết
  - Tìm kiếm theo ID giao dịch
  - Lọc theo loại giao dịch (Chuyển khoản/Rút tiền/Nạp tiền)
  - Lọc theo trạng thái (Thành công/Đang xử lý/Thất bại)
  - Lọc theo ngày giao dịch
  - Xem chi tiết giao dịch

## 5. Tạo tài khoản mới (`/admin/create-user`)
- **Mô tả**: Tạo tài khoản mới cho hệ thống
- **Chức năng**:
  - Form tạo tài khoản với validation đầy đủ
  - Chọn role (Customer/Admin)
  - Chọn level (Silver/Gold/Platinum)
  - Validation mật khẩu và xác nhận mật khẩu
  - Validation email và số điện thoại
  - Hiển thị thông báo thành công/lỗi

## 6. Chi tiết tài khoản (`/admin/account/{id}`)
- **Mô tả**: Xem thông tin chi tiết của một tài khoản
- **Chức năng**:
  - Hiển thị thông tin tài khoản đầy đủ
  - Danh sách thẻ của tài khoản
  - Lịch sử giao dịch của tài khoản
  - Thống kê số lượng thẻ và giao dịch

## Cách sử dụng

### Đăng nhập Admin
1. Truy cập `/auth/login`
2. Đăng nhập với tài khoản có role ADMIN
3. Sau khi đăng nhập thành công, hệ thống sẽ redirect đến `/admin/dashboard`

### Navigation
- Sử dụng sidebar bên trái để di chuyển giữa các chức năng
- Mỗi trang đều có nút "Quay lại Dashboard" để quay về trang chính

### Tìm kiếm và lọc
- Sử dụng các ô tìm kiếm để tìm kiếm nhanh
- Sử dụng các dropdown để lọc dữ liệu
- Nút "Xóa bộ lọc" để reset tất cả bộ lọc

### Thao tác
- Các nút thao tác được hiển thị trong cột cuối của bảng
- Sử dụng modal để xác nhận các thao tác quan trọng
- Thông báo thành công/lỗi được hiển thị rõ ràng

## Bảo mật
- Tất cả các trang admin đều yêu cầu quyền ADMIN
- Kiểm tra quyền trước mỗi thao tác
- Redirect về trang login nếu không có quyền

## Giao diện
- Sử dụng Bootstrap 5 cho responsive design
- Font Awesome cho icons
- Gradient colors cho visual appeal
- Hover effects và animations
- Modal dialogs cho các thao tác

## Database Schema
Các entity chính được sử dụng:
- `Account`: Thông tin tài khoản
- `Card`: Thông tin thẻ
- `Transaction`: Lịch sử giao dịch
- `Balance`: Số dư tài khoản

## API Endpoints
- `GET /admin/dashboard` - Dashboard chính
- `GET /admin/accounts` - Danh sách tài khoản
- `GET /admin/cards` - Danh sách thẻ
- `GET /admin/transactions` - Danh sách giao dịch
- `GET /admin/create-user` - Form tạo tài khoản
- `POST /admin/create-user` - Tạo tài khoản mới
- `POST /admin/update-role/{id}` - Cập nhật role
- `POST /admin/delete-account/{id}` - Xóa tài khoản
- `POST /admin/update-card-status/{id}` - Cập nhật trạng thái thẻ
- `POST /admin/delete-card/{id}` - Xóa thẻ
- `GET /admin/account/{id}` - Chi tiết tài khoản
