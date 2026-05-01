"use client";

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <section className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-md">
        <h1 className="text-xl font-bold text-slate-900">Da xay ra loi</h1>
        <p className="mt-2 text-sm text-slate-600">{error.message}</p>
        <button
          onClick={reset}
          className="mt-4 rounded-lg bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800"
        >
          Thu lai
        </button>
      </section>
    </main>
  );
}
