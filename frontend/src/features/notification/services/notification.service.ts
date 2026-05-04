import { apiGet, apiPatch } from "@/shared/api/http";
import type { PageResult } from "@/features/request/types/request.types";
import { type Notification } from "../types/notification.types";

export async function getNotifications(page = 0, size = 20) {
  const response = await apiGet<PageResult<Notification>>("/notifications", {
    params: { page, size }
  });
  if (!response.success) {
    throw new Error(response.message || "Không tải được thông báo");
  }
  return response.data;
}

export async function markNotificationAsRead(id: string) {
  const response = await apiPatch<void, undefined>(`/notifications/${id}/read`, undefined);
  if (!response.success) {
    throw new Error(response.message || "Không thể đánh dấu đã đọc");
  }
  return response.data;
}

export async function markAllNotificationsAsRead() {
  const response = await apiPatch<void, undefined>("/notifications/read-all", undefined);
  if (!response.success) {
    throw new Error(response.message || "Không thể đánh dấu tất cả đã đọc");
  }
  return response.data;
}
