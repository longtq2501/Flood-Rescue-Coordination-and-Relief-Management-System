package com.floodrescue.module.report.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.floodrescue.module.report.dto.response.DashboardResponse;
import com.floodrescue.module.report.repository.DailyRequestSnapshotRepository;
import com.floodrescue.module.report.repository.DailyResourceSnapshotRepository;
import com.floodrescue.module.report.repository.TeamPerformanceSnapshotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final DailyRequestSnapshotRepository requestSnapshotRepo;
    private final DailyResourceSnapshotRepository resourceSnapshotRepo;
    private final TeamPerformanceSnapshotRepository teamPerformanceRepo;

    @Override
    public DashboardResponse getDashboard() {
        // TODO Tiến: implement
        //
        // Step 1: Lấy snapshot hôm nay (LocalDate.now())
        // requestSnapshotRepo.findBySnapshotDate(LocalDate.now())
        // → nếu không có → trả về số 0 hết
        //
        // Step 2: Lấy 30 ngày gần nhất cho chart
        // requestSnapshotRepo.findTop30ByOrderBySnapshotDateDesc()
        //
        // Step 3: Lấy top team 7 ngày gần nhất
        // teamPerformanceRepo.findBySnapshotDateBetween(...)
        //
        // Step 4: Map sang DashboardResponse
        throw new UnsupportedOperationException("TODO: Tiến implement");
    }

    @Override
    @Transactional
    public void runDailyEtl(LocalDate date) {
        // TODO Tiến: implement (chạy lúc 1:00 AM qua EtlScheduler)
        //
        // Gợi ý: ETL đơn giản — kiểm tra snapshot ngày hôm qua đã tồn tại chưa
        // Nếu chưa có → tạo mới với dữ liệu đã được cập nhật real-time trong ngày
        // (data đã được cập nhật qua updateRequestSnapshot() và
        // updateResourceSnapshot())
        // ETL ở đây chủ yếu để đảm bảo snapshot được "close" cuối ngày
        log.info("Running daily ETL for date: {}", date);
        throw new UnsupportedOperationException("TODO: Tiến implement");
    }

    @Override
    @Transactional
    public void updateRequestSnapshot(LocalDate date, Long teamId, Integer durationMinutes) {
        // TODO Tiến: implement
        //
        // Step 1: Tìm hoặc tạo mới snapshot cho ngày hôm nay
        // DailyRequestSnapshot snapshot = requestSnapshotRepo
        // .findBySnapshotDate(date)
        // .orElse(DailyRequestSnapshot.builder().snapshotDate(date).build());
        //
        // Step 2: Tăng completedCount += 1
        //
        // Step 3: Tính lại avgCompleteMin (running average)
        // BigDecimal newAvg = tính trung bình cộng dồn
        //
        // Step 4: Cập nhật TeamPerformanceSnapshot cho teamId
        //
        // Step 5: Lưu DB
        throw new UnsupportedOperationException("TODO: Tiến implement");
    }

    @Override
    @Transactional
    public void updateResourceSnapshot(LocalDate date, Long warehouseId) {
        // TODO Tiến: implement
        //
        // Step 1: Tìm hoặc tạo mới snapshot cho (date, warehouseId)
        // Step 2: Tăng totalDistributions += 1
        // Step 3: Lưu DB
        throw new UnsupportedOperationException("TODO: Tiến implement");
    }
}