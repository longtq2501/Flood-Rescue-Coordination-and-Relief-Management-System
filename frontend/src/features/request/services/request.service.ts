"use client";

import { apiGet, apiPatch, http } from "@/shared/api/http";
import type { ApiResponse } from "@/shared/types/api";
import type {
  CreateRescueRequestPayload,
  PageResult,
  RescueRequestDetail,
  RescueRequestSummary,
  RequestStatus,
  RequestFilters,
} from "@/features/request/types/request.types";

export async function createRescueRequest(payload: CreateRescueRequestPayload) {
  const formData = new FormData();

  if (payload.lat !== undefined) {
    formData.append("lat", String(payload.lat));
  }
  if (payload.lng !== undefined) {
    formData.append("lng", String(payload.lng));
  }
  if (payload.addressText) {
    formData.append("addressText", payload.addressText);
  }

  formData.append("description", payload.description);
  formData.append("numPeople", String(payload.numPeople));
  formData.append("urgencyLevel", payload.urgencyLevel);

  if (payload.images?.length) {
    Array.from(payload.images).forEach((file) => {
      formData.append("images", file);
    });
  }

  const response = await http.post<ApiResponse<RescueRequestDetail>>("/requests", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });

  if (!response.data.success) {
    throw new Error(response.data.message || "Tao request that bai");
  }

  return response.data.data;
}

export async function getMyRequests(status?: RequestStatus) {
  const response = await apiGet<PageResult<RescueRequestSummary>>("/requests/my", {
    params: {
      page: 0,
      size: 20,
      status,
    },
  });

  if (!response.success) {
    throw new Error(response.message || "Khong tai duoc danh sach request");
  }

  return response.data;
}

export async function getRequestDetail(id: string) {
  const response = await apiGet<RescueRequestDetail>(`/requests/${id}`);
  if (!response.success) {
    throw new Error(response.message || "Khong tai duoc chi tiet request");
  }
  return response.data;
}

export async function cancelRequest(id: string, reason: string) {
  const response = await apiPatch<RescueRequestDetail, { reason: string }>(
    `/requests/${id}/cancel`,
    { reason },
  );
  if (!response.success) {
    throw new Error(response.message || "Khong huy duoc request");
  }
  return response.data;
}

export async function confirmRequest(id: string, note?: string) {
  const response = await apiPatch<RescueRequestDetail, { note?: string }>(
    `/requests/${id}/confirm`,
    { note },
  );
  if (!response.success) {
    throw new Error(response.message || "Khong xac nhan duoc request");
  }
  return response.data;
}

export async function fetchCoordinatorRequests(filters: RequestFilters = {}) {
  const response = await apiGet<PageResult<RescueRequestSummary>>("/requests", {
    params: {
      page: filters.page ?? 0,
      size: filters.size ?? 20,
      status: filters.status || undefined,
      urgencyLevel: filters.urgencyLevel || undefined,
      fromDate: filters.fromDate || undefined,
      toDate: filters.toDate || undefined,
      search: filters.search || undefined,
    },
  });
  if (!response.success) {
    throw new Error(response.message || "Khong tai duoc request board");
  }
  return response.data;
}

export async function verifyRequest(id: string, note?: string) {
  const response = await apiPatch<RescueRequestDetail, { note?: string }>(
    `/requests/${id}/verify`,
    { note },
  );
  if (!response.success) {
    throw new Error(response.message || "Khong verify duoc request");
  }
  return response.data;
}
