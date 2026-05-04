# Task 09: Advanced Request Search & Filters

## Overview
Enhance the Rescue Request board for Coordinators with advanced search, sorting, and filtering capabilities to manage high volumes of requests efficiently.

## UI Requirements
- **Filter Sidebar/Header**: Ability to filter by:
  - Status (Pending, Verified, In Progress, etc.).
  - Urgency Level (Critical, High, Medium, Low).
  - Date Range (From/To).
- **Search Bar**: Search requests by description or location text.
- **Pagination**: Implement server-side pagination controls.

## Backend Integration
- **Service**: `request-service`
- **Endpoints**:
  - `GET /api/requests`: Support query parameters (`status`, `urgencyLevel`, `fromDate`, `toDate`, `page`, `size`).

## Technical Tasks
1. Update `fetchCoordinatorRequests` in `request.service.ts` to accept filter parameters.
2. Create `RequestFilterBar` component in `features/request/components/`.
3. Implement state management for filters (URL-based or local state).
4. Synchronize filtering with the map view if possible.
