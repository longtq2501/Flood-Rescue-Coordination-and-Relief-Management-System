"use client"

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { 
  Menu, X, User, LogOut, Home, Users, Box, 
  Map as MapIcon, ShieldCheck, ClipboardList, 
  Settings, Activity, LayoutDashboard,
  ChevronRight,
  Search
} from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/features/auth/store/auth.store'
import { SseBootstrap } from '@/shared/realtime/sse-bootstrap'
import { NotificationBell } from '@/features/notification/components/notification-bell'
import { cn } from '@/shared/utils/cn'

// Defined navigation based on roles
const navigationConfig = {
  CITIZEN: [
    { href: '/dashboard/citizen', label: 'Dashboard', icon: LayoutDashboard },
    { href: '/dashboard/citizen/requests', label: 'Yêu cầu của tôi', icon: ClipboardList },
    { href: '/dashboard/coordinator/map', label: 'Bản đồ cứu trợ', icon: MapIcon },
  ],
  COORDINATOR: [
    { href: '/dashboard/coordinator', label: 'Điều phối', icon: Activity },
    { href: '/dashboard/coordinator/map', label: 'Bản đồ tổng quan', icon: MapIcon },
    { href: '/dashboard/manager/warehouses', label: 'Quản lý kho', icon: Box },
  ],
  RESCUE_TEAM: [
    { href: '/dashboard/rescue-team', label: 'Nhiệm vụ', icon: ShieldCheck },
    { href: '/dashboard/coordinator/map', label: 'Bản đồ khu vực', icon: MapIcon },
  ],
  MANAGER: [
    { href: '/dashboard/manager', label: 'Thống kê', icon: Activity },
    { href: '/dashboard/manager/warehouses', label: 'Kho bãi', icon: Box },
    { href: '/dashboard/coordinator/map', label: 'Bản đồ', icon: MapIcon },
  ],
  ADMIN: [
    { href: '/dashboard/admin', label: 'Quản trị hệ thống', icon: Settings },
    { href: '/dashboard/coordinator', label: 'Điều phối', icon: Activity },
    { href: '/dashboard/coordinator/map', label: 'Bản đồ', icon: MapIcon },
  ]
}

export function GlobalLayout({ children }: { children: React.ReactNode }) {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false)
  const [isProfileOpen, setIsProfileOpen] = useState(false)
  const [scrolled, setScrolled] = useState(false)
  
  const { user, role, clearSession } = useAuthStore()
  const router = useRouter()
  const pathname = usePathname()

  // Close sidebar on navigation (mobile)
  useEffect(() => {
    setIsSidebarOpen(false)
  }, [pathname])

  // Header scroll effect
  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 10)
    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  const handleLogout = () => {
    clearSession()
    router.push('/login')
  }

  const currentNav = role ? navigationConfig[role] : []

  return (
    <div className="min-h-screen bg-slate-50/50">
      
      {/* Header */}
      <header className={cn(
        "sticky top-0 z-40 w-full transition-all duration-300 border-b",
        scrolled ? "glass shadow-sm py-2" : "bg-white py-3"
      )}>
        <div className="container flex h-14 items-center justify-between px-4 max-w-full">
          <div className="flex items-center gap-4">
            <Button 
              variant="ghost" 
              size="icon" 
              className="md:hidden hover:bg-slate-100 rounded-xl"
              onClick={() => setIsSidebarOpen(!isSidebarOpen)}
            >
              {isSidebarOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            </Button>
            
            <Link href="/" className="flex items-center gap-2 group">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl premium-gradient shadow-lg shadow-teal-600/20 group-hover:scale-105 transition-transform">
                <ShieldCheck className="h-6 w-6 text-white" />
              </div>
              <div className="hidden sm:block">
                <h1 className="text-xl font-bold tracking-tight text-slate-900">
                  Flood<span className="text-teal-600">Rescue</span>
                </h1>
                <p className="text-[10px] font-medium text-slate-400 uppercase tracking-widest leading-none">
                  Coordination System
                </p>
              </div>
            </Link>
          </div>

          <div className="flex items-center gap-2">
            {/* Search - Desktop only */}
            <div className="hidden lg:flex items-center relative mr-2">
              <Search className="absolute left-3 h-4 w-4 text-slate-400" />
              <input 
                type="text" 
                placeholder="Tìm kiếm..." 
                className="h-10 w-64 rounded-xl border-slate-200 bg-slate-100/50 pl-10 pr-4 text-sm focus:bg-white focus:ring-2 focus:ring-teal-500/20 transition-all outline-none border"
              />
            </div>

            <NotificationBell />
            
            <div className="relative">
              <button 
                className="flex items-center gap-2 rounded-xl p-1.5 hover:bg-slate-100 transition-colors"
                onClick={() => setIsProfileOpen(!isProfileOpen)}
              >
                <div className="h-9 w-9 rounded-lg bg-teal-100 flex items-center justify-center text-teal-700 font-bold border border-teal-200 shadow-sm">
                  {user?.fullName?.charAt(0) || 'U'}
                </div>
                <div className="hidden md:block text-left">
                  <p className="text-sm font-semibold text-slate-700 leading-none">{user?.fullName || 'User'}</p>
                  <p className="text-[10px] text-slate-400 mt-1 capitalize">{role?.toLowerCase() || 'guest'}</p>
                </div>
              </button>
              
              <AnimatePresence>
                {isProfileOpen && (
                  <motion.div 
                    initial={{ opacity: 0, y: 10, scale: 0.95 }}
                    animate={{ opacity: 1, y: 0, scale: 1 }}
                    exit={{ opacity: 0, y: 10, scale: 0.95 }}
                    className="absolute right-0 mt-3 w-56 rounded-2xl border bg-white p-2 shadow-xl z-50 overflow-hidden"
                  >
                    <div className="px-3 py-2">
                      <p className="text-xs font-medium text-slate-400 uppercase tracking-wider">Tài khoản</p>
                    </div>
                    <Link 
                      href="/settings/profile"
                      onClick={() => setIsProfileOpen(false)}
                      className="flex w-full items-center gap-3 px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 rounded-xl transition-colors group"
                    >
                      <User className="h-4 w-4 text-slate-400 group-hover:text-teal-600" />
                      <span>Hồ sơ cá nhân</span>
                    </Link>
                    <Link 
                      href="/settings"
                      onClick={() => setIsProfileOpen(false)}
                      className="flex w-full items-center gap-3 px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 rounded-xl transition-colors group"
                    >
                      <Settings className="h-4 w-4 text-slate-400 group-hover:text-teal-600" />
                      <span>Cài đặt</span>
                    </Link>
                    <div className="my-2 border-t border-slate-100" />
                    <button 
                      onClick={handleLogout}
                      className="flex w-full items-center gap-3 px-3 py-2 text-sm text-red-600 hover:bg-red-50 rounded-xl transition-colors group"
                    >
                      <LogOut className="h-4 w-4 group-hover:translate-x-0.5 transition-transform" />
                      <span className="font-medium">Đăng xuất</span>
                    </button>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          </div>
        </div>
      </header>

      <div className="flex flex-1 container max-w-full px-0">
        {/* Sidebar */}
        <aside className={cn(
          "fixed inset-y-0 left-0 z-30 w-72 transform bg-white border-r transition-all duration-300 ease-in-out md:static md:translate-x-0",
          isSidebarOpen ? "translate-x-0" : "-translate-x-full"
        )}>
          <div className="flex flex-col h-full py-4">
            <div className="px-6 mb-6">
              <p className="text-xs font-semibold text-slate-400 uppercase tracking-widest">Hệ thống điều phối</p>
            </div>
            
            <nav className="flex-1 px-4 space-y-1">
              {currentNav.map((item) => {
                const isActive = pathname === item.href;
                return (
                  <Link 
                    key={item.href} 
                    href={item.href}
                    className={cn(
                      "flex items-center justify-between group rounded-xl px-4 py-3 text-sm font-medium transition-all duration-200",
                      isActive 
                        ? "bg-teal-50 text-teal-700 shadow-sm shadow-teal-100" 
                        : "text-slate-600 hover:bg-slate-50 hover:text-teal-700"
                    )}
                  >
                    <div className="flex items-center gap-3">
                      <item.icon className={cn(
                        "h-5 w-5 transition-colors",
                        isActive ? "text-teal-600" : "text-slate-400 group-hover:text-teal-600"
                      )} />
                      <span>{item.label}</span>
                    </div>
                    {isActive && <ChevronRight className="h-4 w-4" />}
                  </Link>
                )
              })}
            </nav>

            <div className="mt-auto px-4 pt-4 border-t border-slate-100">
              <div className="rounded-2xl premium-gradient p-4 text-white shadow-lg shadow-teal-700/20 overflow-hidden relative">
                <div className="relative z-10">
                  <p className="text-xs font-bold opacity-80 uppercase tracking-wider">Trung tâm cứu trợ</p>
                  <p className="mt-1 text-sm font-medium">Hỗ trợ 24/7</p>
                  <Button variant="secondary" size="sm" className="mt-3 w-full bg-white/20 hover:bg-white/30 border-none text-white backdrop-blur-sm">
                    Liên hệ ngay
                  </Button>
                </div>
                <div className="absolute -right-4 -bottom-4 h-24 w-24 bg-white/10 rounded-full blur-2xl" />
              </div>
            </div>
          </div>
        </aside>

        {/* Overlay for mobile */}
        {isSidebarOpen && (
          <div 
            className="fixed inset-0 z-20 bg-slate-900/40 backdrop-blur-sm md:hidden"
            onClick={() => setIsSidebarOpen(false)}
          />
        )}

        {/* Main Content */}
        <main className="flex-1 w-full min-w-0">
          <div className="h-full p-4 md:p-8 flex justify-center">
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, ease: "easeOut" }}
              className="w-full max-w-6xl"
            >
              {children}
            </motion.div>
          </div>
        </main>
      </div>
    </div>
  )
}
