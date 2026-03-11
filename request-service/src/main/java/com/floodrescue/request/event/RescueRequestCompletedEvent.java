/**
 * DUPLICATED CLASS - TECHNICAL DEBT
 * Source of Truth: dispatch-service
 * Purpose: Local DTO for AMQP message consumption.
 * Syncing: Must be updated manually when the source class changes.
 */
package com.floodrescue.request.event;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
