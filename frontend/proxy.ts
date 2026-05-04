import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

import {
  ACCESS_TOKEN_KEY,
  ROLE_TO_DASHBOARD_PATH,
  USER_ROLE_KEY,
  type AppRole,
} from "./src/shared/constants/auth";

const authRoutes = ["/login", "/register"];
const dashboardPrefix = "/dashboard";

function parseRole(value?: string): AppRole | null {
  if (!value) {
    return null;
  }

  if (value in ROLE_TO_DASHBOARD_PATH) {
    return value as AppRole;
  }

  return null;
}

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  const accessToken = request.cookies.get(ACCESS_TOKEN_KEY)?.value;
  const role = parseRole(request.cookies.get(USER_ROLE_KEY)?.value);

  if (authRoutes.includes(pathname) && accessToken && role) {
    return NextResponse.redirect(
      new URL(ROLE_TO_DASHBOARD_PATH[role], request.url),
    );
  }

  if (pathname.startsWith(dashboardPrefix)) {
    if (!accessToken || !role) {
      return NextResponse.redirect(new URL("/login", request.url));
    }

    if (role === "ADMIN") {
      return NextResponse.next();
    }

    const expectedPath = ROLE_TO_DASHBOARD_PATH[role];
    if (!pathname.startsWith(expectedPath)) {
      return NextResponse.redirect(new URL(expectedPath, request.url));
    }
  }

  return NextResponse.next();
}

export const proxyConfig = {
  matcher: ["/login", "/register", "/dashboard/:path*", "/"],
};
