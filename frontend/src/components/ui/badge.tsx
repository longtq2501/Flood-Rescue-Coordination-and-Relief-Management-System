export function Badge({ children, className }: { children: React.ReactNode; className?: string }) {
  return <span className={("inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-700 ") + (className ? ' ' + className : '')}>{children}</span>
}
