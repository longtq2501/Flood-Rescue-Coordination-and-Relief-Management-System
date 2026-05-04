export type NotificationType = "URGENT_REQUEST" | "MISSION_ASSIGNED" | "STATUS_UPDATE" | "SYSTEM_ALERT";

export interface Notification {
  id: string;
  userId: number;
  title: string;
  message: string;
  type: NotificationType;
  relatedId?: string;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  addNotification: (notification: Notification) => void;
  setNotifications: (notifications: Notification[]) => void;
  markAsRead: (id: string) => void;
  markAllAsRead: () => void;
}
