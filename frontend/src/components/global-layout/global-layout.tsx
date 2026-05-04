"use client"
import { useState } from 'react'
import Link from 'next/link'
import { Menu, Bell, Home, Users, Box, X, User, LogOut, Truck, ClipboardList, ShieldCheck } from 'lucide-react'
import clsx from 'clsx'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { useRouter } from 'next/navigation'

const nav = [
  { href: '/dashboard/citizen', label: 'Người dân', icon: Home },
  { href: '/dashboard/coordinator', label: 'Điều phối viên', icon: Users },
  { href: '/dashboard/rescue-team', label: 'Đội cứu hộ', icon: Box },
  { href: '/dashboard/manager', label: 'Quản lý', icon: Users },
  { href: '/dashboard/manager/warehouses', label: 'Kho hàng', icon: Box },
]

export function GlobalLayout({ children }: { children: React.ReactNode }) {
  const [open, setOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  
  const clearSession = useAuthStore((state) => state.clearSession)
  const router = useRouter()

  const handleLogout = () => {
    clearSession()
    router.push('/')
  }

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
              variant="outline" 
              size="sm" 
              className="hidden md:flex text-[10px] h-7 px-2 border-dashed border-teal-200 text-teal-600 hover:bg-teal-50"
              onClick={() => {
                // Mock a new request alert event
                window.dispatchEvent(new MessageEvent('message', {
                  data: JSON.stringify({ id: 1, message: "Hệ thống giả lập: Có yêu cầu cứu trợ khẩn cấp tại Quận 1!" }),
                  lastEventId: 'test-123',
                  origin: window.location.origin
                }));
                // Note: In real life, the EventSource listener handles this. 
                // Since we can't easily inject into the private EventSource instance,
                // we manually trigger the same logic for the demo.
                import('@tanstack/react-query').then(({ useQueryClient }) => {
                  // We'll use a more direct approach since we can't use hooks here
                  const event = new CustomEvent('sse-test-trigger', { 
                    detail: { type: 'new.request.alert', id: '1', message: 'Hệ thống giả lập: Có yêu cầu cứu trợ khẩn cấp tại Quận 1!' } 
                  });
                  window.dispatchEvent(event);
                });
              }}
            >
              Test Realtime
            </Button>

            <Button variant="ghost" className="hidden md:inline-flex"><Bell className="h-4 w-4" /></Button>
            
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
        <aside className={clsx('fixed inset-y-0 left-0 z-10 w-64 transform overflow-auto bg-white border-r transition-transform duration-200 md:static md:translate-x-0', !open && ' -translate-x-full md:translate-x-0')}>
          <nav className="p-4">
            <ul className="space-y-1">
              {nav.map((item) => (
                <li key={item.href}>
                  <Link href={item.href} className="flex items-center gap-3 rounded-md px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 hover:text-teal-800">
                    <item.icon className="h-4 w-4" />
                    <span>{item.label}</span>
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
        </aside>

        <main className="flex-1 p-6 md:ml-64 flex justify-center pt-10">
          <div className="w-full max-w-5xl pr-8">
            {children}
          </div>
        </main>
      </div>
    </div>
  )
}
