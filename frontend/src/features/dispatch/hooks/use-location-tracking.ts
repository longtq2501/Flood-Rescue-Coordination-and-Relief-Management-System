"use client";

import { useState, useEffect, useRef } from "react";
import { updateLocation } from "../services/dispatch.service";
import type { LocationUpdateRequest } from "../types/dispatch.types";

interface LocationState {
  coords: {
    lat: number;
    lng: number;
    speed: number | null;
    heading: number | null;
  } | null;
  error: string | null;
  lastUpdated: Date | null;
}

export function useLocationTracking(isEnabled: boolean, intervalMs: number = 15000) {
  const [state, setState] = useState<LocationState>({
    coords: null,
    error: null,
    lastUpdated: null,
  });

  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (!isEnabled) {
      if (timerRef.current) clearInterval(timerRef.current);
      return;
    }

    const fetchAndSendLocation = () => {
      if (!navigator.geolocation) {
        setState((prev) => ({ ...prev, error: "Geolocation is not supported by your browser" }));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const { latitude, longitude, speed, heading } = position.coords;
          const payload: LocationUpdateRequest = {
            lat: latitude,
            lng: longitude,
            speed: speed ?? undefined,
            heading: heading ?? undefined,
          };

          try {
            await updateLocation(payload);
            setState({
              coords: {
                lat: latitude,
                lng: longitude,
                speed,
                heading,
              },
              error: null,
              lastUpdated: new Date(),
            });
          } catch (err: unknown) {
            // Silence the error if it's just because the user isn't assigned to a team yet
            let errorMsg = "";
            let errorCode = "";
            
            if (err && typeof err === 'object' && 'response' in err) {
              const axiosError = err as { response: { data: { message?: string, code?: string } } };
              errorMsg = axiosError.response.data.message || "";
              errorCode = axiosError.response.data.code || "";
            } else if (err instanceof Error) {
              errorMsg = err.message;
            }

            if (errorCode === "ERR_TEAM_404" || errorMsg.includes("đội cứu hộ") || errorMsg.includes("404")) {
              setState((prev) => ({ ...prev, error: "Tài khoản chưa được gán vào đội cứu hộ" }));
            } else {
              console.error("Failed to update location:", err);
              setState((prev) => ({ ...prev, error: "Lỗi kết nối máy chủ định vị" }));
            }
          }
        },
        (error) => {
          setState((prev) => ({ ...prev, error: error.message }));
        },
        { enableHighAccuracy: true }
      );
    };

    // Initial fetch
    fetchAndSendLocation();

    // Set interval
    timerRef.current = setInterval(fetchAndSendLocation, intervalMs);

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [isEnabled, intervalMs]);

  return state;
}
