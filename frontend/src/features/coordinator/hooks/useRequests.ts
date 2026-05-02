import { useState, useEffect } from 'react';
import { requestApi } from '@/features/request/api/requestApi';
import { RequestFilterParams, RescueRequestResponse } from '@/features/request/types/request.types';
import { PageParams } from '@/shared/types/api.types';
import { Request } from '../types';

export const useRequests = (filters: RequestFilterParams & PageParams & { search?: string }) => {
  const [requests, setRequests] = useState<RescueRequestResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    fetchRequests();
  }, [filters]);

  const fetchRequests = async () => {
    try {
      setLoading(true);
      const response = await requestApi.getAll(filters);
      setRequests(response.content);
      setTotalElements(response.totalElements);
      setTotalPages(response.totalPages);
    } catch (error) {
      console.error('Failed to fetch requests:', error);
    } finally {
      setLoading(false);
    }
  };

  const verifyRequest = async (requestId: number) => {
    try {
      await requestApi.verify(requestId);
      setRequests(prev => prev.map(req => 
        req.id === requestId ? { ...req, status: 'VERIFIED' as any } : req
      ));
    } catch (error) {
      console.error('Failed to verify request:', error);
    }
  };

  const assignTeam = async (requestId: string, teamId: string, vehicleId: string) => {
    // TODO: Implement assign API
    console.log('Assign:', { requestId, teamId, vehicleId });
  };

  return { requests, loading, totalElements, totalPages, verifyRequest, assignTeam, refetch: fetchRequests };
};