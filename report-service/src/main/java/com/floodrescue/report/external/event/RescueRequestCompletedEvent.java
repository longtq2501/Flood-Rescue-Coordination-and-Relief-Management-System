package com.floodrescue.report.external.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescueRequestCompletedEvent {
    private Long requestId;
    private Long teamId;
    private Integer durationMinutes;
}
