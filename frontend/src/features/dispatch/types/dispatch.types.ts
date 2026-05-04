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

export type CreateTeamRequest = {
  name: string;
  leaderId: number;
  capacity: number;
};

export type LocationUpdateRequest = {
  lat: number;
  lng: number;
  teamId?: number;
};

export type Warehouse = {
  id: number;
  name: string;
  location: string;
  lat: number;
  lng: number;
};

export interface MapData {
  requests: RescueRequestSummary[];
  teams: Team[];
  warehouses: Warehouse[];
}
