"use client"
import { useState } from 'react'
import Link from 'next/link'
import { Menu, Bell, Home, Users, Box, X } from 'lucide-react'
import clsx from 'clsx'
import { Button } from '@/components/ui/button'

const nav = [
  { href: '/dashboard/citizen', label: 'Citizen', icon: Home },
  { href: '/dashboard/coordinator', label: 'Coordinator', icon: Users },
  { href: '/dashboard/rescue-team', label: 'Rescue Team', icon: Box },
  { href: '/dashboard/manager', label: 'Manager', icon: Users },
]

export function GlobalLayout({ children }: { children: React.ReactNode }) {
  const [open, setOpen] = useState(false)
  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      <header className="sticky top-0 z-20 border-b bg-white px-4 py-2 shadow-sm">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <button aria-label="toggle menu" className="md:hidden" onClick={() => setOpen(!open)}>
              {open ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            </button>
            <Link href="/" className="text-lg font-bold text-teal-800">Flood Rescue Console</Link>
          </div>
          <div className="flex items-center gap-3">
            <Button variant="ghost" className="hidden md:inline-flex"><Bell className="h-4 w-4" /></Button>
            <Button variant="ghost">Profile</Button>
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

        <main className="flex-1 p-4 md:ml-64">
          {children}
        </main>
      </div>
    </div>
  )
}
