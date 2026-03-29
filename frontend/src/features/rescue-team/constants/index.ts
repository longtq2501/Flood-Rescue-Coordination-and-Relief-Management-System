export const MISSION_STATUS = {
  ACTIVE: 'active',
  IN_PROGRESS: 'in_progress',
  COMPLETED: 'completed',
} as const;

export const MISSION_STATUS_LABELS: Record<keyof typeof MISSION_STATUS, string> = {
  ACTIVE: 'Đang chờ',
  IN_PROGRESS: 'Đang xử lý',
  COMPLETED: 'Hoàn thành',
};

export const MISSION_STATUS_COLORS: Record<keyof typeof MISSION_STATUS, string> = {
  ACTIVE: 'bg-yellow-100 text-yellow-800',
  IN_PROGRESS: 'bg-blue-100 text-blue-800',
  COMPLETED: 'bg-green-100 text-green-800',
};

export const GPS_INTERVAL_MS = 10000; // 10 giây