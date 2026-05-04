"use client";

import { apiGet, apiPost, apiPatch } from "@/shared/api/http";
import type { 
  Vehicle, 
  VehicleStatus, 
  VehicleType, 
  CreateVehicleRequest,
  Warehouse,
  ReliefItem,
  CreateReliefItemRequest,
  UpdateStockRequest
} from "../types";

// Vehicles
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

// Warehouses
export async function getWarehouses() {
  const response = await apiGet<Warehouse[]>("/resources/warehouses");
  if (!response.success) {
    throw new Error(response.message || "Không tải được danh sách kho");
  }
  return response.data;
}

// Inventory
export async function getItemsByWarehouse(warehouseId: number) {
  const response = await apiGet<{ content: ReliefItem[] }>("/resources/items", {
    params: { warehouseId, size: 100 }
  });
  if (!response.success) {
    throw new Error(response.message || "Không tải được danh sách hàng hóa");
  }
  return response.data.content;
}

export async function addItem(data: CreateReliefItemRequest) {
  const response = await apiPost<ReliefItem, CreateReliefItemRequest>("/resources/items", data);
  if (!response.success) {
    throw new Error(response.message || "Không thể thêm hàng hóa mới");
  }
  return response.data;
}

export async function updateStock(id: number, quantity: number, note?: string) {
  const response = await apiPatch<ReliefItem, UpdateStockRequest>(`/resources/items/${id}/stock`, {
    quantity,
    note
  });
  if (!response.success) {
    throw new Error(response.message || "Không thể cập nhật tồn kho");
  }
  return response.data;
}
