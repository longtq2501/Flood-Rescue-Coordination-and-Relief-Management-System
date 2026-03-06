package com.floodrescue.module.dispatch.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.floodrescue.module.dispatch.domain.enums.TeamStatus;
import com.floodrescue.module.dispatch.dto.request.AssignTeamRequest;
import com.floodrescue.module.dispatch.dto.request.LocationUpdateRequest;
import com.floodrescue.module.dispatch.dto.response.AssignmentResponse;
import com.floodrescue.module.dispatch.dto.response.MapDataResponse;
import com.floodrescue.module.dispatch.dto.response.RescueTeamResponse;
import com.floodrescue.module.dispatch.service.DispatchService;
import com.floodrescue.shared.response.ApiResponse;
import com.floodrescue.shared.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    @GetMapping("/teams")
    public ResponseEntity<ApiResponse<List<RescueTeamResponse>>> getTeams(
            @RequestParam(required = false) TeamStatus status) {
        return ResponseEntity.ok(
                ApiResponse.success("OK", dispatchService.getTeams(status)));
    }

    @GetMapping("/teams/{id}")
    public ResponseEntity<ApiResponse<RescueTeamResponse>> getTeamById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("OK", dispatchService.getTeamById(id)));
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assignTeam(
            @Valid @RequestBody AssignTeamRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success("Assign thành công",
                        dispatchService.assignTeam(request, principal.getId())));
    }

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<Page<AssignmentResponse>>> getAssignments(
            Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.success("OK", dispatchService.getAssignments(pageable)));
    }

    @GetMapping("/assignments/my")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getMyAssignments(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success("OK", dispatchService.getMyAssignments(principal.getId())));
    }

    @PatchMapping("/assignments/{id}/start")
    public ResponseEntity<ApiResponse<AssignmentResponse>> startAssignment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success("Bắt đầu nhiệm vụ",
                        dispatchService.startAssignment(id, principal.getId())));
    }

    @PatchMapping("/assignments/{id}/complete")
    public ResponseEntity<ApiResponse<AssignmentResponse>> completeAssignment(
            @PathVariable Long id,
            @RequestParam(required = false) String resultNote,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success("Hoàn thành nhiệm vụ",
                        dispatchService.completeAssignment(id, principal.getId(), resultNote)));
    }

    @PostMapping("/location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @Valid @RequestBody LocationUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        dispatchService.updateLocation(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("OK", null));
    }

    @GetMapping("/map")
    public ResponseEntity<ApiResponse<MapDataResponse>> getMapData() {
        return ResponseEntity.ok(
                ApiResponse.success("OK", dispatchService.getMapData()));
    }
}