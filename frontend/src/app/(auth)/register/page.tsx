import { RegisterForm } from "@/features/auth/components/register-form";

export default function RegisterPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top,#bae6fd_0,#f8fafc_45%,#e2e8f0_100%)] p-4">
      <section className="w-full max-w-md rounded-3xl border border-slate-200 bg-white/95 p-6 shadow-xl backdrop-blur">
        <h1 className="text-2xl font-bold text-slate-900">Tao tai khoan</h1>
        <p className="mt-1 text-sm text-slate-600">
          Ho tro role CITIZEN va RESCUE_TEAM theo contract backend.
        </p>
        <div className="mt-6">
          <RegisterForm />
        </div>
      </section>
    </main>
  );
}
