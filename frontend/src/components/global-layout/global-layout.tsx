"use client"
import { useEffect, useState } from 'react'
import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { Menu, X, User, LogOut } from 'lucide-react'
import clsx from 'clsx'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { NotificationBell } from '@/features/notification/components/notification-bell'
import { DashboardSidebar } from '@/components/global-layout/dashboard-sidebar'
import { ROLE_TO_DASHBOARD_PATH } from '@/shared/constants/auth'

export function GlobalLayout({ children }: { children: React.ReactNode }) {
  const [open, setOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  
  const clearSession = useAuthStore((state) => state.clearSession)
  const role = useAuthStore((state) => state.role)
  const hydrated = useAuthStore((state) => state.hydrated)
  const router = useRouter()
  const pathname = usePathname()

  const handleLogout = () => {
    clearSession()
    router.push('/')
  }

  useEffect(() => {
    if (!hydrated || !role) {
      return;
    }

    if (!pathname.startsWith('/dashboard')) {
      return;
    }

    if (role === 'ADMIN') {
      return;
    }

    const expectedPath = ROLE_TO_DASHBOARD_PATH[role];
    if (!pathname.startsWith(expectedPath)) {
      router.replace(expectedPath);
    }
  }, [hydrated, pathname, role, router]);

  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      <header className="sticky top-0 z-20 border-b bg-white px-4 py-2 shadow-sm">
        <div className="flex w-full items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <button aria-label="toggle menu" className="md:hidden" onClick={() => setOpen(!open)}>
              {open ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            </button>
            <Link href="/" className="text-lg font-bold text-teal-800">Flood Rescue</Link>
          </div>
          <div className="flex items-center gap-3 relative">
            {/* SSE Simulator (Hidden in production, for testing only) */}
            <Button 
              variant="ghost" 
              className="hidden md:flex h-7 border border-dashed border-teal-200 px-2 text-[10px] text-teal-600 hover:bg-teal-50"
              onClick={() => {
                // Mock a new request alert event
                const mockNotification = {
                  id: Math.random().toString(36).substr(2, 9),
                  userId: 1,
                  title: "Yêu cầu mới",
                  message: "Hệ thống giả lập: Có yêu cầu cứu trợ khẩn cấp tại Quận 1!",
                  type: "URGENT_REQUEST",
                  isRead: false,
                  createdAt: new Date().toISOString()
                };
                
                window.dispatchEvent(new MessageEvent('message', {
                  data: JSON.stringify(mockNotification)
                }));
              }}
            >
              Test Realtime
            </Button>

            <NotificationBell />
            
            <div className="relative">
              <Button 
                variant="ghost" 
                className="rounded-full w-10 h-10 p-0"
                onClick={() => setProfileOpen(!profileOpen)}
              >
                <User className="h-5 w-5" />
              </Button>
              
              {profileOpen && (
                <div className="absolute right-0 mt-2 w-48 rounded-md border bg-white py-1 shadow-lg z-50">
                  <Link 
                    href="/settings/profile"
                    onClick={() => setProfileOpen(false)}
                    className="flex w-full items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                  >
                    <User className="h-4 w-4" />
                    <span>Hồ sơ cá nhân</span>
                  </Link>
                  <hr className="my-1 border-slate-100" />
                  <button 
                    onClick={handleLogout}
                    className="flex w-full items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-slate-50"
                  >
                    <LogOut className="h-4 w-4" />
                    <span>Đăng xuất</span>
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </header>

      <div className="flex flex-1">
        <div className={clsx('fixed inset-y-0 left-0 z-10 w-72 transform transition-transform duration-200 md:static md:translate-x-0', !open && ' -translate-x-full md:translate-x-0')}>
          <DashboardSidebar />
        </div>

        <main className="flex-1 p-4 pt-10 md:ml-72 md:p-6 md:pt-10 flex justify-center">
          <div className="w-full max-w-6xl">
            {children}
          </div>
        </main>
      </div>
    </div>
  )
}
