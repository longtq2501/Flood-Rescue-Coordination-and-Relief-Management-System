export const URGENCY_COLORS = {
  critical: 'bg-red-500 text-white',
  high: 'bg-orange-500 text-white',
  medium: 'bg-yellow-500 text-black',
  low: 'bg-green-500 text-white'
} as const;

export const URGENCY_LABELS = {
  critical: 'Nguy cấp',
  high: 'Cao',
  medium: 'Trung bình',
  low: 'Thấp'
} as const;

export const REQUEST_STATUS = {
  pending: 'Chờ xử lý',
  assigned: 'Đã phân công',
  in_progress: 'Đang xử lý',
  completed: 'Hoàn thành',
  cancelled: 'Đã hủy'
} as const;

export const TEAM_STATUS = {
  AVAILABLE: 'Sẵn sàng',
  BUSY: 'Đang làm việc',
  OFFLINE: 'Ngoại tuyến'
} as const;

export const TEAM_STATUS_COLORS = {
  AVAILABLE: 'text-green-600 bg-green-100',
  BUSY: 'text-red-600 bg-red-100',
  OFFLINE: 'text-gray-600 bg-gray-100'
} as const;