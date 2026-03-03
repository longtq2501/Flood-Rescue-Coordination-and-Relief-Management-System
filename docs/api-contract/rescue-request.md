# Rescue Request API

Base: /api/requests

---

## POST /api/requests
Citizen gửi yêu cầu cứu hộ.
Role required: CITIZEN

### Request Body (multipart/form-data)
{
  "lat": 10.762622,                  // required nếu không có addressText
  "lng": 106.660172,                 // required nếu không có addressText
  "addressText": "123 Đường ABC",    // optional, nhập thủ công nếu GPS lệch
  "description": "Bị kẹt trên mái, 3 người lớn 1 trẻ em",  // required
  "numPeople": 4,                    // required, min 1
  "urgencyLevel": "CRITICAL",        // required: CRITICAL|HIGH|MEDIUM|LOW
  "images": [file1, file2]           // optional, max 5 ảnh, mỗi ảnh max 5MB
}

### Response 201
{
  "success": true,
  "message": "Gửi yêu cầu thành công",
  "data": {
    "id": 1,
    "citizenId": 42,
    "lat": 10.762622,
    "lng": 106.660172,
    "addressText": "123 Đường ABC",
    "description": "Bị kẹt trên mái, 3 người lớn 1 trẻ em",
    "numPeople": 4,
    "urgencyLevel": "CRITICAL",
    "status": "PENDING",
    "imageUrls": ["https://minio.../image1.jpg"],
    "createdAt": "2025-01-01T10:00:00Z"
  }
}

---

## GET /api/requests
Lấy danh sách yêu cầu cứu hộ.
Role required: COORDINATOR, MANAGER, ADMIN

### Query Parameters
| Param | Type | Default | Mô tả |
|---|---|---|---|
| page | int | 0 | Trang hiện tại |
| size | int | 10 | Số bản ghi mỗi trang |
| status | string | null | Filter theo status |
| urgencyLevel | string | null | Filter theo urgency |
| fromDate | datetime | null | Từ ngày (ISO 8601) |
| toDate | datetime | null | Đến ngày (ISO 8601) |

### Response 200
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "citizenId": 42,
        "citizenName": "Nguyen Van A",
        "citizenPhone": "0901234567",
        "lat": 10.762622,
        "lng": 106.660172,
        "addressText": "123 Đường ABC",
        "description": "...",
        "numPeople": 4,
        "urgencyLevel": "CRITICAL",
        "status": "PENDING",
        "createdAt": "2025-01-01T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 50,
    "totalPages": 5
  }
}

---

## GET /api/requests/my
Citizen xem danh sách yêu cầu của bản thân.
Role required: CITIZEN

### Query Parameters
| Param | Type | Default |
|---|---|---|
| page | int | 0 |
| size | int | 10 |
| status | string | null |

### Response 200
Tương tự GET /api/requests nhưng chỉ trả về yêu cầu của citizen đang đăng nhập.

---

## GET /api/requests/{id}
Xem chi tiết yêu cầu.
Role required: CITIZEN (chỉ xem của mình), COORDINATOR, MANAGER, ADMIN

### Response 200
{
  "success": true,
  "data": {
    "id": 1,
    "citizenId": 42,
    "citizenName": "Nguyen Van A",
    "citizenPhone": "0901234567",
    "lat": 10.762622,
    "lng": 106.660172,
    "addressText": "123 Đường ABC",
    "description": "...",
    "numPeople": 4,
    "urgencyLevel": "CRITICAL",
    "status": "ASSIGNED",
    "imageUrls": ["https://minio.../image1.jpg"],
    "assignment": {
      "teamId": 5,
      "teamName": "Team Alpha",
      "vehicleId": 3,
      "estimatedArrival": "2025-01-01T10:20:00Z"
    },
    "statusHistory": [
      {
        "fromStatus": null,
        "toStatus": "PENDING",
        "changedBy": "Nguyen Van A",
        "changedAt": "2025-01-01T10:00:00Z"
      },
      {
        "fromStatus": "PENDING",
        "toStatus": "VERIFIED",
        "changedBy": "Coordinator B",
        "changedAt": "2025-01-01T10:05:00Z"
      }
    ],
    "createdAt": "2025-01-01T10:00:00Z",
    "updatedAt": "2025-01-01T10:05:00Z"
  }
}

---

## PATCH /api/requests/{id}/verify
Coordinator xác minh yêu cầu.
Role required: COORDINATOR

### Request Body
{
  "note": "Đã xác minh qua điện thoại"   // optional
}

### Response 200
{
  "success": true,
  "message": "Xác minh thành công",
  "data": { ...RescueRequestObject, "status": "VERIFIED" }
}

### Error Cases
- 404 NOT_FOUND: yêu cầu không tồn tại
- 400 VALIDATION_ERROR: yêu cầu không ở trạng thái PENDING

---

## PATCH /api/requests/{id}/cancel
Hủy yêu cầu.
Role required: CITIZEN (hủy của mình khi còn PENDING), COORDINATOR

### Request Body
{
  "reason": "Đã tự thoát được"   // required
}

### Response 200
{
  "success": true,
  "message": "Hủy yêu cầu thành công",
  "data": { ...RescueRequestObject, "status": "CANCELLED" }
}

---

## PATCH /api/requests/{id}/confirm
Citizen xác nhận đã được cứu hộ.
Role required: CITIZEN

### Request Body
{
  "note": "Đã được cứu hộ an toàn"   // optional
}

### Response 200
{
  "success": true,
  "message": "Xác nhận thành công",
  "data": { ...RescueRequestObject, "status": "CONFIRMED" }
}

### Error Cases
- 400 VALIDATION_ERROR: yêu cầu không ở trạng thái COMPLETED