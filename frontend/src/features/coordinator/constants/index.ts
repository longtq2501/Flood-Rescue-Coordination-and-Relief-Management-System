export const URGENCY_COLORS = {
  CRITICAL: 'bg-red-500 text-white',
  HIGH: 'bg-orange-500 text-white',
  MEDIUM: 'bg-yellow-500 text-black',
  LOW: 'bg-green-500 text-white'
} as const;

export const URGENCY_LABELS = {
  CRITICAL: 'Nguy cấp',
  HIGH: 'Cao',
  MEDIUM: 'Trung bình',
  LOW: 'Thấp'
} as const;

export const REQUEST_STATUS = {
  PENDING: 'Chờ xử lý',
  VERIFIED: 'Đã xác minh',
  ASSIGNED: 'Đã phân công',
  IN_PROGRESS: 'Đang xử lý',
  COMPLETED: 'Hoàn thành',
  CONFIRMED: 'Đã xác nhận',
  CANCELLED: 'Đã hủy'
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