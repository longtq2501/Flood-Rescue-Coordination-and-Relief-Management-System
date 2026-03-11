package com.floodrescue.notification.dto.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseEvent {
    private String eventType; // ví dụ: "new.request.alert"
    private Map<String, Object> payload;
    private String id; // Last-Event-ID để client replay nếu mất kết nối
}
