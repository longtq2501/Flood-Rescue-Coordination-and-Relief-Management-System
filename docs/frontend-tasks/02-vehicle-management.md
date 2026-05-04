# Task 02: Vehicle Management

## Overview
Implement the UI for managing rescue vehicles (boats, trucks, helicopters, etc.). This module is essential for tracking the status and availability of transport resources.

## UI Requirements
- **Vehicle List View**: Filterable list of vehicles by status (Available, In Use, Maintenance) and type.
- **Add Vehicle Form**: Form to register new vehicles with plate numbers and capacity.
- **Status Update**: Quick actions to change vehicle status (e.g., mark as in maintenance or available).
- **Navigation**: Add "Vehicles" link to the Manager/Coordinator sidebar.

## Backend Integration
- **Service**: `resource-service`
- **Endpoints**:
  - `GET /api/resources/vehicles`: List vehicles with optional filters (`status`, `type`).
  - `POST /api/resources/vehicles`: Create new vehicle.
  - `PATCH /api/resources/vehicles/{id}/status`: Update vehicle status and add notes.

## Technical Tasks
1. Update `Vehicle` type in `features/resource/types`.
2. Implement `addVehicle` and `updateVehicleStatus` in `resource.service.ts`.
3. Create components in `features/resource/components/vehicle/`.
4. Register routes in `app/(app)/dashboard/manager/vehicles`.
