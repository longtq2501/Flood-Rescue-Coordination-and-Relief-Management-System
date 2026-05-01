"use client";

import { apiGet } from "@/shared/api/http";
import type { PageResult } from "@/features/request/types/request.types";

export type Vehicle = {
  id: number;
  plateNumber: string;
  type: "BOAT" | "TRUCK" | "HELICOPTER" | "AMBULANCE" | "OTHER";
  capacity: number;
  status: "AVAILABLE" | "IN_USE" | "MAINTENANCE" | "OFFLINE";
};

export async function getVehicles() {
  const response = await apiGet<PageResult<Vehicle>>("/resources/vehicles", {
    params: {
      status: "AVAILABLE",
      page: 0,
      size: 50,
    },
  });
  if (!response.success) {
    throw new Error(response.message || "Khong tai duoc danh sach vehicle");
  }
  return response.data;
}
