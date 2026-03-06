package com.floodrescue.module.resource.event;

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
public class ResourceEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * TODO Tiến: gọi trong DistributionServiceImpl.create()
     * sau mỗi lần distribute, kiểm tra từng item có below threshold không
     * nếu có → gọi method này cho từng item đó
     */
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

    /**
     * TODO Tiến: gọi trong DistributionServiceImpl.create()
     * sau khi lưu Distribution vào DB thành công
     */
    public void publishDistributed(ResourceDistributedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(RabbitMQConfig.RK_RESOURCE_DISTRIBUTED);
        event.setTimestamp(LocalDateTime.now());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RK_RESOURCE_DISTRIBUTED,
                event);
        log.info("Published rescue.resource.distributed: distributionId={}",
                event.getDistributionId());
    }
}