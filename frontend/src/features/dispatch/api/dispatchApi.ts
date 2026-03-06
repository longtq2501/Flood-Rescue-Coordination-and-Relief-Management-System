import axiosInstance from '@/shared/api/axiosInstance';
import { ApiResponse, PageResponse, PageParams } from '@/shared/types/api.types';
import {
    RescueTeamResponse,
    AssignmentResponse,
    AssignTeamRequest,
    LocationUpdateRequest,
    MapDataResponse,
    TeamStatus,
} from '../types/dispatch.types';

export const dispatchApi = {

    getTeams: async (status?: TeamStatus): Promise<RescueTeamResponse[]> => {
        const res = await axiosInstance.get<ApiResponse<RescueTeamResponse[]>>(
            '/api/dispatch/teams', { params: { status } }
        );
        return res.data.data;
    },

    getTeamById: async (id: number): Promise<RescueTeamResponse> => {
        const res = await axiosInstance.get<ApiResponse<RescueTeamResponse>>(
            `/api/dispatch/teams/${id}`
        );
        return res.data.data;
    },

    assignTeam: async (data: AssignTeamRequest): Promise<AssignmentResponse> => {
        const res = await axiosInstance.post<ApiResponse<AssignmentResponse>>(
            '/api/dispatch/assign', data
        );
        return res.data.data;
    },

    getAssignments: async (
        params: PageParams
    ): Promise<PageResponse<AssignmentResponse>> => {
        const res = await axiosInstance.get<ApiResponse<PageResponse<AssignmentResponse>>>(
            '/api/dispatch/assignments', { params }
        );
        return res.data.data;
    },

    getMyAssignments: async (): Promise<AssignmentResponse[]> => {
        const res = await axiosInstance.get<ApiResponse<AssignmentResponse[]>>(
            '/api/dispatch/assignments/my'
        );
        return res.data.data;
    },

    startAssignment: async (id: number): Promise<AssignmentResponse> => {
        const res = await axiosInstance.patch<ApiResponse<AssignmentResponse>>(
            `/api/dispatch/assignments/${id}/start`
        );
        return res.data.data;
    },

    completeAssignment: async (
        id: number,
        resultNote?: string
    ): Promise<AssignmentResponse> => {
        const res = await axiosInstance.patch<ApiResponse<AssignmentResponse>>(
            `/api/dispatch/assignments/${id}/complete`,
            null,
            { params: { resultNote } }
        );
        return res.data.data;
    },

    updateLocation: async (data: LocationUpdateRequest): Promise<void> => {
        await axiosInstance.post('/api/dispatch/location', data);
    },

    getMapData: async (): Promise<MapDataResponse> => {
        const res = await axiosInstance.get<ApiResponse<MapDataResponse>>(
            '/api/dispatch/map'
        );
        return res.data.data;
    },
};