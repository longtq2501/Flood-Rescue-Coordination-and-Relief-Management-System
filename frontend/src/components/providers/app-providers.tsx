"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "sonner";
import { useEffect, useState } from "react";
import dynamic from "next/dynamic";
import { normalizeAuthCookies } from "@/shared/api/http";
const SseBootstrap = dynamic(
  () => import("@/shared/realtime/sse-bootstrap").then((mod) => mod.SseBootstrap),
  { ssr: false }
);

export function AppProviders({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            retry: 1,
            refetchOnWindowFocus: false,
          },
        },
      }),
  );

  useEffect(() => {
    normalizeAuthCookies();
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <SseBootstrap />
      {children}
      <Toaster richColors position="top-right" />
    </QueryClientProvider>
  );
}
