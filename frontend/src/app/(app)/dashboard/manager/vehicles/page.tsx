import { VehicleList } from "@/features/resource/components/vehicle/VehicleList";
import { Truck } from "lucide-react";

export const metadata = {
  title: "Quản lý phương tiện | Flood Rescue",
  description: "Quản lý danh sách phương tiện cứu hộ và trạng thái hoạt động",
};

export default function VehiclesPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <div className="p-3 rounded-2xl bg-teal-600 text-white shadow-lg shadow-teal-100">
          <Truck className="h-6 w-6" />
        </div>
        <div>
          <h1 className="text-3xl font-bold text-slate-900 tracking-tight">Quản lý phương tiện</h1>
          <p className="text-slate-500">Theo dõi, điều phối và bảo trì đội xe cứu hộ</p>
        </div>
      </div>

      <VehicleList />
    </div>
  );
}
