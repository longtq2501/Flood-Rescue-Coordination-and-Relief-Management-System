"use client"
import clsx from "clsx"

export function Input(props: React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      className={clsx('block w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm placeholder:text-slate-400 focus:border-brand-500 focus:ring-1 focus:ring-brand-300')}
      {...props}
    />
  )
}
