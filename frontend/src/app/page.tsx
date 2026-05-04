import { redirect } from "next/navigation";
import { cookies } from "next/headers";

import {
  USER_ROLE_KEY,
  ROLE_TO_DASHBOARD_PATH,
  type AppRole,
} from "@/shared/constants/auth";

function isAppRole(value: string): value is AppRole {
  return value in ROLE_TO_DASHBOARD_PATH;
}

export default async function Home() {
  const cookieStore = await cookies();
  const roleCookie = cookieStore.get(USER_ROLE_KEY)?.value;

  if (roleCookie && isAppRole(roleCookie)) {
    redirect(ROLE_TO_DASHBOARD_PATH[roleCookie]);
  }

  redirect("/login");
}
