package com.floodrescue.dispatch.event;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.floodrescue.dispatch.shared.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishRequestAssigned(RescueRequestAssignedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(RabbitMQConfig.RK_REQUEST_ASSIGNED);
        event.setTimestamp(LocalDateTime.now());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RK_REQUEST_ASSIGNED,
                event);
        log.info("Published rescue.request.assigned: requestId={}", event.getRequestId());
    }

    public void publishRequestCompleted(RescueRequestCompletedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(RabbitMQConfig.RK_REQUEST_COMPLETED);
        event.setTimestamp(LocalDateTime.now());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RK_REQUEST_COMPLETED,
                event);
        log.info("Published rescue.request.completed: requestId={}", event.getRequestId());
    }

    public void publishTeamLocationUpdated(TeamLocationUpdatedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(RabbitMQConfig.RK_TEAM_LOCATION);
        event.setTimestamp(LocalDateTime.now());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RK_TEAM_LOCATION,
                event);
    }
}
