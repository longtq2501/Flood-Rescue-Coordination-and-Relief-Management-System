package com.floodrescue.dispatch.controller;

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

import com.floodrescue.dispatch.domain.enums.TeamStatus;
import com.floodrescue.dispatch.dto.request.AssignTeamRequest;
import com.floodrescue.dispatch.dto.request.LocationUpdateRequest;
import com.floodrescue.dispatch.dto.response.AssignmentResponse;
import com.floodrescue.dispatch.dto.response.MapDataResponse;
import com.floodrescue.dispatch.dto.response.RescueTeamResponse;
import com.floodrescue.dispatch.service.DispatchService;
import com.floodrescue.dispatch.shared.response.ApiResponse;
import com.floodrescue.dispatch.shared.security.UserPrincipal;

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
                ApiResponse.success(dispatchService.getTeams(status), "OK"));
    }

    @GetMapping("/teams/{id}")
    public ResponseEntity<ApiResponse<RescueTeamResponse>> getTeamById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(dispatchService.getTeamById(id), "OK"));
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assignTeam(
            @Valid @RequestBody AssignTeamRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success(dispatchService.assignTeam(request, principal.getId()), "Assign thành công"));
    }

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<Page<AssignmentResponse>>> getAssignments(
            Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.success(dispatchService.getAssignments(pageable), "OK"));
    }

    @GetMapping("/assignments/my")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getMyAssignments(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success(dispatchService.getMyAssignments(principal.getId()), "OK"));
    }

    @PatchMapping("/assignments/{id}/start")
    public ResponseEntity<ApiResponse<AssignmentResponse>> startAssignment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success(dispatchService.startAssignment(id, principal.getId()), "Bắt đầu nhiệm vụ"));
    }

    @PatchMapping("/assignments/{id}/complete")
    public ResponseEntity<ApiResponse<AssignmentResponse>> completeAssignment(
            @PathVariable Long id,
            @RequestParam(required = false) String resultNote,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success(dispatchService.completeAssignment(id, principal.getId(), resultNote), "Hoàn thành nhiệm vụ"));
    }

    @PostMapping("/location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @Valid @RequestBody LocationUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        dispatchService.updateLocation(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("OK")
                .build());
    }

    @GetMapping("/map")
    public ResponseEntity<ApiResponse<MapDataResponse>> getMapData() {
        return ResponseEntity.ok(
                ApiResponse.success(dispatchService.getMapData(), "OK"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Dispatch Service is UP", "OK"));
    }
}
