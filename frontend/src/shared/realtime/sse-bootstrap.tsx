"use client";

import Cookies from "js-cookie";
import { EventSourcePolyfill } from "event-source-polyfill";
import { useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";

import { env } from "@/shared/config/env";
import {
  ACCESS_TOKEN_KEY,
  USER_ROLE_KEY,
  ROLE_TO_DASHBOARD_PATH,
  type AppRole,
} from "@/shared/constants/auth";
import { useSseStatus } from "@/shared/realtime/use-sse-status";

// ─────────────────────────────────────────────────────────────
// Static maps declared outside component — never cause re-renders
// ─────────────────────────────────────────────────────────────

const KNOWN_EVENTS = [
  "request.status.updated",
  "request.assigned",
  "request.completed",
  "new.request.alert",
  "resource.low.alert",
  "system.broadcast",
] as const;

type KnownEvent = (typeof KNOWN_EVENTS)[number];

/** TanStack Query cache keys to invalidate per SSE event */
const EVENT_QUERY_KEY_MAP: Record<KnownEvent, readonly string[]> = {
  "request.status.updated": ["requests", "coordinator-requests", "my-requests", "manager-dashboard"],
  "request.assigned":       ["requests", "coordinator-requests", "my-requests", "dispatch-teams", "resource-vehicles", "manager-dashboard"],
  "request.completed":      ["requests", "coordinator-requests", "my-requests", "manager-dashboard"],
  "new.request.alert":      ["requests", "coordinator-requests", "manager-dashboard"],
  "resource.low.alert":     ["resources", "resource-vehicles", "warehouses", "manager-dashboard"],
  "system.broadcast":       [],
};

/** URL path segment for "View Detail" deep-link */
const EVENT_PATH_SEGMENT: Partial<Record<KnownEvent, string>> = {
  "request.status.updated": "requests",
  "request.assigned":       "requests",
  "request.completed":      "requests",
  "new.request.alert":      "requests",
  "resource.low.alert":     "warehouses", // Map low resource alerts to warehouse dashboard
};

/** Human-readable toast titles */
const EVENT_TITLE: Record<KnownEvent, string> = {
  "request.status.updated": "Yêu cầu cập nhật trạng thái",
  "request.assigned":       "Yêu cầu đã được phân công",
  "request.completed":      "Yêu cầu đã hoàn thành",
  "new.request.alert":      "🆘 Có yêu cầu mới!",
  "resource.low.alert":     "⚠️ Cảnh báo tài nguyên thấp",
  "system.broadcast":       "📢 Thông báo hệ thống",
};

// ─────────────────────────────────────────────────────────────
// Reconnect / exponential back-off config
// ─────────────────────────────────────────────────────────────

const MAX_RETRY_ATTEMPTS = 10;
const BASE_BACKOFF_MS = 1_000;
const MAX_BACKOFF_MS = 30_000;

function getBackoffMs(attempt: number): number {
  return Math.min(BASE_BACKOFF_MS * 2 ** attempt, MAX_BACKOFF_MS);
}

// ─────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────

function getRoleDashboardBase(): string {
  const role = Cookies.get(USER_ROLE_KEY) as AppRole | undefined;
  if (role && role in ROLE_TO_DASHBOARD_PATH) {
    return ROLE_TO_DASHBOARD_PATH[role];
  }
  return "/dashboard/citizen";
}

interface SsePayload {
  id?: string;
  message?: string;
  [key: string]: unknown;
}

function parsePayload(raw: string): SsePayload {
  try {
    return JSON.parse(raw) as SsePayload;
  } catch {
    return { message: raw };
  }
}

// ─────────────────────────────────────────────────────────────
// Component
// ─────────────────────────────────────────────────────────────

export function SseBootstrap() {
  const queryClient = useQueryClient();
  const router = useRouter();
  const setStatus = useSseStatus((s) => s.setStatus);

  // Keep latest hook values in refs so the main effect (dep=[])
  // never goes stale — prevents re-render loops.
  const queryClientRef = useRef(queryClient);
  const routerRef = useRef(router);
  const setStatusRef = useRef(setStatus);
  useEffect(() => {
    queryClientRef.current = queryClient;
    routerRef.current = router;
    setStatusRef.current = setStatus;
  }); // no dep array — syncs refs after every render

  // All reconnect state in refs only — zero useState = zero re-renders
  const sourceRef = useRef<InstanceType<typeof EventSourcePolyfill> | null>(null);
  const retryCountRef = useRef(0);
  const retryTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const unmountedRef = useRef(false);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    unmountedRef.current = false;

    function connect() {
      if (unmountedRef.current) return;

      const token = Cookies.get(ACCESS_TOKEN_KEY);
      if (!token) return; // unauthenticated — skip

      sourceRef.current?.close();

      const source = new EventSourcePolyfill(env.sseUrl, {
        headers: { Authorization: `Bearer ${token}` },
        heartbeatTimeout: 45_000,
      });

      sourceRef.current = source;

      // ── onopen ─────────────────────────────────────────────
      source.onopen = () => {
        if (unmountedRef.current) return;
        const wasReconnect = retryCountRef.current > 0;
        retryCountRef.current = 0;
        setStatusRef.current("connected");
        toast.success(
          wasReconnect ? "Đã kết nối lại realtime" : "Đã kết nối realtime",
          { description: "Luồng dữ liệu trực tiếp đang hoạt động.", duration: 2_500 },
        );
      };

      // ── onerror → exponential back-off reconnect ──────────
      source.onerror = () => {
        if (unmountedRef.current) return;
        source.close();
        sourceRef.current = null;

        if (retryCountRef.current === 0) {
          // Only show toast on first disconnect (avoid spam)
          toast.warning("Mất kết nối realtime", {
            description: "Đang thử kết nối lại tự động…",
          });
        }

        if (retryCountRef.current < MAX_RETRY_ATTEMPTS) {
          const delay = getBackoffMs(retryCountRef.current);
          retryCountRef.current += 1;
          setStatusRef.current("reconnecting", retryCountRef.current);
          retryTimerRef.current = setTimeout(connect, delay);
        } else {
          setStatusRef.current("failed");
          toast.error("Không thể kết nối lại", {
            description: "Vui lòng tải lại trang để tiếp tục nhận thông báo.",
          });
        }
      };

      // ── Per-event handlers ─────────────────────────────────
      KNOWN_EVENTS.forEach((eventType) => {
        source.addEventListener(eventType, (event) => {
          if (unmountedRef.current) return;

          const raw =
            "data" in event ? (event.data as string) : "";
          const payload = parsePayload(raw);

          // 1. Invalidate React Query cache → background refetch, no full-page reload
          const keys = EVENT_QUERY_KEY_MAP[eventType];
          if (keys.length > 0) {
            void queryClientRef.current.invalidateQueries({
              queryKey: [...keys],
            });
          }

          // 2. Build "View Detail" deep-link when payload contains an id
          const segment = EVENT_PATH_SEGMENT[eventType];
          const detailPath =
            payload.id && segment
              ? `${getRoleDashboardBase()}/${segment}/${payload.id}`
              : undefined;

          // 3. Show toast with optional "Xem chi tiết" action button
          toast.message(EVENT_TITLE[eventType], {
            description: payload.message ?? raw.slice(0, 140),
            action: detailPath
              ? {
                  label: "Xem chi tiết",
                  onClick: () => routerRef.current.push(detailPath),
                }
              : undefined,
          });
        });
      });

      // ── Test Trigger Listener (For Dev/Manual Testing) ─────
      const handleTestTrigger = (e: Event) => {
        const detail = (e as CustomEvent).detail;
        const eventType = detail.type as KnownEvent;
        const payload = { id: detail.id, message: detail.message };

        // Execute same logic as real SSE
        const keys = EVENT_QUERY_KEY_MAP[eventType];
        if (keys.length > 0) {
          void queryClientRef.current.invalidateQueries({ queryKey: [...keys] });
        }
        const segment = EVENT_PATH_SEGMENT[eventType];
        const detailPath = payload.id && segment ? `${getRoleDashboardBase()}/${segment}/${payload.id}` : undefined;

        toast.message(EVENT_TITLE[eventType], {
          description: payload.message,
          action: detailPath ? {
            label: "Xem chi tiết",
            onClick: () => routerRef.current.push(detailPath),
          } : undefined,
        });
      };
      window.addEventListener('sse-test-trigger', handleTestTrigger);

      return () => {
        unmountedRef.current = true;
        window.removeEventListener('sse-test-trigger', handleTestTrigger);
        if (retryTimerRef.current) clearTimeout(retryTimerRef.current);
        sourceRef.current?.close();
        sourceRef.current = null;
      };
  }, []); // run exactly once on mount

  return null;
}
