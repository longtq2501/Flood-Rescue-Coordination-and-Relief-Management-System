# Frontend Development Backlog & Roadmap

Tài liệu này tổng hợp các hạng mục công việc còn thiếu của phần Frontend. Các thành viên trong nhóm cần tuân thủ quy tắc đặt tên nhánh và yêu cầu kỹ thuật bên dưới.

## 📌 Quy tắc đặt tên nhánh (Git Branching)
- Tính năng mới: `feat/<tên-task>` (Ví dụ: `feat/ui-design-system`)
- Sửa lỗi: `fix/<tên-task>` (Ví dụ: `fix/sse-reconnect`)
- Cải thiện code: `refactor/<tên-task>`

---

## 🚀 Danh sách các Task

### 1. Task: Premium UI Design System & Layout
- **Git Branch**: `feat/ui-design-system`
- **Description**: Xây dựng hệ thống giao diện đồng nhất, chuyên nghiệp và bộ khung layout toàn cục.
- **Steps**:
    1. Thiết kế `GlobalLayout` với Sidebar (collapsible) và Header (breadcrumbs, notifications).
    2. Định nghĩa Design Tokens (màu sắc, spacing, typography) trong Tailwind 4.
    3. Tạo bộ thư viện UI Base: `Button`, `Input`, `Card`, `Badge`, `Modal` với hiệu ứng chuyển cảnh mượt mà.
- **Requirements**:
    - Mobile-first, tương thích iPhone SE và iPhone 16.
    - Sử dụng `lucide-react` cho icons.
    - Đảm bảo độ phản hồi (responsive) 100%.
- **⚠️ Note (Lỗi đang có cần lưu ý)**: Hiện dự án đang dùng React 19 và Next.js 15, khi cài Shadcn/Radix UI có thể báo lỗi xung đột peer dependency (`ERESOLVE`), hãy dùng `--legacy-peer-deps` hoặc `--force` khi cài đặt các component UI.

### 2. Task: Interactive Map Integration (Leaflet)
- **Git Branch**: `feat/map-integration`
- **Description**: Tích hợp bản đồ trực quan để theo dõi yêu cầu cứu trợ và vị trí đội cứu hộ.
- **Steps**:
    1. Cài đặt và cấu hình `react-leaflet` component.
    2. Hiển thị Marker cho các Requests (màu sắc Marker theo Urgency Level).
    3. Hiển thị Marker cho Rescue Teams và Vehicles.
    4. Cài đặt Popup hiển thị thông tin nhanh khi click vào marker.
- **Requirements**:
    - Hỗ trợ Cluster Marker khi dữ liệu quá dày đặc.
    - Bản đồ tự động cập nhật vị trí marker khi có dữ liệu mới.

### 3. Task: Resource & Team Management (CRUD)
- **Git Branch**: `feat/resource-management`
- **Description**: Xây dựng trang quản lý tài nguyên dành cho Manager và Coordinator.
- **Steps**:
    1. Tạo trang danh sách Đội cứu hộ và Phương tiện.
    2. Implement Form thêm mới/chỉnh sửa (Create/Edit) với validation Zod.
    3. Thêm chức năng cập nhật trạng thái nhanh (Sẵn sàng/Đang bận/Bảo trì).
- **Requirements**:
    - Phân quyền (Role Guard) chỉ cho phép Manager/Coordinator truy cập.
    - Hiển thị thông báo (Toast) khi thao tác thành công/thất bại.
- **⚠️ Note (Lỗi gọi API trượt trong Docker)**: Nếu bạn dùng Server Components (SSR) để fetch data danh sách, đừng dùng `http://localhost:8080/api`. Do chạy trong Docker, localhost là chính container frontend nên sẽ bị Connection Refused. Với SSR hãy cấu hình gọi qua DNS của container: `http://gateway:8080/api`. Chỉ Client Components mới dùng `localhost:8080`.

### 4. Task: SSE Real-time UI Synchronization
- **Git Branch**: `feat/sse-realtime-sync`
- **Description**: Tối ưu hóa SSE để cập nhật dữ liệu tự động trên UI mà không cần tải lại trang.
- **Steps**:
    1. Cấu hình `SseBootstrap` để gọi `queryClient.invalidateQueries` khi nhận event.
    2. Thêm action "View Detail" trực tiếp trên thông báo Toast.
    3. Xử lý logic tự động kết nối lại khi gặp sự cố mạng (Heartbeat).
- **Requirements**:
    - Đảm bảo không gây loop re-render.
    - Xử lý mượt mà trạng thái loading khi dữ liệu đang được làm mới ngầm.
- **⚠️ Note (Lỗi kết nối SSE)**: Để SSE gọi qua Gateway có xác thực, browser API `EventSource` mặc định không hỗ trợ truyền custom headers (Authorization). Phải sử dụng thư viện `event-source-polyfill` (đã được cài) để truyền JWT Token hoặc đảm bảo Cookie được đính kèm (`withCredentials: true`).

### 5. Task: Media Upload for Requests
- **Git Branch**: `feat/media-upload`
- **Description**: Cho phép người dân gửi hình ảnh hiện trường để đánh giá tình hình thực tế.
- **Steps**:
    1. Thêm Input chọn ảnh vào Form tạo yêu cầu.
    2. Xây dựng component Preview ảnh trước khi gửi.
    3. Tích hợp API Multipart upload để lưu trữ hình ảnh.
    4. Hiển thị danh sách ảnh trong trang chi tiết yêu cầu.
- **Requirements**:
    - Giới hạn kích thước file và số lượng ảnh tối đa.
    - Có hiệu ứng loading/progress khi đang upload.

### 6. Task: Search, Advanced Filters & Pagination
- **Git Branch**: `feat/search-filter-pagination`
- **Description**: Cải thiện khả năng tìm kiếm và lọc dữ liệu cho các bảng danh sách lớn.
- **Steps**:
    1. Implement Search bar (debounced search) cho Requests và Resources.
    2. Tạo bộ lọc theo Trạng thái (Status) và Mức độ khẩn cấp (Urgency).
    3. Đồng bộ hóa bộ lọc với URL Search Params.
    4. Cài đặt phân trang (Pagination) ở phía Server.
- **Requirements**:
    - Kết quả tìm kiếm phải mượt mà, hỗ trợ Empty State nếu không tìm thấy.
    - Bộ lọc phải hoạt động kết hợp được với nhau (Multiple filters).

---

## 📚 Phụ lục: Payload API tạo tài khoản Test
Hệ thống không có sẵn dữ liệu mẫu, hãy dùng Postman gọi API **POST** `http://localhost:8080/api/auth/register` với các JSON Body sau để tạo tài khoản test:

**1. Tạo tài khoản Quản lý (MANAGER)**
```json
{
  "fullName": "System Manager",
  "phone": "0999999999",
  "email": "manager@floodrescue.com",
  "password": "Password@123",
  "role": "MANAGER"
}
```

**2. Tạo tài khoản Điều phối viên (COORDINATOR)**
```json
{
  "fullName": "Main Coordinator",
  "phone": "0888888888",
  "email": "coordinator@floodrescue.com",
  "password": "Password@123",
  "role": "COORDINATOR"
}
```

---
*Ghi chú: Mỗi thành viên sau khi hoàn thành task cần chạy `npm run lint` và `npm run build` trước khi tạo Pull Request.*
