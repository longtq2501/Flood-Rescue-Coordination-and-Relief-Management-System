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
  UpdateStockRequest,
  Distribution,
  CreateDistributionRequest,
  DistributionFilters,
} from "../types/resource.types";

type DistributionPageResult = PageResult<Distribution>;

function normalizeDistributionPage(
  data: DistributionPageResult | Distribution[] | null | undefined,
): DistributionPageResult {
  if (Array.isArray(data)) {
    return {
      content: data,
      totalElements: data.length,
      totalPages: data.length > 0 ? 1 : 0,
      size: data.length,
      page: 0,
      last: true,
    };
  }

  return (
    data ?? {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 0,
      page: 0,
      last: true,
    }
  );
}

export async function getVehicles() {
  const response = await apiGet<PageResult<Vehicle> | Vehicle[]>("/resources/vehicles", {
    params: {
      page: 0,
      size: 50,
    },
  });

  if (!response.success) {
    throw new Error(response.message || "Không tải được danh sách phương tiện");
  }

  // response.data could be a paginated PageResult or a raw array from the API.
  const raw = response.data as PageResult<Vehicle> | Vehicle[] | undefined;

  let content: Vehicle[] = [];
  let pageMeta: Partial<PageResult<Vehicle>> = {};

  if (Array.isArray(raw)) {
    content = raw;
    pageMeta = { content: raw, totalElements: raw.length, totalPages: raw.length > 0 ? 1 : 0, size: raw.length, page: 0 };
  } else if (raw && typeof raw === "object" && "content" in raw) {
    content = (raw as PageResult<Vehicle>).content || [];
    pageMeta = raw as PageResult<Vehicle>;
  }

  const vehiclesWithLocations = content.map((vehicle, index) => ({
    ...vehicle,
    currentLat: (vehicle as any).currentLat || 10.75 + index * 0.015,
    currentLng: (vehicle as any).currentLng || 106.65 + index * 0.015,
  }));

  const defaultPage: PageResult<Vehicle> = {
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 50,
    page: 0,
  };

  return {
    ...(pageMeta as PageResult<Vehicle> || defaultPage),
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

export async function getDistributions(filters: DistributionFilters = {}) {
  const response = await apiGet<DistributionPageResult | Distribution[]>("/resources/distributions", {
    params: {
      requestId: filters.requestId,
      fromDate: filters.fromDate,
      toDate: filters.toDate,
      page: filters.page ?? 0,
      size: filters.size ?? 20,
    },
  });

  if (!response.success) {
    throw new Error(response.message || "Không tải được lịch sử phân phối");
  }

  return normalizeDistributionPage(response.data);
}

export async function getDistributionsByRequest(requestId: number) {
  const response = await apiGet<DistributionPageResult | Distribution[]>(`/resources/distributions/by-request/${requestId}`);

  if (!response.success) {
    throw new Error(response.message || "Không tải được lịch sử phân phối của yêu cầu");
  }

  return normalizeDistributionPage(response.data);
}

export async function createDistribution(data: CreateDistributionRequest) {
  const response = await apiPost<Distribution, CreateDistributionRequest>("/resources/distributions", data);

  if (!response.success) {
    throw new Error(response.message || "Không thể tạo phân phối");
  }

  return response.data;
}
