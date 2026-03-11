package com.floodrescue.dispatch.event;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RescueRequestCompletedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private Long requestId;
    private Long teamId;
    private Long citizenId;
    private Long operatorId;
    private LocalDateTime completedAt;
    private Integer durationMinutes;
    private String result;
    private String notes;
}
