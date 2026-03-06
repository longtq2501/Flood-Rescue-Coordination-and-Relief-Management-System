'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/features/auth/stores/authStore';
import { ROUTES } from '@/constants/routes';
import { ROLES } from '@/constants/roles';

export default function RootPage() {
    const router = useRouter();
    const { user, isAuthenticated } = useAuthStore();

    useEffect(() => {
        if (!isAuthenticated) {
            router.replace(ROUTES.LOGIN);
            return;
        }
        switch (user?.role) {
            case ROLES.CITIZEN: router.replace(ROUTES.CITIZEN.REQUESTS); break;
            case ROLES.COORDINATOR: router.replace(ROUTES.COORDINATOR.DASHBOARD); break;
            case ROLES.RESCUE_TEAM: router.replace(ROUTES.RESCUE_TEAM.MISSIONS); break;
            case ROLES.MANAGER:
            case ROLES.ADMIN: router.replace(ROUTES.MANAGER.DASHBOARD); break;
            default: router.replace(ROUTES.LOGIN);
        }
    }, [isAuthenticated, user, router]);

    return null;
}