package com.floodrescue.notification.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.floodrescue.notification.dto.response.SseEvent;

public interface SseService {
    SseEmitter subscribe(Long userId, String role);
    void sendToUser(Long userId, SseEvent event);
    void sendToRole(String role, SseEvent event);
    void sendToAll(SseEvent event);
    void removeEmitter(Long userId);
}
