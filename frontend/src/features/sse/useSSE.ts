import { useEffect, useRef, useCallback } from 'react';
import { useAuthStore } from '@/features/auth/stores/authStore';

export interface SseEvent {
  eventType: string;
  payload: Record<string, unknown>;
  id?: string;
}

type SseHandler = (event: SseEvent) => void;

export const useSSE = (onEvent: SseHandler) => {
  const { isAuthenticated } = useAuthStore();
  const eventSourceRef = useRef<EventSource | null>(null);
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;

  const connect = useCallback(() => {
    if (!isAuthenticated || !token) return;

    const url = `${process.env.NEXT_PUBLIC_API_URL}/api/notifications/sse?token=${token}`;
    const es = new EventSource(url);
    eventSourceRef.current = es;

    es.onopen = () => {
      console.log('SSE connected');
    };

    es.onmessage = (e) => {
      try {
        const data = JSON.parse(e.data) as SseEvent;
        onEvent(data);
      } catch {
        console.error('SSE parse error', e.data);
      }
    };

    es.onerror = () => {
      console.warn('SSE error, reconnecting in 5s...');
      es.close();
      setTimeout(connect, 5000);
    };
  }, [isAuthenticated, token, onEvent]);

  useEffect(() => {
    connect();
    return () => {
      eventSourceRef.current?.close();
    };
  }, [connect]);
};