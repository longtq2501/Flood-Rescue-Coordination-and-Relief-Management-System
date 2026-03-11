package com.floodrescue.resource.event;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.floodrescue.resource.shared.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceEventPublisher {

        private final RabbitTemplate rabbitTemplate;

        public void publishStockLow(ResourceStockLowEvent event) {
                event.setEventId(UUID.randomUUID().toString());
                event.setEventType(RabbitMQConfig.RK_RESOURCE_LOW);
                event.setTimestamp(LocalDateTime.now());
                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.EXCHANGE,
                                RabbitMQConfig.RK_RESOURCE_LOW,
                                event);
                log.warn("Published rescue.resource.stock.low: item={}, qty={}",
                                event.getItemName(), event.getCurrentQuantity());
        }

        public void publishDistributed(ResourceDistributedEvent event) {
                event.setEventId(UUID.randomUUID().toString());
                event.setEventType(RabbitMQConfig.RK_RESOURCE_DIST);
                event.setTimestamp(LocalDateTime.now());
                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.EXCHANGE,
                                RabbitMQConfig.RK_RESOURCE_DIST,
                                event);
                log.info("Published rescue.resource.distributed: distributionId={}",
                                event.getDistributionId());
        }
}
