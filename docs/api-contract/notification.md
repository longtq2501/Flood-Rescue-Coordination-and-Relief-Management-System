# Notification API

Base: /api/notifications

---

## GET /api/notifications/sse
Kết nối SSE stream để nhận real-time notification.
Role required: All roles (authenticated)

### Cách sử dụng (Frontend)
const eventSource = new EventSource(
  'http://localhost:8080/api/notifications/sse',
  { headers: { Authorization: 'Bearer <token>' } }
);

eventSource.addEventListener('request.status.updated', (e) => {
  const data = JSON.parse(e.data);
  console.log(data);
});

eventSource.addEventListener('heartbeat', () => {
  // server gửi mỗi 30 giây để giữ kết nối
});

### Event Format
event: request.status.updated
id: 1704067200000
data: {
  "eventId": "uuid",
  "eventType": "request.status.updated",
  "timestamp": "2025-01-01T10:05:00Z",
  "payload": {
    "requestId": 1,
    "oldStatus": "PENDING",
    "newStatus": "VERIFIED",
    "message": "Yêu cầu của bạn đã được xác minh"
  }
}

### Events theo Role
| Event Type | Role nhận |
|---|---|
| request.status.updated | CITIZEN |
| request.assigned | CITIZEN |
| request.completed | CITIZEN |
| new.request.alert | COORDINATOR |
| resource.low.alert | MANAGER |
| system.broadcast | Tất cả |

---

## GET /api/notifications
Lấy lịch sử notification của user hiện tại.
Role required: All roles (authenticated)

### Query Parameters
| Param | Type | Default | Mô tả |
|---|---|---|---|
| page | int | 0 | Trang |
| size | int | 20 | Số bản ghi |
| status | string | null | PENDING|SENT|FAILED |

### Response 200
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "eventType": "request.assigned",
        "payload": { ... },
        "status": "SENT",
        "sentAt": "2025-01-01T10:10:00Z",
        "createdAt": "2025-01-01T10:10:00Z"
      }
    ],
    ...pagination
  }
}

---

## WebSocket — STOMP

### Connect
URL: ws://localhost:8080/ws
STOMP Connect Header:
  Authorization: Bearer <token>

### Subscribe Topics

/topic/map.tracking
→ Nhận GPS update của tất cả team đang hoạt động
→ Role: COORDINATOR
→ Payload:
{
  "teamId": 5,
  "teamName": "Team Alpha",
  "lat": 10.771234,
  "lng": 106.658900,
  "speed": 40,
  "timestamp": "2025-01-01T10:15:00Z"
}

/topic/team.{teamId}.status
→ Nhận cập nhật trạng thái team
→ Role: COORDINATOR, RESCUE_TEAM
→ Payload:
{
  "teamId": 5,
  "oldStatus": "AVAILABLE",
  "newStatus": "BUSY",
  "assignmentId": 20
}

/topic/dispatch.board
→ Cập nhật dashboard điều phối
→ Role: COORDINATOR
→ Payload:
{
  "type": "NEW_REQUEST",    // NEW_REQUEST | ASSIGNMENT_UPDATED | TEAM_STATUS_CHANGED
  "data": { ... }
}

/queue/task
→ Team nhận nhiệm vụ mới (private, mỗi team 1 queue riêng)
→ Role: RESCUE_TEAM
→ Payload:
{
  "assignmentId": 20,
  "requestId": 1,
  "lat": 10.762622,
  "lng": 106.660172,
  "addressText": "123 Đường ABC",
  "description": "...",
  "urgencyLevel": "CRITICAL",
  "numPeople": 4
}

### Send (Client → Server)

/app/location.update
→ Team gửi GPS lên server
→ Role: RESCUE_TEAM
→ Payload:
{
  "lat": 10.771234,
  "lng": 106.658900,
  "speed": 40.5,
  "heading": 270.0
}