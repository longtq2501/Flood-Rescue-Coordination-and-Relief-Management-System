export const ROUTES = {
    // Auth
    LOGIN: '/login',
    REGISTER: '/register',

    // Citizen
    CITIZEN: {
        REQUESTS: '/requests',
        NEW_REQUEST: '/requests/new',
        REQUEST: (id: number) => `/requests/${id}`,
    },

    // Coordinator
    COORDINATOR: {
        DASHBOARD: '/coordinator/dashboard',
        MAP: '/coordinator/map',
        ASSIGNMENTS: '/coordinator/assignments',
    },

    // Rescue Team
    RESCUE_TEAM: {
        MISSIONS: '/missions',
        MISSION: (id: number) => `/missions/${id}`,
    },

    // Manager
    MANAGER: {
        DASHBOARD: '/manager/dashboard',
        WAREHOUSES: '/manager/warehouses',
        VEHICLES: '/manager/vehicles',
    },
} as const;