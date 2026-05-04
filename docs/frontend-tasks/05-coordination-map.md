# Task 05: Interactive Coordination Map

## Overview
Implement a comprehensive map view for Coordinators to visualize all active requests, rescue teams, and warehouses in real-time.

## UI Requirements
- **Integrated Map**: A large map (Leaflet or Mapbox) with markers for:
  - Rescue Requests (color-coded by urgency).
  - Rescue Teams (icon showing status).
  - Warehouses.
- **Marker Interactivity**: Clicking a marker shows a popup with basic info and a link to full details.
- **Layers Toggle**: Ability to toggle visibility of requests, teams, or warehouses.
- **Navigation**: "Full Map" link in the Coordinator dashboard.

## Backend Integration
- **Service**: `dispatch-service`
- **Endpoints**:
  - `GET /api/dispatch/map`: Get aggregated map data (locations of requests, teams, etc.).

## Technical Tasks
1. Create `MapData` type in `features/dispatch/types`.
2. Implement `getMapData` in `dispatch.service.ts`.
3. Create `CoordinationMap` component in `features/dispatch/components/map/`.
4. Implement dynamic marker rendering and clustering.
