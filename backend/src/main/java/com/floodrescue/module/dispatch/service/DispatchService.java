package com.floodrescue.module.dispatch.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.floodrescue.module.dispatch.domain.enums.TeamStatus;
import com.floodrescue.module.dispatch.dto.request.AssignTeamRequest;
import com.floodrescue.module.dispatch.dto.request.LocationUpdateRequest;
import com.floodrescue.module.dispatch.dto.response.AssignmentResponse;
import com.floodrescue.module.dispatch.dto.response.MapDataResponse;
import com.floodrescue.module.dispatch.dto.response.RescueTeamResponse;

public interface DispatchService {

    List<RescueTeamResponse> getTeams(TeamStatus status);

    RescueTeamResponse getTeamById(Long teamId);

    /**
     * Dùng Redis distributed lock để tránh race condition
     * 2 coordinator cùng assign 1 team cùng lúc
     */
    AssignmentResponse assignTeam(AssignTeamRequest request, Long coordinatorId);

    Page<AssignmentResponse> getAssignments(Pageable pageable);

    List<AssignmentResponse> getMyAssignments(Long userId);

    AssignmentResponse startAssignment(Long assignmentId, Long userId);

    AssignmentResponse completeAssignment(Long assignmentId, Long userId, String resultNote);

    void updateLocation(LocationUpdateRequest request, Long userId);

    MapDataResponse getMapData();
}