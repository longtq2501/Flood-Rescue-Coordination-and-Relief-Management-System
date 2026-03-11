package com.floodrescue.notification.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.floodrescue.notification.domain.entity.NotificationEvent;
import com.floodrescue.notification.dto.response.NotificationResponse;
import com.floodrescue.notification.repository.NotificationEventRepository;
import com.floodrescue.notification.service.SseService;
import com.floodrescue.notification.shared.response.ApiResponse;
import com.floodrescue.notification.shared.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseService sseService;
    private final NotificationEventRepository notificationEventRepository;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal UserPrincipal principal) {
        return sseService.subscribe(principal.getId(), principal.getRole());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        Page<NotificationEvent> events = notificationEventRepository
                .findByTargetUserIdOrderByCreatedAtDesc(principal.getId(), pageable);

        Page<NotificationResponse> responsePage = events.map(e -> NotificationResponse.builder()
                .id(e.getId())
                .eventType(e.getEventType())
                .channel(e.getChannel())
                .payload(e.getPayload())
                .status(e.getStatus())
                .sentAt(e.getSentAt())
                .createdAt(e.getCreatedAt())
                .build());

        return ResponseEntity.ok(ApiResponse.success(responsePage, "Lấy lịch sử thông báo thành công"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Notification Service is UP", "OK"));
    }
}
