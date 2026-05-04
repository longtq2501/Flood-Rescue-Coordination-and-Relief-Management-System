"use client";

import { apiGet } from "@/shared/api/http";
import { DashboardData } from "../types";

export async function getDashboardData() {
  const response = await apiGet<DashboardData>("/reports/dashboard");
  if (!response.success) {
    throw new Error(response.message || "Không tải được dữ liệu dashboard");
  }
  return response.data;
}

// Keep for backward compatibility if needed, or redirect to getDashboardData
export const getManagerDashboard = getDashboardData;
