"use client";

import axios, {
  AxiosError,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from "axios";
import Cookies from "js-cookie";

import { env } from "@/shared/config/env";
import {
  ACCESS_TOKEN_KEY,
  REFRESH_TOKEN_KEY,
  USER_ROLE_KEY,
} from "@/shared/constants/auth";
import type { ApiResponse, AuthTokens } from "@/shared/types/api";

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
};

const http = axios.create({
  baseURL: env.apiBaseUrl,
  timeout: 15000,
});

const refreshClient = axios.create({
  baseURL: env.apiBaseUrl,
  timeout: 15000,
});

let isRefreshing = false;
let pendingQueue: Array<(token: string | null) => void> = [];

function processQueue(token: string | null) {
  pendingQueue.forEach((resolve) => resolve(token));
  pendingQueue = [];
}

function setAuthCookies(tokens: AuthTokens) {
  Cookies.set(ACCESS_TOKEN_KEY, tokens.accessToken, { sameSite: "strict" });
  Cookies.set(REFRESH_TOKEN_KEY, tokens.refreshToken, { sameSite: "strict" });
  Cookies.set(USER_ROLE_KEY, tokens.user.role, { sameSite: "strict" });
}

function clearAuthCookies() {
  Cookies.remove(ACCESS_TOKEN_KEY);
  Cookies.remove(REFRESH_TOKEN_KEY);
  Cookies.remove(USER_ROLE_KEY);
}

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const accessToken = Cookies.get(ACCESS_TOKEN_KEY);
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;
    if (!originalRequest) {
      return Promise.reject(error);
    }

    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }

    const refreshToken = Cookies.get(REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      clearAuthCookies();
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        pendingQueue.push((token) => {
          if (!token) {
            reject(error);
            return;
          }
          originalRequest.headers.Authorization = `Bearer ${token}`;
          resolve(http(originalRequest));
        });
      });
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const response = await refreshClient.post<ApiResponse<AuthTokens>>(
        "/auth/refresh",
        { refreshToken },
      );

      if (!response.data.success) {
        throw new Error(response.data.message || "Refresh token failed");
      }

      setAuthCookies(response.data.data);
      processQueue(response.data.data.accessToken);

      originalRequest.headers.Authorization =
        `Bearer ${response.data.data.accessToken}`;
      return http(originalRequest);
    } catch (refreshError) {
      processQueue(null);
      clearAuthCookies();
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);

export async function apiGet<T>(url: string, config?: AxiosRequestConfig) {
  const response = await http.get<ApiResponse<T>>(url, config);
  return response.data;
}

export async function apiPost<T, B>(
  url: string,
  body?: B,
  config?: AxiosRequestConfig,
) {
  const response = await http.post<ApiResponse<T>>(url, body, config);
  return response.data;
}

export async function apiPut<T, B>(
  url: string,
  body?: B,
  config?: AxiosRequestConfig,
) {
  const response = await http.put<ApiResponse<T>>(url, body, config);
  return response.data;
}

export async function apiPatch<T, B>(
  url: string,
  body?: B,
  config?: AxiosRequestConfig,
) {
  const response = await http.patch<ApiResponse<T>>(url, body, config);
  return response.data;
}

export async function apiDelete<T>(url: string, config?: AxiosRequestConfig) {
  const response = await http.delete<ApiResponse<T>>(url, config);
  return response.data;
}

export { clearAuthCookies, setAuthCookies, http };
