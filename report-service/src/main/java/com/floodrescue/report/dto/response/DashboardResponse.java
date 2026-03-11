package com.floodrescue.report.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {

    // Tổng quan hôm nay
    private SummaryToday today;

    // Chart 30 ngày
    private List<DailyRequestChartDto> requestChart;

    // Top team
    private List<TeamRankingDto> teamRanking;

    @Data
    @Builder
    public static class SummaryToday {
        private Integer totalRequests;
        private Integer criticalCount;
        private Integer completedCount;
        private Integer cancelledCount;
        private BigDecimal avgResponseMin;
        private BigDecimal avgCompleteMin;
        private Integer teamsActive; // số team đang BUSY
        private Integer totalDistributions;
    }

    @Data
    @Builder
    public static class DailyRequestChartDto {
        private String date; // "2025-01-01"
        private Integer totalRequests;
        private Integer completedCount;
        private Integer criticalCount;
    }

    @Data
    @Builder
    public static class TeamRankingDto {
        private Long teamId;
        private Integer missionsCompleted;
        private BigDecimal avgDurationMin;
    }
}
