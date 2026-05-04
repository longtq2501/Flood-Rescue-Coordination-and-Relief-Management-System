# Task 10: Real-time Notification System

## Overview
Implement the UI for receiving and viewing real-time alerts and notifications using Server-Sent Events (SSE).

## UI Requirements
- **Notification Toast**: Popup alerts for new urgent requests or mission assignments.
- **Notification Center**: A sidebar or dropdown menu listing historical notifications.
- **Unread Counter**: A badge showing the number of unread notifications.

## Backend Integration
- **Service**: `notification-service`
- **Endpoints**:
  - `GET /api/notifications/sse`: Subscribe to real-time events.
  - `GET /api/notifications`: Fetch notification history.

## Technical Tasks
1. Create `Notification` type in `features/notification/types`.
2. Implement `useNotifications` hook to handle SSE connection and state.
3. Create `NotificationBell` and `NotificationList` components.
4. Integrate SSE listener at the layout level to ensure global coverage.
