"use client";

import { apiGet, apiPost, apiPatch } from "@/shared/api/http";
import type { PageResult } from "@/features/request/types/request.types";
import type { Warehouse, CreateWarehousePayload } from "../types/warehouse.types";
import type { 
  Vehicle, 
  CreateVehicleRequest, 
  VehicleStatus, 
  ReliefItem, 
  CreateReliefItemRequest, 
  UpdateStockRequest 
} from "../types/resource.types";

export async function getVehicles() {
  const response = await apiGet<PageResult<Vehicle>>("/resources/vehicles", {
    params: {
      page: 0,
      size: 50,
    },
  });
  
  if (!response.success) {
    throw new Error(response.message || "Không tải được danh sách phương tiện");
  }

  const defaultPage: PageResult<Vehicle> = {
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 50,
    page: 0
  };

  const content = response.data?.content || [];

  const vehiclesWithLocations = content.map((vehicle, index) => ({
    ...vehicle,
    currentLat: vehicle.currentLat || 10.75 + (index * 0.015),
    currentLng: vehicle.currentLng || 106.65 + (index * 0.015),
  }));

  return {
    ...(response.data || defaultPage),
    content: vehiclesWithLocations,
  };
}

export async function addVehicle(data: CreateVehicleRequest) {
  const response = await apiPost<Vehicle, CreateVehicleRequest>("/resources/vehicles", data);
  if (!response.success) {
    throw new Error(response.message || "Không thể thêm phương tiện");
  }
  return response.data;
}

export async function updateVehicleStatus(id: number, status: VehicleStatus, note?: string) {
  const response = await apiPatch<Vehicle, Record<string, unknown>>(`/resources/vehicles/${id}/status`, {}, {
    params: { status, note }
  });
  if (!response.success) {
    throw new Error(response.message || "Không thể cập nhật trạng thái phương tiện");
  }
  return response.data;
}

export async function getWarehouses() {
  const response = await apiGet<PageResult<Warehouse>>("/resources/warehouses", {
    params: {
      page: 0,
      size: 50,
    },
  });
  if (!response.success) {
    throw new Error(response.message || "Không tải được danh sách kho");
  }

  const defaultPage: PageResult<Warehouse> = {
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 50,
    page: 0
  };

  const content = response.data?.content || [];
  
  const warehousesWithLocations = content.map((warehouse, index) => ({
    ...warehouse,
    lat: warehouse.lat || (10.75 + (index * 0.02)),
    lng: warehouse.lng || (106.65 + (index * 0.02)),
  }));

  return {
    ...(response.data || defaultPage),
    content: warehousesWithLocations,
  };
}

export async function getWarehouseDetails(id: number) {
  const response = await apiGet<Warehouse>(`/resources/warehouses/${id}`);
  if (!response.success) {
    throw new Error(response.message || "Không tải được thông tin kho");
  }
  return response.data;
}

export async function createWarehouse(data: CreateWarehousePayload) {
  const response = await apiPost<Warehouse, CreateWarehousePayload>("/resources/warehouses", data);
  if (!response.success) {
    throw new Error(response.message || "Không thể tạo kho");
  }
  return response.data;
}

export async function getItemsByWarehouse(warehouseId: number) {
  const response = await apiGet<PageResult<ReliefItem>>("/resources/items", {
    params: { warehouseId, page: 0, size: 100 }
  });
  if (!response.success) {
    throw new Error(response.message || "Không tải được danh sách hàng hóa");
  }
  return response.data;
}

export async function addItem(data: CreateReliefItemRequest) {
  const response = await apiPost<ReliefItem, CreateReliefItemRequest>("/resources/items", data);
  if (!response.success) {
    throw new Error(response.message || "Không thể thêm hàng hóa");
  }
  return response.data;
}

export async function updateStock(id: number, data: UpdateStockRequest) {
  const response = await apiPatch<ReliefItem, UpdateStockRequest>(`/resources/items/${id}/stock`, data);
  if (!response.success) {
    throw new Error(response.message || "Không thể cập nhật tồn kho");
  }
  return response.data;
}
