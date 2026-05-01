"use client";

import { apiGet } from "@/shared/api/http";

type DashboardSummary = {
  totalRequests: number;
  completedRequests: number;
  pendingRequests: number;
  inProgressRequests: number;
  completionRate: number;
  avgResponseMinutes: number;
  avgCompleteMinutes: number;
};

export type DashboardData = {
  summary: DashboardSummary;
  byUrgency: Record<string, number>;
  resourceUsage: {
    vehiclesDeployed: number;
    totalDistributions: number;
    activeTeams: number;
    totalTeams: number;
  };
};

export async function getManagerDashboard() {
  const response = await apiGet<DashboardData>("/reports/dashboard");
  if (!response.success) {
    throw new Error(response.message || "Khong tai duoc dashboard");
  }
  return response.data;
}
