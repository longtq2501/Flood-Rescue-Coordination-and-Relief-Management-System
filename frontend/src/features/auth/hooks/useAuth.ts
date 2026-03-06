import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { authApi } from '../api/authApi';
import { useAuthStore } from '../stores/authStore';
import { LoginRequest, RegisterRequest } from '../types/auth.types';
import { QUERY_KEYS } from '@/constants/queryKeys';
import { ROUTES } from '@/constants/routes';
import { ROLES } from '@/constants/roles';

export const useAuth = () => {
    const router = useRouter();
    const queryClient = useQueryClient();
    const { user, isAuthenticated, setAuth, setUser, clearAuth } = useAuthStore();

    // Login mutation
    const loginMutation = useMutation({
        mutationFn: (data: LoginRequest) => authApi.login(data),
        onSuccess: (data) => {
            setAuth(data.user, data.accessToken, data.refreshToken);
            // Redirect theo role
            redirectByRole(data.user.role);
        },
    });

    // Register mutation
    const registerMutation = useMutation({
        mutationFn: (data: RegisterRequest) => authApi.register(data),
        onSuccess: () => {
            router.push(ROUTES.LOGIN);
        },
    });

    // Logout mutation
    const logoutMutation = useMutation({
        mutationFn: () => authApi.logout(),
        onSettled: () => {
            clearAuth();
            queryClient.clear();
            router.push(ROUTES.LOGIN);
        },
    });

    // Lấy thông tin user hiện tại
    const { data: currentUser } = useQuery({
        queryKey: QUERY_KEYS.ME,
        queryFn: authApi.getMe,
        enabled: isAuthenticated,
        staleTime: 5 * 60 * 1000, // 5 phút
    });

    const redirectByRole = (role: string) => {
        switch (role) {
            case ROLES.CITIZEN: return router.push(ROUTES.CITIZEN.REQUESTS);
            case ROLES.COORDINATOR: return router.push(ROUTES.COORDINATOR.DASHBOARD);
            case ROLES.RESCUE_TEAM: return router.push(ROUTES.RESCUE_TEAM.MISSIONS);
            case ROLES.MANAGER:
            case ROLES.ADMIN: return router.push(ROUTES.MANAGER.DASHBOARD);
            default: return router.push(ROUTES.LOGIN);
        }
    };

    return {
        user: currentUser ?? user,
        isAuthenticated,
        login: loginMutation.mutate,
        register: registerMutation.mutate,
        logout: logoutMutation.mutate,
        isLoggingIn: loginMutation.isPending,
        isRegistering: registerMutation.isPending,
        loginError: loginMutation.error,
        registerError: registerMutation.error,
    };
};