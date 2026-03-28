import { useState, useEffect } from 'react';
import axiosInstance from '@/shared/api/axiosInstance';
import { CitizenRequest } from '../types';

export function useCitizenRequests() {
  const [requests, setRequests] = useState<CitizenRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchRequests = async () => {
      try {
        setLoading(true);
        const response = await axiosInstance.get('/api/requests/me');
        // Giả sử API trả về data.data là array requests
        setRequests(response.data.data || []);
      } catch (err) {
        console.error('Failed to fetch requests', err);
        setError('Không thể tải danh sách yêu cầu. Vui lòng thử lại sau.');
      } finally {
        setLoading(false);
      }
    };

    fetchRequests();
  }, []);

  return { requests, loading, error };
}
