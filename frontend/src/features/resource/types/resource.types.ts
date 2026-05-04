export type VehicleType = "BOAT" | "TRUCK" | "HELICOPTER" | "AMBULANCE" | "OTHER";

export type VehicleStatus = "AVAILABLE" | "IN_USE" | "MAINTENANCE" | "OFFLINE";

export interface Vehicle {
  id: number;
  plateNumber: string;
  type: VehicleType;
  capacity: number;
  status: VehicleStatus;
  currentLat?: number;
  currentLng?: number;
  assignedTeamId?: number;
}

export interface CreateVehicleRequest {
  plateNumber: string;
  type: VehicleType;
  capacity: number;
}

export interface UpdateVehicleStatusRequest {
  status: VehicleStatus;
  note?: string;
}

export interface Warehouse {
  id: number;
  name: string;
  address: string;
  managerId: number;
  lat?: number;
  lng?: number;
}

export interface ReliefItem {
  id: number;
  warehouseId: number;
  name: string;
  category: string;
  unit: string;
  quantity: number;
  lowThreshold: number;
  belowThreshold: boolean;
  updatedAt: string;
}

export interface CreateReliefItemRequest {
  warehouseId: number;
  name: string;
  category: string;
  unit: string;
  quantity: number;
  lowThreshold: number;
}

export interface UpdateStockRequest {
  quantity: number;
  note?: string;
}

export interface DistributionLineItem {
  reliefItemId: number;
  itemName: string;
  quantity: number;
  unit: string;
}

export interface CreateDistributionItemRequest {
  reliefItemId: number;
  quantity: number;
}

export interface CreateDistributionRequest {
  requestId: number;
  recipientId: number;
  items: CreateDistributionItemRequest[];
  note?: string;
}

export interface Distribution {
  id: number;
  requestId: number;
  recipientId: number;
  recipientName: string;
  items: DistributionLineItem[];
  note?: string;
  distributedAt: string;
}

export interface DistributionFilters {
  requestId?: number;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
}
