import type { RescueRequestSummary } from "@/features/request/types/request.types";

export type TeamStatus = "AVAILABLE" | "BUSY" | "RETURNING" | "OFFLINE";

export type Team = {
  id: number;
  name: string;
  status: TeamStatus;
  capacity: number;
  memberCount: number;
  lat?: number;
  lng?: number;
};

export type Assignment = {
  id: number;
  requestId: number;
  teamId: number;
  teamName: string;
  vehicleId: number;
  vehiclePlate: string;
  status: "ACTIVE" | "COMPLETED" | "CANCELLED";
  assignedAt?: string;
  startedAt?: string;
  completedAt?: string;
};

export type DispatchAssignmentPayload = {
  requestId: number;
  teamId: number;
  vehicleId: number;
  note?: string;
};

export type Warehouse = {
  id: number;
  name: string;
  address: string;
  lat: number;
  lng: number;
  managerId: number;
};

export type MapData = {
  teams: Team[];
  requests: RescueRequestSummary[];
  warehouses: Warehouse[];
};

export type LocationUpdateRequest = {
  lat: number;
  lng: number;
  speed?: number;
  heading?: number;
};
