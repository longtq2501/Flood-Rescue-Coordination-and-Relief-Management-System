import { GlobalLayout } from "@/components/global-layout/global-layout";

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return <GlobalLayout>{children}</GlobalLayout>;
}
