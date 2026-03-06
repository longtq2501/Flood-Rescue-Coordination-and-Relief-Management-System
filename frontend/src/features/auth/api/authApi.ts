import axiosInstance from '@/shared/api/axiosInstance';
import { ApiResponse } from '@/shared/types/api.types';
import { LoginRequest, LoginResponse, RegisterRequest, User } from '../types/auth.types';

export const authApi = {

    login: async (data: LoginRequest): Promise<LoginResponse> => {
        const res = await axiosInstance.post<ApiResponse<LoginResponse>>(
            '/api/auth/login', data
        );
        return res.data.data;
    },

    register: async (data: RegisterRequest): Promise<User> => {
        const res = await axiosInstance.post<ApiResponse<User>>(
            '/api/auth/register', data
        );
        return res.data.data;
    },

    getMe: async (): Promise<User> => {
        const res = await axiosInstance.get<ApiResponse<User>>('/api/auth/me');
        return res.data.data;
    },

    logout: async (): Promise<void> => {
        const refreshToken = localStorage.getItem('refreshToken');
        await axiosInstance.post('/api/auth/logout', { refreshToken });
    },
};