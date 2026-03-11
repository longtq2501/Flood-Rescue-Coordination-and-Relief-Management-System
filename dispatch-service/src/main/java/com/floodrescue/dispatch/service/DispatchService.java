package com.floodrescue.dispatch.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.floodrescue.dispatch.domain.enums.TeamStatus;
import com.floodrescue.dispatch.dto.request.AssignTeamRequest;
import com.floodrescue.dispatch.dto.request.LocationUpdateRequest;
import com.floodrescue.dispatch.dto.response.AssignmentResponse;
import com.floodrescue.dispatch.dto.response.MapDataResponse;
import com.floodrescue.dispatch.dto.response.RescueTeamResponse;

public interface DispatchService {

    List<RescueTeamResponse> getTeams(TeamStatus status);

    RescueTeamResponse getTeamById(Long teamId);

    AssignmentResponse assignTeam(AssignTeamRequest request, Long coordinatorId);

    Page<AssignmentResponse> getAssignments(Pageable pageable);

    List<AssignmentResponse> getMyAssignments(Long userId);

    AssignmentResponse startAssignment(Long assignmentId, Long userId);

    AssignmentResponse completeAssignment(Long assignmentId, Long userId, String resultNote);

    void updateLocation(LocationUpdateRequest request, Long userId);

    MapDataResponse getMapData();
}
