// DUPLICATED FROM: dispatch-service/.../RescueRequestCompletedEvent.java
// IF YOU CHANGE THIS, ALSO UPDATE: dispatch-service, backend (report module)
// ROUTING KEY: rescue.request.completed — DO NOT CHANGE
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
    private LocalDateTime completedAt;
    private Integer durationMinutes;
    private String result;
    private String notes;
}
