import axiosInstance from '@/shared/api/axiosInstance';
import { ApiResponse } from '@/shared/types/api.types';
import { DashboardResponse } from '../types/report.types';

export const reportApi = {

    getDashboard: async (): Promise<DashboardResponse> => {
        const res = await axiosInstance.get<ApiResponse<DashboardResponse>>(
            '/api/reports/dashboard'
        );
        return res.data.data;
    },
};