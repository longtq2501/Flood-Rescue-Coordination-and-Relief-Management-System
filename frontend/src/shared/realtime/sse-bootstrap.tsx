"use client";

import Cookies from "js-cookie";
import { EventSourcePolyfill } from "event-source-polyfill";
import { useEffect } from "react";
import { toast } from "sonner";

import { env } from "@/shared/config/env";
import { ACCESS_TOKEN_KEY } from "@/shared/constants/auth";

const KNOWN_EVENTS = [
  "request.status.updated",
  "request.assigned",
  "request.completed",
  "new.request.alert",
  "resource.low.alert",
  "system.broadcast",
];

export function SseBootstrap() {
  useEffect(() => {
    const token = Cookies.get(ACCESS_TOKEN_KEY);
    if (!token) {
      return;
    }

    const source = new EventSourcePolyfill(env.sseUrl, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
      heartbeatTimeout: 45000,
    });

    source.onopen = () => {
      toast.success("SSE connected", { description: "Realtime stream is live" });
    };

    source.onerror = () => {
      toast.error("SSE disconnected", {
        description: "Dang thu reconnect tu dong...",
      });
    };

    KNOWN_EVENTS.forEach((eventType) => {
      source.addEventListener(eventType, (event) => {
        const message = event instanceof MessageEvent ? event.data : "New event";
        toast.message(eventType, {
          description: String(message).slice(0, 140),
        });
      });
    });

    return () => {
      source.close();
    };
  }, []);

  return null;
}
