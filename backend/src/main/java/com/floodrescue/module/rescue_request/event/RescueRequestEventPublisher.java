package com.floodrescue.module.rescue_request.event;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.floodrescue.shared.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RescueRequestEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * TODO Cường: gọi method này trong RescueRequestServiceImpl.create()
     * ngay sau khi lưu RescueRequest vào DB thành công
     */
    public void publishRequestCreated(RescueRequestCreatedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(RabbitMQConfig.RK_REQUEST_CREATED);
        event.setTimestamp(LocalDateTime.now());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RK_REQUEST_CREATED,
                event);
        log.info("Published rescue.request.created: requestId={}", event.getRequestId());
    }

    /**
     * TODO Cường: gọi method này trong mọi method đổi status
     * (verify, cancel, confirm)
     */
    public void publishStatusUpdated(RescueRequestStatusUpdatedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(RabbitMQConfig.RK_REQUEST_STATUS);
        event.setTimestamp(LocalDateTime.now());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RK_REQUEST_STATUS,
                event);
        log.info("Published rescue.request.status.updated: requestId={}", event.getRequestId());
    }
}