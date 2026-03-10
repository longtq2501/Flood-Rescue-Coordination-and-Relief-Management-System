package com.floodrescue.request.event;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RescueRequestStatusUpdatedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private Long requestId;
    private Long citizenId;
    private String fromStatus;
    private String toStatus;
    private Long changedBy;
    private String note;
}
