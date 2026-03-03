# API Contract — Flood Rescue System

## Base URL
http://localhost:8080/api

## Authentication
Tất cả endpoint (trừ /auth/**) đều yêu cầu JWT token trong header:
Authorization: Bearer <access_token>

## Standard Response Format

### Success
{
  "success": true,
  "message": "OK",
  "data": { ... },
  "timestamp": "2025-01-01T10:00:00Z"
}

### Success với pagination
{
  "success": true,
  "message": "OK",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "last": false
  },
  "timestamp": "2025-01-01T10:00:00Z"
}

### Error
{
  "success": false,
  "code": "ERROR_CODE",
  "message": "Mô tả lỗi chi tiết",
  "timestamp": "2025-01-01T10:00:00Z"
}

## Error Codes
| Code | HTTP Status | Mô tả |
|---|---|---|
| UNAUTHORIZED | 401 | Token không hợp lệ hoặc hết hạn |
| FORBIDDEN | 403 | Không có quyền truy cập |
| NOT_FOUND | 404 | Resource không tồn tại |
| VALIDATION_ERROR | 400 | Dữ liệu đầu vào không hợp lệ |
| DUPLICATE_PHONE | 409 | Số điện thoại đã tồn tại |
| DUPLICATE_EMAIL | 409 | Email đã tồn tại |
| TEAM_UNAVAILABLE | 409 | Đội cứu hộ không khả dụng |
| VEHICLE_UNAVAILABLE | 409 | Phương tiện không khả dụng |
| INSUFFICIENT_STOCK | 409 | Tồn kho không đủ |
| INTERNAL_ERROR | 500 | Lỗi hệ thống |

## Role Permissions
| Role | Quyền truy cập |
|---|---|
| CITIZEN | Gửi/xem yêu cầu của bản thân, xác nhận cứu hộ |
| RESCUE_TEAM | Xem nhiệm vụ được assign, cập nhật trạng thái, gửi GPS |
| COORDINATOR | Quản lý yêu cầu, assign đội, xem map |
| MANAGER | Quản lý kho, phương tiện, phân phối, xem báo cáo |
| ADMIN | Toàn quyền |