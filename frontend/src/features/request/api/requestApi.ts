import axiosInstance from '@/shared/api/axiosInstance';
import { ApiResponse, PageResponse, PageParams } from '@/shared/types/api.types';
import {
    CreateRescueRequestDto,
    RescueRequestResponse,
    VerifyRequestDto,
    CancelRequestDto,
    RequestFilterParams,
} from '../types/request.types';

export const requestApi = {

    create: async (
        data: CreateRescueRequestDto,
        images?: File[]
    ): Promise<RescueRequestResponse> => {
        const formData = new FormData();
        formData.append('data', new Blob([JSON.stringify(data)], {
            type: 'application/json'
        }));
        images?.forEach((img) => formData.append('images', img));

        const res = await axiosInstance.post<ApiResponse<RescueRequestResponse>>(
            '/api/requests',
            formData,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        );
        return res.data.data;
    },

    getAll: async (
        params: RequestFilterParams & PageParams
    ): Promise<PageResponse<RescueRequestResponse>> => {
        const res = await axiosInstance.get<ApiResponse<PageResponse<RescueRequestResponse>>>(
            '/api/requests', { params }
        );
        return res.data.data;
    },

    getMy: async (
        params: PageParams
    ): Promise<PageResponse<RescueRequestResponse>> => {
        const res = await axiosInstance.get<ApiResponse<PageResponse<RescueRequestResponse>>>(
            '/api/requests/my', { params }
        );
        return res.data.data;
    },

    getById: async (id: number): Promise<RescueRequestResponse> => {
        const res = await axiosInstance.get<ApiResponse<RescueRequestResponse>>(
            `/api/requests/${id}`
        );
        return res.data.data;
    },

    verify: async (id: number, data?: VerifyRequestDto): Promise<RescueRequestResponse> => {
        const res = await axiosInstance.patch<ApiResponse<RescueRequestResponse>>(
            `/api/requests/${id}/verify`, data
        );
        return res.data.data;
    },

    cancel: async (id: number, data: CancelRequestDto): Promise<RescueRequestResponse> => {
        const res = await axiosInstance.patch<ApiResponse<RescueRequestResponse>>(
            `/api/requests/${id}/cancel`, data
        );
        return res.data.data;
    },

    confirm: async (id: number): Promise<RescueRequestResponse> => {
        const res = await axiosInstance.patch<ApiResponse<RescueRequestResponse>>(
            `/api/requests/${id}/confirm`
        );
        return res.data.data;
    },
};