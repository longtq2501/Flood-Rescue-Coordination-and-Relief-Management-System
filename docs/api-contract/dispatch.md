# Dispatch API

Base: /api/dispatch

---

## GET /api/dispatch/teams
Lấy danh sách đội cứu hộ.
Role required: COORDINATOR, MANAGER, ADMIN

### Query Parameters
| Param | Type | Mô tả |
|---|---|---|
| status | string | Filter: AVAILABLE|BUSY|RETURNING|OFFLINE |
| page | int | Trang |
| size | int | Số bản ghi |

### Response 200
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 5,
        "name": "Team Alpha",
        "leaderId": 10,
        "leaderName": "Tran Van B",
        "capacity": 4,
        "memberCount": 4,
        "status": "AVAILABLE",
        "currentLat": 10.771234,
        "currentLng": 106.658900,
        "currentAssignmentId": null
      }
    ],
    ...pagination
  }
}

---

## GET /api/dispatch/teams/{id}
Xem chi tiết đội cứu hộ.
Role required: COORDINATOR, MANAGER, ADMIN

### Response 200
{
  "success": true,
  "data": {
    "id": 5,
    "name": "Team Alpha",
    "leaderId": 10,
    "leaderName": "Tran Van B",
    "capacity": 4,
    "status": "AVAILABLE",
    "currentLat": 10.771234,
    "currentLng": 106.658900,
    "members": [
      { "userId": 10, "name": "Tran Van B", "phone": "0901111111", "isLeader": true },
      { "userId": 11, "name": "Le Van C",   "phone": "0902222222", "isLeader": false }
    ]
  }
}

---

## POST /api/dispatch/assign
Coordinator assign đội cứu hộ cho yêu cầu.
Role required: COORDINATOR

### Request Body
{
  "requestId": 1,      // required
  "teamId": 5,         // required
  "vehicleId": 3,      // required
  "note": "Ưu tiên cao, cần thuyền lớn"   // optional
}

### Response 201
{
  "success": true,
  "message": "Phân công thành công",
  "data": {
    "id": 20,
    "requestId": 1,
    "teamId": 5,
    "teamName": "Team Alpha",
    "vehicleId": 3,
    "vehiclePlate": "51A-12345",
    "coordinatorId": 8,
    "status": "ACTIVE",
    "assignedAt": "2025-01-01T10:10:00Z",
    "estimatedArrival": "2025-01-01T10:25:00Z"
  }
}

### Error Cases
- 404 NOT_FOUND: requestId hoặc teamId hoặc vehicleId không tồn tại
- 409 TEAM_UNAVAILABLE: team đang BUSY
- 409 VEHICLE_UNAVAILABLE: vehicle đang IN_USE
- 400 VALIDATION_ERROR: request không ở trạng thái VERIFIED

---

## GET /api/dispatch/assignments
Lấy danh sách assignment.
Role required: COORDINATOR, MANAGER, ADMIN

### Query Parameters
| Param | Type | Mô tả |
|---|---|---|
| status | string | ACTIVE|COMPLETED|CANCELLED |
| teamId | long | Filter theo team |
| page | int | Trang |
| size | int | Số bản ghi |

### Response 200
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 20,
        "requestId": 1,
        "urgencyLevel": "CRITICAL",
        "teamId": 5,
        "teamName": "Team Alpha",
        "vehicleId": 3,
        "vehiclePlate": "51A-12345",
        "status": "ACTIVE",
        "assignedAt": "2025-01-01T10:10:00Z"
      }
    ],
    ...pagination
  }
}

---

## GET /api/dispatch/assignments/my
Rescue Team xem nhiệm vụ của team mình.
Role required: RESCUE_TEAM

### Response 200
Tương tự trên nhưng chỉ trả về assignment của team đang đăng nhập.

---

## PATCH /api/dispatch/assignments/{id}/start
Rescue Team bắt đầu thực hiện nhiệm vụ.
Role required: RESCUE_TEAM

### Response 200
{
  "success": true,
  "data": { ...AssignmentObject, "status": "ACTIVE", "startedAt": "..." }
}

---

## PATCH /api/dispatch/assignments/{id}/complete
Rescue Team báo cáo hoàn thành nhiệm vụ.
Role required: RESCUE_TEAM

### Request Body
{
  "resultNote": "Đã cứu 3 người, đưa về điểm tập kết"   // required
}

### Response 200
{
  "success": true,
  "data": { ...AssignmentObject, "status": "COMPLETED", "completedAt": "..." }
}

---

## POST /api/dispatch/location
Rescue Team gửi cập nhật vị trí GPS.
Role required: RESCUE_TEAM

### Request Body
{
  "lat": 10.771234,
  "lng": 106.658900,
  "speed": 40.5,      // optional, km/h
  "heading": 270.0    // optional, độ
}

### Response 200
{
  "success": true,
  "message": "Cập nhật vị trí thành công",
  "data": null
}

---

## GET /api/dispatch/map
Lấy toàn bộ dữ liệu cho bản đồ real-time.
Role required: COORDINATOR

### Response 200
{
  "success": true,
  "data": {
    "teams": [
      {
        "teamId": 5,
        "teamName": "Team Alpha",
        "status": "BUSY",
        "lat": 10.771234,
        "lng": 106.658900,
        "currentRequestId": 1
      }
    ],
    "pendingRequests": [
      {
        "requestId": 2,
        "lat": 10.762622,
        "lng": 106.660172,
        "urgencyLevel": "HIGH",
        "numPeople": 2,
        "waitingMinutes": 15
      }
    ]
  }
}