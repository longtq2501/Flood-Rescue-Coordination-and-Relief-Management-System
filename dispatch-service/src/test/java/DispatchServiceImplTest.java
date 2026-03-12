import com.floodrescue.dispatch.domain.entity.Assignment;
import com.floodrescue.dispatch.domain.entity.RescueTeam;
import com.floodrescue.dispatch.domain.enums.AssignmentStatus;
import com.floodrescue.dispatch.domain.enums.TeamStatus;
import com.floodrescue.dispatch.dto.response.AssignmentResponse;
import com.floodrescue.dispatch.dto.response.RescueTeamResponse;
import com.floodrescue.dispatch.event.DispatchEventPublisher;
import com.floodrescue.dispatch.repository.AssignmentRepository;
import com.floodrescue.dispatch.repository.LocationLogRepository;
import com.floodrescue.dispatch.repository.RescueTeamRepository;
import com.floodrescue.dispatch.service.DispatchServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispatchServiceImplTest {

    //Mock all dependencies - no need for Spring Context, and Database
    @Mock private RescueTeamRepository teamRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private LocationLogRepository locationLogRepository;
    @Mock private DispatchEventPublisher eventPublisher;
    @Mock private RedisTemplate<String, Object> redisTemplate;

    //Inject all mocks into service
    @InjectMocks private DispatchServiceImpl dispatchService;


    // ========================== getTeamById ================================

    @Test
    void getTeamById_shouldReturnTeamResponse_whenTeamExists() {
        // ARRANGE - prepare fake data
        RescueTeam mockTeam = RescueTeam.builder()
                .id(1L)
                .name("Team Alpha")
                .leaderId(10L)
                .capacity(5)
                .status(TeamStatus.AVAILABLE)
                .members(List.of())
                .build();

        // when repository.findByIdWithMemebers(1L) is called -> return mockTeam
        when(teamRepository.findByIdWithMembers(1L))
                .thenReturn(Optional.of(mockTeam));

        // ACT - call method which needs test
        RescueTeamResponse response = dispatchService.getTeamById(1L);

        // ASSERT - checking result
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Team Alpha");
        assertThat(response.getStatus()).isEqualTo(TeamStatus.AVAILABLE);

        // Verify repository which is called for one time with correct argument
        verify(teamRepository, times(1)).findByIdWithMembers(1L);
    }

    // ========================== getTeams ================================

    @Test
    void getTeams_shouldReturnAllTeams_whenStatusIsNull() {
        // ARRANGE
        List<RescueTeam> mockTeams = List.of(
                RescueTeam.builder().id(1L).name("Team Alpha").leaderId(10L)
                        .capacity(5).status(TeamStatus.AVAILABLE).members(List.of()).build(),
                RescueTeam.builder().id(2L).name("Team Beta").leaderId(20L)
                        .capacity(3).status(TeamStatus.BUSY).members(List.of()).build()
        );
        when(teamRepository.findAll()).thenReturn(mockTeams);

        // ACT
        List<RescueTeamResponse> responses = dispatchService.getTeams(null);

        // ASSERT
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Team Alpha");
        assertThat(responses.get(1).getName()).isEqualTo("Team Beta");

        // VERIFY
        verify(teamRepository, times(1)).findAll();
        verify(teamRepository, never()).findByStatus(any());
    }

    @Test
    void getTeams_shouldReturnAllTeams_whenStatusIsAvailable() {
        // ARRANGE
        List<RescueTeam> mockTeams = List.of(
                RescueTeam.builder().id(1L).name("Team Alpha").leaderId(10L)
                        .capacity(5).status(TeamStatus.AVAILABLE).members(List.of()).build()
        );
        when(teamRepository.findByStatus(TeamStatus.AVAILABLE)).thenReturn(mockTeams);

        // ACT
        List<RescueTeamResponse> responses = dispatchService.getTeams(TeamStatus.AVAILABLE);

        // ASSERT
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getName()).isEqualTo("Team Alpha");
        assertThat(responses.getFirst().getStatus()).isEqualTo(TeamStatus.AVAILABLE);

        // VERIFY
        verify(teamRepository, times(1)).findByStatus(TeamStatus.AVAILABLE);
    }

    // ========================== getAssignments ================================

    @Test
    void getAssignments_shouldReturnPagedAssignments() {
        // ARRANGE
        RescueTeam mockTeam = RescueTeam.builder()
                .id(1L)
                .name("Team Alpha")
                .build();
        Assignment mockAssignment = Assignment.builder()
                .id(1L)
                .requestId(100L)
                .team(mockTeam)
                .vehicleId(200L)
                .coordinatorId(10L)
                .status(AssignmentStatus.ACTIVE)
                .build();

        Pageable pageable = PageRequest.of(0, 10); // page 0, size 10
        Page<Assignment> mockPage = new PageImpl<>(
                List.of(mockAssignment),
                pageable,
                1);

        when(assignmentRepository.findAllWithTeam(pageable)).thenReturn(mockPage);

        // ACT
        Page<AssignmentResponse> responses = dispatchService.getAssignments(pageable);

        // ASSERT
        assertThat(responses.getTotalElements()).isEqualTo(1);
        assertThat(responses.getContent().getFirst().getId()).isEqualTo(1L);
        assertThat(responses.getContent().getFirst().getStatus()).isEqualTo(AssignmentStatus.ACTIVE);

        // VERIFY
        verify(assignmentRepository, times(1)).findAllWithTeam(pageable);
    }

    // ========================== getMyAssignments ================================
    @Test
    void getMyAssignments_shouldReturnAllAssignments_whenHasUserId() {
        // ARRANGE
        RescueTeam mockTeam = RescueTeam.builder()
                .id(1L)
                .name("Team Alpha")
                .build();

        List<Assignment> mockAssignments = List.of(
                Assignment.builder()
                        .id(1L)
                        .requestId(100L)
                        .team(mockTeam)
                        .vehicleId(200L)
                        .coordinatorId(10L)
                        .status(AssignmentStatus.ACTIVE)
                        .build(),
                Assignment.builder()
                        .id(2L)
                        .requestId(150L)
                        .team(mockTeam)
                        .vehicleId(300L)
                        .coordinatorId(11L)
                        .status(AssignmentStatus.ACTIVE)
                        .build()
        );

        when(teamRepository.findByLeaderId(10L)).thenReturn(Optional.of(mockTeam));
        when(assignmentRepository.findByTeamIdAndStatusOrderByAssignedAtDesc(
                mockTeam.getId(),
                mockAssignments.getFirst().getStatus())).thenReturn(mockAssignments);

        // ACT
        List<AssignmentResponse> responses = dispatchService.getMyAssignments(10L);

        // ASSERT
        assertThat(responses).hasSize(2);
        assertThat(responses.getFirst().getStatus()).isEqualTo(AssignmentStatus.ACTIVE);

        //VERIFY
        verify(teamRepository, times(1)).findByLeaderId(10L);
        verify(assignmentRepository, times(1))
                .findByTeamIdAndStatusOrderByAssignedAtDesc(1L, AssignmentStatus.ACTIVE);
    }




}
