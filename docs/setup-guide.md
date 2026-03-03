# Setup Guide — Flood Rescue System
Hướng dẫn setup môi trường local cho toàn bộ thành viên.

---

## Yêu cầu cài đặt

| Tool | Version | Download |
|---|---|---|
| Java JDK | 17+ | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org |
| Node.js | 18+ | https://nodejs.org |
| Docker Desktop | Latest | https://docker.com |
| Git | Latest | https://git-scm.com |
| IntelliJ IDEA | Latest | https://jetbrains.com/idea |
| VS Code | Latest | https://code.visualstudio.com |

---

## Bước 1 — Clone repo
```bash
git clone https://github.com/longtq2501/Flood-Rescue-Coordination-and-Relief-Management-System.git
cd Flood-Rescue-Coordination-and-Relief-Management-System
```

---

## Bước 2 — Chạy infrastructure
```bash
cd infrastructure
docker compose -f docker-compose.infra.yml up -d
```

Kiểm tra tất cả container đang chạy:
```bash
docker ps
```

Phải thấy 4 container đều **healthy/running:**
```
rescue-mysql     → localhost:3306
rescue-redis     → localhost:6379
rescue-rabbitmq  → localhost:5672  (UI: localhost:15672)
rescue-minio     → localhost:9000  (Console: localhost:9001)
```

Kiểm tra 6 database đã tạo:
```bash
docker exec -it rescue-mysql mysql -u rescueuser -prescuepassword -e "SHOW DATABASES;"
```

Phải thấy: db_auth, db_dispatch, db_notification, db_report, db_request, db_resource

---

## Bước 3 — Chạy Backend
```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Hoặc mở IntelliJ → Run `FloodRescueApplication.java` với profile `dev`.

Kiểm tra backend đang chạy:
```
http://localhost:8080/actuator/health
→ {"status":"UP"}
```

Flyway sẽ tự động tạo bảng khi backend khởi động lần đầu.

---

## Bước 4 — Chạy Frontend
```bash
cd frontend
cp .env.local.example .env.local
npm install
npm run dev
```

Truy cập: http://localhost:3000

---

## Thông tin kết nối

### MySQL
```
Host:     localhost:3306
Username: rescueuser
Password: rescuepassword
```

### Redis
```
Host:     localhost:6379
Password: redispassword
```

### RabbitMQ
```
AMQP:     localhost:5672
UI:       http://localhost:15672
Username: rescueuser
Password: rescuepassword
```

### MinIO
```
API:      http://localhost:9000
Console:  http://localhost:9001
Username: minioadmin
Password: minioadmin123
Bucket:   rescue-images
```

---

## Cấu hình IDE

### IntelliJ IDEA
1. File → Open → chọn folder `backend`
2. Chờ Maven download dependencies
3. Edit Configurations → Add → Spring Boot
   - Main class: `com.floodrescue.FloodRescueApplication`
   - Active profiles: `dev`
4. Bấm Run

### VS Code (Frontend)
1. File → Open Folder → chọn folder `frontend`
2. Cài extensions: ESLint, Prettier, Tailwind CSS IntelliSense
3. Terminal → `npm run dev`

---

## Branch convention
```bash
# Tạo branch mới để làm feature
git checkout develop
git pull origin develop
git checkout -b feature/<module>-<task>

# Ví dụ
git checkout -b feature/request-create-api
git checkout -b feature/dispatch-assign-team
git checkout -b feature/notification-sse
```

Quy tắc đặt tên branch:
```
feature/<module>-<mô tả>   → tính năng mới
fix/<module>-<mô tả>       → bug fix
chore/<mô tả>              → config, setup
```

---

## Quy trình làm việc
```
1. Lấy task từ Jira Sprint 1
2. Tạo branch từ develop
3. Code + test local
4. Commit với message rõ ràng
5. Push branch lên GitHub
6. Tạo Pull Request vào develop
7. Tag Long để review
8. Sau khi được approve → Merge
```

### Commit message convention
```
feat(<module>): mô tả ngắn
fix(<module>): mô tả ngắn
chore: mô tả ngắn

Ví dụ:
feat(auth): add login and register API
feat(request): add urgency classification logic
fix(dispatch): fix redis lock timeout issue
chore: update docker-compose redis config
```

---

## Troubleshooting

### Docker container không start
```bash
docker compose -f docker-compose.infra.yml down
docker compose -f docker-compose.infra.yml up -d --force-recreate
```

### Maven build lỗi
```bash
mvn clean
mvn dependency:resolve
mvn install -DskipTests
```

### Port bị chiếm
```bash
# Windows — tìm process đang dùng port 3306
netstat -ano | findstr :3306
# Kill process theo PID
taskkill /PID <pid> /F
```

### Flyway migration lỗi
Không được sửa file V đã commit. Nếu cần thay đổi schema thì báo Long tạo file migration mới.
```
V1__create_auth_schema.sql      ← KHÔNG sửa
V1_1__alter_users_add_column.sql ← tạo file mới
```

### Không kết nối được RabbitMQ
```bash
docker exec -it rescue-rabbitmq rabbitmqctl list_users
# Nếu không thấy rescueuser thì chạy:
docker exec -it rescue-rabbitmq rabbitmqctl add_user rescueuser rescuepassword
docker exec -it rescue-rabbitmq rabbitmqctl set_user_tags rescueuser administrator
docker exec -it rescue-rabbitmq rabbitmqctl set_permissions -p / rescueuser ".*" ".*" ".*"
```

---

## Liên hệ

Mọi thắc mắc về setup hoặc infrastructure liên hệ **Tôn Quỳnh Long** qua Teams/Zalo nhóm.