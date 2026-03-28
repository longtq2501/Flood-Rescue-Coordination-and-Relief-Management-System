import { useState, useEffect } from 'react';
import { Request } from '../types';

export const useRequests = () => {
  const [requests, setRequests] = useState<Request[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRequests();
  }, []);

  const fetchRequests = async () => {
    try {
      const response = await fetch('/api/requests');
      const data = await response.json();
      setRequests(data);
    } catch (error) {
      console.error('Failed to fetch requests:', error);
    } finally {
      setLoading(false);
    }
  };

  const verifyRequest = async (requestId: string) => {
    try {
      await fetch(`/api/requests/${requestId}/verify`, { method: 'POST' });
      setRequests(prev => prev.map(req => 
        req.id === requestId ? { ...req, status: 'pending' } : req
      ));
    } catch (error) {
      console.error('Failed to verify request:', error);
    }
  };

  const assignTeam = async (requestId: string, teamId: string, vehicleId: string) => {
    try {
      await fetch('/api/assignments', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ requestId, teamId, vehicleId })
      });
      setRequests(prev => prev.map(req => 
        req.id === requestId ? { ...req, status: 'assigned' } : req
      ));
    } catch (error) {
      console.error('Failed to assign team:', error);
    }
  };

  return { requests, loading, verifyRequest, assignTeam };
};