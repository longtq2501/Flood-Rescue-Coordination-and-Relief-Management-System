"use client";

import * as React from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getTeams, deleteTeam, updateTeamStatus } from "@/features/dispatch/services/dispatch.service";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { 
  Users, 
  User, 
  Trash2, 
  Edit, 
  Plus, 
  RefreshCw 
} from "lucide-react";
import { toast } from "sonner";
import { AddTeamModal } from "@/features/dispatch/components/management/AddTeamModal";

export function TeamList() {
  const queryClient = useQueryClient();
  const [selectedTeam, setSelectedTeam] = React.useState<any>(null);
  const [isModalOpen, setIsModalOpen] = React.useState(false);

  const { data, isLoading, refetch } = useQuery({
    queryKey: ["teams"],
    queryFn: getTeams,
  });

  const deleteMutation = useMutation({
    mutationFn: deleteTeam,
    onSuccess: () => {
      toast.success("Đã xóa đội cứu hộ");
      queryClient.invalidateQueries({ queryKey: ["teams"] });
    },
    onError: (error: any) => {
      toast.error(error.message || "Không thể xóa đội cứu hộ");
    },
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) => updateTeamStatus(id, status),
    onSuccess: () => {
      toast.success("Đã cập nhật trạng thái");
      queryClient.invalidateQueries({ queryKey: ["teams"] });
    },
    onError: (error: any) => {
      toast.error(error.message || "Không thể cập nhật trạng thái");
    },
  });

  const handleStatusChange = (id: number, status: string) => {
    updateStatusMutation.mutate({ id, status });
  };

  const handleEdit = (team: any) => {
    setSelectedTeam(team);
    setIsModalOpen(true);
  };

  const handleAdd = () => {
    setSelectedTeam(null);
    setIsModalOpen(true);
  };

  const teams = data || [];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-lg bg-cyan-50 text-cyan-600">
            <Users className="h-5 w-5" />
          </div>
          <h3 className="text-lg font-bold text-slate-900 tracking-tight">Danh sách Đội cứu hộ</h3>
        </div>
        <div className="flex gap-2">
          <Button variant="ghost" onClick={() => refetch()} disabled={isLoading} className="rounded-full h-10 w-10 p-0">
            <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
          </Button>
          <Button onClick={handleAdd} className="bg-cyan-600 hover:bg-cyan-700 text-white font-bold rounded-full px-6">
            <Plus className="h-4 w-4 mr-2" />
            Thêm đội mới
          </Button>
        </div>
      </div>

      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="bg-slate-50/80 border-b border-slate-200">
              <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider">Tên đội</th>
              <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider">Trưởng đội ID</th>
              <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider">Sức chứa</th>
              <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider">Trạng thái</th>
              <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {isLoading ? (
              Array.from({ length: 3 }).map((_, i) => (
                <tr key={i}>
                  <td colSpan={5} className="px-6 py-10">
                    <div className="h-4 bg-slate-100 animate-pulse rounded-full w-full" />
                  </td>
                </tr>
              ))
            ) : teams.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-6 py-16 text-center text-slate-400 italic">
                  <Users className="h-10 w-10 mx-auto mb-2 opacity-10" />
                  <p>Chưa có đội cứu hộ nào được tạo</p>
                </td>
              </tr>
            ) : (
              teams.map((team: any) => (
                <tr key={team.id} className="hover:bg-slate-50/50 transition-colors">
                  <td className="px-6 py-4">
                    <p className="font-bold text-slate-900">{team.name}</p>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <User className="h-3 w-3 text-slate-400" />
                      <span className="font-medium text-slate-600">ID: {team.leaderId}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className="font-bold text-slate-900">{team.capacity} người</span>
                  </td>
                  <td className="px-6 py-4">
                    <select
                      className={`text-xs font-bold border rounded-full px-3 py-1 bg-white focus:outline-none focus:ring-2 focus:ring-cyan-500 ${
                        team.status === 'AVAILABLE' ? 'text-emerald-700 border-emerald-200 bg-emerald-50' :
                        team.status === 'BUSY' ? 'text-orange-700 border-orange-200 bg-orange-50' :
                        team.status === 'RETURNING' ? 'text-blue-700 border-blue-200 bg-blue-50' :
                        'text-slate-700 border-slate-200 bg-slate-50'
                      }`}
                      value={team.status}
                      onChange={(e) => handleStatusChange(team.id, e.target.value)}
                      disabled={updateStatusMutation.isPending}
                    >
                      <option value="AVAILABLE">SẴN SÀNG</option>
                      <option value="BUSY">ĐANG NHIỆM VỤ</option>
                      <option value="RETURNING">ĐANG TRỞ VỀ</option>
                      <option value="OFFLINE">NGOẠI TUYẾN</option>
                    </select>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <div className="flex justify-end gap-2">
                      <Button variant="ghost" onClick={() => handleEdit(team)} className="h-8 w-8 p-0 text-slate-400 hover:text-cyan-600">
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button 
                        variant="ghost" 
                        onClick={() => {
                          if (confirm(`Bạn có chắc chắn muốn xóa đội ${team.name}?`)) {
                            deleteMutation.mutate(team.id);
                          }
                        }}
                        className="h-8 w-8 p-0 text-slate-400 hover:text-red-600"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <AddTeamModal 
        open={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        team={selectedTeam} 
      />
    </div>
  );
}
