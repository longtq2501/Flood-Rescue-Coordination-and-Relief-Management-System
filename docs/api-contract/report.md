# Report API

Base: /api/reports
Role required: MANAGER, ADMIN cho tất cả endpoints

---

## GET /api/reports/dashboard
Lấy tổng quan dashboard.

### Query Parameters
| Param | Type | Default | Mô tả |
|---|---|---|---|
| fromDate | date | 7 ngày trước | Từ ngày (yyyy-MM-dd) |
| toDate | date | Hôm nay | Đến ngày |

### Response 200
{
  "success": true,
  "data": {
    "summary": {
      "totalRequests": 150,
      "completedRequests": 120,
      "pendingRequests": 10,
      "inProgressRequests": 20,
      "completionRate": 80.0,
      "avgResponseMinutes": 8.5,
      "avgCompleteMinutes": 45.2
    },
    "byUrgency": {
      "CRITICAL": 20,
      "HIGH": 50,
      "MEDIUM": 60,
      "LOW": 20
    },
    "resourceUsage": {
      "vehiclesDeployed": 12,
      "totalDistributions": 85,
      "activeTeams": 6,
      "totalTeams": 8
    },
    "topTeams": [
      {
        "teamId": 5,
        "teamName": "Team Alpha",
        "missionsCompleted": 18,
        "avgDurationMinutes": 38.5
      }
    ]
  }
}

---

## GET /api/reports/requests/daily
Thống kê yêu cầu theo ngày.

### Query Parameters
| Param | Type | Mô tả |
|---|---|---|
| fromDate | date | Từ ngày (yyyy-MM-dd) |
| toDate | date | Đến ngày |

### Response 200
{
  "success": true,
  "data": [
    {
      "date": "2025-01-01",
      "totalRequests": 25,
      "criticalCount": 3,
      "highCount": 10,
      "mediumCount": 9,
      "lowCount": 3,
      "completedCount": 22,
      "cancelledCount": 1,
      "avgResponseMinutes": 7.2,
      "avgCompleteMinutes": 42.0
    }
  ]
}

---

## GET /api/reports/teams/performance
Thống kê hiệu suất đội cứu hộ.

### Query Parameters
| Param | Type | Mô tả |
|---|---|---|
| fromDate | date | Từ ngày |
| toDate | date | Đến ngày |
| teamId | long | Filter theo team (optional) |

### Response 200
{
  "success": true,
  "data": [
    {
      "teamId": 5,
      "teamName": "Team Alpha",
      "missionsCompleted": 45,
      "avgDurationMinutes": 38.5,
      "period": {
        "from": "2025-01-01",
        "to": "2025-01-07"
      }
    }
  ]
}

---

## GET /api/reports/resources/summary
Thống kê sử dụng tài nguyên.

### Query Parameters
| Param | Type | Mô tả |
|---|---|---|
| fromDate | date | Từ ngày |
| toDate | date | Đến ngày |

### Response 200
{
  "success": true,
  "data": {
    "distributions": [
      {
        "date": "2025-01-01",
        "warehouseId": 1,
        "warehouseName": "Kho Trung tâm Q1",
        "totalDistributions": 12,
        "vehiclesDeployed": 4
      }
    ],
    "lowStockItems": [
      {
        "itemId": 7,
        "itemName": "Áo phao",
        "currentQuantity": 5,
        "threshold": 10,
        "warehouseName": "Kho Trung tâm Q1"
      }
    ]
  }
}

---

## GET /api/reports/export
Xuất báo cáo ra file Excel/PDF.
Role required: ADMIN

### Query Parameters
| Param | Type | Mô tả |
|---|---|---|
| type | string | REQUESTS|TEAMS|RESOURCES |
| fromDate | date | Từ ngày |
| toDate | date | Đến ngày |
| format | string | EXCEL|PDF |

### Response 200
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="report_2025-01-01.xlsx"
<file binary>