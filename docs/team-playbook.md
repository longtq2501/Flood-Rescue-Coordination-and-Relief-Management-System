# Team Playbook — Flood Rescue System
> Đọc kỹ trước khi bắt đầu code. Đây là quy trình bắt buộc cho toàn nhóm.

---

# MỤC LỤC
1. Quy tắc chung
2. Cách tạo Entity
3. Cách viết API endpoint chuẩn
4. Cách publish RabbitMQ event
5. Cách viết RabbitMQ Listener
6. Cách viết unit test
7. Cách tạo PR và commit
8. Các lỗi thường gặp

---

# 1. QUY TẮC CHUNG

## Không được vi phạm
```
❌ KHÔNG import class từ module khác trực tiếp
   Sai:  import com.floodrescue.module.auth.repository.UserRepository; (trong Request module)
   Đúng: Dùng RabbitMQ event hoặc báo Long nếu cần data cross-module

❌ KHÔNG push thẳng vào develop hoặc main
   Luôn tạo branch feature → tạo PR → đợi Long approve

❌ KHÔNG sửa file Flyway đã commit
   Muốn thay đổi schema → tạo file V mới → báo Long

❌ KHÔNG hardcode password, secret key trong code
   Luôn dùng ${app.jwt.secret} từ application.yml
```

## Bắt buộc làm
```
✅ Mỗi API phải dùng ApiResponse wrapper
✅ Mỗi lỗi phải throw AppException với ErrorCode
✅ Mỗi method service phải có @Transactional nếu write DB
✅ Commit message phải đúng convention
✅ Tạo PR xong phải tag Long trên Teams để review
```

---

# 2. CÁCH TẠO ENTITY

## Template chuẩn — copy và sửa lại
```java
@Entity
@Table(name = "ten_bang")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Các field của bạn ở đây
    @Column(name = "ten_field", nullable = false, length = 100)
    private String tenField;

    // Enum thì dùng STRING, không dùng ORDINAL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenEnum status;

    // Timestamp — luôn có created_at
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

## Quy tắc đặt tên
```
Class:  PascalCase     → RescueRequest, RefreshToken
Table:  snake_case     → rescue_requests, refresh_tokens
Field:  camelCase      → urgencyLevel, createdAt
Column: snake_case     → urgency_level, created_at
```

---

# 3. CÁCH VIẾT API ENDPOINT CHUẨN

## Luôn trả về ApiResponse — không trả về object thẳng
```java
// ❌ SAI
@GetMapping("/{id}")
public RescueRequest getById(@PathVariable Long id) {
    return service.findById(id);
}

// ✅ ĐÚNG
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<RescueRequestResponse>> getById(
        @PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success("OK", service.findById(id))
    );
}
```

## Luôn throw AppException — không throw Exception thẳng
```java
// ❌ SAI
if (request == null) {
    throw new RuntimeException("Not found");
}

// ✅ ĐÚNG
if (request == null) {
    throw new AppException(ErrorCode.NOT_FOUND);
}

// Có custom message
throw new AppException(ErrorCode.NOT_FOUND, "Yêu cầu cứu hộ không tồn tại");
```

## Template Controller chuẩn
```java
@RestController
@RequestMapping("/api/your-module")
@RequiredArgsConstructor
public class YourController {

    private final YourService service;

    // GET list — có pagination
    @GetMapping
    public ResponseEntity<ApiResponse<Page<YourResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
            ApiResponse.success("OK", service.findAll(page, size))
        );
    }

    // GET detail
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<YourResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("OK", service.findById(id))
        );
    }

    // POST create
    @PostMapping
    public ResponseEntity<ApiResponse<YourResponse>> create(
            @Valid @RequestBody CreateYourRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo thành công",
                        service.create(request, principal.getId())));
    }

    // PATCH update
    @PatchMapping("/{id}/action")
    public ResponseEntity<ApiResponse<YourResponse>> doAction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
            ApiResponse.success("Thành công", service.doAction(id, principal.getId()))
        );
    }
}
```

## Lấy userId từ JWT trong Controller
```java
// Inject UserPrincipal vào method parameter
@GetMapping("/my")
public ResponseEntity<ApiResponse<?>> getMy(
        @AuthenticationPrincipal UserPrincipal principal) {

    Long userId = principal.getId();       // lấy userId
    String role = principal.getRole();     // lấy role
    return ResponseEntity.ok(...);
}
```

---

# 4. CÁCH PUBLISH RABBITMQ EVENT

## Bước 1 — Tạo Event class trong module của bạn
```
module/your_module/
└── event/
    └── YourModuleEvent.java     ← tạo file này
```
```java
// YourModuleEvent.java
@Data
@Builder
public class YourModuleEvent {
    private String eventId;          // UUID — bắt buộc
    private String eventType;        // routing key — bắt buộc
    private LocalDateTime timestamp; // bắt buộc
    
    // Các field dữ liệu của event
    private Long requestId;
    private Long citizenId;
    private String urgencyLevel;
    // ... thêm field tùy event
}
```

## Bước 2 — Tạo Publisher class
```
module/your_module/
└── event/
    ├── YourModuleEvent.java
    └── YourModuleEventPublisher.java   ← tạo file này
```
```java
// YourModuleEventPublisher.java
@Slf4j
@Component
@RequiredArgsConstructor
public class YourModuleEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishSomethingHappened(/* các param cần thiết */) {
        YourModuleEvent event = YourModuleEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(RabbitMQConfig.RK_YOUR_ROUTING_KEY)
                .timestamp(LocalDateTime.now())
                // .field1(value1)
                // .field2(value2)
                .build();

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE,           // luôn dùng constant này
            RabbitMQConfig.RK_YOUR_ROUTING_KEY, // routing key tương ứng
            event
        );

        log.info("Published {}: id={}", event.getEventType(), event.getEventId());
    }
}
```

## Bước 3 — Gọi Publisher trong Service, SAU KHI lưu DB
```java
// YourServiceImpl.java
@Service
@RequiredArgsConstructor
public class YourServiceImpl implements YourService {

    private final YourRepository repository;
    private final YourModuleEventPublisher eventPublisher; // inject vào

    @Transactional
    public YourResponse create(CreateRequest dto) {
        // 1. Validate
        // ...

        // 2. Lưu DB TRƯỚC
        YourEntity entity = repository.save(buildEntity(dto));

        // 3. Publish event SAU KHI lưu DB thành công
        eventPublisher.publishSomethingHappened(entity);

        // 4. Trả về response
        return toResponse(entity);
    }
}
```

## Routing key tương ứng module của bạn
```
Cường (Request Module):
  → RabbitMQConfig.RK_REQUEST_CREATED   sau khi tạo yêu cầu
  → RabbitMQConfig.RK_REQUEST_STATUS    sau khi đổi status

Tuấn Anh (Dispatch Module):
  → RabbitMQConfig.RK_REQUEST_ASSIGNED  sau khi assign team
  → RabbitMQConfig.RK_REQUEST_COMPLETED sau khi complete
  → RabbitMQConfig.RK_TEAM_LOCATION     sau khi nhận GPS

Tiến (Resource Module):
  → RabbitMQConfig.RK_RESOURCE_LOW      khi tồn kho < threshold
  → RabbitMQConfig.RK_RESOURCE_DIST     sau khi phân phối hàng
```

## Cách test publisher đã hoạt động chưa
```
1. Chạy Spring Boot
2. Gọi API tạo yêu cầu qua Postman
3. Vào http://localhost:15672 (RabbitMQ UI)
4. Vào tab "Queues"
5. Bấm vào queue "q.notification.request.created"
6. Bấm "Get messages"
→ Nếu thấy message → publisher hoạt động đúng ✅
→ Nếu không thấy → xem log Spring Boot có lỗi không
```

---

# 5. CÁCH VIẾT RABBITMQ LISTENER

> Chỉ Quý Mạnh và Tiến cần đọc phần này.

## Quý Mạnh — Tạo file này
```
module/notification/
└── listener/
    └── NotificationEventListener.java
```
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final SseService sseService;
    // private final WebSocketService webSocketService; (Sprint 2)

    // ==================== XỬ LÝ TỪNG EVENT ====================

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_REQUEST_CREATED)
    public void handleRequestCreated(RescueRequestCreatedEvent event) {
        log.info("Handling new request: id={}", event.getRequestId());
        try {
            // Gửi SSE đến tất cả COORDINATOR đang online
            sseService.sendToRole("COORDINATOR", "new.request.alert", Map.of(
                "requestId",    event.getRequestId(),
                "citizenName",  event.getCitizenName(),
                "urgencyLevel", event.getUrgencyLevel(),
                "lat",          event.getLat(),
                "lng",          event.getLng(),
                "message",      "Có yêu cầu cứu hộ mới"
            ));
        } catch (Exception e) {
            log.error("Failed handleRequestCreated", e);
            throw e; // throw để RabbitMQ retry, sau đó vào DLQ
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_REQUEST_ASSIGNED)
    public void handleRequestAssigned(RescueRequestAssignedEvent event) {
        log.info("Handling request assigned: id={}", event.getRequestId());
        try {
            // Gửi SSE đến đúng CITIZEN được assign
            sseService.sendToUser(event.getCitizenId(), "request.assigned", Map.of(
                "requestId",  event.getRequestId(),
                "teamName",   event.getTeamName(),
                "eta",        event.getEstimatedArrival(),
                "message",    "Đội cứu hộ đang trên đường đến"
            ));
        } catch (Exception e) {
            log.error("Failed handleRequestAssigned", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_REQUEST_COMPLETED)
    public void handleRequestCompleted(RescueRequestCompletedEvent event) {
        log.info("Handling request completed: id={}", event.getRequestId());
        try {
            // Gửi SSE đến CITIZEN để xác nhận
            sseService.sendToUser(event.getCitizenId(), "request.completed", Map.of(
                "requestId", event.getRequestId(),
                "message",   "Bạn đã được cứu hộ. Vui lòng xác nhận."
            ));
        } catch (Exception e) {
            log.error("Failed handleRequestCompleted", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_RESOURCE_LOW)
    public void handleResourceLow(ResourceStockLowEvent event) {
        log.info("Handling resource low: item={}", event.getItemName());
        try {
            // Gửi SSE đến tất cả MANAGER
            sseService.sendToRole("MANAGER", "resource.low.alert", Map.of(
                "itemName",        event.getItemName(),
                "currentQuantity", event.getCurrentQuantity(),
                "threshold",       event.getThreshold(),
                "message",         "Cảnh báo: tồn kho sắp hết"
            ));
        } catch (Exception e) {
            log.error("Failed handleResourceLow", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_BROADCAST)
    public void handleBroadcast(SystemBroadcastEvent event) {
        log.info("Handling broadcast: {}", event.getMessage());
        try {
            // Gửi SSE đến TẤT CẢ user đang online
            sseService.sendToAll("system.broadcast", Map.of(
                "message", event.getMessage(),
                "level",   event.getLevel() // INFO, WARNING, CRITICAL
            ));
        } catch (Exception e) {
            log.error("Failed handleBroadcast", e);
            throw e;
        }
    }
}
```

## Tiến — Tạo file này
```
module/report/
└── listener/
    └── ReportEventListener.java
```
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportEventListener {

    private final ReportAggregationService aggregationService;

    @RabbitListener(queues = RabbitMQConfig.Q_REPORT_COMPLETED)
    public void handleRequestCompleted(RescueRequestCompletedEvent event) {
        log.info("Report: handling completed request: id={}", event.getRequestId());
        try {
            // Cập nhật snapshot báo cáo ngày hôm nay
            aggregationService.updateDailySnapshot(
                LocalDate.now(),
                event.getRequestId(),
                event.getDurationMinutes()
            );
        } catch (Exception e) {
            log.error("Failed to update report snapshot", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_REPORT_DISTRIBUTED)
    public void handleResourceDistributed(ResourceDistributedEvent event) {
        log.info("Report: handling distribution: id={}", event.getDistributionId());
        try {
            aggregationService.updateResourceSnapshot(
                LocalDate.now(),
                event.getWarehouseId()
            );
        } catch (Exception e) {
            log.error("Failed to update resource snapshot", e);
            throw e;
        }
    }
}
```

---

# 6. CÁCH VIẾT UNIT TEST

## Template test cho Service
```java
@ExtendWith(MockitoExtension.class)
class YourServiceTest {

    @Mock
    private YourRepository repository;

    @Mock
    private YourEventPublisher eventPublisher;

    @InjectMocks
    private YourServiceImpl service;

    @Test
    @DisplayName("Tạo thành công khi input hợp lệ")
    void create_success() {
        // ARRANGE — chuẩn bị dữ liệu
        CreateYourRequest request = CreateYourRequest.builder()
                .field1("value1")
                .build();

        YourEntity savedEntity = YourEntity.builder()
                .id(1L)
                .field1("value1")
                .build();

        when(repository.save(any())).thenReturn(savedEntity);

        // ACT — gọi method cần test
        YourResponse result = service.create(request, 1L);

        // ASSERT — kiểm tra kết quả
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("value1", result.getField1());

        // Kiểm tra publisher được gọi đúng 1 lần
        verify(eventPublisher, times(1)).publishSomethingHappened(any());
    }

    @Test
    @DisplayName("Throw NOT_FOUND khi không tìm thấy")
    void findById_notFound_throwException() {
        // ARRANGE
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        AppException ex = assertThrows(AppException.class,
                () -> service.findById(999L));

        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode());
    }
}
```

## Đặt file test đúng chỗ
```
backend/src/test/java/com/floodrescue/
└── module/
    └── your_module/
        └── service/
            └── YourServiceTest.java
```

## Chạy test
```bash
# Chạy tất cả test
mvn test

# Chạy test của 1 class
mvn test -Dtest=YourServiceTest

# Chạy test của 1 method
mvn test -Dtest=YourServiceTest#create_success
```

---

# 7. CÁCH TẠO PR VÀ COMMIT

## Commit message convention

Format:
  <JiraKey> [MODULE] mô tả ngắn gọn

Ví dụ đúng:
  FRS-1  [AUTH] add User entity and RefreshToken entity
  FRS-1  [AUTH] implement login and register API
  FRS-5  [REQUEST] add create rescue request endpoint
  FRS-5  [REQUEST] add urgency classification logic
  FRS-12 [DISPATCH] implement Redis distributed lock for assignment
  FRS-20 [NOTIFICATION] add SSE stream per userId
  FRS-35 [FRONTEND] add login page and route guard
  FRS-48 [INFRA] add Flyway V1 auth schema migration

Ví dụ sai:
  fix bug                  ← không có Jira key, không có module
  FRS-5 update code        ← không rõ làm gì
  [AUTH] add login         ← thiếu Jira key → Jira không tracking được
  FRS-5                    ← thiếu mô tả

Quy tắc:
  - JiraKey: lấy từ task trong Jira (FRS-1, FRS-2, ...)
  - MODULE: AUTH | REQUEST | DISPATCH | RESOURCE | NOTIFICATION | REPORT | FRONTEND | INFRA
  - Mô tả: tiếng Anh, ngắn gọn, bắt đầu bằng động từ (add, implement, fix, update, refactor)
  - 1 commit = 1 việc cụ thể, không gộp nhiều task vào 1 commit

Lợi ích:
  - Jira tự động hiển thị commit trong task tương ứng
  - Dễ tìm lại commit khi cần debug
  - Long dễ review PR hơn
```

## Quy trình tạo PR
```bash
# 1. Đảm bảo đang ở branch feature của mình
git branch
# phải thấy * feature/your-feature

# 2. Lấy code mới nhất từ develop về (tránh conflict)
git fetch origin develop
git rebase origin/develop
# nếu có conflict → fix conflict → git add . → git rebase --continue

# 3. Push branch lên
git push origin feature/your-feature

# 4. Lên GitHub → tạo Pull Request
#    base: develop ← compare: feature/your-feature
#    Title: [MODULE] Mô tả ngắn
#    Description: Liệt kê những gì đã làm

# 5. Tag Long trong comment để review
```

## PR Description template
```
## Đã làm
- [ ] Tạo RescueRequest entity
- [ ] Implement create API
- [ ] Publish RabbitMQ event

## Test
- [ ] Chạy unit test pass
- [ ] Test manual qua Postman

## Notes
(Ghi chú gì cần Long biết khi review)
```

---

# 8. CÁC LỖI THƯỜNG GẶP

## Lỗi 1 — Flyway validation failed
```
Cause: sửa file V đã commit
Fix:   git revert lại file đó, tạo file V mới nếu muốn thay đổi schema
```

## Lỗi 2 — RabbitMQ connection refused
```
Cause: Docker chưa chạy
Fix:   cd infrastructure && docker compose -f docker-compose.infra.yml up -d
```

## Lỗi 3 — JWT 401 Unauthorized khi test Postman
```
Cause: Quên gắn token vào header
Fix:   Authorization: Bearer <access_token>
       (lấy access_token từ response của POST /api/auth/login)
```

## Lỗi 4 — LazyInitializationException
```
Cause: Truy cập quan hệ @OneToMany ngoài transaction
Fix:   Thêm @Transactional vào method service
       Hoặc dùng JOIN FETCH trong query
```

## Lỗi 5 — Conflict khi rebase
```
Fix:
  git status                  → xem file conflict
  # mở file, chọn giữ code nào
  git add <file-đã-fix>
  git rebase --continue
```

## Lỗi 6 — Cannot determine embedded database driver class
```
Cause: Thiếu datasource config hoặc MySQL chưa chạy
Fix:   Kiểm tra docker ps → rescue-mysql phải healthy
       Kiểm tra application.yml có đúng DB URL không
```

## Lỗi 7 — @RabbitListener không nhận được message
```
Fix checklist:
  1. Tên queue trong @RabbitListener có khớp với RabbitMQConfig không?
  2. RabbitMQConfig có khai báo Queue và Binding đúng không?
  3. Vào localhost:15672 → Queues → xem message có trong queue không?
  4. Log Spring Boot có lỗi khi start không?
```

---

> Cập nhật lần cuối: Sprint 1
> Mọi thắc mắc → tag Long trên Teams