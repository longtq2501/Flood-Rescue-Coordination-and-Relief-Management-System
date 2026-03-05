package com.floodrescue.module.notification.dto.response;

import java.time.LocalDateTime;

import com.floodrescue.module.notification.domain.enums.NotificationChannel;
import com.floodrescue.module.notification.domain.enums.NotificationEventType;
import com.floodrescue.module.notification.domain.enums.NotificationStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationEventType eventType;
    private NotificationChannel channel;
    private String payload;
    private NotificationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}