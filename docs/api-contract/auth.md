# Auth API

Base: /api/auth

---

## POST /api/auth/register
Đăng ký tài khoản mới.
Role required: Public

### Request Body
{
  "fullName": "Nguyen Van A",       // required, max 100
  "phone": "0901234567",            // required, unique, 10-11 số
  "email": "user@example.com",      // optional, unique
  "password": "Password@123",       // required, min 8 ký tự
  "role": "CITIZEN"                 // required: CITIZEN | RESCUE_TEAM
                                    // COORDINATOR, MANAGER, ADMIN chỉ ADMIN mới tạo được
}

### Response 201
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "id": 1,
    "fullName": "Nguyen Van A",
    "phone": "0901234567",
    "email": "user@example.com",
    "role": "CITIZEN",
    "status": "ACTIVE",
    "createdAt": "2025-01-01T10:00:00Z"
  }
}

### Error Cases
- 400 VALIDATION_ERROR: thiếu field bắt buộc, sai format phone/email/password
- 409 DUPLICATE_PHONE: số điện thoại đã tồn tại
- 409 DUPLICATE_EMAIL: email đã tồn tại

---

## POST /api/auth/login
Đăng nhập, nhận JWT token.
Role required: Public

### Request Body
{
  "phone": "0901234567",     // required
  "password": "Password@123" // required
}

### Response 200
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "fullName": "Nguyen Van A",
      "phone": "0901234567",
      "role": "CITIZEN",
      "avatarUrl": null
    }
  }
}

### Error Cases
- 400 VALIDATION_ERROR: thiếu phone hoặc password
- 401 UNAUTHORIZED: sai phone hoặc password
- 403 FORBIDDEN: tài khoản bị BANNED

---

## POST /api/auth/refresh
Làm mới access token.
Role required: Public

### Request Body
{
  "refreshToken": "eyJhbGci..."
}

### Response 200
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "expiresIn": 86400
  }
}

### Error Cases
- 401 UNAUTHORIZED: refresh token hết hạn hoặc đã bị revoke

---

## POST /api/auth/logout
Đăng xuất, revoke refresh token.
Role required: All roles (authenticated)

### Request Body
{
  "refreshToken": "eyJhbGci..."
}

### Response 200
{
  "success": true,
  "message": "Đăng xuất thành công",
  "data": null
}

---

## GET /api/auth/me
Lấy thông tin user hiện tại.
Role required: All roles (authenticated)

### Response 200
{
  "success": true,
  "data": {
    "id": 1,
    "fullName": "Nguyen Van A",
    "phone": "0901234567",
    "email": "user@example.com",
    "role": "CITIZEN",
    "status": "ACTIVE",
    "lat": 10.762622,
    "lng": 106.660172,
    "avatarUrl": null,
    "createdAt": "2025-01-01T10:00:00Z"
  }
}

---

## PUT /api/auth/me
Cập nhật thông tin cá nhân.
Role required: All roles (authenticated)

### Request Body
{
  "fullName": "Nguyen Van B",    // optional
  "email": "new@example.com",   // optional
  "lat": 10.762622,              // optional
  "lng": 106.660172              // optional
}

### Response 200
{
  "success": true,
  "message": "Cập nhật thành công",
  "data": { ...UserObject }
}

---

## PUT /api/auth/change-password
Đổi mật khẩu.
Role required: All roles (authenticated)

### Request Body
{
  "currentPassword": "Password@123",
  "newPassword": "NewPass@456",
  "confirmPassword": "NewPass@456"
}

### Response 200
{
  "success": true,
  "message": "Đổi mật khẩu thành công",
  "data": null
}

### Error Cases
- 400 VALIDATION_ERROR: newPassword và co