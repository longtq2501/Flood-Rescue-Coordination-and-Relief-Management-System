package com.floodrescue.dispatch.dto.response;

import java.time.LocalDateTime;

import com.floodrescue.dispatch.domain.enums.AssignmentStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignmentResponse {
    private Long id;
    private Long requestId;
    private Long teamId;
    private String teamName;
    private Long vehicleId;
    private Long coordinatorId;
    private AssignmentStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String resultNote;
}
