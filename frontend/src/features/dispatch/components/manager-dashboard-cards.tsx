"use client";

import { useQuery } from "@tanstack/react-query";

import { getManagerDashboard } from "@/features/report/services/report.service";

export function ManagerDashboardCards() {
  const dashboardQuery = useQuery({
    queryKey: ["manager-dashboard"],
    queryFn: getManagerDashboard,
  });

  if (dashboardQuery.isLoading) {
    return <div className="rounded-2xl border border-slate-200 bg-white p-4">Dang tai dashboard...</div>;
  }

  if (dashboardQuery.isError || !dashboardQuery.data) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
        {dashboardQuery.error instanceof Error
          ? dashboardQuery.error.message
          : "Khong tai duoc dashboard"}
      </div>
    );
  }

  const { summary, resourceUsage, byUrgency } = dashboardQuery.data;

  return (
    <div className="space-y-4">
      <section className="grid gap-3 md:grid-cols-3">
        <article className="rounded-2xl border border-slate-200 bg-white p-4">
          <p className="text-sm text-slate-500">Total requests</p>
          <p className="text-3xl font-bold text-slate-900">{summary.totalRequests}</p>
        </article>
        <article className="rounded-2xl border border-slate-200 bg-white p-4">
          <p className="text-sm text-slate-500">Completion rate</p>
          <p className="text-3xl font-bold text-slate-900">{summary.completionRate}%</p>
        </article>
        <article className="rounded-2xl border border-slate-200 bg-white p-4">
          <p className="text-sm text-slate-500">Avg response</p>
          <p className="text-3xl font-bold text-slate-900">{summary.avgResponseMinutes}m</p>
        </article>
      </section>

      <section className="grid gap-3 md:grid-cols-2">
        <article className="rounded-2xl border border-slate-200 bg-white p-4">
          <h3 className="font-semibold text-slate-900">By urgency</h3>
          <div className="mt-3 space-y-1 text-sm text-slate-700">
            {Object.entries(byUrgency).map(([urgency, count]) => (
              <p key={urgency}>
                {urgency}: {count}
              </p>
            ))}
          </div>
        </article>

        <article className="rounded-2xl border border-slate-200 bg-white p-4">
          <h3 className="font-semibold text-slate-900">Resource usage</h3>
          <div className="mt-3 space-y-1 text-sm text-slate-700">
            <p>Vehicles deployed: {resourceUsage.vehiclesDeployed}</p>
            <p>Total distributions: {resourceUsage.totalDistributions}</p>
            <p>
              Active teams: {resourceUsage.activeTeams}/{resourceUsage.totalTeams}
            </p>
          </div>
        </article>
      </section>
    </div>
  );
}
