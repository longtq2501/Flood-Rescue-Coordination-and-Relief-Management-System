package com.floodrescue.notification.event.external;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DUPLICATED CLASS - TECHNICAL DEBT
 * Source of Truth: dispatch-service
 * Purpose: Local DTO for AMQP message consumption.
 * Syncing: Must be updated manually when the source class changes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescueRequestAssignedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private Long assignmentId;
    private Long requestId;
    private Long teamId;
    private String teamName;
    private Long citizenId;
    private LocalDateTime estimatedArrival;
}
