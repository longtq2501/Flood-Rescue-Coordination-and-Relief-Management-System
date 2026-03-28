import { ReactNode } from 'react';

interface RoleGuardProps {
  children: ReactNode;
  allowedRoles?: string[]; // nếu không dùng thì có thể bỏ qua
}

export default function RoleGuard({ children }: RoleGuardProps) {
  return <>{children}</>;
}