export interface NotificationResponse {
    id: number;
    eventType: string;
    channel: string;
    payload: string;
    status: 'PENDING' | 'SENT' | 'FAILED';
    sentAt?: string;
    createdAt: string;
}