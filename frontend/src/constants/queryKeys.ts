export const QUERY_KEYS = {
    // Auth
    ME: ['me'],

    // Request
    REQUESTS: ['requests'],
    REQUEST: (id: number) => ['requests', id],
    MY_REQUESTS: ['requests', 'my'],

    // Dispatch
    TEAMS: ['teams'],
    TEAM: (id: number) => ['teams', id],
    ASSIGNMENTS: ['assignments'],
    MY_ASSIGNMENTS: ['assignments', 'my'],
    MAP_DATA: ['map-data'],

    // Resource
    WAREHOUSES: ['warehouses'],
    WAREHOUSE: (id: number) => ['warehouses', id],
    ITEMS: (warehouseId: number) => ['items', warehouseId],
    VEHICLES: ['vehicles'],

    // Report
    DASHBOARD: ['dashboard'],

    // Notification
    NOTIFICATIONS: ['notifications'],
} as const;