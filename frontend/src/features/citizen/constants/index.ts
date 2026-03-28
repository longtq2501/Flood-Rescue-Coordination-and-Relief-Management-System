// ... các export trước
export const REQUEST_STATUS = {
  pending: 'pending',
  verified: 'verified',
  assigned: 'assigned',
  in_progress: 'in_progress',
  completed: 'completed',
  cancelled: 'cancelled',
} as const;

export const URGENCY_LEVELS = {
  low: 'low',
  medium: 'medium',
  high: 'high',
  critical: 'critical',
} as const;

export const REQUEST_STATUS_LABELS: Record<keyof typeof REQUEST_STATUS, string> = {
  pending: 'Chờ xác nhận',
  verified: 'Đã xác nhận',
  assigned: 'Đã phân công',
  in_progress: 'Đang xử lý',
  completed: 'Hoàn thành',
  cancelled: 'Đã hủy',
};

export const REQUEST_STATUS_COLORS: Record<keyof typeof REQUEST_STATUS, string> = {
  pending: 'bg-yellow-100 text-yellow-800',
  verified: 'bg-blue-100 text-blue-800',
  assigned: 'bg-purple-100 text-purple-800',
  in_progress: 'bg-orange-100 text-orange-800',
  completed: 'bg-green-100 text-green-800',
  cancelled: 'bg-gray-100 text-gray-800',
};

export const TIMELINE_STEPS = [
  { key: REQUEST_STATUS.pending, label: 'Yêu cầu đã gửi', icon: '📝' },
  { key: REQUEST_STATUS.verified, label: 'Đã xác thực', icon: '✅' },
  { key: REQUEST_STATUS.assigned, label: 'Đã phân công', icon: '🚚' },
  { key: REQUEST_STATUS.in_progress, label: 'Đang xử lý', icon: '🔄' },
  { key: REQUEST_STATUS.completed, label: 'Hoàn thành', icon: '🎉' },
];