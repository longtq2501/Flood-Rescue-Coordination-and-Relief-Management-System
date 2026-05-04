"use client";

import { apiGet, apiPost, apiPatch } from "@/shared/api/http";
import type { 
  Vehicle, 
  VehicleStatus, 
  VehicleType, 
  CreateVehicleRequest 
} from "../types";

export async function getVehicles(filters?: { status?: VehicleStatus; type?: VehicleType }) {
  const response = await apiGet<Vehicle[]>("/resources/vehicles", {
    params: filters,
  });
  
  if (!response.success) {
    throw new Error(response.message || "Không tải được danh sách phương tiện");
  }

  // Add mock locations if currentLat/Lng are missing for demo purposes
  const vehiclesWithLocations = response.data.map((vehicle, index) => ({
    ...vehicle,
    currentLat: vehicle.currentLat || 10.75 + (index * 0.015),
    currentLng: vehicle.currentLng || 106.65 + (index * 0.015),
  }));

  return vehiclesWithLocations;
}

export async function addVehicle(data: CreateVehicleRequest) {
  const response = await apiPost<Vehicle, CreateVehicleRequest>("/resources/vehicles", data);
  
  if (!response.success) {
    throw new Error(response.message || "Không thể thêm phương tiện mới");
  }
  
  return response.data;
}

export async function updateVehicleStatus(id: number, status: VehicleStatus, note?: string) {
  const response = await apiPatch<Vehicle, any>(`/resources/vehicles/${id}/status`, null, {
    params: { status, note }
  });
  
  if (!response.success) {
    throw new Error(response.message || "Không thể cập nhật trạng thái phương tiện");
  }
  
  return response.data;
}
