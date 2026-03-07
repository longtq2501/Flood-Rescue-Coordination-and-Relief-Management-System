package com.floodrescue.module.notification.listener;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.floodrescue.module.dispatch.event.RescueRequestAssignedEvent;
import com.floodrescue.module.dispatch.event.RescueRequestCompletedEvent;
import com.floodrescue.module.notification.dto.response.SseEvent;
import com.floodrescue.module.notification.service.SseService;
import com.floodrescue.module.rescue_request.event.RescueRequestCreatedEvent;
import com.floodrescue.module.resource.event.ResourceStockLowEvent;
import com.floodrescue.module.notification.event.SystemBroadcastEvent;
import com.floodrescue.shared.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final SseService sseService;

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_REQUEST_CREATED)
    public void handleRequestCreated(RescueRequestCreatedEvent event) {
        log.info("Received rescue.request.created: requestId={}", event.getRequestId());
        try {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("requestId", event.getRequestId());
            payload.put("citizenName", event.getCitizenName());
            payload.put("urgencyLevel", event.getUrgencyLevel());
            payload.put("lat", event.getLat());
            payload.put("lng", event.getLng());
            payload.put("numPeople", event.getNumPeople());
            payload.put("description", event.getDescription());
            payload.put("message", "Có yêu cầu cứu hộ mới cần xử lý");

            // Gửi SSE đến tất cả COORDINATOR đang online
            sseService.sendToRole("COORDINATOR", SseEvent.builder()
                    .eventType("new.request.alert")
                    .payload(payload)
                    .build());
        } catch (Exception e) {
            log.error("Failed handleRequestCreated: requestId={}", event.getRequestId(), e);
            throw e; // RabbitMQ retry → sau 3 lần vào DLQ
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_REQUEST_ASSIGNED)
    public void handleRequestAssigned(RescueRequestAssignedEvent event) {
        log.info("Received rescue.request.assigned: requestId={}", event.getRequestId());
        try {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("requestId", event.getRequestId());
            payload.put("citizenId", event.getCitizenId());
            payload.put("teamName", event.getTeamName());
            payload.put("estimatedArrival", event.getEstimatedArrival());
            payload.put("message", "Đội cứu hộ đã được phân công và đang trên đường đến");

            sseService.sendToUser(event.getCitizenId(), SseEvent.builder()
                    .eventType("request.assigned")
                    .payload(payload)
                    .build());
        } catch (Exception e) {
            log.error("Failed handleRequestAssigned", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_REQUEST_COMPLETED)
    public void handleRequestCompleted(RescueRequestCompletedEvent event) {
        log.info("Received rescue.request.completed: requestId={}", event.getRequestId());
        try {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("requestId", event.getRequestId());
            payload.put("completedAt", event.getCompletedAt());
            payload.put("durationMinutes", event.getDurationMinutes());
            payload.put("message", "Đội cứu hộ đã hoàn thành, vui lòng xác nhận");

            sseService.sendToUser(event.getCitizenId(), SseEvent.builder()
                    .eventType("request.completed")
                    .payload(payload)
                    .build());
        } catch (Exception e) {
            log.error("Failed handleRequestCompleted", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_RESOURCE_LOW)
    public void handleResourceLow(ResourceStockLowEvent event) {
        log.info("Received rescue.resource.stock.low: item={}", event.getItemName());
        try {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("itemName", event.getItemName());
            payload.put("warehouseName", event.getWarehouseName());
            payload.put("unit", event.getUnit());
            payload.put("currentQuantity", event.getCurrentQuantity());
            payload.put("threshold", event.getThreshold());
            payload.put("message", String.format("Cảnh báo: Tồn kho %s tại %s đang ở mức thấp", event.getItemName(),
                    event.getWarehouseName()));

            sseService.sendToRole("MANAGER", SseEvent.builder()
                    .eventType("resource.low.alert")
                    .payload(payload)
                    .build());
        } catch (Exception e) {
            log.error("Failed handleResourceLow", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_BROADCAST)
    public void handleBroadcast(SystemBroadcastEvent event) {
        log.info("Received system broadcast: {}", event.getMessage());
        try {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("message", event.getMessage());
            payload.put("level", event.getLevel());

            sseService.sendToAll(SseEvent.builder()
                    .eventType("system.broadcast")
                    .payload(payload)
                    .build());
        } catch (Exception e) {
            log.error("Failed handleBroadcast", e);
            throw e;
        }
    }
}