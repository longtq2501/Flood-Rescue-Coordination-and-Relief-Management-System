import axiosInstance from '@/shared/api/axiosInstance';
import { ApiResponse, PageResponse, PageParams } from '@/shared/types/api.types';
import {
    WarehouseResponse,
    VehicleResponse,
    VehicleStatus,
    VehicleType,
    CreateDistributionRequest,
    DistributionResponse,
} from '../types/resource.types';

export const resourceApi = {

    // Warehouses
    getWarehouses: async (): Promise<WarehouseResponse[]> => {
        const res = await axiosInstance.get<ApiResponse<WarehouseResponse[]>>(
            '/api/resources/warehouses'
        );
        return res.data.data;
    },

    getWarehouseById: async (id: number): Promise<WarehouseResponse> => {
        const res = await axiosInstance.get<ApiResponse<WarehouseResponse>>(
            `/api/resources/warehouses/${id}`
        );
        return res.data.data;
    },

    // Vehicles
    getVehicles: async (
        status?: VehicleStatus,
        type?: VehicleType
    ): Promise<VehicleResponse[]> => {
        const res = await axiosInstance.get<ApiResponse<VehicleResponse[]>>(
            '/api/resources/vehicles', { params: { status, type } }
        );
        return res.data.data;
    },

    // Distributions
    createDistribution: async (
        data: CreateDistributionRequest
    ): Promise<DistributionResponse> => {
        const res = await axiosInstance.post<ApiResponse<DistributionResponse>>(
            '/api/resources/distributions', data
        );
        return res.data.data;
    },

    getDistributions: async (
        params: PageParams
    ): Promise<PageResponse<DistributionResponse>> => {
        const res = await axiosInstance.get<ApiResponse<PageResponse<DistributionResponse>>>(
            '/api/resources/distributions', { params }
        );
        return res.data.data;
    },
};