import { notFound } from "next/navigation";

const DEMO_WIDGETS: Record<string, string[]> = {
  citizen: [
    "Create rescue request",
    "My requests list",
    "Request detail and timeline",
  ],
  coordinator: [
    "Verification queue",
    "Assign team and vehicle",
    "Dispatch board and map",
  ],
  "rescue-team": [
    "My active assignments",
    "Start mission",
    "Complete mission",
  ],
  manager: ["KPI dashboard", "Resource alerts", "Operations summary"],
  admin: ["System oversight", "Report export"],
};

type Props = {
  params: Promise<{ role: string }>;
};

export default async function RoleDashboardPage({ params }: Props) {
  const { role } = await params;
  const widgets = DEMO_WIDGETS[role];

  if (!widgets) {
    notFound();
  }

  return (
    <section className="space-y-4">
      <div className="rounded-2xl bg-gradient-to-r from-teal-700 to-cyan-700 p-6 text-white">
        <h1 className="text-2xl font-bold capitalize">{role} dashboard</h1>
        <p className="mt-1 text-sm text-teal-50">
          Skeleton da san sang cho demo toi nay. Tiep theo la gan API theo flow P0.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        {widgets.map((item) => (
          <article key={item} className="rounded-2xl border border-slate-200 bg-white p-4">
            <h2 className="font-semibold text-slate-900">{item}</h2>
            <p className="mt-1 text-sm text-slate-600">Trang thai: pending integration.</p>
          </article>
        ))}
      </div>
    </section>
  );
}
