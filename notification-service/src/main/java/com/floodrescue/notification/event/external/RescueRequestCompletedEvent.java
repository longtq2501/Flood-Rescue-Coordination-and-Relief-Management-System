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
public class RescueRequestCompletedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private Long assignmentId;
    private Long requestId;
    private Long citizenId;
    private LocalDateTime completedAt;
    private Long durationMinutes;
}
