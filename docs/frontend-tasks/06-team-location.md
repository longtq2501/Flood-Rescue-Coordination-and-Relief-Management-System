# Task 06: Real-time Team Location Tracking

## Overview
Implement the functionality for Rescue Teams to report their current GPS location and for the system to track them in real-time.

## UI Requirements
- **Location Reporter (Rescue Team)**: A background process (or manual button) in the Rescue Team app to send current GPS coordinates.
- **Location Status Indicator**: Visual confirmation for the team member that their location is being tracked.
- **Auto-Update**: Periodically send coordinates when the team is "On Duty".

## Backend Integration
- **Service**: `dispatch-service`
- **Endpoints**:
  - `POST /api/dispatch/location`: Send current latitude and longitude.

## Technical Tasks
1. Implement `updateLocation` in `dispatch.service.ts`.
2. Create a custom hook `useLocationTracking` to handle browser Geolocation API.
3. Integrate tracking into `app/(app)/dashboard/rescue-team`.
4. Ensure appropriate permissions handling and error messages if GPS is disabled.
