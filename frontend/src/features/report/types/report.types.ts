export interface DashboardResponse {
    today: SummaryToday;
    requestChart: DailyRequestChartDto[];
    teamRanking: TeamRankingDto[];
}

export interface SummaryToday {
    totalRequests: number;
    criticalCount: number;
    completedCount: number;
    cancelledCount: number;
    avgResponseMin: number;
    avgCompleteMin: number;
    teamsActive: number;
    totalDistributions: number;
}

export interface DailyRequestChartDto {
    date: string;
    totalRequests: number;
    completedCount: number;
    criticalCount: number;
}

export interface TeamRankingDto {
    teamId: number;
    missionsCompleted: number;
    avgDurationMin: number;
}