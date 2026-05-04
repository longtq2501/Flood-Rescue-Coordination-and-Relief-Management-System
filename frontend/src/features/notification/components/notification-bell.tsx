"use client";

import * as React from "react";
import { Bell, X, Check, BellOff } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useNotificationStore } from "../store/notification.store";
import { markNotificationAsRead, markAllNotificationsAsRead, getNotifications } from "../services/notification.service";
import { formatDistanceToNow } from "date-fns";
import { vi } from "date-fns/locale";
import clsx from "clsx";

export function NotificationBell() {
  const [isOpen, setIsOpen] = React.useState(false);
  const { notifications, unreadCount, setNotifications, markAsRead, markAllAsRead } = useNotificationStore();

  React.useEffect(() => {
    // Initial fetch of notifications
    getNotifications().then((data) => {
      setNotifications(data.content);
    });
  }, [setNotifications]);

  const handleMarkAsRead = async (id: string) => {
    try {
      await markNotificationAsRead(id);
      markAsRead(id);
    } catch (error) {
      console.error("Mark as read error:", error);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await markAllNotificationsAsRead();
      markAllAsRead();
    } catch (error) {
      console.error("Mark all as read error:", error);
    }
  };

  return (
    <div className="relative">
      <Button 
        variant="ghost" 
        className="relative h-10 w-10 p-0 rounded-full hover:bg-slate-100"
        onClick={() => setIsOpen(!isOpen)}
      >
        <Bell className="h-5 w-5 text-slate-600" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white ring-2 ring-white animate-in zoom-in">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
      </Button>

      {isOpen && (
        <>
          <div 
            className="fixed inset-0 z-30" 
            onClick={() => setIsOpen(false)} 
          />
          <div className="absolute right-0 mt-2 w-80 sm:w-96 rounded-2xl border border-slate-200 bg-white shadow-2xl z-40 overflow-hidden animate-in fade-in slide-in-from-top-2 duration-200">
            <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/50 px-4 py-3">
              <h3 className="text-sm font-bold text-slate-900">Thông báo</h3>
              <div className="flex items-center gap-2">
                <button 
                  onClick={handleMarkAllAsRead}
                  className="text-[10px] font-bold text-brand-600 hover:text-brand-700"
                >
                  Đánh dấu tất cả đã đọc
                </button>
                <button onClick={() => setIsOpen(false)} className="text-slate-400 hover:text-slate-600">
                  <X className="h-4 w-4" />
                </button>
              </div>
            </div>

            <div className="max-h-[400px] overflow-y-auto">
              {notifications.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-12 text-slate-400">
                  <BellOff className="h-10 w-10 mb-2 opacity-20" />
                  <p className="text-sm">Không có thông báo nào</p>
                </div>
              ) : (
                <div className="divide-y divide-slate-50">
                  {notifications.map((notification) => (
                    <div 
                      key={notification.id}
                      className={clsx(
                        "group relative flex gap-3 p-4 transition-colors hover:bg-slate-50 cursor-pointer",
                        !notification.isRead && "bg-brand-50/30"
                      )}
                      onClick={() => !notification.isRead && handleMarkAsRead(notification.id)}
                    >
                      <div className={clsx(
                        "mt-1 flex h-8 w-8 shrink-0 items-center justify-center rounded-full",
                        notification.type === "URGENT_REQUEST" ? "bg-red-100 text-red-600" :
                        notification.type === "MISSION_ASSIGNED" ? "bg-brand-100 text-brand-600" :
                        "bg-blue-100 text-blue-600"
                      )}>
                        <Bell className="h-4 w-4" />
                      </div>
                      <div className="flex-1 space-y-1">
                        <p className={clsx("text-sm", !notification.isRead ? "font-bold text-slate-900" : "text-slate-700")}>
                          {notification.title}
                        </p>
                        <p className="text-xs text-slate-500 line-clamp-2">
                          {notification.message}
                        </p>
                        <p className="text-[10px] font-medium text-slate-400">
                          {formatDistanceToNow(new Date(notification.createdAt), { addSuffix: true, locale: vi })}
                        </p>
                      </div>
                      {!notification.isRead && (
                        <div className="mt-1 h-2 w-2 shrink-0 rounded-full bg-brand-500" />
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="border-t border-slate-100 p-2 text-center">
              <button className="text-[11px] font-bold text-slate-500 hover:text-brand-600">
                Xem tất cả thông báo
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
