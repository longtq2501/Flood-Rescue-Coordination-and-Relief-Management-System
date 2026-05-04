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
  citizenId: number;
  teamId: number;
  vehicleId: number;
  note?: string;
};

export type CreateTeamRequest = {
  name: string;
  leaderId: number;
  capacity: number;
};

export type MapData = {
  teams: {
    teamId: number;
    teamName: string;
    status: TeamStatus;
    lat: number;
    lng: number;
    lastUpdated?: string;
  }[];
};

export type LocationUpdateRequest = {
  lat: number;
  lng: number;
  speed?: number;
  heading?: number;
};
