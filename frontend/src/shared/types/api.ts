import type { AppRole } from "@/shared/constants/auth";

export type ApiSuccess<T> = {
  success: true;
  message?: string;
  data: T;
  timestamp?: string;
};

export type ApiError = {
  success: false;
  code?: string;
  message: string;
  timestamp?: string;
};

export type ApiResponse<T> = ApiSuccess<T> | ApiError;

export type AuthUser = {
  id: number;
  fullName: string;
  phone: string;
  email?: string | null;
  role: AppRole;
  avatarUrl?: string | null;
};

export type LoginRequest = {
  phone: string;
  password: string;
};

export type RegisterRequest = {
  fullName: string;
  phone: string;
  email?: string;
  password: string;
  role: Extract<AppRole, "CITIZEN" | "RESCUE_TEAM">;
};

export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
  tokenType: "Bearer";
  expiresIn: number;
  user: AuthUser;
};
