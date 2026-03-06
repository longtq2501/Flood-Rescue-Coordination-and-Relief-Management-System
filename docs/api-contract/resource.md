# Resource Management API

Base: /api/resources

---

## GET /api/resources/warehouses
Lấy danh sách kho hàng.
Role required: MANAGER, ADMIN

### Response 200
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Kho Trung tâm Q1",
        "address": "123 Đường XYZ",
        "managerId": 15,
        "managerName": "Nguyen Manager",
        "totalItems": 12,
        "lowStockCount": 2
      }
    ],
    ...pagination
  }
}

---

## GET /api/resources/warehouses/{id}/items
Lấy danh sách hàng trong kho.
Role required: MANAGER, ADMIN

### Query Parameters
| Param | Type | Mô tả |
|---|---|---|
| category | string | Filter theo loại hàng |
| lowStock | boolean | Chỉ hiện hàng sắp hết |
| page | int | Trang |
| size | int | Số bản ghi |

### Response 200
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 7,
        "name": "Áo phao",
        "category": "Thiết bị an toàn",
        "unit": "cái",
        "quantity": 5,
        "lowThreshold": 10,
        "isLowStock": true,
        "warehouseId": 1
      }
    ],
    ...pagination
  }
}

---

## POST /api/resources/warehouses/{id}/items
Thêm hàng hóa vào kho.
Role required: MANAGER, ADMIN

### Request Body
{
  "name": "Mì gói",
  "category": "Thực phẩm",
  "unit": "thùng",
  "quantity": 100,
  "lowThreshold": 20
}

### Response 201
{
  "success": true,
  "data": { ...ReliefItemObject }
}

---

## PUT /api/resources/items/{id}
Cập nhật thông tin hàng hóa.
Role required: MANAGER, ADMIN

### Request Body
{
  "name": "Mì gói Hảo Hảo",    // optional
  "quantity": 150,               // optional — cộng thêm vào (nhập kho)
  "lowThreshold": 25             // optional
}

### Response 200
{
  "success": true,
  "data": { ...ReliefItemObject }
}

---

## GET /api/resources/vehicles
Lấy danh sách phương tiện.
Role required: COORDINATOR, MANAGER, ADMIN

### Query Parameters
| Param | Type | Mô tả |
|---|---|---|
| status | string | AVAILABLE|IN_USE|MAINTENANCE|OFFLINE |
| type | string | BOAT|TRUCK|HELICOPTER|AMBULANCE|OTHER |
| page | int | Trang |
| size | int | Số bản ghi |

### Response 200
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 3,
        "plateNumber": "51A-12345",
        "type": "BOAT",
        "capacity": 8,
        "status": "AVAILABLE",
        "currentLat": 10.755,
        "currentLng": 106.650,
        "assignedTeamId": null
      }
    ],
    ...pagination
  }
}

---

## POST /api/resources/vehicles
Thêm phương tiện mới.
Role required: MANAGER, ADMIN

### Request Body
{
  "plateNumber": "51B-67890",
  "type": "BOAT",
  "capacity": 6
}

### Response 201
{
  "success": true,
  "data": { ...VehicleObject }
}

---

## PATCH /api/resources/vehicles/{id}/status
Cập nhật trạng thái phương tiện.
Role required: MANAGER, ADMIN

### Request Body
{
  "status": "MAINTENANCE",
  "note": "Bảo dưỡng định kỳ"
}

### Response 200
{
  "success": true,
  "data": { ...VehicleObject }
}

---

## POST /api/resources/distributions
Ghi nhận phân phối hàng cứu trợ.
Role required: COORDINATOR, MANAGER

### Request Body
{
  "requestId": 1,           // required
  "recipientId": 42,        // required — userId citizen
  "items": [
    { "reliefItemId": 7, "quantity": 2 },
    { "reliefItemId": 9, "quantity": 1 }
  ],
  "note": "Phát cho gia đình 4 người"   // optional
}

### Response 201
{
  "success": true,
  "data": {
    "id": 30,
    "requestId": 1,
    "recipientId": 42,
    "recipientName": "Nguyen Van A",
    "items": [
      { "itemName": "Áo phao", "quantity": 2, "unit": "cái" },
      { "itemName": "Mì gói",  "quantity": 1, "unit": "thùng" }
    ],
    "distributedAt": "2025-01-01T12:00:00Z"
  }
}

### Error Cases
- 409 INSUFFICIENT_STOCK: số lượng yêu cầu vượt tồn kho

---

## GET /api/resources/distributions
Lịch sử phân phối cứu trợ.
Role required: MANAGER, ADMIN

### Query Parameters
| Param | Type | Mô tả |
|---|---|---|
| requestId | long | Filter theo yêu cầu |
| fromDate | datetime | Từ ngày |
| toDate | datetime | Đến ngày |
| page | int | Trang |
| size | int | Số bản ghi |

### Response 200
{
  "success": true,
  "data": { "content": [ ...DistributionObject ], ...pagination }
}