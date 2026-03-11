package com.floodrescue.report.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.floodrescue.report.external.enums.TeamStatus;
import com.floodrescue.report.external.repository.RescueTeamRepository;
import com.floodrescue.report.domain.entity.DailyRequestSnapshot;
import com.floodrescue.report.domain.entity.DailyResourceSnapshot;
import com.floodrescue.report.domain.entity.TeamPerformanceSnapshot;
import com.floodrescue.report.dto.response.DashboardResponse;
import com.floodrescue.report.dto.response.DashboardResponse.DailyRequestChartDto;
import com.floodrescue.report.dto.response.DashboardResponse.SummaryToday;
import com.floodrescue.report.dto.response.DashboardResponse.TeamRankingDto;
import com.floodrescue.report.repository.DailyRequestSnapshotRepository;
import com.floodrescue.report.repository.DailyResourceSnapshotRepository;
import com.floodrescue.report.repository.TeamPerformanceSnapshotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final DailyRequestSnapshotRepository requestSnapshotRepo;
    private final DailyResourceSnapshotRepository resourceSnapshotRepo;
    private final TeamPerformanceSnapshotRepository teamPerformanceRepo;
    // Removed RescueTeamRepository - will use API call to dispatch-service

    // ─── getDashboard ────────────────────────────────────────────────────────────
    @Override
    @Transactional(value = "reportTransactionManager", readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();

        // Step 1: Tổng quan hôm nay
        SummaryToday summaryToday = buildSummaryToday(today);

        // Step 2: Chart 30 ngày
        List<DailyRequestChartDto> requestChart = buildRequestChart();

        // Step 3: Top team 7 ngày gần nhất
        List<TeamRankingDto> teamRanking = buildTeamRanking(today);

        return DashboardResponse.builder()
                .today(summaryToday)
                .requestChart(requestChart)
                .teamRanking(teamRanking)
                .build();
    }

    // ─── runDailyEtl ─────────────────────────────────────────────────────────────
    @Override
    @Transactional("reportTransactionManager")
    public void runDailyEtl(LocalDate date) {
        log.info("Running daily ETL for date: {}", date);

        // Đảm bảo snapshot ngày đó tồn tại (close ngày)
        requestSnapshotRepo.findBySnapshotDate(date)
                .orElseGet(() -> {
                    log.info("No request snapshot found for {}, creating empty one", date);
                    return requestSnapshotRepo.save(
                            DailyRequestSnapshot.builder()
                                    .snapshotDate(date)
                                    .build());
                });

        log.info("Daily ETL completed for date: {}", date);
    }

    // ─── updateRequestSnapshot ───────────────────────────────────────────────────
    @Override
    @Transactional("reportTransactionManager")
    public void updateRequestSnapshot(LocalDate date, Long teamId, Integer durationMinutes) {
        // Step 1: Tìm hoặc tạo mới snapshot cho ngày
        DailyRequestSnapshot snapshot = requestSnapshotRepo.findBySnapshotDate(date)
                .orElseGet(() -> DailyRequestSnapshot.builder()
                        .snapshotDate(date)
                        .build());

        // Step 2: Tăng completedCount
        int oldCount = snapshot.getCompletedCount();
        int newCount = oldCount + 1;
        snapshot.setCompletedCount(newCount);

        // Step 3: Tính lại avgCompleteMin (running average)
        if (durationMinutes != null) {
            BigDecimal oldAvg = snapshot.getAvgCompleteMin() != null
                    ? snapshot.getAvgCompleteMin()
                    : BigDecimal.ZERO;
            BigDecimal newAvg = oldAvg
                    .multiply(BigDecimal.valueOf(oldCount))
                    .add(BigDecimal.valueOf(durationMinutes))
                    .divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);
            snapshot.setAvgCompleteMin(newAvg);
        }

        // Step 4: Lưu request snapshot
        requestSnapshotRepo.save(snapshot);

        // Step 5: Cập nhật TeamPerformanceSnapshot
        if (teamId != null) {
            updateTeamPerformance(date, teamId, durationMinutes);
        }
    }

    // ─── updateResourceSnapshot ──────────────────────────────────────────────────
    @Override
    @Transactional("reportTransactionManager")
    public void updateResourceSnapshot(LocalDate date, Long warehouseId) {
        // Defensive: skip nếu thiếu warehouseId
        if (warehouseId == null) {
            log.warn("updateResourceSnapshot called with null warehouseId, skipping");
            return;
        }

        // Find-or-create snapshot cho (date, warehouseId)
        DailyResourceSnapshot snapshot = resourceSnapshotRepo
                .findBySnapshotDateAndWarehouseId(date, warehouseId)
                .orElseGet(() -> DailyResourceSnapshot.builder()
                        .snapshotDate(date)
                        .warehouseId(warehouseId)
                        .build());

        // Tăng totalDistributions
        snapshot.setTotalDistributions(snapshot.getTotalDistributions() + 1);

        resourceSnapshotRepo.save(snapshot);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Private helpers
    // ═══════════════════════════════════════════════════════════════════════════════

    private SummaryToday buildSummaryToday(LocalDate today) {
        return requestSnapshotRepo.findBySnapshotDate(today)
                .map(snapshot -> {
                    // totalDistributions = tổng từ tất cả warehouse hôm nay
                    int totalDist = resourceSnapshotRepo.findBySnapshotDate(today)
                            .stream()
                            .mapToInt(DailyResourceSnapshot::getTotalDistributions)
                            .sum();

                    // teamsActive = số team đang BUSY (TODO: Call dispatch-service API)
                    int teamsActive = 0;

                    return SummaryToday.builder()
                            .totalRequests(snapshot.getTotalRequests())
                            .criticalCount(snapshot.getCriticalCount())
                            .completedCount(snapshot.getCompletedCount())
                            .cancelledCount(snapshot.getCancelledCount())
                            .avgResponseMin(snapshot.getAvgResponseMin())
                            .avgCompleteMin(snapshot.getAvgCompleteMin())
                            .teamsActive(teamsActive)
                            .totalDistributions(totalDist)
                            .build();
                })
                .orElseGet(() -> SummaryToday.builder()
                        .totalRequests(0)
                        .criticalCount(0)
                        .completedCount(0)
                        .cancelledCount(0)
                        .avgResponseMin(BigDecimal.ZERO)
                        .avgCompleteMin(BigDecimal.ZERO)
                        .teamsActive(0) // TODO: Call dispatch-service API
                        .totalDistributions(0)
                        .build());
    }

    private List<DailyRequestChartDto> buildRequestChart() {
        List<DailyRequestSnapshot> last30 = requestSnapshotRepo
                .findTop30ByOrderBySnapshotDateDesc();

        if (last30.isEmpty()) {
            return Collections.emptyList();
        }

        return last30.stream()
                .map(s -> DailyRequestChartDto.builder()
                        .date(s.getSnapshotDate().toString())
                        .totalRequests(s.getTotalRequests())
                        .completedCount(s.getCompletedCount())
                        .criticalCount(s.getCriticalCount())
                        .build())
                .toList();
    }

    private List<TeamRankingDto> buildTeamRanking(LocalDate today) {
        LocalDate weekAgo = today.minusDays(7);

        List<TeamPerformanceSnapshot> performances = teamPerformanceRepo
                .findBySnapshotDateBetweenOrderByMissionsCompletedDesc(weekAgo, today);

        if (performances.isEmpty()) {
            return Collections.emptyList();
        }

        return performances.stream()
                .map(p -> TeamRankingDto.builder()
                        .teamId(p.getTeamId())
                        .missionsCompleted(p.getMissionsCompleted())
                        .avgDurationMin(p.getAvgDurationMin())
                        .build())
                .toList();
    }

    private void updateTeamPerformance(LocalDate date, Long teamId,
            Integer durationMinutes) {
        TeamPerformanceSnapshot teamSnap = teamPerformanceRepo
                .findBySnapshotDateAndTeamId(date, teamId)
                .orElseGet(() -> TeamPerformanceSnapshot.builder()
                        .snapshotDate(date)
                        .teamId(teamId)
                        .build());

        int oldMissions = teamSnap.getMissionsCompleted();
        int newMissions = oldMissions + 1;
        teamSnap.setMissionsCompleted(newMissions);

        // Running average cho avgDurationMin
        if (durationMinutes != null) {
            BigDecimal oldAvg = teamSnap.getAvgDurationMin() != null
                    ? teamSnap.getAvgDurationMin()
                    : BigDecimal.ZERO;
            BigDecimal newAvg = oldAvg
                    .multiply(BigDecimal.valueOf(oldMissions))
                    .add(BigDecimal.valueOf(durationMinutes))
                    .divide(BigDecimal.valueOf(newMissions), 2, RoundingMode.HALF_UP);
            teamSnap.setAvgDurationMin(newAvg);
        }

        teamPerformanceRepo.save(teamSnap);
    }
}
