package com.floodrescue.report.scheduler;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.floodrescue.report.service.ReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EtlScheduler {

    private final ReportService reportService;

    /**
     * Chạy lúc 1:00 AM mỗi ngày
     * "Close" snapshot của ngày hôm qua
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void runDailyEtl() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("ETL started for date: {}", yesterday);
        try {
            reportService.runDailyEtl(yesterday);
            log.info("ETL completed for date: {}", yesterday);
        } catch (Exception e) {
            log.error("ETL failed for date: {}", yesterday, e);
        }
    }
}
