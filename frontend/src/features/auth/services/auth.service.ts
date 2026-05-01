"use client";

import { apiGet, apiPost, clearAuthCookies, setAuthCookies } from "@/shared/api/http";
import type {
  AuthTokens,
  AuthUser,
  LoginRequest,
  RegisterRequest,
} from "@/shared/types/api";

export async function login(payload: LoginRequest) {
  const response = await apiPost<AuthTokens, LoginRequest>("/auth/login", payload);
  if (!response.success) {
    throw new Error(response.message || "Dang nhap that bai");
  }
  setAuthCookies(response.data);
  return response.data;
}

export async function register(payload: RegisterRequest) {
  const response = await apiPost<AuthUser, RegisterRequest>("/auth/register", payload);
  if (!response.success) {
    throw new Error(response.message || "Dang ky that bai");
  }
  return response.data;
}

export async function getMe() {
  const response = await apiGet<AuthUser>("/auth/me");
  if (!response.success) {
    throw new Error(response.message || "Khong lay duoc thong tin user");
  }
  return response.data;
}

export async function logout(refreshToken: string) {
  const response = await apiPost<null, { refreshToken: string }>("/auth/logout", {
    refreshToken,
  });
  if (!response.success) {
    throw new Error(response.message || "Dang xuat that bai");
  }
  clearAuthCookies();
  return response;
}
