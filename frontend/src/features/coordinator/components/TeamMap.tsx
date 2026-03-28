'use client';

import { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { Team } from '../types';

delete (L.Icon.Default.prototype as unknown as { _getIconUrl?: unknown })._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

interface TeamMapProps {
  teams: Team[];
  center?: [number, number];
  onTeamClick?: (team: Team) => void;
}

export function TeamMap({ teams, center = [10.8231, 106.6297], onTeamClick }: TeamMapProps) {
  const mapRef = useRef<L.Map | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const markersRef = useRef<Record<string, L.Marker>>({});

  useEffect(() => {
    if (!containerRef.current || mapRef.current) return;

    mapRef.current = L.map(containerRef.current).setView(center, 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
    }).addTo(mapRef.current);

    return () => {
      mapRef.current?.remove();
      mapRef.current = null;
    };
  }, [center]);

  useEffect(() => {
    if (!mapRef.current) return;

    Object.values(markersRef.current).forEach(marker => marker.remove());
    markersRef.current = {};

    teams.forEach(team => {
      const marker = L.marker([team.location.lat, team.location.lng]).addTo(mapRef.current!);
      marker.bindPopup(`
        <div class="p-2">
          <strong>${team.name}</strong><br/>
          Trạng thái: ${team.status}<br/>
          Thành viên: ${team.members}<br/>
          Xe: ${team.vehicle}
        </div>
      `);
      marker.on('click', () => onTeamClick?.(team));
      markersRef.current[team.id] = marker;
    });
  }, [teams, onTeamClick]);

  return <div ref={containerRef} className="w-full h-[600px] rounded-lg border" />;
}