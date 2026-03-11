package com.floodrescue.request.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.floodrescue.request.domain.enums.RequestStatus;
import com.floodrescue.request.event.RescueRequestAssignedEvent;
import com.floodrescue.request.event.RescueRequestStartedEvent;
import com.floodrescue.request.event.RescueRequestCompletedEvent;
import com.floodrescue.request.service.RescueRequestService;
import com.floodrescue.request.shared.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RescueRequestEventListener {

    private final RescueRequestService rescueRequestService;

    @RabbitListener(queues = RabbitMQConfig.Q_REQUEST_SYNC_ASSIGNED)
    public void handleRequestAssigned(RescueRequestAssignedEvent event) {
        log.info("RescueRequest module received assigned event for requestId={}", event.getRequestId());
        try {
            rescueRequestService.syncStatus(
                    event.getRequestId(),
                    RequestStatus.ASSIGNED,
                    "Đã phân công đội cứu hộ: " + event.getTeamName(),
                    event.getCoordinatorId());
        } catch (Exception e) {
            log.error("Failed to sync assigned status for requestId={}", event.getRequestId(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_REQUEST_SYNC_STARTED)
    public void handleRequestStarted(RescueRequestStartedEvent event) {
        log.info("RescueRequest module received started event for requestId={}", event.getRequestId());
        try {
            rescueRequestService.syncStatus(
                    event.getRequestId(),
                    RequestStatus.IN_PROGRESS,
                    "Đội cứu hộ đang thực hiện nhiệm vụ",
                    event.getOperatorId());
        } catch (Exception e) {
            log.error("Failed to sync in_progress status for requestId={}", event.getRequestId(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_REQUEST_SYNC_COMPLETED)
    public void handleRequestCompleted(RescueRequestCompletedEvent event) {
        log.info("RescueRequest module received completed event for requestId={}", event.getRequestId());
        try {
            rescueRequestService.syncStatus(
                    event.getRequestId(),
                    RequestStatus.COMPLETED,
                    "Đội cứu hộ hoàn thành nhiệm vụ: " + event.getResult(),
                    event.getOperatorId()
            );
        } catch (Exception e) {
            log.error("Failed to sync completed status for requestId={}", event.getRequestId(), e);
            throw e;
        }
    }
}
