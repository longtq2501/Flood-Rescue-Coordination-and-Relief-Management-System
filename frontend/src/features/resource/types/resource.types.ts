export type VehicleStatus = 'AVAILABLE' | 'IN_USE' | 'MAINTENANCE' | 'OFFLINE';
export type VehicleType = 'BOAT' | 'TRUCK' | 'HELICOPTER' | 'AMBULANCE' | 'OTHER';

export interface WarehouseResponse {
    id: number;
    name: string;
    address?: string;
    managerId: number;
    createdAt: string;
    items: ReliefItemResponse[];
}

export interface ReliefItemResponse {
    id: number;
    warehouseId: number;
    name: string;
    category?: string;
    unit: string;
    quantity: number;
    lowThreshold: number;
    belowThreshold: boolean;
    updatedAt: string;
}

export interface VehicleResponse {
    id: number;
    plateNumber: string;
    type: VehicleType;
    capacity?: number;
    status: VehicleStatus;
    currentLat?: number;
    currentLng?: number;
    assignedTeamId?: number;
}

export interface CreateDistributionRequest {
    requestId: number;
    recipientId: number;
    note?: string;
    items: DistributionItemRequest[];
}

export interface DistributionItemRequest {
    reliefItemId: number;
    quantity: number;
}

export interface DistributionResponse {
    id: number;
    requestId: number;
    recipientId: number;
    coordinatorId: number;
    note?: string;
    distributedAt: string;
    items: DistributionItemResponse[];
}

export interface DistributionItemResponse {
    reliefItemId: number;
    itemName: string;
    unit: string;
    quantity: number;
}