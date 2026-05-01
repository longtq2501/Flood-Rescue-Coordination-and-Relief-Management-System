"use client"

export function Modal({ open, onClose, title, children }: { open: boolean; onClose: () => void; title?: string; children: React.ReactNode }) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="relative z-10 w-full max-w-lg rounded-xl bg-white p-4 shadow-lg">
        {title && <h3 className="mb-2 text-lg font-semibold">{title}</h3>}
        <div>{children}</div>
        <div className="mt-4 flex justify-end">
          <button className="rounded-md bg-slate-100 px-3 py-2 text-sm" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  )
}
