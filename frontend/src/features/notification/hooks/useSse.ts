import { useEffect, useRef, useCallback } from 'react';
import { useAuthStore } from '@/features/auth/stores/authStore';

export interface SseEvent {
    eventType: string;
    payload: Record<string, unknown>;
    id?: string;
}

type SseHandler = (event: SseEvent) => void;

export const useSse = (onEvent: SseHandler) => {
    const { isAuthenticated } = useAuthStore();
    const eventSourceRef = useRef<EventSource | null>(null);
    const token = localStorage.getItem('accessToken');

    const connect = useCallback(() => {
        if (!isAuthenticated || !token) return;

        // SSE không support custom header nên truyền token qua query param
        // Backend cần support: GET /api/notifications/sse?token=xxx
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

        es.addEventListener('connected', () => {
            console.log('SSE ready');
        });

        es.onerror = () => {
            console.warn('SSE error, reconnecting in 5s...');
            es.close();
            // Auto reconnect sau 5 giây
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