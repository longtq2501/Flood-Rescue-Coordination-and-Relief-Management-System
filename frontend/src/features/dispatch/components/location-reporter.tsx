"use client";

import { useState } from "react";
import { MapPin, Navigation } from "lucide-react";
import { useLocationTracking } from "../hooks/use-location-tracking";

export function LocationReporter() {
  const [isTracking, setIsTracking] = useState(false);
  const { coords, error, lastUpdated } = useLocationTracking(isTracking);

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className={`p-2.5 rounded-xl ${isTracking ? 'bg-teal-50 text-teal-600' : 'bg-slate-50 text-slate-400'}`}>
            <Navigation className={`w-5 h-5 ${isTracking ? 'animate-pulse' : ''}`} />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-900">Trạng thái định vị</h3>
            <p className="text-xs text-slate-500">
              {isTracking ? 'Đang gửi tọa độ thời gian thực' : 'Đã tắt theo dõi vị trí'}
            </p>
          </div>
        </div>
        
        <button
          onClick={() => setIsTracking(!isTracking)}
          className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-teal-600 focus:ring-offset-2 ${
            isTracking ? 'bg-teal-600' : 'bg-slate-200'
          }`}
        >
          <span
            className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
              isTracking ? 'translate-x-6' : 'translate-x-1'
            }`}
          />
        </button>
      </div>

      {isTracking && (
        <div className="mt-4 pt-4 border-t border-slate-100 grid grid-cols-2 gap-4">
          <div className="space-y-1">
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Tọa độ hiện tại</span>
            <div className="flex items-center gap-1.5 text-xs font-mono font-medium text-slate-700">
              <MapPin className="w-3 h-3 text-slate-400" />
              {coords ? `${coords.lat.toFixed(5)}, ${coords.lng.toFixed(5)}` : 'Đang lấy...'}
            </div>
          </div>
          <div className="space-y-1 text-right">
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Cập nhật lần cuối</span>
            <p className="text-xs font-medium text-slate-700">
              {lastUpdated ? lastUpdated.toLocaleTimeString() : '---'}
            </p>
          </div>
        </div>
      )}

      {error && (
        <div className="mt-3 p-2.5 bg-red-50 rounded-xl border border-red-100 flex items-center gap-2">
          <div className="w-1.5 h-1.5 rounded-full bg-red-500 animate-pulse" />
          <p className="text-xs text-red-600 font-medium">{error}</p>
        </div>
      )}
    </div>
  );
}
