package com.floodrescue.report.service;

import java.time.LocalDate;

import com.floodrescue.report.dto.response.DashboardResponse;

public interface ReportService {

    /** GET /api/reports/dashboard — tổng quan cho MANAGER/ADMIN */
    DashboardResponse getDashboard();

    /**
     * Được gọi bởi EtlScheduler mỗi ngày lúc 1:00 AM
     * Tổng hợp data của ngày hôm qua vào snapshot tables
     */
    void runDailyEtl(LocalDate date);

    /**
     * Được gọi bởi ReportEventListener khi nhận event request.completed
     * Cập nhật real-time snapshot của ngày hiện tại
     */
    void updateRequestSnapshot(LocalDate date, Long teamId, Integer durationMinutes);

    /**
     * Được gọi bởi ReportEventListener khi nhận event resource.distributed
     * Cập nhật real-time snapshot của ngày hiện tại
     */
    void updateResourceSnapshot(LocalDate date, Long warehouseId);
}
