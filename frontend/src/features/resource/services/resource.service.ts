"use client";

import { apiGet } from "@/shared/api/http";
import type { PageResult } from "@/features/request/types/request.types";

export type Vehicle = {
  id: number;
  plateNumber: string;
  type: "BOAT" | "TRUCK" | "HELICOPTER" | "AMBULANCE" | "OTHER";
  capacity: number;
  status: "AVAILABLE" | "IN_USE" | "MAINTENANCE" | "OFFLINE";
  lat?: number;
  lng?: number;
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

  // Add mock locations for demo purposes
  const vehiclesWithLocations = response.data.content.map((vehicle, index) => ({
    ...vehicle,
    lat: 10.75 + (index * 0.015), // Mock locations around Ho Chi Minh City
    lng: 106.65 + (index * 0.015),
  }));

  return {
    ...response.data,
    content: vehiclesWithLocations,
  };
}
