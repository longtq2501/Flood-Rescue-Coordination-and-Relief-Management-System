const DEFAULT_API_BASE_URL = "http://localhost:8080/api";

export const env = {
  apiBaseUrl: process.env.NEXT_PUBLIC_API_BASE_URL ?? DEFAULT_API_BASE_URL,
  sseUrl:
    process.env.NEXT_PUBLIC_SSE_URL ??
    "http://localhost:8080/api/notifications/sse",
};
