"use client";

import { apiDelete, apiGet, apiPatch, apiPost } from "@/shared/api/http";
import type { PageResult } from "@/features/request/types/request.types";
import type {
  Assignment,
  DispatchAssignmentPayload,
  LocationUpdateRequest,
  MapData,
  Team,
  CreateTeamRequest,
} from "@/features/dispatch/types/dispatch.types";
import type { RescueRequestSummary } from "@/features/request/types/request.types";

// Removed duplicate interface TeamLocationDto as it's now in dispatch.types.ts

export async function getTeams() {
  const response = await apiGet<Team[]>("/dispatch/teams", {
    params: { page: 0, size: 50 },
  });
  if (!response.success) {
    throw new Error(response.message || "Khong tai duoc danh sach team");
  }

  // Add mock locations for demo purposes
  const teamsWithLocations = response.data.map((team, index) => ({
    ...team,
    lat: 10.8 + (index * 0.01), // Mock locations around Ho Chi Minh City
    lng: 106.6 + (index * 0.01),
  }));

  return teamsWithLocations;
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
  const response = await apiGet<Assignment[]>("/dispatch/assignments/my");
  if (!response.success) {
    throw new Error(response.message || "Khong tai duoc assignment");
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

export async function createTeam(data: CreateTeamRequest) {
  const response = await apiPost<Team, CreateTeamRequest>("/dispatch/teams", data);
  if (!response.success) {
    throw new Error(response.message || "Khong the tao doi cuu ho");
  }
  return response.data;
}

export async function updateTeam(id: number, data: CreateTeamRequest) {
  const response = await apiPatch<Team, CreateTeamRequest>(`/dispatch/teams/${id}`, data);
  if (!response.success) {
    throw new Error(response.message || "Khong the cap nhat doi cuu ho");
  }
  return response.data;
}

export async function updateTeamStatus(id: number, status: string) {
  const response = await apiPatch<Team, undefined>(`/dispatch/teams/${id}/status`, undefined, {
    params: { status }
  });
  if (!response.success) {
    throw new Error(response.message || "Khong the cap nhat trang thai");
  }
  return response.data;
}

export async function deleteTeam(id: number) {
  const response = await apiDelete<void>(`/dispatch/teams/${id}`);
  if (!response.success) {
    throw new Error(response.message || "Khong the xoa doi cuu ho");
  }
  return response.data;
}

export async function getMapData() {
  const response = await apiGet<MapData>("/dispatch/map");
  if (!response.success) {
    throw new Error(response.message || "Khong the tai du lieu ban do");
  }
  return response.data;
}

export async function updateLocation(data: LocationUpdateRequest) {
  const response = await apiPost<void, LocationUpdateRequest>("/dispatch/location", data);
  if (!response.success) {
    throw new Error(response.message || "Khong the cap nhat vi tri");
  }
  return response.data;
}
