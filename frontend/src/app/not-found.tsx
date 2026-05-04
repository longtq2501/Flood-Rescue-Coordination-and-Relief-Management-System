import Link from "next/link";

export default function NotFound() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <section className="w-full max-w-md rounded-2xl bg-white p-6 text-center shadow-md">
        <h1 className="text-2xl font-bold text-slate-900">Khong tim thay trang</h1>
        <p className="mt-2 text-sm text-slate-600">Duong dan khong ton tai hoac ban khong co quyen truy cap.</p>
        <Link
          href="/login"
          className="mt-4 inline-flex rounded-lg bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800"
        >
          Ve trang dang nhap
        </Link>
      </section>
    </main>
  );
}
