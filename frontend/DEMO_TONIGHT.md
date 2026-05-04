# Demo Runbook - Flood Rescue Frontend

## 1) Preflight (5-10 phut truoc demo)

- Chay backend gateway va cac service lien quan.
- Xac nhan API gateway: http://localhost:8080/api
- Frontend env:
  - NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
  - NEXT_PUBLIC_SSE_URL=http://localhost:8080/api/notifications/sse

## 2) Lenh chay nhanh

```bash
cd frontend
npm install
npm run dev
```

Truy cap: http://localhost:3000

## 3) Tai khoan demo de chuan bi

Can it nhat 4 account:
- CITIZEN
- COORDINATOR
- RESCUE_TEAM
- MANAGER

Neu chua co account CITIZEN/RESCUE_TEAM co the tao tai /register.
COORDINATOR va MANAGER nen duoc seed tu backend.

## 4) Script demo 5 buoc (5-7 phut)

### Buoc 1 - Citizen
- Dang nhap role CITIZEN tai /login
- Tao request moi tai dashboard citizen
- Mo chi tiet request vua tao

Expected:
- Request vao danh sach "Yeu cau cua toi"
- Status ban dau: PENDING

### Buoc 2 - Coordinator
- Dang nhap role COORDINATOR
- Vao dashboard coordinator
- Bam Verify cho request
- Chon request VERIFIED + team + vehicle
- Bam Assign ngay

Expected:
- Verify va assign thanh cong, khong loi 4xx/5xx

### Buoc 3 - Rescue Team
- Dang nhap role RESCUE_TEAM
- Vao dashboard rescue-team
- Chon assignment vua duoc gan
- Bam Start, sau do bam Complete

Expected:
- Trang thai assignment cap nhat thanh cong

### Buoc 4 - Citizen lai
- Dang nhap lai CITIZEN
- Vao detail request
- Neu request da COMPLETED, bam "Xac nhan da duoc cuu"

Expected:
- Request chuyen CONFIRMED

### Buoc 5 - Manager
- Dang nhap MANAGER
- Mo dashboard manager

Expected:
- KPI tong quan hien thi tu /reports/dashboard

## 5) Realtime (SSE)

Frontend da mount SSE bootstrap global.
Expected:
- Co toast "SSE connected" sau khi login
- Khi co event phu hop role se hien toast event type

## 6) Fallback neu demo gap su co

- Neu SSE khong vao duoc: tiep tuc demo bang polling/list refresh va neu ro backend auth header cho SSE.
- Neu assign fail do team/vehicle unavailable: doi sang team/vehicle khac trong danh sach AVAILABLE.
- Neu account role he thong chua seed: dung nhanh luong Citizen + Register RESCUE_TEAM de demo API auth va request flow.

## 7) Go / No-Go truoc khi ngu

Go:
- Login duoc 4 role
- Chay duoc script 5 buoc it nhat 1 lan lien mach
- npm run lint pass
- npm run build pass

No-Go:
- Login/refresh token bi out bat thuong
- Coordinator assign khong thanh cong du payload dung
- Rescue team khong start/complete duoc assignment
