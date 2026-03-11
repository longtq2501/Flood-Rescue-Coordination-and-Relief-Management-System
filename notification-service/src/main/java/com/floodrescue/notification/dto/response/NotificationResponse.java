package com.floodrescue.notification.dto.response;

import java.time.LocalDateTime;

import com.floodrescue.notification.domain.enums.NotificationChannel;
import com.floodrescue.notification.domain.enums.NotificationEventType;
import com.floodrescue.notification.domain.enums.NotificationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private NotificationEventType eventType;
    private NotificationChannel channel;
    private String payload;
    private NotificationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
