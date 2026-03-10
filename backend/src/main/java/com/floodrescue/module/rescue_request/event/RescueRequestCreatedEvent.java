package com.floodrescue.module.rescue_request.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RescueRequestCreatedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private Long requestId;
    private Long citizenId;
    private BigDecimal lat;
    private BigDecimal lng;
    private String urgencyLevel;
    private String description;
    private Integer numPeople;
}