import com.floodrescue.dispatch.domain.entity.Assignment;
import com.floodrescue.dispatch.domain.entity.LocationLog;
import com.floodrescue.dispatch.domain.entity.RescueTeam;
import com.floodrescue.dispatch.domain.enums.AssignmentStatus;
import com.floodrescue.dispatch.domain.enums.TeamStatus;
import com.floodrescue.dispatch.dto.request.AssignTeamRequest;
import com.floodrescue.dispatch.dto.request.LocationUpdateRequest;
import com.floodrescue.dispatch.dto.response.AssignmentResponse;
import com.floodrescue.dispatch.dto.response.MapDataResponse;
import com.floodrescue.dispatch.dto.response.RescueTeamResponse;
import com.floodrescue.dispatch.event.DispatchEventPublisher;
import com.floodrescue.dispatch.event.RescueRequestAssignedEvent;
import com.floodrescue.dispatch.event.RescueRequestCompletedEvent;
import com.floodrescue.dispatch.event.RescueRequestStartedEvent;
import com.floodrescue.dispatch.event.TeamLocationUpdatedEvent;
import com.floodrescue.dispatch.repository.AssignmentRepository;
import com.floodrescue.dispatch.repository.LocationLogRepository;
import com.floodrescue.dispatch.repository.RescueTeamRepository;
import com.floodrescue.dispatch.service.DispatchServiceImpl;
import com.floodrescue.dispatch.shared.exception.AppException;
import com.floodrescue.dispatch.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispatchServiceImplTest {

    // =====================================================================
    // MOCK DEPENDENCIES — replace all real infrastructure.
    // (No need for Spring Context, Database, real Redis)
    // =====================================================================
    @Mock private RescueTeamRepository teamRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private LocationLogRepository locationLogRepository;
    @Mock private DispatchEventPublisher eventPublisher;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations; // nested mock cho redisTemplate.opsForValue()

    @InjectMocks private DispatchServiceImpl dispatchService;

    // =====================================================================
    // HELPER — create common fake data, avoid duplicate code in tests
    // =====================================================================

    private RescueTeam buildTeam(Long id, String name, TeamStatus status) {
        return RescueTeam.builder()
                .id(id)
                .name(name)
                .leaderId(10L)
                .capacity(5)
                .status(status)
                .members(List.of())
                .build();
    }

    private Assignment buildAssignment(Long id, RescueTeam team, AssignmentStatus status) {
        return Assignment.builder()
                .id(id)
                .requestId(100L)
                .citizenId(50L)
                .team(team)
                .vehicleId(200L)
                .coordinatorId(10L)
                .status(status)
                .build();
    }

    // =====================================================================
    // getTeamById
    // =====================================================================

    @Nested
    @DisplayName("getTeamById()")
    class GetTeamById {

        @Test
        @DisplayName("should return team response when team exists")
        void success() {
            // ARRANGE
            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.AVAILABLE);
            when(teamRepository.findByIdWithMembers(1L)).thenReturn(Optional.of(mockTeam));

            // ACT
            RescueTeamResponse response = dispatchService.getTeamById(1L);

            // ASSERT
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Team Alpha");
            assertThat(response.getStatus()).isEqualTo(TeamStatus.AVAILABLE);
        }

        @Test
        @DisplayName("should throw AppException TEAM_NOT_FOUND when team does not exist")
        void teamNotFound() {
            // ARRANGE
            when(teamRepository.findByIdWithMembers(99L)).thenReturn(Optional.empty());

            // ASSERT — kỳ vọng exception được ném ra khi ACT
            assertThatThrownBy(() -> dispatchService.getTeamById(99L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_NOT_FOUND);
        }
    }

    // =====================================================================
    // getTeams
    // =====================================================================

    @Nested
    @DisplayName("getTeams()")
    class GetTeams {

        @Test
        @DisplayName("should call findAll and return all teams when status is null")
        void statusNull_shouldReturnAll() {
            // ARRANGE
            List<RescueTeam> mockTeams = List.of(
                    buildTeam(1L, "Team Alpha", TeamStatus.AVAILABLE),
                    buildTeam(2L, "Team Beta", TeamStatus.BUSY)
            );
            when(teamRepository.findAll()).thenReturn(mockTeams);

            // ACT
            List<RescueTeamResponse> responses = dispatchService.getTeams(null);

            // ASSERT
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getName()).isEqualTo("Team Alpha");
            assertThat(responses.get(1).getName()).isEqualTo("Team Beta");

            // VERIFY — need verify because there are two branches (findAll vs findByStatus)
            // and we need to call correct branch (status is null)
            verify(teamRepository).findAll();
            verify(teamRepository, never()).findByStatus(any());
        }

        @Test
        @DisplayName("should call findByStatus and return filtered teams when status is AVAILABLE")
        void statusAvailable_shouldReturnFiltered() {
            // ARRANGE
            List<RescueTeam> mockTeams = List.of(
                    buildTeam(1L, "Team Alpha", TeamStatus.AVAILABLE)
            );
            when(teamRepository.findByStatus(TeamStatus.AVAILABLE)).thenReturn(mockTeams);

            // ACT
            List<RescueTeamResponse> responses = dispatchService.getTeams(TeamStatus.AVAILABLE);

            // ASSERT
            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().getStatus()).isEqualTo(TeamStatus.AVAILABLE);

            // VERIFY — ensure that findAll is not called when the status is available
            verify(teamRepository).findByStatus(TeamStatus.AVAILABLE);
            verify(teamRepository, never()).findAll();
        }

        @Test
        @DisplayName("should return empty list when no teams match status")
        void noMatchingTeams_shouldReturnEmpty() {
            when(teamRepository.findByStatus(TeamStatus.BUSY)).thenReturn(List.of());

            List<RescueTeamResponse> responses = dispatchService.getTeams(TeamStatus.BUSY);

            assertThat(responses).isEmpty();
        }
    }

    // =====================================================================
    // assignTeam
    // =====================================================================

    @Nested
    @DisplayName("assignTeam()")
    class AssignTeam {

        // Use @BeforeEach-style setup via helper because tests in this group
        // need redis mock — avoid duplicate in each test
        private void mockRedisLockSuccess() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
        }

        @Test
        @DisplayName("should assign team, update status to BUSY, publish event and return response")
        void success() {
            // ARRANGE
            mockRedisLockSuccess();

            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.AVAILABLE);
            AssignTeamRequest request = AssignTeamRequest.builder()
                    .teamId(1L)
                    .requestId(100L)
                    .citizenId(50L)
                    .vehicleId(200L)
                    .build();

            when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
            when(assignmentRepository.existsByRequestIdAndStatus(100L, AssignmentStatus.ACTIVE))
                    .thenReturn(false);
            when(assignmentRepository.save(any(Assignment.class)))
                    .thenAnswer(invocation -> {
                        Assignment saved = invocation.getArgument(0);
                        saved.setId(1L); // simulate DB-generated ID
                        return saved;
                    });
            when(teamRepository.save(any(RescueTeam.class))).thenReturn(mockTeam);

            // ACT
            AssignmentResponse response = dispatchService.assignTeam(request, 10L);

            // ASSERT
            assertThat(response.getRequestId()).isEqualTo(100L);
            assertThat(response.getStatus()).isEqualTo(AssignmentStatus.ACTIVE);

            // VERIFY side effects — we must use "verify" because:
            // 1. Team must be updated the busy status
            // 2. Event must be published
            // 3. Redis lock must be released after finishing.
            ArgumentCaptor<RescueTeam> teamCaptor = ArgumentCaptor.forClass(RescueTeam.class);
            verify(teamRepository).save(teamCaptor.capture());
            assertThat(teamCaptor.getValue().getStatus()).isEqualTo(TeamStatus.BUSY);

            verify(eventPublisher).publishRequestAssigned(any(RescueRequestAssignedEvent.class));
            verify(redisTemplate).delete(anyString()); // lock phải được release
        }

        @Test
        @DisplayName("should throw TEAM_NOT_AVAILABLE when Redis lock cannot be acquired")
        void redisLockFailed_shouldThrow() {
            // ARRANGE — stimulate lock is being held by another request.
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(false);

            AssignTeamRequest request = AssignTeamRequest.builder().teamId(1L).requestId(100L).build();

            // ASSERT
            assertThatThrownBy(() -> dispatchService.assignTeam(request, 10L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_NOT_AVAILABLE);

            // VERIFY — ensure that there is no side effect occurs when lock fail.
            verify(teamRepository, never()).findById(any());
            verify(assignmentRepository, never()).save(any());
            verify(eventPublisher, never()).publishRequestAssigned(any());
        }

        @Test
        @DisplayName("should throw TEAM_NOT_FOUND when team does not exist")
        void teamNotFound_shouldThrow() {
            mockRedisLockSuccess();
            when(teamRepository.findById(99L)).thenReturn(Optional.empty());

            AssignTeamRequest request = AssignTeamRequest.builder().teamId(99L).requestId(100L).build();

            assertThatThrownBy(() -> dispatchService.assignTeam(request, 10L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_NOT_FOUND);

            // VERIFY — redis must be released, even when throwing (finally block).
            verify(redisTemplate).delete(anyString());
            verify(eventPublisher, never()).publishRequestAssigned(any());
        }

        @Test
        @DisplayName("should throw TEAM_NOT_AVAILABLE when team status is BUSY")
        void teamBusy_shouldThrow() {
            mockRedisLockSuccess();
            RescueTeam busyTeam = buildTeam(1L, "Team Alpha", TeamStatus.BUSY);
            when(teamRepository.findById(1L)).thenReturn(Optional.of(busyTeam));

            AssignTeamRequest request = AssignTeamRequest.builder().teamId(1L).requestId(100L).build();

            assertThatThrownBy(() -> dispatchService.assignTeam(request, 10L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_NOT_AVAILABLE);

            verify(redisTemplate).delete(anyString()); // lock must still be released.
        }

        @Test
        @DisplayName("should throw VALIDATION_ERROR when request is already assigned")
        void requestAlreadyAssigned_shouldThrow() {
            mockRedisLockSuccess();
            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.AVAILABLE);
            when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));
            when(assignmentRepository.existsByRequestIdAndStatus(100L, AssignmentStatus.ACTIVE))
                    .thenReturn(true); // had ACTIVE assignment for this request.

            AssignTeamRequest request = AssignTeamRequest.builder().teamId(1L).requestId(100L).build();

            assertThatThrownBy(() -> dispatchService.assignTeam(request, 10L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);

            verify(assignmentRepository, never()).save(any());
            verify(redisTemplate).delete(anyString());
        }
    }

    // =====================================================================
    // getAssignments
    // =====================================================================

    @Nested
    @DisplayName("getAssignments()")
    class GetAssignments {

        @Test
        @DisplayName("should return paged assignments")
        void success() {
            // ARRANGE
            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.AVAILABLE);
            Assignment mockAssignment = buildAssignment(1L, mockTeam, AssignmentStatus.ACTIVE);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Assignment> mockPage = new PageImpl<>(List.of(mockAssignment), pageable, 1);
            when(assignmentRepository.findAllWithTeam(pageable)).thenReturn(mockPage);

            // ACT
            Page<AssignmentResponse> responses = dispatchService.getAssignments(pageable);

            // ASSERT
            assertThat(responses.getTotalElements()).isEqualTo(1);
            assertThat(responses.getContent().getFirst().getId()).isEqualTo(1L);
            assertThat(responses.getContent().getFirst().getStatus()).isEqualTo(AssignmentStatus.ACTIVE);
        }

        @Test
        @DisplayName("should return empty page when no assignments exist")
        void empty() {
            Pageable pageable = PageRequest.of(0, 10);
            when(assignmentRepository.findAllWithTeam(pageable))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            Page<AssignmentResponse> responses = dispatchService.getAssignments(pageable);

            assertThat(responses.getTotalElements()).isZero();
            assertThat(responses.getContent()).isEmpty();
        }
    }

    // =====================================================================
    // getMyAssignments
    // =====================================================================

    @Nested
    @DisplayName("getMyAssignments()")
    class GetMyAssignments {

        @Test
        @DisplayName("should return assignments when user is team leader")
        void userIsLeader() {
            // ARRANGE
            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.BUSY);
            List<Assignment> mockAssignments = List.of(
                    buildAssignment(1L, mockTeam, AssignmentStatus.ACTIVE),
                    buildAssignment(2L, mockTeam, AssignmentStatus.ACTIVE)
            );
            when(teamRepository.findByLeaderId(10L)).thenReturn(Optional.of(mockTeam));
            when(assignmentRepository.findByTeamIdAndStatusOrderByAssignedAtDesc(1L, AssignmentStatus.ACTIVE))
                    .thenReturn(mockAssignments);

            // ACT
            List<AssignmentResponse> responses = dispatchService.getMyAssignments(10L);

            // ASSERT
            assertThat(responses).hasSize(2);
            assertThat(responses.getFirst().getStatus()).isEqualTo(AssignmentStatus.ACTIVE);
        }

        @Test
        @DisplayName("should return assignments when user is a team member (not leader)")
        void userIsMember() {
            // ARRANGE — user 99L is but a team leader, but a member.
            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.BUSY);
            List<Assignment> mockAssignments = List.of(buildAssignment(1L, mockTeam, AssignmentStatus.ACTIVE));

            when(teamRepository.findByLeaderId(99L)).thenReturn(Optional.empty()); // not leader
            when(teamRepository.findByMemberUserId(99L)).thenReturn(Optional.of(mockTeam)); // is member
            when(assignmentRepository.findByTeamIdAndStatusOrderByAssignedAtDesc(1L, AssignmentStatus.ACTIVE))
                    .thenReturn(mockAssignments);

            // ACT
            List<AssignmentResponse> responses = dispatchService.getMyAssignments(99L);

            // ASSERT
            assertThat(responses).hasSize(1);

            // VERIFY — ensure fallback associated with findByMemberUserId is called.
            verify(teamRepository).findByLeaderId(99L);
            verify(teamRepository).findByMemberUserId(99L);
        }

        @Test
        @DisplayName("should throw TEAM_NOT_FOUND when user belongs to no team")
        void userBelongsToNoTeam_shouldThrow() {
            when(teamRepository.findByLeaderId(99L)).thenReturn(Optional.empty());
            when(teamRepository.findByMemberUserId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dispatchService.getMyAssignments(99L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_NOT_FOUND);
        }
    }

    // =====================================================================
    // startAssignment
    // =====================================================================

    @Nested
    @DisplayName("startAssignment()")
    class StartAssignment {

        @Test
        @DisplayName("should set startedAt, save assignment and publish event when leader starts")
        void leaderStartsAssignment() {
            // ARRANGE
            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.BUSY);
            // leaderId = 10L (set in buildTeam)
            Assignment mockAssignment = buildAssignment(1L, mockTeam, AssignmentStatus.ACTIVE);

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(mockAssignment));
            when(assignmentRepository.save(any(Assignment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ACT
            AssignmentResponse response = dispatchService.startAssignment(1L, 10L); // userId = leaderId

            // ASSERT
            assertThat(response.getStatus()).isEqualTo(AssignmentStatus.ACTIVE);

            // VERIFY side effect — startedAt must be set and event must be published.
            ArgumentCaptor<Assignment> assignmentCaptor = ArgumentCaptor.forClass(Assignment.class);
            verify(assignmentRepository).save(assignmentCaptor.capture());
            assertThat(assignmentCaptor.getValue().getStartedAt()).isNotNull();

            verify(eventPublisher).publishRequestStarted(any(RescueRequestStartedEvent.class));
        }

        @Test
        @DisplayName("should throw ASSIGNMENT_NOT_FOUND when assignment does not exist")
        void assignmentNotFound() {
            when(assignmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dispatchService.startAssignment(99L, 10L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("should throw PERMISSION_DENIED when user is not in the team")
        void userNotInTeam_shouldThrow() {
            // ARRANGE — team has leaderId=10, members is empty. User 999 is not belong to the team.
            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.BUSY);
            Assignment mockAssignment = buildAssignment(1L, mockTeam, AssignmentStatus.ACTIVE);

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(mockAssignment));

            assertThatThrownBy(() -> dispatchService.startAssignment(1L, 999L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERMISSION_DENIED);

            verify(assignmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ASSIGNMENT_INVALID_STATUS when assignment is not ACTIVE")
        void assignmentNotActive_shouldThrow() {
            // ARRANGE — assignment has been COMPLETED, which cannot start again.
            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.AVAILABLE);
            Assignment mockAssignment = buildAssignment(1L, mockTeam, AssignmentStatus.COMPLETED);

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(mockAssignment));

            assertThatThrownBy(() -> dispatchService.startAssignment(1L, 10L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSIGNMENT_INVALID_STATUS);
        }
    }

    // =====================================================================
    // completeAssignment
    // =====================================================================

    @Nested
    @DisplayName("completeAssignment()")
    class CompleteAssignment {

        @Test
        @DisplayName("should set COMPLETED status, free team to AVAILABLE and publish event")
        void success() {
            // ARRANGE — assignment has started (startedAt != null).
            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.BUSY);
            Assignment mockAssignment = buildAssignment(1L, mockTeam, AssignmentStatus.ACTIVE);
            mockAssignment.setStartedAt(LocalDateTime.now().minusMinutes(30));

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(mockAssignment));
            when(assignmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(teamRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // ACT
            AssignmentResponse response = dispatchService.completeAssignment(1L, 10L, "Rescued successfully");

            // ASSERT
            assertThat(response.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
            assertThat(response.getResultNote()).isEqualTo("Rescued successfully");

            // VERIFY side effects — team must be available, event must be published.
            ArgumentCaptor<RescueTeam> teamCaptor = ArgumentCaptor.forClass(RescueTeam.class);
            verify(teamRepository).save(teamCaptor.capture());
            assertThat(teamCaptor.getValue().getStatus()).isEqualTo(TeamStatus.AVAILABLE);

            ArgumentCaptor<RescueRequestCompletedEvent> eventCaptor =
                    ArgumentCaptor.forClass(RescueRequestCompletedEvent.class);
            verify(eventPublisher).publishRequestCompleted(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getDurationMinutes()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should throw ASSIGNMENT_NOT_FOUND when assignment does not exist")
        void assignmentNotFound() {
            when(assignmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dispatchService.completeAssignment(99L, 10L, "note"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("should throw VALIDATION_ERROR when assignment has not been started yet")
        void notStartedYet_shouldThrow() {
            // ARRANGE — startedAt = null means that the assignment has not been started.
            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.BUSY);
            Assignment mockAssignment = buildAssignment(1L, mockTeam, AssignmentStatus.ACTIVE);
            // startedAt sets null (default).

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(mockAssignment));

            assertThatThrownBy(() -> dispatchService.completeAssignment(1L, 10L, "note"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);

            verify(assignmentRepository, never()).save(any());
            verify(teamRepository, never()).save(any());
        }
    }

    // =====================================================================
    // updateLocation
    // =====================================================================

    @Nested
    @DisplayName("updateLocation()")
    class UpdateLocation {

        @Test
        @DisplayName("should save location log, update team coords and publish event when leader updates")
        void leaderUpdatesLocation() {
            // ARRANGE
            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.BUSY);
            LocationUpdateRequest request = LocationUpdateRequest.builder()
                    .lat(BigDecimal.valueOf(10.762622))
                    .lng(BigDecimal.valueOf(106.660172))
                    .speed(BigDecimal.valueOf(30.0))
                    .heading(BigDecimal.valueOf(90.0))
                    .build();

            when(teamRepository.findByLeaderId(10L)).thenReturn(Optional.of(mockTeam));
            when(locationLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(teamRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // ACT
            dispatchService.updateLocation(request, 10L);

            // VERIFY — method void, and only verify can check.
            // 1. Log must be saved.
            verify(locationLogRepository).save(any(LocationLog.class));

            // 2. Team coords must be updated
            ArgumentCaptor<RescueTeam> teamCaptor = ArgumentCaptor.forClass(RescueTeam.class);
            verify(teamRepository).save(teamCaptor.capture());
            assertThat(teamCaptor.getValue().getCurrentLat())
                    .isEqualByComparingTo(BigDecimal.valueOf(10.762622));
            assertThat(teamCaptor.getValue().getCurrentLng())
                    .isEqualByComparingTo(BigDecimal.valueOf(106.660172));

            // 3. Event must be published.
            ArgumentCaptor<TeamLocationUpdatedEvent> eventCaptor =
                    ArgumentCaptor.forClass(TeamLocationUpdatedEvent.class);
            verify(eventPublisher).publishTeamLocationUpdated(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getTeamId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should update location when user is a team member (not leader)")
        void memberUpdatesLocation() {
            RescueTeam mockTeam = buildTeam(1L, "Team Alpha", TeamStatus.BUSY);
            LocationUpdateRequest request = LocationUpdateRequest.builder()
                    .lat(BigDecimal.valueOf(10.0)).lng(BigDecimal.valueOf(106.0)).speed(BigDecimal.valueOf(0.0)).heading(BigDecimal.valueOf(0.0)).build();

            when(teamRepository.findByLeaderId(99L)).thenReturn(Optional.empty());
            when(teamRepository.findByMemberUserId(99L)).thenReturn(Optional.of(mockTeam));
            when(locationLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(teamRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            dispatchService.updateLocation(request, 99L);

            verify(locationLogRepository).save(any());
            verify(eventPublisher).publishTeamLocationUpdated(any());
        }

        @Test
        @DisplayName("should throw TEAM_NOT_FOUND when user belongs to no team")
        void userNotInAnyTeam_shouldThrow() {
            when(teamRepository.findByLeaderId(99L)).thenReturn(Optional.empty());
            when(teamRepository.findByMemberUserId(99L)).thenReturn(Optional.empty());

            LocationUpdateRequest request = LocationUpdateRequest.builder()
                    .lat(BigDecimal.valueOf(10.0)).lng(BigDecimal.valueOf(106.0)).build();

            assertThatThrownBy(() -> dispatchService.updateLocation(request, 99L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_NOT_FOUND);

            verify(locationLogRepository, never()).save(any());
        }
    }

    // =====================================================================
    // getMapData
    // =====================================================================

    @Nested
    @DisplayName("getMapData()")
    class GetMapData {

        @Test
        @DisplayName("should return map data with location for teams that have location logs")
        void teamsWithLocation() {
            // ARRANGE
            RescueTeam team1 = buildTeam(1L, "Team Alpha", TeamStatus.AVAILABLE);
            RescueTeam team2 = buildTeam(2L, "Team Beta", TeamStatus.BUSY);

            LocationLog log = LocationLog.builder()
                    .team(team1)
                    .lat(BigDecimal.valueOf(10.762622))
                    .lng(BigDecimal.valueOf(106.660172))
                    .loggedAt(LocalDateTime.now())
                    .build();

            when(teamRepository.findAll()).thenReturn(List.of(team1, team2));
            // team1 has log, but team2 doesn't.
            when(locationLogRepository.findLatestByTeamIds(List.of(1L, 2L))).thenReturn(List.of(log));

            // ACT
            MapDataResponse response = dispatchService.getMapData();

            // ASSERT
            assertThat(response.getTeams()).hasSize(2);

            MapDataResponse.TeamLocationDto dto1 = response.getTeams().stream()
                    .filter(t -> t.getTeamId().equals(1L)).findFirst().orElseThrow();
            assertThat(dto1.getLat()).isEqualByComparingTo(BigDecimal.valueOf(10.762622));
            assertThat(dto1.getLng()).isEqualByComparingTo(BigDecimal.valueOf(106.660172));
            assertThat(dto1.getLastUpdated()).isNotNull();

            // team2 doesn't have log — lat/lng must be null.
            MapDataResponse.TeamLocationDto dto2 = response.getTeams().stream()
                    .filter(t -> t.getTeamId().equals(2L)).findFirst().orElseThrow();
            assertThat(dto2.getLat()).isNull();
            assertThat(dto2.getLng()).isNull();
        }

        @Test
        @DisplayName("should return empty teams list when no teams exist")
        void noTeams() {
            when(teamRepository.findAll()).thenReturn(List.of());
            when(locationLogRepository.findLatestByTeamIds(List.of())).thenReturn(List.of());

            MapDataResponse response = dispatchService.getMapData();

            assertThat(response.getTeams()).isEmpty();
        }
    }
}