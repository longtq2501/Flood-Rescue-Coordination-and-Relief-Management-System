import { LoginForm } from "@/features/auth/components/login-form";

export default function LoginPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top,#99f6e4_0,#f8fafc_45%,#e2e8f0_100%)] p-4">
      <section className="w-full max-w-md rounded-3xl border border-slate-200 bg-white/95 p-6 shadow-xl backdrop-blur">
        <h1 className="text-2xl font-bold text-slate-900">Flood Rescue Login</h1>
        <p className="mt-1 text-sm text-slate-600">
          Dang nhap de tiep tuc dieu phoi va van hanh cuu ho.
        </p>
        <div className="mt-6">
          <LoginForm />
        </div>
      </section>
    </main>
  );
}
