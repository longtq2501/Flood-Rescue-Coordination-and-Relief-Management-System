"use client";

import { apiGet, apiPost } from "@/shared/api/http";
import type { PageResult } from "@/features/request/types/request.types";
import type { Warehouse, CreateWarehousePayload } from "../types/warehouse.types";

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

  // Default empty page result
  const defaultPage: PageResult<Vehicle> = {
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 50,
    page: 0
  };

  // Ensure content exists
  const content = response.data?.content || [];

  // Add mock locations for demo purposes
  const vehiclesWithLocations = content.map((vehicle, index) => ({
    ...vehicle,
    lat: 10.75 + (index * 0.015), // Mock locations around Ho Chi Minh City
    lng: 106.65 + (index * 0.015),
  }));

  return {
    ...(response.data || defaultPage),
    content: vehiclesWithLocations,
  };
}

export async function getWarehouses() {
  const response = await apiGet<PageResult<Warehouse>>("/resources/warehouses", {
    params: {
      page: 0,
      size: 50,
    },
  });
  if (!response.success) {
    throw new Error(response.message || "Khong tai duoc danh sach warehouse");
  }

  // Default empty page result
  const defaultPage: PageResult<Warehouse> = {
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 50,
    page: 0
  };

  // Ensure content exists
  const content = response.data?.content || [];
  
  // Add mock locations for demo purposes if they are missing
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
    throw new Error(response.message || "Khong tai duoc thong tin warehouse");
  }
  return response.data;
}

export async function createWarehouse(data: CreateWarehousePayload) {
  const response = await apiPost<Warehouse, CreateWarehousePayload>("/resources/warehouses", data);
  if (!response.success) {
    throw new Error(response.message || "Khong the tao warehouse");
  }
  return response.data;
}
