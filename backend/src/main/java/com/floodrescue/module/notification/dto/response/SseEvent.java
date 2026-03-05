package com.floodrescue.module.notification.dto.response;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SseEvent {
    private String eventType; // ví dụ: "new.request.alert"
    private Map<String, Object> payload;
    private String id; // Last-Event-ID để client replay nếu mất kết nối
}