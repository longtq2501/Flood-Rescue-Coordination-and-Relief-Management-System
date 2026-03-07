package com.floodrescue.module.notification.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.floodrescue.module.notification.dto.response.SseEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    private final ObjectMapper objectMapper;

    // userId → SseEmitter (thread-safe)
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // userId → role (để sendToRole biết gửi cho ai)
    private final Map<Long, String> userRoles = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 phút

    @Override
    public SseEmitter subscribe(Long userId, String role) {
        // Nếu user đã có connection cũ → đóng lại
        SseEmitter existing = emitters.get(userId);
        if (existing != null) {
            existing.complete();
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(userId, emitter);
        userRoles.put(userId, role);

        // Cleanup khi connection đóng
        emitter.onCompletion(() -> removeEmitter(userId));
        emitter.onTimeout(() -> removeEmitter(userId));
        emitter.onError(e -> removeEmitter(userId));

        // Gửi event "connected" để client biết SSE đã sẵn sàng
        try {
            emitter.send(SseEmitter.event()
                    .id(UUID.randomUUID().toString())
                    .name("connected")
                    .data("{\"message\":\"SSE connected\",\"userId\":" + userId + "}"));
        } catch (IOException e) {
            log.error("Failed to send connected event to userId={}", userId);
            removeEmitter(userId);
        }

        log.info("SSE subscribed: userId={}, role={}, total={}", userId, role, emitters.size());
        return emitter;
    }

    @Override
    public void sendToUser(Long userId, SseEvent event) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.debug("No SSE connection for userId={}", userId);
            return;
        }
        doSend(userId, emitter, event);
    }

    @Override
    public void sendToRole(String role, SseEvent event) {
        userRoles.entrySet().stream()
                .filter(entry -> role.equals(entry.getValue()))
                .forEach(entry -> sendToUser(entry.getKey(), event));
    }

    @Override
    public void sendToAll(SseEvent event) {
        emitters.keySet().forEach(userId -> sendToUser(userId, event));
    }

    @Override
    public void removeEmitter(Long userId) {
        emitters.remove(userId);
        userRoles.remove(userId);
        log.info("SSE disconnected: userId={}, remaining={}", userId, emitters.size());
    }

    // ==================== PRIVATE ====================

    private void doSend(Long userId, SseEmitter emitter, SseEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .id(event.getId() != null ? event.getId() : UUID.randomUUID().toString())
                    .name(event.getEventType())
                    .data(objectMapper.writeValueAsString(event.getPayload())));
            log.debug("SSE sent to userId={}: eventType={}", userId, event.getEventType());
        } catch (IOException e) {
            log.warn("SSE send failed to userId={}, removing emitter", userId);
            removeEmitter(userId);
        }
    }
}