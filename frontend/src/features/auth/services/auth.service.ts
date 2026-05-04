"use client";

import { apiGet, apiPost, apiPut, clearAuthCookies, setAuthCookies } from "@/shared/api/http";
import type {
  AuthTokens,
  AuthUser,
  ChangePasswordRequest,
  LoginRequest,
  RegisterRequest,
  UpdateProfileRequest,
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

export async function updateProfile(payload: UpdateProfileRequest) {
  const response = await apiPut<AuthUser, UpdateProfileRequest>("/auth/me", payload);
  if (!response.success) {
    throw new Error(response.message || "Cập nhật hồ sơ thất bại");
  }
  return response.data;
}

export async function changePassword(payload: ChangePasswordRequest) {
  const response = await apiPut<null, ChangePasswordRequest>("/auth/change-password", payload);
  if (!response.success) {
    throw new Error(response.message || "Đổi mật khẩu thất bại");
  }
  return response.data;
}
