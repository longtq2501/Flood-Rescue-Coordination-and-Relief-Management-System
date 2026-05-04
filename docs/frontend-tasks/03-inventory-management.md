# Task 03: Inventory & Stock Management

## Overview
Implement the UI for tracking relief items (food, water, medicine, etc.) stored within warehouses. This includes adding items and updating stock levels.

## UI Requirements
- **Inventory List (per Warehouse)**: Display items in a specific warehouse with current stock quantities and unit types.
- **Add Relief Item Form**: Form to add a new type of item to a warehouse.
- **Update Stock**: Modal or inline form to adjust stock levels (add/subtract quantities).
- **Navigation**: Integrated into the Warehouse details view or as a standalone "Inventory" page.

## Backend Integration
- **Service**: `resource-service`
- **Endpoints**:
  - `GET /api/resources/items?warehouseId={id}`: List items in a specific warehouse.
  - `POST /api/resources/items`: Add a new relief item.
  - `PATCH /api/resources/items/{id}/stock`: Update item stock quantity.

## Technical Tasks
1. Create `ReliefItem` type in `features/resource/types`.
2. Implement `getItemsByWarehouse`, `addItem`, and `updateStock` in `resource.service.ts`.
3. Create components in `features/resource/components/inventory/`.
4. Implement stock adjustment logic and validation.
