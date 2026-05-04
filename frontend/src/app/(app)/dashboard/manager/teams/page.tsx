import { TeamList } from "@/features/dispatch/components/management/TeamList";
import { ShieldCheck } from "lucide-react";

export const metadata = {
  title: "Quản lý Đội cứu hộ | Flood Rescue",
  description: "Quản lý danh sách, thông tin và tình trạng hoạt động của các đội cứu hộ",
};

export default function TeamsPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <div className="p-3 rounded-2xl bg-cyan-600 text-white shadow-lg shadow-cyan-100">
          <ShieldCheck className="h-6 w-6" />
        </div>
        <div>
          <h1 className="text-3xl font-bold text-slate-900 tracking-tight">Quản lý Đội cứu hộ</h1>
          <p className="text-slate-500">Thiết lập và giám sát lực lượng phản ứng nhanh</p>
        </div>
      </div>

      <TeamList />
    </div>
  );
}
