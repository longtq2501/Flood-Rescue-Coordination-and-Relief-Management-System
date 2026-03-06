'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/features/auth/stores/authStore';
import { ROUTES } from '@/constants/routes';
import { Role } from '@/constants/roles';

interface RoleGuardProps {
    allowedRoles: Role[];
    children: React.ReactNode;
}

export default function RoleGuard({ allowedRoles, children }: RoleGuardProps) {
    const router = useRouter();
    const { user, isAuthenticated } = useAuthStore();

    useEffect(() => {
        if (!isAuthenticated) {
            router.replace(ROUTES.LOGIN);
            return;
        }
        if (user && !allowedRoles.includes(user.role as Role)) {
            // Redirect về trang đúng role
            router.replace(ROUTES.LOGIN);
        }
    }, [isAuthenticated, user, allowedRoles, router]);

    if (!isAuthenticated || !user) return null;
    if (!allowedRoles.includes(user.role as Role)) return null;

    return <>{children}</>;
}