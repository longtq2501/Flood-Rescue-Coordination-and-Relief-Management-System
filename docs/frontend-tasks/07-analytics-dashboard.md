# Task 07: Management Analytics Dashboard

## Overview
Implement the high-level dashboard for Managers and Coordinators, providing statistical summaries and trends of the rescue operation.

## UI Requirements
- **Statistics Cards**: Total requests, active missions, available teams, and warehouse occupancy levels.
- **Charts/Graphs**:
  - Request volume over time.
  - Distribution of request status (Pending, Verified, Completed, Cancelled).
  - Urgency level breakdown.
- **Recent Activity**: A feed of the latest events in the system.

## Backend Integration
- **Service**: `report-service`
- **Endpoints**:
  - `GET /api/reports/dashboard`: Get aggregated statistics and chart data.

## Technical Tasks
1. Create `DashboardData` type in `features/report/types`.
2. Implement `getDashboardData` in `features/report/services/report.service.ts`.
3. Create `AnalyticsDashboard` component using a chart library (e.g., Recharts or Chart.js).
4. Integrate into the main dashboard routes for Manager and Coordinator roles.
