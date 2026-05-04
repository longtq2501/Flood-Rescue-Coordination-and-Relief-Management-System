"use client"
import { useState } from 'react'
import Link from 'next/link'
import { Menu, Bell, Home, Users, Box, X, User, LogOut, Truck, ClipboardList, ShieldCheck } from 'lucide-react'
import clsx from 'clsx'
import { Button } from '@/components/ui/button'

const nav = [
  { href: '/dashboard/citizen', label: 'Người dân', icon: Home },
  { href: '/dashboard/coordinator', label: 'Điều phối viên', icon: Users },
  { href: '/dashboard/rescue-team', label: 'Dashboard Đội cứu hộ', icon: Box },
  { href: '/dashboard/manager', label: 'Dashboard Quản lý', icon: Users },
  { href: '/dashboard/manager/teams', label: 'Đội cứu hộ', icon: ShieldCheck },
  { href: '/dashboard/manager/vehicles', label: 'Phương tiện', icon: Truck },
  { href: '/dashboard/manager/inventory', label: 'Tồn kho', icon: ClipboardList },
]

import { useAuthStore } from '@/features/auth/store/auth.store'
import { useRouter } from 'next/navigation'

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
