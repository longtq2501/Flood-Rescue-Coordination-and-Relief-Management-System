"use client";

import Cookies from "js-cookie";
import { create } from "zustand";
import { persist } from "zustand/middleware";

import {
  ACCESS_TOKEN_KEY,
  REFRESH_TOKEN_KEY,
  USER_ROLE_KEY,
  type AppRole,
} from "@/shared/constants/auth";
import type { AuthUser } from "@/shared/types/api";

type AuthState = {
  user: AuthUser | null;
  role: AppRole | null;
  hydrated: boolean;
  setSession: (user: AuthUser) => void;
  clearSession: () => void;
  updateUser: (user: AuthUser) => void;
  setHydrated: (value: boolean) => void;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      role: null,
      hydrated: false,
      setSession: (user) =>
        set({
          user,
          role: user.role,
        }),
      updateUser: (user) =>
        set({
          user,
        }),
      clearSession: () => {
        Cookies.remove(ACCESS_TOKEN_KEY);
        Cookies.remove(REFRESH_TOKEN_KEY);
        Cookies.remove(USER_ROLE_KEY);
        set({ user: null, role: null });
      },
      setHydrated: (value) => set({ hydrated: value }),
    }),
    {
      name: "fr-auth-session",
      partialize: (state) => ({ user: state.user, role: state.role }),
      onRehydrateStorage: () => (state) => {
        state?.setHydrated(true);
      },
    },
  ),
);
