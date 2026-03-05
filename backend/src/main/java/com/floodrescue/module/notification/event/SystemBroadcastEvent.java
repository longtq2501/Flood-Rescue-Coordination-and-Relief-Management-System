package com.floodrescue.module.notification.event;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemBroadcastEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String message;
    private String level; // INFO | WARNING | CRITICAL
}