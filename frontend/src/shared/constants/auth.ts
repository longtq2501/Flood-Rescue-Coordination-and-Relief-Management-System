export const ACCESS_TOKEN_KEY = "fr_access_token";
export const REFRESH_TOKEN_KEY = "fr_refresh_token";
export const USER_ROLE_KEY = "fr_user_role";

export const APP_ROLES = [
  "CITIZEN",
  "RESCUE_TEAM",
  "COORDINATOR",
  "MANAGER",
  "ADMIN",
] as const;

export type AppRole = (typeof APP_ROLES)[number];

export const ROLE_TO_DASHBOARD_PATH: Record<AppRole, string> = {
  CITIZEN: "/dashboard/citizen",
  RESCUE_TEAM: "/dashboard/rescue-team",
  COORDINATOR: "/dashboard/coordinator",
  MANAGER: "/dashboard/manager",
  ADMIN: "/dashboard/admin",
};
