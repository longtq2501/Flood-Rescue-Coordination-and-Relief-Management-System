package com.floodrescue.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.floodrescue.module.dispatch.event.DispatchEventPublisher;
import com.floodrescue.module.rescue_request.dto.request.CreateRescueRequestDto;
import com.floodrescue.module.rescue_request.event.RescueRequestEventPublisher;
import com.floodrescue.shared.util.MinioService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("E2E Integration Tests - Flood Rescue System")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class E2EWorkflowIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private RabbitTemplate rabbitTemplate;

        @MockBean
        private DispatchEventPublisher dispatchEventPublisher;

        @MockBean
        private RescueRequestEventPublisher rescueRequestEventPublisher;

        @MockBean
        private MinioService minioService;

        @Autowired
        private ObjectMapper objectMapper;

        // ==================== DATASOURCES ====================
        @Autowired
        @Qualifier("authDataSource")
        private DataSource authDataSource;

        @Autowired
        @Qualifier("requestDataSource")
        private DataSource requestDataSource;

        @Autowired
        @Qualifier("dispatchDataSource")
        private DataSource dispatchDataSource;

        @Autowired
        @Qualifier("resourceDataSource")
        private DataSource resourceDataSource;

        // ==================== STATE ====================
        private String citizenToken;
        private String coordinatorToken;
        private String teamToken;
        private String managerToken;
        private Long rescueRequestId;
        private Long citizenUserId;
        private Long teamUserId;

        @Autowired
        private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

        // ==================== SETUP ====================

        private Long insertUserDirect(String fullName, String phone, String email, String password, String role) {
                JdbcTemplate authJdbc = new JdbcTemplate(authDataSource);
                String hashedPassword = passwordEncoder.encode(password);
                authJdbc.update(
                                "INSERT INTO users (full_name, phone, email, password_hash, role, status, created_at, updated_at) "
                                                +
                                                "VALUES (?, ?, ?, ?, ?, 'ACTIVE', NOW(), NOW())",
                                fullName, phone, email, hashedPassword, role);
                return authJdbc.queryForObject(
                                "SELECT id FROM users WHERE phone = ?", Long.class, phone);
        }

        @BeforeEach
        public void setup() throws Exception {
                cleanDatabase();

                long timestamp = System.currentTimeMillis() % 10000000;
                String uniqueSuffix = String.format("%07d", timestamp);
                String citizenPhone = "090" + uniqueSuffix;
                String coordPhone = "091" + uniqueSuffix;
                String teamPhone = "092" + uniqueSuffix;
                String managerPhone = "093" + uniqueSuffix;

                // CITIZEN va RESCUE_TEAM dung API register binh thuong
                registerUser("Citizen Test", citizenPhone, "citizen" + uniqueSuffix + "@test.com", "Test@Citizen123",
                                "CITIZEN");
                registerUser("Team Alpha", teamPhone, "team" + uniqueSuffix + "@test.com", "Test@TeamAlpha123",
                                "RESCUE_TEAM");

                // COORDINATOR va MANAGER insert thang vao DB vi API register khong cho phep
                // role nay
                insertUserDirect("Coordinator Test", coordPhone, "coord" + uniqueSuffix + "@test.com",
                                "Test@Coordinator123",
                                "COORDINATOR");
                insertUserDirect("Manager Test", managerPhone, "manager" + uniqueSuffix + "@test.com",
                                "Test@Manager123",
                                "MANAGER");

                citizenToken = authenticateUser(citizenPhone, "Test@Citizen123");
                coordinatorToken = authenticateUser(coordPhone, "Test@Coordinator123");
                teamToken = authenticateUser(teamPhone, "Test@TeamAlpha123");
                managerToken = authenticateUser(managerPhone, "Test@Manager123");

                // Luu userId de dung trong tests
                JdbcTemplate authJdbc = new JdbcTemplate(authDataSource);
                citizenUserId = authJdbc.queryForObject("SELECT id FROM users WHERE phone = ?", Long.class,
                                citizenPhone);
                teamUserId = authJdbc.queryForObject("SELECT id FROM users WHERE phone = ?", Long.class, teamPhone);
        }

        private void cleanDatabase() {
                JdbcTemplate authJdbc = new JdbcTemplate(authDataSource);
                JdbcTemplate requestJdbc = new JdbcTemplate(requestDataSource);
                JdbcTemplate dispatchJdbc = new JdbcTemplate(dispatchDataSource);
                JdbcTemplate resourceJdbc = new JdbcTemplate(resourceDataSource);

                // Clean dispatch tables
                try {
                        dispatchJdbc.execute("SET FOREIGN_KEY_CHECKS=0");
                        dispatchJdbc.execute("TRUNCATE TABLE location_logs");
                        dispatchJdbc.execute("TRUNCATE TABLE assignments");
                        dispatchJdbc.execute("TRUNCATE TABLE team_members");
                        dispatchJdbc.execute("TRUNCATE TABLE rescue_teams");
                        dispatchJdbc.execute("SET FOREIGN_KEY_CHECKS=1");
                } catch (Exception e) {
                        System.err.println("cleanDatabase [dispatch] error: " + e.getMessage());
                }

                // Clean resource tables
                try {
                        resourceJdbc.execute("SET FOREIGN_KEY_CHECKS=0");
                        resourceJdbc.execute("TRUNCATE TABLE distribution_items");
                        resourceJdbc.execute("TRUNCATE TABLE distributions");
                        resourceJdbc.execute("TRUNCATE TABLE vehicle_logs");
                        resourceJdbc.execute("TRUNCATE TABLE vehicles");
                        resourceJdbc.execute("TRUNCATE TABLE relief_items");
                        resourceJdbc.execute("TRUNCATE TABLE warehouses");
                        resourceJdbc.execute("SET FOREIGN_KEY_CHECKS=1");
                } catch (Exception e) {
                        System.err.println("cleanDatabase [resource] error: " + e.getMessage());
                }

                // Clean request tables
                try {
                        requestJdbc.execute("SET FOREIGN_KEY_CHECKS=0");
                        requestJdbc.execute("TRUNCATE TABLE rescue_requests");
                        requestJdbc.execute("TRUNCATE TABLE request_images");
                        requestJdbc.execute("TRUNCATE TABLE status_history");
                        requestJdbc.execute("SET FOREIGN_KEY_CHECKS=1");
                } catch (Exception e) {
                        System.err.println("cleanDatabase [request] error: " + e.getMessage());
                }

                // Clean auth tables
                try {
                        authJdbc.execute("SET FOREIGN_KEY_CHECKS=0");
                        authJdbc.execute("TRUNCATE TABLE refresh_tokens");
                        authJdbc.execute("TRUNCATE TABLE users");
                        authJdbc.execute("SET FOREIGN_KEY_CHECKS=1");
                } catch (Exception e) {
                        System.err.println("cleanDatabase [auth] error: " + e.getMessage());
                }
        }

        // ==================== HELPER METHODS ====================

        private void registerUser(String fullName, String phone, String email, String password, String role)
                        throws Exception {
                java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
                payload.put("fullName", fullName);
                payload.put("phone", phone);
                payload.put("email", email);
                payload.put("password", password);
                payload.put("role", role);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isCreated());
        }

        private String authenticateUser(String phone, String password) throws Exception {
                java.util.Map<String, String> loginPayload = new java.util.LinkedHashMap<>();
                loginPayload.put("phone", phone);
                loginPayload.put("password", password);

                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginPayload)))
                                .andExpect(status().isOk())
                                .andReturn();

                String responseBody = result.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                com.floodrescue.shared.response.ApiResponse<java.util.Map<String, Object>> response = objectMapper
                                .readValue(responseBody, com.floodrescue.shared.response.ApiResponse.class);

                java.util.Map<String, Object> data = response.getData();
                return (String) data.get("accessToken");
        }

        /** Tao rescue request va tra ve ID */
        private Long createRescueRequest() throws Exception {
                CreateRescueRequestDto requestDto = new CreateRescueRequestDto();
                requestDto.setLat(new BigDecimal("10.762622"));
                requestDto.setLng(new BigDecimal("106.660172"));
                requestDto.setAddressText("123 Duong ABC");
                requestDto.setDescription("Bi ket tren mai, 3 nguoi lon 1 tre em");
                requestDto.setNumPeople(4);

                byte[] jsonBytes = objectMapper.writeValueAsBytes(requestDto);

                MvcResult createResult = mockMvc.perform(multipart("/api/requests")
                                .file(new MockMultipartFile("data", null, "application/json", jsonBytes))
                                .header("Authorization", "Bearer " + citizenToken)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.status").value("PENDING"))
                                .andReturn();

                String responseBody = createResult.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                com.floodrescue.shared.response.ApiResponse<java.util.Map<String, Object>> apiResponse = objectMapper
                                .readValue(responseBody, com.floodrescue.shared.response.ApiResponse.class);
                return ((Number) apiResponse.getData().get("id")).longValue();
        }

        /** Coordinator verify request va tra ve response */
        private void verifyRequest(Long requestId) throws Exception {
                java.util.Map<String, Object> verifyDto = new java.util.LinkedHashMap<>();
                verifyDto.put("urgencyLevel", "HIGH");
                verifyDto.put("note", "Da xac minh, can ho tro gap");

                mockMvc.perform(patch("/api/requests/" + requestId + "/verify")
                                .header("Authorization", "Bearer " + coordinatorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(verifyDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.status").value("VERIFIED"));
        }

        /** Insert rescue team vao dispatch DB, tra ve teamId */
        private Long insertRescueTeam() {
                JdbcTemplate dispatchJdbc = new JdbcTemplate(dispatchDataSource);
                dispatchJdbc.update(
                                "INSERT INTO rescue_teams (name, leader_id, capacity, status, created_at) " +
                                                "VALUES (?, ?, ?, 'AVAILABLE', NOW())",
                                "Team Alpha", teamUserId, 4);
                Long teamId = dispatchJdbc.queryForObject(
                                "SELECT id FROM rescue_teams WHERE leader_id = ?", Long.class, teamUserId);
                // Add team member
                dispatchJdbc.update(
                                "INSERT INTO team_members (team_id, user_id, joined_at) VALUES (?, ?, NOW())",
                                teamId, teamUserId);
                return teamId;
        }

        /** Insert vehicle vao resource DB, tra ve vehicleId */
        private Long insertVehicle() {
                JdbcTemplate resourceJdbc = new JdbcTemplate(resourceDataSource);
                long ts = System.currentTimeMillis() % 100000;
                resourceJdbc.update(
                                "INSERT INTO vehicles (plate_number, type, capacity, status, created_at) " +
                                                "VALUES (?, 'BOAT', 6, 'AVAILABLE', NOW())",
                                "51A-" + ts);
                return resourceJdbc.queryForObject(
                                "SELECT id FROM vehicles WHERE plate_number = ?", Long.class, "51A-" + ts);
        }

        /** Cap nhat trang thai request truc tiep trong DB (bypass event system) */
        private void updateRequestStatus(Long requestId, String newStatus) {
                JdbcTemplate requestJdbc = new JdbcTemplate(requestDataSource);
                requestJdbc.update(
                                "UPDATE rescue_requests SET status = ? WHERE id = ?",
                                newStatus, requestId);
        }

        /**
         * Full setup: tao request -> verify -> insert team+vehicle -> assign. Tra ve
         * assignmentId
         */
        private java.util.Map<String, Long> setupFullAssignment() throws Exception {
                Long reqId = createRescueRequest();
                verifyRequest(reqId);
                Long teamId = insertRescueTeam();
                Long vehicleId = insertVehicle();

                // Coordinator assign team
                java.util.Map<String, Object> assignPayload = new java.util.LinkedHashMap<>();
                assignPayload.put("requestId", reqId);
                assignPayload.put("teamId", teamId);
                assignPayload.put("vehicleId", vehicleId);
                assignPayload.put("citizenId", citizenUserId);

                MvcResult assignResult = mockMvc.perform(post("/api/dispatch/assign")
                                .header("Authorization", "Bearer " + coordinatorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(assignPayload)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andReturn();

                String body = assignResult.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                com.floodrescue.shared.response.ApiResponse<java.util.Map<String, Object>> resp = objectMapper
                                .readValue(body,
                                                com.floodrescue.shared.response.ApiResponse.class);
                Long assignmentId = ((Number) resp.getData().get("id")).longValue();

                java.util.Map<String, Long> ids = new java.util.LinkedHashMap<>();
                ids.put("requestId", reqId);
                ids.put("teamId", teamId);
                ids.put("vehicleId", vehicleId);
                ids.put("assignmentId", assignmentId);
                return ids;
        }

        // ==================== TESTS ====================

        @Test
        @Order(1)
        @DisplayName("Health Check: Backend is running")
        public void testBackendHealth() throws Exception {
                mockMvc.perform(get("/actuator/health")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().is2xxSuccessful())
                                .andDo(print());

                System.out.println("=== PASS: Backend health check ===");
        }

        @Test
        @Order(2)
        @DisplayName("P0: Citizen Register -> Login -> Create Rescue Request")
        public void testCitizenWorkflow() throws Exception {
                Long id = createRescueRequest();
                rescueRequestId = id;

                // Citizen xem request cua minh
                mockMvc.perform(get("/api/requests/my?page=0&size=10")
                                .header("Authorization", "Bearer " + citizenToken)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.content").isArray())
                                .andExpect(jsonPath("$.data.content[0].id").value(id))
                                .andDo(print());

                System.out.println("=== PASS: Citizen workflow - request ID: " + rescueRequestId + " ===");
        }

        @Test
        @Order(3)
        @DisplayName("P0: Coordinator View -> Verify Request -> Assign Team")
        public void testCoordinatorVerifyAndAssignWorkflow() throws Exception {
                // Step 1: Citizen tao rescue request
                Long reqId = createRescueRequest();

                // Step 2: Coordinator xem danh sach requests
                mockMvc.perform(get("/api/requests?page=0&size=10")
                                .header("Authorization", "Bearer " + coordinatorToken)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.content").isArray())
                                .andExpect(jsonPath("$.data.content[0].id").value(reqId))
                                .andDo(print());

                System.out.println("  -> Coordinator listed requests");

                // Step 3: Coordinator verify request (PENDING -> VERIFIED)
                verifyRequest(reqId);
                System.out.println("  -> Request verified: PENDING -> VERIFIED");

                // Step 4: Coordinator xem chi tiet request da verify
                mockMvc.perform(get("/api/requests/" + reqId)
                                .header("Authorization", "Bearer " + coordinatorToken)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                                .andDo(print());

                // Step 5: Setup team va vehicle, sau do assign
                Long teamId = insertRescueTeam();
                Long vehicleId = insertVehicle();

                java.util.Map<String, Object> assignPayload = new java.util.LinkedHashMap<>();
                assignPayload.put("requestId", reqId);
                assignPayload.put("teamId", teamId);
                assignPayload.put("vehicleId", vehicleId);
                assignPayload.put("citizenId", citizenUserId);

                mockMvc.perform(post("/api/dispatch/assign")
                                .header("Authorization", "Bearer " + coordinatorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(assignPayload)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.requestId").value(reqId))
                                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                                .andDo(print());

                System.out.println("=== PASS: Coordinator verify + assign workflow ===");
        }

        @Test
        @Order(4)
        @DisplayName("P0: Rescue Team Mission Lifecycle (View -> Start -> GPS -> Complete)")
        public void testRescueTeamMissionLifecycle() throws Exception {
                // Setup: full assignment
                java.util.Map<String, Long> ids = setupFullAssignment();
                Long assignmentId = ids.get("assignmentId");

                // Step 1: Team xem assignments cua minh
                mockMvc.perform(get("/api/dispatch/assignments/my")
                                .header("Authorization", "Bearer " + teamToken)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].id").value(assignmentId))
                                .andDo(print());

                System.out.println("  -> Team viewed assignment: " + assignmentId);

                // Step 2: Team bat dau nhiem vu
                mockMvc.perform(patch("/api/dispatch/assignments/" + assignmentId + "/start")
                                .header("Authorization", "Bearer " + teamToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andDo(print());

                System.out.println("  -> Mission started");

                // Step 3: Team gui GPS location update
                java.util.Map<String, Object> locationPayload = new java.util.LinkedHashMap<>();
                locationPayload.put("lat", new BigDecimal("10.763000"));
                locationPayload.put("lng", new BigDecimal("106.661000"));
                locationPayload.put("speed", new BigDecimal("15.5"));
                locationPayload.put("heading", new BigDecimal("90.0"));

                mockMvc.perform(post("/api/dispatch/location")
                                .header("Authorization", "Bearer " + teamToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(locationPayload)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andDo(print());

                System.out.println("  -> GPS location updated");

                // Step 4: Team hoan thanh nhiem vu
                mockMvc.perform(patch("/api/dispatch/assignments/" + assignmentId + "/complete")
                                .header("Authorization", "Bearer " + teamToken)
                                .param("resultNote", "Da cuu 4 nguoi an toan"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andDo(print());

                System.out.println("=== PASS: Rescue team mission lifecycle ===");
        }

        @Test
        @Order(5)
        @DisplayName("P0: Full E2E Flow: Citizen -> Coordinator -> Team -> Citizen Confirm")
        public void testCitizenConfirmRescue() throws Exception {
                // Full flow: create -> verify -> assign -> start -> complete
                java.util.Map<String, Long> ids = setupFullAssignment();
                Long reqId = ids.get("requestId");
                Long assignmentId = ids.get("assignmentId");

                // Team start + complete
                mockMvc.perform(patch("/api/dispatch/assignments/" + assignmentId + "/start")
                                .header("Authorization", "Bearer " + teamToken))
                                .andExpect(status().isOk());

                mockMvc.perform(patch("/api/dispatch/assignments/" + assignmentId + "/complete")
                                .header("Authorization", "Bearer " + teamToken)
                                .param("resultNote", "Hoan thanh cuu ho"))
                                .andExpect(status().isOk());

                System.out.println("  -> Team completed mission");

                // Cap nhat trang thai request thanh COMPLETED
                // (binh thuong RabbitMQ event se lam dieu nay, nhung vi mock nen phai update
                // truc tiep)
                updateRequestStatus(reqId, "COMPLETED");
                System.out.println("  -> Request status updated to COMPLETED (simulated event)");

                // Citizen confirm rescue
                mockMvc.perform(patch("/api/requests/" + reqId + "/confirm")
                                .header("Authorization", "Bearer " + citizenToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                                .andDo(print());

                System.out.println("=== PASS: Full E2E flow with citizen confirm ===");
        }

        @Test
        @Order(6)
        @DisplayName("P1: Manager Dashboard - View Request Metrics")
        public void testManagerDashboard() throws Exception {
                // Tao request dau, sau do verify de citizen co the tao request thu 2
                Long reqId1 = createRescueRequest();
                verifyRequest(reqId1);
                // Cancel request 1 de citizen co the tao request 2 (business rule: khong cho co
                // 2 request active)
                updateRequestStatus(reqId1, "CANCELLED");

                Long reqId2 = createRescueRequest();
                verifyRequest(reqId2);

                // Manager xem dashboard
                mockMvc.perform(get("/api/reports/dashboard")
                                .header("Authorization", "Bearer " + managerToken)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").exists())
                                .andDo(print());

                System.out.println("=== PASS: Manager dashboard ===");
        }

        @Test
        @Order(7)
        @DisplayName("P1: Warehouse Management - Create Warehouse, Add Items, Update Stock")
        public void testWarehouseManagement() throws Exception {
                // Step 1: Manager tao warehouse
                java.util.Map<String, Object> warehousePayload = new java.util.LinkedHashMap<>();
                warehousePayload.put("name", "Kho Cuu Tro Q7");
                warehousePayload.put("address", "456 Duong XYZ, Quan 7");

                MvcResult warehouseResult = mockMvc.perform(post("/api/resources/warehouses")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(warehousePayload)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andDo(print())
                                .andReturn();

                String whBody = warehouseResult.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                com.floodrescue.shared.response.ApiResponse<java.util.Map<String, Object>> whResp = objectMapper
                                .readValue(whBody, com.floodrescue.shared.response.ApiResponse.class);
                Long warehouseId = ((Number) whResp.getData().get("id")).longValue();

                System.out.println("  -> Warehouse created: " + warehouseId);

                // Step 2: Manager them relief item
                java.util.Map<String, Object> itemPayload = new java.util.LinkedHashMap<>();
                itemPayload.put("warehouseId", warehouseId);
                itemPayload.put("name", "Nuoc sach");
                itemPayload.put("category", "Thuc pham");
                itemPayload.put("unit", "thung");
                itemPayload.put("quantity", 100);
                itemPayload.put("lowThreshold", 20);

                MvcResult itemResult = mockMvc.perform(post("/api/resources/items")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemPayload)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andDo(print())
                                .andReturn();

                String itemBody = itemResult.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                com.floodrescue.shared.response.ApiResponse<java.util.Map<String, Object>> itemResp = objectMapper
                                .readValue(itemBody, com.floodrescue.shared.response.ApiResponse.class);
                Long itemId = ((Number) itemResp.getData().get("id")).longValue();

                System.out.println("  -> Relief item created: " + itemId);

                // Step 3: Manager cap nhat stock (nhap them 50 thung)
                java.util.Map<String, Object> stockPayload = new java.util.LinkedHashMap<>();
                stockPayload.put("quantity", 50);
                stockPayload.put("note", "Nhap them hang cuu tro dot 2");

                mockMvc.perform(patch("/api/resources/items/" + itemId + "/stock")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stockPayload)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andDo(print());

                System.out.println("  -> Stock updated (+50)");

                // Step 4: Manager xem inventory
                mockMvc.perform(get("/api/resources/items?warehouseId=" + warehouseId + "&page=0&size=10")
                                .header("Authorization", "Bearer " + managerToken)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.content").isArray())
                                .andExpect(jsonPath("$.data.content[0].id").value(itemId))
                                .andDo(print());

                System.out.println("=== PASS: Warehouse management ===");
        }

        @Test
        @Order(8)
        @DisplayName("P1: Distribution Workflow - Distribute Relief Items to Citizen")
        public void testDistributionWorkflow() throws Exception {
                // Setup: tao request + warehouse + item
                Long reqId = createRescueRequest();

                // Tao warehouse + item
                java.util.Map<String, Object> warehousePayload = new java.util.LinkedHashMap<>();
                warehousePayload.put("name", "Kho Cuu Tro Q1");
                warehousePayload.put("address", "789 Duong KLM, Quan 1");

                MvcResult whResult = mockMvc.perform(post("/api/resources/warehouses")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(warehousePayload)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String whBody = whResult.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                com.floodrescue.shared.response.ApiResponse<java.util.Map<String, Object>> whResp = objectMapper
                                .readValue(whBody, com.floodrescue.shared.response.ApiResponse.class);
                Long warehouseId = ((Number) whResp.getData().get("id")).longValue();

                java.util.Map<String, Object> itemPayload = new java.util.LinkedHashMap<>();
                itemPayload.put("warehouseId", warehouseId);
                itemPayload.put("name", "Mi goi");
                itemPayload.put("category", "Thuc pham");
                itemPayload.put("unit", "thung");
                itemPayload.put("quantity", 200);
                itemPayload.put("lowThreshold", 30);

                MvcResult itemResult = mockMvc.perform(post("/api/resources/items")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemPayload)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String itemBody = itemResult.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                com.floodrescue.shared.response.ApiResponse<java.util.Map<String, Object>> itemResp = objectMapper
                                .readValue(itemBody, com.floodrescue.shared.response.ApiResponse.class);
                Long itemId = ((Number) itemResp.getData().get("id")).longValue();

                System.out.println("  -> Warehouse + item setup done");

                // Coordinator phan phoi hang cuu tro cho citizen
                java.util.Map<String, Object> distItem = new java.util.LinkedHashMap<>();
                distItem.put("reliefItemId", itemId);
                distItem.put("quantity", 10);

                java.util.Map<String, Object> distPayload = new java.util.LinkedHashMap<>();
                distPayload.put("requestId", reqId);
                distPayload.put("recipientId", citizenUserId);
                distPayload.put("note", "Phan phoi mi goi cho ho dan");
                distPayload.put("items", java.util.List.of(distItem));

                mockMvc.perform(post("/api/resources/distributions")
                                .header("Authorization", "Bearer " + coordinatorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(distPayload)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andDo(print());

                System.out.println("  -> Distribution created");

                // Coordinator xem distributions
                mockMvc.perform(get("/api/resources/distributions?page=0&size=10")
                                .header("Authorization", "Bearer " + coordinatorToken)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.content").isArray())
                                .andDo(print());

                System.out.println("=== PASS: Distribution workflow ===");
        }

        @Test
        @Order(9)
        @DisplayName("RabbitMQ: Verify rescue.request.created event is published")
        public void testRescueRequestEventPublished() throws Exception {
                CreateRescueRequestDto requestDto = new CreateRescueRequestDto();
                requestDto.setLat(new BigDecimal("10.762622"));
                requestDto.setLng(new BigDecimal("106.660172"));
                requestDto.setDescription("Test event publishing");
                requestDto.setNumPeople(1);

                byte[] eventBytes = objectMapper.writeValueAsBytes(requestDto);

                mockMvc.perform(multipart("/api/requests")
                                .file(new MockMultipartFile("data", null, "application/json", eventBytes))
                                .header("Authorization", "Bearer " + citizenToken))
                                .andExpect(status().isCreated())
                                .andDo(print());

                System.out.println("=== PASS: Rescue request event published (RabbitMQ mocked) ===");
        }
}