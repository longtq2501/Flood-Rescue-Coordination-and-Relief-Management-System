package com.floodrescue.report.integration;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * E2E Integration Test - Refactored for Microservices Architecture.
 * Hits the API Gateway (localhost:8080) and verifies state across all databases.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient(timeout = "30000")
@ActiveProfiles("test")
@DisplayName("E2E Integration Tests - Flood Rescue Microservices System")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class E2EWorkflowIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

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

    @Autowired
    @Qualifier("reportDataSource")
    private DataSource reportDataSource;

    // ==================== STATE ====================
    private String citizenToken;
    private String coordinatorToken;
    private String teamToken;
    private String managerToken;
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
                        + "VALUES (?, ?, ?, ?, ?, 'ACTIVE', NOW(), NOW())",
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

        // Register users
        registerUser("Citizen Test", citizenPhone, "citizen" + uniqueSuffix + "@test.com", "Test@Citizen123", "CITIZEN");
        registerUser("Team Alpha", teamPhone, "team" + uniqueSuffix + "@test.com", "Test@TeamAlpha123", "RESCUE_TEAM");

        insertUserDirect("Coordinator Test", coordPhone, "coord" + uniqueSuffix + "@test.com", "Test@Coordinator123", "COORDINATOR");
        insertUserDirect("Manager Test", managerPhone, "manager" + uniqueSuffix + "@test.com", "Test@Manager123", "MANAGER");

        citizenToken = authenticateUser(citizenPhone, "Test@Citizen123");
        coordinatorToken = authenticateUser(coordPhone, "Test@Coordinator123");
        teamToken = authenticateUser(teamPhone, "Test@TeamAlpha123");
        managerToken = authenticateUser(managerPhone, "Test@Manager123");

        JdbcTemplate authJdbc = new JdbcTemplate(authDataSource);
        citizenUserId = authJdbc.queryForObject("SELECT id FROM users WHERE phone = ?", Long.class, citizenPhone);
        teamUserId = authJdbc.queryForObject("SELECT id FROM users WHERE phone = ?", Long.class, teamPhone);
    }

    private void cleanDatabase() {
        JdbcTemplate[] jdbcs = {
                new JdbcTemplate(authDataSource),
                new JdbcTemplate(requestDataSource),
                new JdbcTemplate(dispatchDataSource),
                new JdbcTemplate(resourceDataSource),
                new JdbcTemplate(reportDataSource)
        };

        for (JdbcTemplate jdbc : jdbcs) {
            try {
                jdbc.execute("SET FOREIGN_KEY_CHECKS=0");
                List<String> tables = jdbc.queryForList("SHOW TABLES", String.class);
                for (String table : tables) {
                     if (!table.toLowerCase().contains("flyway")) {
                         jdbc.execute("TRUNCATE TABLE " + table);
                     }
                }
                jdbc.execute("SET FOREIGN_KEY_CHECKS=1");
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    // ==================== HELPER METHODS ====================

    private void registerUser(String fullName, String phone, String email, String password, String role) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("fullName", fullName);
        payload.put("phone", phone);
        payload.put("email", email);
        payload.put("password", password);
        payload.put("role", role);

        webTestClient.post()
                .uri("/api/auth/register")
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated();
    }

    private String authenticateUser(String phone, String password) {
        Map<String, String> loginPayload = new LinkedHashMap<>();
        loginPayload.put("phone", phone);
        loginPayload.put("password", password);

        Map response = webTestClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginPayload)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Map data = (Map) response.get("data");
        return (String) data.get("accessToken");
    }

    private Long createRescueRequest() {
        Map<String, Object> requestDto = new LinkedHashMap<>();
        requestDto.put("lat", new BigDecimal("10.762622"));
        requestDto.put("lng", new BigDecimal("106.660172"));
        requestDto.put("addressText", "123 Duong ABC");
        requestDto.put("description", "Bi ket tren mai, 3 nguoi lon 1 tre em");
        requestDto.put("numPeople", 4);

        Map response = webTestClient.post()
                .uri("/api/requests")
                .header("Authorization", "Bearer " + citizenToken)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Map data = (Map) response.get("data");
        return ((Number) data.get("id")).longValue();
    }

    private void verifyRequest(Long requestId) {
        Map<String, Object> verifyDto = new LinkedHashMap<>();
        verifyDto.put("urgencyLevel", "HIGH");
        verifyDto.put("note", "Da xac minh, can ho tro gap");

        webTestClient.patch()
                .uri("/api/requests/" + requestId + "/verify")
                .header("Authorization", "Bearer " + coordinatorToken)
                .bodyValue(verifyDto)
                .exchange()
                .expectStatus().isOk();
    }

    private Long insertRescueTeam() {
        JdbcTemplate dispatchJdbc = new JdbcTemplate(dispatchDataSource);
        dispatchJdbc.update(
                "INSERT INTO rescue_teams (name, leader_id, capacity, status, created_at) VALUES (?, ?, ?, 'AVAILABLE', NOW())",
                "Team Alpha", teamUserId, 4);
        Long teamId = dispatchJdbc.queryForObject("SELECT id FROM rescue_teams WHERE leader_id = ?", Long.class, teamUserId);
        dispatchJdbc.update("INSERT INTO team_members (team_id, user_id, joined_at) VALUES (?, ?, NOW())", teamId, teamUserId);
        return teamId;
    }

    private Long insertVehicle() {
        JdbcTemplate resourceJdbc = new JdbcTemplate(resourceDataSource);
        long ts = System.currentTimeMillis() % 100000;
        resourceJdbc.update(
                "INSERT INTO vehicles (plate_number, type, capacity, status, created_at) VALUES (?, 'BOAT', 6, 'AVAILABLE', NOW())",
                "51A-" + ts);
        return resourceJdbc.queryForObject("SELECT id FROM vehicles WHERE plate_number = ?", Long.class, "51A-" + ts);
    }

    private void updateRequestStatus(Long requestId, String newStatus) {
        JdbcTemplate requestJdbc = new JdbcTemplate(requestDataSource);
        requestJdbc.update("UPDATE rescue_requests SET status = ? WHERE id = ?", newStatus, requestId);
    }

    // ==================== TESTS ====================

    @Test
    @Order(1)
    @DisplayName("Health Check: Services are routed via Gateway")
    public void testGatewayRouting() {
        webTestClient.get().uri("/api/auth/health").exchange().expectStatus().isOk();
        webTestClient.get().uri("/api/requests/health").exchange().expectStatus().isOk();
        webTestClient.get().uri("/api/dispatch/health").exchange().expectStatus().isOk();
        webTestClient.get().uri("/api/resources/health").exchange().expectStatus().isOk();
        webTestClient.get().uri("/api/notifications/health").exchange().expectStatus().isOk();
        webTestClient.get().uri("/api/reports/health").exchange().expectStatus().isOk();
    }

    @Test
    @Order(2)
    @DisplayName("P0: Full Rescue Workflow")
    public void testFullRescueWorkflow() throws Exception {
        // 1. Citizen creates request
        Long reqId = createRescueRequest();
        
        // 2. Coordinator verifies
        verifyRequest(reqId);
        
        // 3. Coordinator assigns
        Long teamId = insertRescueTeam();
        Long vehicleId = insertVehicle();

        Map<String, Object> assignPayload = new LinkedHashMap<>();
        assignPayload.put("requestId", reqId);
        assignPayload.put("teamId", teamId);
        assignPayload.put("vehicleId", vehicleId);
        assignPayload.put("citizenId", citizenUserId);

        webTestClient.post()
                .uri("/api/dispatch/assign")
                .header("Authorization", "Bearer " + coordinatorToken)
                .bodyValue(assignPayload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);

        // 4. Team starts mission
        JdbcTemplate dispatchJdbc = new JdbcTemplate(dispatchDataSource);
        Long assignmentId = dispatchJdbc.queryForObject("SELECT id FROM assignments WHERE request_id = ?", Long.class, reqId);

        webTestClient.patch()
                .uri("/api/dispatch/assignments/" + assignmentId + "/start")
                .header("Authorization", "Bearer " + teamToken)
                .exchange()
                .expectStatus().isOk();

        // 5. Team completes mission
        webTestClient.patch()
                .uri("/api/dispatch/assignments/" + assignmentId + "/complete")
                .header("Authorization", "Bearer " + teamToken)
                .attribute("resultNote", "Mission accomplished")
                .exchange()
                .expectStatus().isOk();
        
        System.out.println("=== PASS: Full rescue workflow ===");
    }
}
