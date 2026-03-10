// DUPLICATED FROM: dispatch-service/.../RescueRequestAssignedEvent.java
// IF YOU CHANGE THIS, ALSO UPDATE: dispatch-service
// ROUTING KEY: rescue.request.assigned — DO NOT CHANGE
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
