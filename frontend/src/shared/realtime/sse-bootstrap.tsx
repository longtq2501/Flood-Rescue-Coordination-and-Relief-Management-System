"use client";

import { useEffect } from "react";
import { EventSourcePolyfill } from "event-source-polyfill";
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

    const sseUrl = `${env.apiBaseUrl}/notifications/sse`;
    
    const eventSource = new EventSourcePolyfill(sseUrl, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
      heartbeatTimeout: 60000,
    });

    eventSource.onmessage = (event) => {
      try {
        const notification: Notification = JSON.parse(event.data);
        addNotification(notification);
        
        // Show Toast based on type
        showToast(notification);
      } catch (error) {
        console.error("Error parsing SSE data:", error);
      }
    };

    eventSource.addEventListener("notification", (event: any) => {
      try {
        const notification: Notification = JSON.parse(event.data);
        addNotification(notification);
        showToast(notification);
      } catch (error) {
        console.error("Error parsing notification event:", error);
      }
    });

    eventSource.onerror = (err) => {
      console.error("SSE connection error:", err);
      // EventSourcePolyfill handles reconnection automatically usually
    };

    return () => {
      eventSource.close();
    };
  }, [addNotification]);

  const showToast = (notification: Notification) => {
    const Icon = getIcon(notification.type);
    
    toast(notification.title, {
      description: notification.message,
      icon: <Icon className="h-5 w-5" />,
      duration: 5000,
      action: notification.relatedId ? {
        label: "Xem chi tiết",
        onClick: () => {
          // Navigate to related object
          window.location.href = getLink(notification);
        }
      } : undefined
    });
  };

  const getIcon = (type: string) => {
    switch (type) {
      case "URGENT_REQUEST": return AlertTriangle;
      case "MISSION_ASSIGNED": return Bell;
      case "STATUS_UPDATE": return CheckCircle;
      default: return Info;
    }
  };

  const getLink = (notification: Notification) => {
    switch (notification.type) {
      case "URGENT_REQUEST": return `/dashboard/coordinator`;
      case "MISSION_ASSIGNED": return `/dashboard/rescue-team`;
      default: return "#";
    }
  };

  return null; // This is a background listener
}
