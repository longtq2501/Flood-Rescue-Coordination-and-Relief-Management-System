package com.floodrescue.notification.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.floodrescue.notification.dto.response.SseEvent;
import com.floodrescue.notification.service.SseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    private final ObjectMapper objectMapper;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, String> userRoles = new ConcurrentHashMap<>();

    @Value("${app.sse.timeout-ms:1800000}")
    private long sseTimeout;

    @Override
    public SseEmitter subscribe(Long userId, String role) {
        SseEmitter emitter = new SseEmitter(sseTimeout);

        SseEmitter oldEmitter = emitters.put(userId, emitter);
        userRoles.put(userId, role);

        if (oldEmitter != null) {
            oldEmitter.complete();
        }

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .id(UUID.randomUUID().toString())
                    .name("connected")
                    .data("{\"message\":\"SSE connected\",\"userId\":" + userId + "}"));
        } catch (java.io.IOException e) {
            log.error("Failed to send connected event to userId={}", userId);
            removeEmitter(userId, emitter);
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
        List<Long> targetUsers = userRoles.entrySet().stream()
                .filter(entry -> role.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        for (Long userId : targetUsers) {
            sendToUser(userId, event);
        }
    }

    @Override
    public void sendToAll(SseEvent event) {
        new ArrayList<>(emitters.keySet()).forEach(userId -> sendToUser(userId, event));
    }

    public void removeEmitter(Long userId, SseEmitter emitter) {
        if (emitters.remove(userId, emitter)) {
            if (!emitters.containsKey(userId)) {
                userRoles.remove(userId);
            }
            log.info("SSE disconnected: userId={}, remaining={}", userId, emitters.size());
        }
    }

    @Override
    public void removeEmitter(Long userId) {
        emitters.remove(userId);
        userRoles.remove(userId);
        log.info("SSE disconnected by userId: {}, remaining={}", userId, emitters.size());
    }

    private void doSend(Long userId, SseEmitter emitter, SseEvent event) {
        try {
            String data = objectMapper.writeValueAsString(event.getPayload());
            emitter.send(SseEmitter.event()
                    .id(event.getId() != null ? event.getId() : UUID.randomUUID().toString())
                    .name(event.getEventType())
                    .data(data));
            log.debug("SSE sent to userId={}: eventType={}", userId, event.getEventType());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to serialize SSE payload for userId={}", userId, e);
        } catch (java.io.IOException e) {
            log.warn("SSE send failed to userId={}, removing emitter", userId);
            removeEmitter(userId, emitter);
        }
    }
}
