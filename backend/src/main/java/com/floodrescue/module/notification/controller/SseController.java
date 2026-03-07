package com.floodrescue.module.notification.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.floodrescue.module.notification.dto.response.NotificationResponse;
import com.floodrescue.module.notification.repository.NotificationEventRepository;
import com.floodrescue.module.notification.service.SseService;
import com.floodrescue.shared.response.ApiResponse;
import com.floodrescue.shared.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class SseController {

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
        Page<com.floodrescue.module.notification.domain.entity.NotificationEvent> events = notificationEventRepository
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

        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử thông báo thành công", responsePage));
    }
}