package com.floodrescue.module.dispatch.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.floodrescue.module.dispatch.domain.entity.Assignment;
import com.floodrescue.module.dispatch.domain.entity.RescueTeam;
import com.floodrescue.module.dispatch.domain.enums.TeamStatus;
import com.floodrescue.module.dispatch.dto.request.AssignTeamRequest;
import com.floodrescue.module.dispatch.dto.request.LocationUpdateRequest;
import com.floodrescue.module.dispatch.dto.response.AssignmentResponse;
import com.floodrescue.module.dispatch.dto.response.MapDataResponse;
import com.floodrescue.module.dispatch.dto.response.RescueTeamResponse;
import com.floodrescue.module.dispatch.dto.response.TeamMemberResponse;
import com.floodrescue.module.dispatch.event.DispatchEventPublisher;
import com.floodrescue.module.dispatch.repository.AssignmentRepository;
import com.floodrescue.module.dispatch.repository.LocationLogRepository;
import com.floodrescue.module.dispatch.repository.RescueTeamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchServiceImpl implements DispatchService {

    private final RescueTeamRepository teamRepository;
    private final AssignmentRepository assignmentRepository;
    private final LocationLogRepository locationLogRepository;
    private final DispatchEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TEAM_LOCK_PREFIX = "lock:team:";
    private static final long LOCK_TIMEOUT_SECONDS = 30;

    @Override
    public List<RescueTeamResponse> getTeams(TeamStatus status) {
        // TODO Tuấn Anh: implement
        // Gợi ý: status == null → findAll(), có status → findByStatus(status)
        // map sang RescueTeamResponse dùng toTeamResponse()
        throw new UnsupportedOperationException("TODO: Tuấn Anh implement");
    }

    @Override
    public RescueTeamResponse getTeamById(Long teamId) {
        // TODO Tuấn Anh: implement
        // Gợi ý: teamRepository.findByIdWithMembers(teamId)
        // .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))
        throw new UnsupportedOperationException("TODO: Tuấn Anh implement");
    }

    @Override
    @Transactional
    public AssignmentResponse assignTeam(AssignTeamRequest request, Long coordinatorId) {
        // TODO Tuấn Anh: implement với Redis distributed lock
        //
        // Step 1: Kiểm tra team AVAILABLE
        // RescueTeam team = teamRepository.findById(request.getTeamId())
        // .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        // if (team.getStatus() != TeamStatus.AVAILABLE)
        // throw new AppException(ErrorCode.TEAM_UNAVAILABLE);
        //
        // Step 2: Kiểm tra request chưa có ACTIVE assignment
        // if (assignmentRepository.existsByRequestIdAndStatus(request.getRequestId(),
        // AssignmentStatus.ACTIVE))
        // throw new AppException(ErrorCode.VALIDATION_ERROR, "Request đã được assign");
        //
        // Step 3: Acquire Redis lock
        // String lockKey = TEAM_LOCK_PREFIX + request.getTeamId();
        // Boolean locked = redisTemplate.opsForValue()
        // .setIfAbsent(lockKey, "LOCKED", LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        // if (!Boolean.TRUE.equals(locked))
        // throw new AppException(ErrorCode.TEAM_UNAVAILABLE);
        //
        // Step 4: Tạo Assignment, lưu DB
        // Step 5: Đổi team.status = BUSY, lưu DB
        // Step 6: Release lock: redisTemplate.delete(lockKey)
        // Step 7: Publish event: eventPublisher.publishRequestAssigned(...)
        // Step 8: Trả về toAssignmentResponse(assignment)
        throw new UnsupportedOperationException("TODO: Tuấn Anh implement");
    }

    @Override
    public Page<AssignmentResponse> getAssignments(Pageable pageable) {
        // TODO Tuấn Anh: implement
        // assignmentRepository.findAllByOrderByAssignedAtDesc(pageable)
        // .map(this::toAssignmentResponse)
        throw new UnsupportedOperationException("TODO: Tuấn Anh implement");
    }

    @Override
    public List<AssignmentResponse> getMyAssignments(Long userId) {
        // TODO Tuấn Anh: implement
        // Step 1: Tìm team của userId (là leader hoặc member)
        // teamRepository.findByLeaderId(userId)
        // hoặc teamRepository.findByMemberUserId(userId)
        // Step 2: Lấy assignments của team đó
        throw new UnsupportedOperationException("TODO: Tuấn Anh implement");
    }

    @Override
    @Transactional
    public AssignmentResponse startAssignment(Long assignmentId, Long userId) {
        // TODO Tuấn Anh: implement
        // Step 1: Tìm assignment, kiểm tra status == ACTIVE
        // Step 2: Set startedAt = LocalDateTime.now()
        // Step 3: Lưu DB, trả về toAssignmentResponse()
        throw new UnsupportedOperationException("TODO: Tuấn Anh implement");
    }

    @Override
    @Transactional
    public AssignmentResponse completeAssignment(Long assignmentId, Long userId, String resultNote) {
        // TODO Tuấn Anh: implement
        // Step 1: Tìm assignment
        // Step 2: Set status = COMPLETED, completedAt = now(), resultNote
        // Step 3: Đổi team.status = AVAILABLE
        // Step 4: Tính durationMinutes = giữa assignedAt và completedAt
        // Step 5: Publish: eventPublisher.publishRequestCompleted(...)
        // (cần citizenId — Tuấn Anh hỏi Long nếu cần query sang db_request)
        // Step 6: Trả về toAssignmentResponse()
        throw new UnsupportedOperationException("TODO: Tuấn Anh implement");
    }

    @Override
    @Transactional
    public void updateLocation(LocationUpdateRequest request, Long userId) {
        // TODO Tuấn Anh: implement
        // Step 1: Tìm team của userId
        // teamRepository.findByLeaderId(userId) hoặc findByMemberUserId(userId)
        // Step 2: Lưu LocationLog mới
        // Step 3: Update team.currentLat + currentLng
        // Step 4: Publish: eventPublisher.publishTeamLocationUpdated(...)
        throw new UnsupportedOperationException("TODO: Tuấn Anh implement");
    }

    @Override
    public MapDataResponse getMapData() {
        // TODO Tuấn Anh: implement
        // Step 1: Lấy tất cả teams
        // Step 2: Với mỗi team lấy latest location từ
        // locationLogRepository.findLatestByTeamId()
        // Step 3: Map sang MapDataResponse.TeamLocationDto
        throw new UnsupportedOperationException("TODO: Tuấn Anh implement");
    }

    // ==================== PRIVATE HELPERS ====================

    private RescueTeamResponse toTeamResponse(RescueTeam team) {
        return RescueTeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .leaderId(team.getLeaderId())
                .capacity(team.getCapacity())
                .status(team.getStatus())
                .currentLat(team.getCurrentLat())
                .currentLng(team.getCurrentLng())
                .createdAt(team.getCreatedAt())
                .members(team.getMembers().stream()
                        .map(m -> TeamMemberResponse.builder()
                                .userId(m.getUserId())
                                .joinedAt(m.getJoinedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private AssignmentResponse toAssignmentResponse(Assignment a) {
        return AssignmentResponse.builder()
                .id(a.getId())
                .requestId(a.getRequestId())
                .teamId(a.getTeam().getId())
                .teamName(a.getTeam().getName())
                .vehicleId(a.getVehicleId())
                .coordinatorId(a.getCoordinatorId())
                .status(a.getStatus())
                .assignedAt(a.getAssignedAt())
                .startedAt(a.getStartedAt())
                .completedAt(a.getCompletedAt())
                .resultNote(a.getResultNote())
                .build();
    }
}