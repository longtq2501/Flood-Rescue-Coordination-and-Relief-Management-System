export type DashboardSummary = {
  totalRequests: number;
  completedRequests: number;
  pendingRequests: number;
  inProgressRequests: number;
  completionRate: number;
  avgResponseMinutes: number;
  avgCompleteMinutes: number;
};

export type ResourceUsage = {
  vehiclesDeployed: number;
  totalDistributions: number;
  activeTeams: number;
  totalTeams: number;
  warehouseOccupancy: number;
};

export type RequestVolumeTrend = {
  date: string;
  count: number;
};

export type StatusDistribution = {
  status: string;
  count: number;
};

export type UrgencyBreakdown = {
  level: string;
  count: number;
};

export type RecentActivity = {
  id: string;
  type: "REQUEST" | "MISSION" | "RESOURCE" | "ALERT";
  description: string;
  timestamp: string;
  user: string;
};

export type DashboardData = {
  summary: DashboardSummary;
  resourceUsage: ResourceUsage;
  volumeTrend: RequestVolumeTrend[];
  statusDistribution: StatusDistribution[];
  urgencyBreakdown: UrgencyBreakdown[];
  recentActivities: RecentActivity[];
};
