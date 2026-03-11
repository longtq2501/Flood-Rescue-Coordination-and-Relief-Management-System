package com.floodrescue.dispatch.event;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RescueRequestAssignedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private Long requestId;
    private Long teamId;
    private String teamName;
    private Long vehicleId;
    private Long coordinatorId;
    private Long citizenId;
    private LocalDateTime estimatedArrival;
}
