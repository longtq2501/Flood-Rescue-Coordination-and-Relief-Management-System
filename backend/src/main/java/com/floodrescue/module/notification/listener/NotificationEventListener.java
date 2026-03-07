package com.floodrescue.module.notification.listener;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.floodrescue.module.dispatch.event.RescueRequestAssignedEvent;
import com.floodrescue.module.dispatch.event.RescueRequestCompletedEvent;
import com.floodrescue.module.notification.dto.response.SseEvent;
import com.floodrescue.module.notification.service.SseService;
import com.floodrescue.module.rescue_request.event.RescueRequestCreatedEvent;
import com.floodrescue.shared.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final SseService sseService;

    // ================================================================
    // TODO Quý Mạnh: implement từng handler bên dưới
    // Mỗi handler:
    // 1. Log nhận được event
    // 2. Build SseEvent với đúng eventType + payload
    // 3. Gọi sseService.sendToUser() hoặc sendToRole()
    // 4. Wrap trong try-catch, throw exception nếu lỗi (RabbitMQ sẽ retry)
    // ================================================================

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_REQUEST_CREATED)
    public void handleRequestCreated(RescueRequestCreatedEvent event) {
        log.info("Received rescue.request.created: requestId={}", event.getRequestId());
        try {
            // Gửi SSE đến tất cả COORDINATOR đang online
            sseService.sendToRole("COORDINATOR", SseEvent.builder()
                    .eventType("new.request.alert")
                    .payload(Map.of(
                            "requestId", event.getRequestId(),
                            "urgencyLevel", event.getUrgencyLevel(),
                            "lat", event.getLat(),
                            "lng", event.getLng(),
                            "numPeople", event.getNumPeople(),
                            "description", event.getDescription(),
                            "message", "Có yêu cầu cứu hộ mới cần xử lý"))
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
            // TODO Quý Mạnh: gửi SSE đến CITIZEN có citizenId = event.getCitizenId()
            // Payload cần có: requestId, teamName, estimatedArrival, message
            throw new UnsupportedOperationException("TODO: Quý Mạnh implement");
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed handleRequestAssigned", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_REQUEST_COMPLETED)
    public void handleRequestCompleted(RescueRequestCompletedEvent event) {
        log.info("Received rescue.request.completed: requestId={}", event.getRequestId());
        try {
            // TODO Quý Mạnh: gửi SSE đến CITIZEN
            // Payload: requestId, message "Đội cứu hộ đã hoàn thành, vui lòng xác nhận"
            throw new UnsupportedOperationException("TODO: Quý Mạnh implement");
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed handleRequestCompleted", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_RESOURCE_LOW)
    public void handleResourceLow(
            com.floodrescue.module.resource.event.ResourceStockLowEvent event) {
        log.info("Received rescue.resource.stock.low: item={}", event.getItemName());
        try {
            // TODO Quý Mạnh: gửi SSE đến tất cả MANAGER
            // Payload: itemName, currentQuantity, threshold, message "Cảnh báo tồn kho
            // thấp"
            throw new UnsupportedOperationException("TODO: Quý Mạnh implement");
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed handleResourceLow", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIF_BROADCAST)
    public void handleBroadcast(
            com.floodrescue.module.notification.event.SystemBroadcastEvent event) {
        log.info("Received system broadcast: {}", event.getMessage());
        try {
            // TODO Quý Mạnh: gửi SSE đến TẤT CẢ user đang online
            // Payload: message, level (INFO/WARNING/CRITICAL)
            throw new UnsupportedOperationException("TODO: Quý Mạnh implement");
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed handleBroadcast", e);
            throw e;
        }
    }
}