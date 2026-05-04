"use client"
import * as React from "react"
import { LucideIcon } from "lucide-react"
import clsx from "clsx"

export type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "default" | "ghost" | "danger"
  icon?: LucideIcon
}

export function Button({ variant = "default", className, icon: Icon, children, disabled, ...rest }: ButtonProps) {
  const base = 'inline-flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition focus:outline-none'
  const variants: Record<string,string> = {
    default: 'bg-brand-500 text-white hover:bg-brand-600',
    ghost: 'bg-transparent text-slate-700 hover:bg-slate-100',
    danger: 'bg-red-600 text-white hover:bg-red-700',
  }
  return (
    <button className={clsx(base, variants[variant], className, disabled && 'opacity-60 cursor-not-allowed')} disabled={disabled} {...rest}>
      {Icon && <Icon className="h-4 w-4" />}
      <span>{children}</span>
    </button>
  )
}
