'use client';

import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Button } from '@/shared/components/ui/button';
import { Request, Team, Vehicle } from '../types';

interface AssignModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  request: Request | null;
  onAssign: (requestId: string, teamId: string, vehicleId: string) => Promise<void>;
}

export function AssignModal({ open, onOpenChange, request, onAssign }: AssignModalProps) {
  const [teams, setTeams] = useState<Team[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [selectedTeam, setSelectedTeam] = useState('');
  const [selectedVehicle, setSelectedVehicle] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (open) {
      loadAvailableTeams();
    }
  }, [open]);

  useEffect(() => {
    if (selectedTeam) {
      loadTeamVehicles(selectedTeam);
    }
  }, [selectedTeam]);

  const loadAvailableTeams = async () => {
    try {
      const res = await fetch('/api/teams?status=AVAILABLE');
      const data = await res.json();
      setTeams(data);
    } catch (error) {
      console.error('Failed to load teams', error);
      setTeams([]);
    }
  };

  const loadTeamVehicles = async (teamId: string) => {
    try {
      const res = await fetch(`/api/teams/${teamId}/vehicles?status=AVAILABLE`);
      const data = await res.json();
      setVehicles(data);
    } catch (error) {
      console.error('Failed to load vehicles', error);
      setVehicles([]);
    }
  };

  const handleAssign = async () => {
    if (!request || !selectedTeam || !selectedVehicle) return;
    setLoading(true);
    try {
      await onAssign(request.id, selectedTeam, selectedVehicle);
      onOpenChange(false);
    } catch (error) {
      console.error('Assign failed', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Phân công đội cứu hộ</DialogTitle>
        </DialogHeader>
        <div className="space-y-4 py-4">
          {request && (
            <div className="bg-blue-50 p-3 rounded-lg text-sm">
              <div className="font-medium">{request.title}</div>
              <div className="text-gray-600 mt-1">{request.location}</div>
              <div className="text-gray-500 text-xs mt-1">
                {request.customerName} - {request.customerPhone}
              </div>
            </div>
          )}
          <div className="space-y-2">
            <label className="text-sm font-medium">Chọn đội cứu hộ</label>
            <Select value={selectedTeam} onValueChange={setSelectedTeam}>
              <SelectTrigger>
                <SelectValue placeholder="Chọn đội..." />
              </SelectTrigger>
              <SelectContent>
                {teams.map((team) => (
                  <SelectItem key={team.id} value={team.id}>
                    {team.name} ({team.members} người)
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">Chọn xe</label>
            <Select
              value={selectedVehicle}
              onValueChange={setSelectedVehicle}
              disabled={!selectedTeam}
            >
              <SelectTrigger>
                <SelectValue placeholder={!selectedTeam ? 'Chọn đội trước' : 'Chọn xe...'} />
              </SelectTrigger>
              <SelectContent>
                {vehicles.map((vehicle) => (
                  <SelectItem key={vehicle.id} value={vehicle.id}>
                    {vehicle.name} - {vehicle.plate}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="flex justify-end gap-3 pt-4">
            <Button variant="outline" onClick={() => onOpenChange(false)}>
              Hủy
            </Button>
            <Button onClick={handleAssign} disabled={!selectedTeam || !selectedVehicle || loading}>
              {loading ? 'Đang xử lý...' : 'Xác nhận phân công'}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}