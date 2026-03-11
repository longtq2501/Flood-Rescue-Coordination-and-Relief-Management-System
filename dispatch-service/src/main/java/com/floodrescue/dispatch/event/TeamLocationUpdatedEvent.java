package com.floodrescue.dispatch.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamLocationUpdatedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private Long teamId;
    private BigDecimal lat;
    private BigDecimal lng;
    private BigDecimal speed;
    private BigDecimal heading;
}
