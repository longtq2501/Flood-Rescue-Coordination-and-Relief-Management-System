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
