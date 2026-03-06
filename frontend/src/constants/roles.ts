export const ROLES = {
    CITIZEN: 'CITIZEN',
    COORDINATOR: 'COORDINATOR',
    RESCUE_TEAM: 'RESCUE_TEAM',
    MANAGER: 'MANAGER',
    ADMIN: 'ADMIN',
} as const;

export type Role = keyof typeof ROLES;