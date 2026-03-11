package com.floodrescue.notification.event.external;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DUPLICATED CLASS - TECHNICAL DEBT
 * Source of Truth: request-service
 * Purpose: Local DTO for AMQP message consumption.
 * Syncing: Must be updated manually when the source class changes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescueRequestCreatedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private Long requestId;
    private Long citizenId;
    private String urgencyLevel;
    private String status;
    private String contactPhone;
    private String address;
    private Double lat;
    private Double lng;
    private Integer numPeople;
    private String description;
}
