import { Badge } from "@/components/ui/badge";
import type { VehicleStatus } from "../../types";

const statusConfig: Record<VehicleStatus, { label: string; className: string }> = {
  AVAILABLE: {
    label: "Sẵn sàng",
    className: "bg-green-100 text-green-700 border-green-200",
  },
  IN_USE: {
    label: "Đang sử dụng",
    className: "bg-blue-100 text-blue-700 border-blue-200",
  },
  MAINTENANCE: {
    label: "Bảo trì",
    className: "bg-orange-100 text-orange-700 border-orange-200",
  },
  OFFLINE: {
    label: "Ngoại tuyến",
    className: "bg-slate-100 text-slate-700 border-slate-200",
  },
};

interface VehicleStatusBadgeProps {
  status: VehicleStatus;
}

export function VehicleStatusBadge({ status }: VehicleStatusBadgeProps) {
  const config = statusConfig[status] || statusConfig.OFFLINE;

  return (
    <Badge className={config.className}>
      {config.label}
    </Badge>
  );
}
