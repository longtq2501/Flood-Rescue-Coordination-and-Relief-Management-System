# Task 01: Warehouse Management

## Overview
Implement the UI for managing warehouses where relief supplies are stored. This module is part of the Resource Management section and is primarily used by Managers.

## UI Requirements
- **Warehouse List View**: A table or grid displaying all warehouses with their names, locations, and capacities.
- **Warehouse Details View**: A detailed view of a specific warehouse.
- **Create Warehouse Form**: A form to add a new warehouse.
- **Navigation**: Add "Warehouses" link to the Manager sidebar/navigation.

## Backend Integration
- **Service**: `resource-service`
- **Endpoints**:
  - `GET /api/resources/warehouses`: List all warehouses.
  - `POST /api/resources/warehouses`: Create a new warehouse.
  - `GET /api/resources/warehouses/{id}`: Get warehouse details.

## Technical Tasks
1. Create `Warehouse` type in `features/resource/types`.
2. Implement API calls in `features/resource/services/resource.service.ts`.
3. Create components in `features/resource/components/warehouse/`.
4. Register routes in `app/(app)/dashboard/manager/warehouses`.
