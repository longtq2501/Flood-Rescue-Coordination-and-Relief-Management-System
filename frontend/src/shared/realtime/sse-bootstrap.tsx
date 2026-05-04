"use client";

import { useEffect } from "react";
import Cookies from "js-cookie";
import { toast } from "sonner";
import { ACCESS_TOKEN_KEY } from "@/shared/constants/auth";
import { env } from "@/shared/config/env";
import { useNotificationStore } from "@/features/notification/store/notification.store";
import { type Notification } from "@/features/notification/types/notification.types";
import { Bell, AlertTriangle, CheckCircle, Info } from "lucide-react";

export function SseBootstrap() {
  const addNotification = useNotificationStore((s) => s.addNotification);

  useEffect(() => {
    const token = Cookies.get(ACCESS_TOKEN_KEY);
    if (!token) return;

    let eventSource: EventSource | null = null;
    let reconnectTimeout: NodeJS.Timeout | null = null;

    const showToast = (notification: Notification) => {
      const Icon = getIcon(notification.type);

      toast(notification.title, {
        description: notification.message,
        icon: <Icon className="h-5 w-5" />,
        duration: 5000,
        action: notification.relatedId
          ? {
              label: "Xem chi tiết",
              onClick: () => {
                window.location.href = getLink(notification);
              },
            }
          : undefined,
      });
    };

    const setupSse = () => {
      if (eventSource) eventSource.close();

      const sseUrl = `${env.sseUrl}?token=${token}`;
      eventSource = new EventSource(sseUrl);

      eventSource.onmessage = (event) => {
        try {
          const notification: Notification = JSON.parse(event.data);
          addNotification(notification);
          showToast(notification);
        } catch (error) {
          console.warn("Error parsing SSE data:", error);
        }
      };

      eventSource.addEventListener("notification", (event: MessageEvent) => {
        try {
          const notification: Notification = JSON.parse(event.data);
          addNotification(notification);
          showToast(notification);
        } catch (error) {
          console.warn("Error parsing notification event:", error);
        }
      });

      eventSource.onerror = (err) => {
        if (eventSource) eventSource.close();

        // Reconnect after 5 seconds
        reconnectTimeout = setTimeout(() => {
          console.warn("SSE: Attempting to reconnect...");
          setupSse();
        }, 5000);
      };
    };

    setupSse();

    return () => {
      if (eventSource) eventSource.close();
      if (reconnectTimeout) clearTimeout(reconnectTimeout);
    };
  }, [addNotification]);

  return null; // This is a background listener
}

const getIcon = (type: string) => {
  switch (type) {
    case "URGENT_REQUEST":
      return AlertTriangle;
    case "MISSION_ASSIGNED":
      return Bell;
    case "STATUS_UPDATE":
      return CheckCircle;
    default:
      return Info;
  }
};

const getLink = (notification: Notification) => {
  switch (notification.type) {
    case "URGENT_REQUEST":
      return `/dashboard/coordinator`;
    case "MISSION_ASSIGNED":
      return `/dashboard/rescue-team`;
    default:
      return "#";
  }
};
