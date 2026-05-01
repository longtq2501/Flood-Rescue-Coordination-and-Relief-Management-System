import Link from "next/link";

const links = [
  { href: "/dashboard/citizen", label: "Citizen" },
  { href: "/dashboard/coordinator", label: "Coordinator" },
  { href: "/dashboard/rescue-team", label: "Rescue Team" },
  { href: "/dashboard/manager", label: "Manager" },
];

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <Link className="text-lg font-bold text-teal-800" href="/">
            Flood Rescue Console
          </Link>
          <nav className="flex flex-wrap items-center gap-3 text-sm">
            {links.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className="rounded-lg px-3 py-1.5 text-slate-700 hover:bg-teal-50 hover:text-teal-800"
              >
                {item.label}
              </Link>
            ))}
            <Link
              href="/login"
              className="rounded-lg bg-slate-900 px-3 py-1.5 text-white hover:bg-slate-700"
            >
              Switch account
            </Link>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl p-4">{children}</main>
    </div>
  );
}
