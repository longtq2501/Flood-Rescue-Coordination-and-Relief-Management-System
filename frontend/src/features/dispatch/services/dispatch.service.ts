"use client";

import { apiGet, apiPatch, apiPost } from "@/shared/api/http";
import type { PageResult } from "@/features/request/types/request.types";
import type {
  Assignment,
  DispatchAssignmentPayload,
  MapData,
  Team,
  TeamStatus,
  Warehouse,
} from "@/features/dispatch/types/dispatch.types";
import type { RescueRequestSummary } from "@/features/request/types/request.types";

interface TeamLocationDto {
  teamId: number;
  teamName: string;
  status: TeamStatus;
  lat: number;
  lng: number;
}

export async function getTeams() {
  const response = await apiGet<PageResult<Team>>("/dispatch/teams", {
    params: { page: 0, size: 50 },
  });
  if (!response.success) {
    throw new Error(response.message || "Khong tai duoc danh sach team");
  }

  // Add mock locations for demo purposes
  const teamsWithLocations = response.data.content.map((team: Team, index: number) => ({
    ...team,
    lat: 10.8 + (index * 0.01), // Mock locations around Ho Chi Minh City
    lng: 106.6 + (index * 0.01),
  }));

  return {
    ...response.data,
    content: teamsWithLocations,
  };
}

export async function assignTeam(payload: DispatchAssignmentPayload) {
  const response = await apiPost<Assignment, DispatchAssignmentPayload>(
    "/dispatch/assign",
    payload,
  );
  if (!response.success) {
    throw new Error(response.message || "Assign that bai");
  }
  return response.data;
}

export async function getMyAssignments() {
  const response = await apiGet<PageResult<Assignment>>("/dispatch/assignments/my", {
    params: { page: 0, size: 20 },
  });
  if (!response.success) {
    throw new Error(response.message || "Khong tai duoc assignments");
  }
  return response.data;
}

export async function startAssignment(id: number) {
  const response = await apiPatch<Assignment, undefined>(
    `/dispatch/assignments/${id}/start`,
    undefined,
  );
  if (!response.success) {
    throw new Error(response.message || "Khong start duoc assignment");
  }
  return response.data;
}

export async function completeAssignment(id: number, resultNote: string) {
  const response = await apiPatch<Assignment, { resultNote: string }>(
    `/dispatch/assignments/${id}/complete`,
    { resultNote },
  );
  if (!response.success) {
    throw new Error(response.message || "Khong complete duoc assignment");
  }
  return response.data;
}

export async function getMapData() {
  // 1. Fetch data from multiple sources
  const [teamsRes, requestsRes, warehousesRes] = await Promise.all([
    apiGet<{ teams: TeamLocationDto[] }>("/dispatch/map"),
    apiGet<PageResult<RescueRequestSummary>>("/requests", { params: { size: 100 } }),
    apiGet<Warehouse[]>("/resources/warehouses"),
  ]);

  // 2. Aggregate and normalize data
  const mapData: MapData = {
    teams: (teamsRes.success ? teamsRes.data.teams : []).map((t: TeamLocationDto) => ({
      id: t.teamId,
      name: t.teamName,
      status: t.status as TeamStatus,
      lat: t.lat,
      lng: t.lng,
      capacity: 0,
      memberCount: 0,
    })),
    requests: requestsRes.success ? requestsRes.data.content : [],
    warehouses: (warehousesRes.success ? warehousesRes.data : []).map((w: Warehouse, index: number) => ({
      ...w,
      // Mock locations if backend doesn't have them
      lat: w.lat || 10.7 + (index * 0.02),
      lng: w.lng || 106.5 + (index * 0.02),
    })),
  };

  return mapData;
}
