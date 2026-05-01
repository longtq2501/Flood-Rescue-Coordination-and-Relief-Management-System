/**
 * Zustand store for SSE connection status.
 * Components can subscribe to show a real-time connectivity badge
 * without prop-drilling or triggering SseBootstrap re-renders.
 */
import { create } from "zustand";

export type SseConnectionStatus = "idle" | "connected" | "reconnecting" | "failed";

interface SseStatusState {
  status: SseConnectionStatus;
  attempt: number;
  setStatus: (status: SseConnectionStatus, attempt?: number) => void;
}

export const useSseStatus = create<SseStatusState>((set) => ({
  status: "idle",
  attempt: 0,
  setStatus: (status, attempt = 0) => set({ status, attempt }),
}));
