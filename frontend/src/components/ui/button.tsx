"use client"
import * as React from "react"
import { LucideIcon } from "lucide-react"
import { cn } from "@/shared/utils/cn"

export type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "default" | "ghost" | "danger" | "outline" | "secondary"
  size?: "default" | "sm" | "lg" | "icon"
  icon?: LucideIcon
}

export function Button({ 
  variant = "default", 
  size = "default",
  className, 
  icon: Icon, 
  children, 
  disabled, 
  ...rest 
}: ButtonProps) {
  const base = 'inline-flex items-center justify-center gap-2 rounded-xl font-semibold transition-all focus:outline-none disabled:opacity-50 disabled:pointer-events-none active:scale-[0.98]'
  
  const variants = {
    default: 'bg-teal-600 text-white hover:bg-teal-700 shadow-md shadow-teal-600/20',
    ghost: 'bg-transparent text-slate-600 hover:bg-slate-100 hover:text-teal-700',
    danger: 'bg-red-600 text-white hover:bg-red-700 shadow-md shadow-red-600/20',
    outline: 'bg-transparent border-2 border-slate-200 text-slate-700 hover:border-teal-600 hover:text-teal-600',
    secondary: 'bg-white/20 text-white backdrop-blur-md hover:bg-white/30 border border-white/20',
  }

  const sizes = {
    default: 'h-11 px-5 text-sm',
    sm: 'h-9 px-3 text-xs',
    lg: 'h-14 px-8 text-base',
    icon: 'h-10 w-10',
  }

  return (
    <button 
      className={cn(base, variants[variant], sizes[size], className)} 
      disabled={disabled} 
      {...rest}
    >
      {Icon && <Icon className={cn("h-4 w-4", size === "lg" && "h-5 w-5")} />}
      {children && <span>{children}</span>}
    </button>
  )
}
