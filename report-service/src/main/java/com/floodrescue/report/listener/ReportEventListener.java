package com.floodrescue.report.listener;

import java.time.LocalDate;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.floodrescue.report.external.event.RescueRequestCompletedEvent;
import com.floodrescue.report.external.event.ResourceDistributedEvent;
import com.floodrescue.report.service.ReportService;
import com.floodrescue.report.shared.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportEventListener {

    private final ReportService reportService;

    @RabbitListener(queues = RabbitMQConfig.Q_REPORT_COMPLETED)
    public void handleRequestCompleted(RescueRequestCompletedEvent event) {
        log.info("Report received request.completed: requestId={}", event.getRequestId());
        try {
            reportService.updateRequestSnapshot(
                    LocalDate.now(),
                    event.getTeamId(),
                    event.getDurationMinutes());
        } catch (Exception e) {
            log.error("Failed to update request snapshot", e);
            throw e; // retry → DLQ
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_REPORT_DISTRIBUTED)
    public void handleResourceDistributed(ResourceDistributedEvent event) {
        log.info("Report received resource.distributed: distributionId={}",
                event.getDistributionId());
        try {
            reportService.updateResourceSnapshot(
                    LocalDate.now(),
                    event.getWarehouseId());
        } catch (Exception e) {
            log.error("Failed to update resource snapshot", e);
            throw e;
        }
    }
}
