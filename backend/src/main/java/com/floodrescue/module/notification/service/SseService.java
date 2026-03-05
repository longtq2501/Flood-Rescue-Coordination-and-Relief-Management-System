package com.floodrescue.module.notification.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.floodrescue.module.notification.dto.response.SseEvent;

public interface SseService {

    /**
     * Client gọi GET /api/notifications/sse để tạo SSE connection
     * Server trả về SseEmitter — giữ kết nối mở cho đến khi timeout hoặc disconnect
     */
    SseEmitter subscribe(Long userId, String role);

    /**
     * Gửi event đến 1 user cụ thể (ví dụ: citizen nhận thông báo request được
     * assign)
     */
    void sendToUser(Long userId, SseEvent event);

    /**
     * Gửi event đến tất cả user có role cụ thể đang online
     * (ví dụ: coordinator nhận alert có request mới)
     */
    void sendToRole(String role, SseEvent event);

    /**
     * Gửi event đến tất cả user đang kết nối SSE
     * (ví dụ: system broadcast)
     */
    void sendToAll(SseEvent event);

    /**
     * Dọn dẹp connection đã timeout hoặc disconnect
     */
    void removeEmitter(Long userId);
}