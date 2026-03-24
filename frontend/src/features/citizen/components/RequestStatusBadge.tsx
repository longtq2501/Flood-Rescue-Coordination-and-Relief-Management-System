import { REQUEST_STATUS_LABELS, REQUEST_STATUS_COLORS } from '../constants';

interface RequestStatusBadgeProps {
  status: keyof typeof REQUEST_STATUS_LABELS;
}

export function RequestStatusBadge({ status }: RequestStatusBadgeProps) {
  return (
    <span className={`px-2 py-1 rounded-full text-xs font-medium ${REQUEST_STATUS_COLORS[status]}`}>
      {REQUEST_STATUS_LABELS[status]}
    </span>
  );
}