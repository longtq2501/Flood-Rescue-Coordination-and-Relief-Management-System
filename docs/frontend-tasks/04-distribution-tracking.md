# Task 04: Relief Distribution Tracking

## Overview
Implement the UI for tracking the distribution of relief items to specific rescue requests. This module links the resources (Inventory) with the needs (Requests).

## UI Requirements
- **Distribution History**: A list of all distribution events, showing what was sent, where, and when.
- **Request-specific Distributions**: View all distributions made for a specific rescue request.
- **Create Distribution**: A form to allocate items from a warehouse to a request.
- **Navigation**: Integrated into the Request details view and a standalone "Distributions" page for Managers.

## Backend Integration
- **Service**: `resource-service`
- **Endpoints**:
  - `GET /api/resources/distributions`: List all distributions.
  - `GET /api/resources/distributions/by-request/{requestId}`: List distributions for a specific request.
  - `POST /api/resources/distributions`: Create a new distribution record.

## Technical Tasks
1. Create `Distribution` type in `features/resource/types`.
2. Implement `getDistributions` and `createDistribution` in `resource.service.ts`.
3. Create components in `features/resource/components/distribution/`.
4. Integrate "Distribute Relief" button into the Coordinator and Manager views of a Request.
