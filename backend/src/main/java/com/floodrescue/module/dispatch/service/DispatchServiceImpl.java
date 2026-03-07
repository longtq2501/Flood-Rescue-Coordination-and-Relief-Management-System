package com.floodrescue.module.dispatch.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.floodrescue.module.dispatch.domain.entity.Assignment;
import com.floodrescue.module.dispatch.domain.entity.LocationLog;
import com.floodrescue.module.dispatch.domain.entity.RescueTeam;
import com.floodrescue.module.dispatch.domain.enums.AssignmentStatus;
import com.floodrescue.module.dispatch.domain.enums.TeamStatus;
import com.floodrescue.module.dispatch.dto.request.AssignTeamRequest;
import com.floodrescue.module.dispatch.dto.request.LocationUpdateRequest;
import com.floodrescue.module.dispatch.dto.response.AssignmentResponse;
import com.floodrescue.module.dispatch.dto.response.MapDataResponse;
import com.floodrescue.module.dispatch.dto.response.RescueTeamResponse;
import com.floodrescue.module.dispatch.dto.response.TeamMemberResponse;
import com.floodrescue.module.dispatch.event.DispatchEventPublisher;
import com.floodrescue.module.dispatch.event.RescueRequestAssignedEvent;
import com.floodrescue.module.dispatch.event.RescueRequestCompletedEvent;
import com.floodrescue.module.dispatch.event.TeamLocationUpdatedEvent;
import com.floodrescue.module.dispatch.repository.AssignmentRepository;
import com.floodrescue.shared.exception.AppException;
import com.floodrescue.shared.exception.ErrorCode;
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
                List<RescueTeam> teams = (status == null) ? teamRepository.findAll()
                                : teamRepository.findByStatus(status);
                return teams.stream()
                                .map(team -> RescueTeamResponse.builder()
                                                .id(team.getId())
                                                .name(team.getName())
                                                .leaderId(team.getLeaderId())
                                                .capacity(team.getCapacity())
                                                .status(team.getStatus())
                                                .currentLat(team.getCurrentLat())
                                                .currentLng(team.getCurrentLng())
                                                .createdAt(team.getCreatedAt())
                                                .build())
                                .collect(Collectors.toList());
        }

        @Override
        public RescueTeamResponse getTeamById(Long teamId) {
                RescueTeam team = teamRepository.findByIdWithMembers(teamId)
                                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));
                return toTeamResponse(team);
        }

        @Override
        @Transactional
        public AssignmentResponse assignTeam(AssignTeamRequest request, Long coordinatorId) {
                String lockKey = TEAM_LOCK_PREFIX + request.getTeamId();
                Boolean locked = redisTemplate.opsForValue()
                                .setIfAbsent(lockKey, "LOCKED", LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                if (!Boolean.TRUE.equals(locked)) {
                        throw new AppException(ErrorCode.TEAM_UNAVAILABLE);
                }

                try {
                        RescueTeam team = teamRepository.findById(request.getTeamId())
                                        .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));
                        if (team.getStatus() != TeamStatus.AVAILABLE) {
                                throw new AppException(ErrorCode.TEAM_UNAVAILABLE);
                        }

                        if (assignmentRepository.existsByRequestIdAndStatus(request.getRequestId(), AssignmentStatus.ACTIVE)) {
                                throw new AppException(ErrorCode.VALIDATION_ERROR, "Request đã được assign");
                        }

                        Assignment assignment = Assignment.builder()
                                        .requestId(request.getRequestId())
                                        .citizenId(request.getCitizenId())
                                        .team(team)
                                        .vehicleId(request.getVehicleId())
                                        .coordinatorId(coordinatorId)
                                        .status(AssignmentStatus.ACTIVE)
                                        .build();
                        assignment = assignmentRepository.save(assignment);

                        team.setStatus(TeamStatus.BUSY);
                        teamRepository.save(team);

                        RescueRequestAssignedEvent event = RescueRequestAssignedEvent.builder()
                                        .requestId(assignment.getRequestId())
                                        .teamId(team.getId())
                                        .teamName(team.getName())
                                        .vehicleId(assignment.getVehicleId())
                                        .coordinatorId(coordinatorId)
                                        .citizenId(assignment.getCitizenId())
                                        .build();
                        eventPublisher.publishRequestAssigned(event);

                        return toAssignmentResponse(assignment);
                } finally {
                        redisTemplate.delete(lockKey);
                }
        }

        @Override
        public Page<AssignmentResponse> getAssignments(Pageable pageable) {
                return assignmentRepository.findAllByOrderByAssignedAtDesc(pageable)
                                .map(this::toAssignmentResponse);
        }

        @Override
        public List<AssignmentResponse> getMyAssignments(Long userId) {
                RescueTeam team = teamRepository.findByLeaderId(userId)
                                .orElseGet(() -> teamRepository.findByMemberUserId(userId)
                                                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND)));

                List<Assignment> assignments = assignmentRepository.findByTeamIdAndStatusOrderByAssignedAtDesc(team.getId(), AssignmentStatus.ACTIVE);
                return assignments.stream()
                                .map(this::toAssignmentResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public AssignmentResponse startAssignment(Long assignmentId, Long userId) {
                Assignment assignment = assignmentRepository.findById(assignmentId)
                                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

                RescueTeam team = assignment.getTeam();
                boolean isLeader = team.getLeaderId().equals(userId);
                boolean isMember = team.getMembers().stream().anyMatch(m -> m.getUserId().equals(userId));
                if (!isLeader && !isMember) {
                        throw new AppException(ErrorCode.VALIDATION_ERROR, "User không thuộc team thực hiện nhiệm vụ");
                }

                if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
                        throw new AppException(ErrorCode.ASSIGNMENT_INVALID_STATUS);
                }

                assignment.setStartedAt(LocalDateTime.now());
                assignment = assignmentRepository.save(assignment);

                return toAssignmentResponse(assignment);
        }

        @Override
        @Transactional
        public AssignmentResponse completeAssignment(Long assignmentId, Long userId, String resultNote) {
                Assignment assignment = assignmentRepository.findById(assignmentId)
                                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

                if (assignment.getStartedAt() == null) {
                        throw new AppException(ErrorCode.VALIDATION_ERROR, "Nhiệm vụ chưa bắt đầu");
                }

                assignment.setStatus(AssignmentStatus.COMPLETED);
                assignment.setCompletedAt(LocalDateTime.now());
                assignment.setResultNote(resultNote);
                assignment = assignmentRepository.save(assignment);

                RescueTeam team = assignment.getTeam();
                team.setStatus(TeamStatus.AVAILABLE);
                teamRepository.save(team);

                int durationMinutes = 0;
                if (assignment.getStartedAt() != null && assignment.getCompletedAt() != null) {
                        durationMinutes = (int) Duration
                                        .between(assignment.getStartedAt(), assignment.getCompletedAt()).toMinutes();
                }

                RescueRequestCompletedEvent event = RescueRequestCompletedEvent.builder()
                                .requestId(assignment.getRequestId())
                                .teamId(team.getId())
                                .citizenId(assignment.getCitizenId())
                                .completedAt(assignment.getCompletedAt())
                                .durationMinutes(durationMinutes)
                                .result(resultNote)
                                .build();
                eventPublisher.publishRequestCompleted(event);

                return toAssignmentResponse(assignment);
        }

        @Override
        @Transactional
        public void updateLocation(LocationUpdateRequest request, Long userId) {
                RescueTeam team = teamRepository.findByLeaderId(userId)
                                .orElseGet(() -> teamRepository.findByMemberUserId(userId)
                                                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND)));

                LocationLog logEntry = LocationLog.builder()
                                .team(team)
                                .lat(request.getLat())
                                .lng(request.getLng())
                                .speed(request.getSpeed())
                                .heading(request.getHeading())
                                .build();
                locationLogRepository.save(logEntry);

                team.setCurrentLat(request.getLat());
                team.setCurrentLng(request.getLng());
                teamRepository.save(team);

                TeamLocationUpdatedEvent event = TeamLocationUpdatedEvent.builder()
                                .teamId(team.getId())
                                .lat(request.getLat())
                                .lng(request.getLng())
                                .speed(request.getSpeed())
                                .heading(request.getHeading())
                                .build();
                eventPublisher.publishTeamLocationUpdated(event);
        }

        @Override
        public MapDataResponse getMapData() {
                List<RescueTeam> teams = teamRepository.findAll();
                List<MapDataResponse.TeamLocationDto> dtoList = teams.stream()
                                .map(team -> {
                                        MapDataResponse.TeamLocationDto.TeamLocationDtoBuilder builder = MapDataResponse.TeamLocationDto
                                                        .builder()
                                                        .teamId(team.getId())
                                                        .teamName(team.getName())
                                                        .status(team.getStatus().name());

                                        locationLogRepository.findLatestByTeamId(team.getId()).ifPresent(log -> {
                                                builder.lat(log.getLat())
                                                                .lng(log.getLng())
                                                                .lastUpdated(log.getLoggedAt());
                                        });

                                        return builder.build();
                                })
                                .collect(Collectors.toList());

                return MapDataResponse.builder()
                                .teams(dtoList)
                                .build();
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