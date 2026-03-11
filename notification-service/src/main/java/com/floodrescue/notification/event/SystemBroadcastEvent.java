package com.floodrescue.notification.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemBroadcastEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String message;
    private String level; // INFO | WARNING | CRITICAL
}
