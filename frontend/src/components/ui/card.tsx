export function Card({ children, className }: { children: React.ReactNode; className?: string }) {
  return (
    <div className={['rounded-xl border border-slate-100 bg-white p-4 shadow-sm', className].filter(Boolean).join(' ')}>
      {children}
    </div>
  )
}
