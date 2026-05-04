import { CreateRequestForm } from "@/features/request/components/create-request-form";
import { MyRequestsList } from "@/features/request/components/my-requests-list";

export default function CitizenDashboardPage() {
  return (
    <div className="space-y-6">
      <section className="rounded-3xl bg-gradient-to-r from-teal-700 via-teal-700 to-cyan-700 px-5 py-6 text-white shadow-sm sm:px-6">
        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-teal-100">CITIZEN DASHBOARD</p>
        <h1 className="mt-2 text-2xl font-bold sm:text-3xl">Tiếp nhận cứu hộ và theo dõi yêu cầu</h1>
        <p className="mt-2 max-w-3xl text-sm leading-6 text-teal-50/90">
          Trang này chỉ hiển thị các thao tác của người dân: tạo yêu cầu, kiểm tra tiến độ và xem chi tiết.
        </p>
      </section>

      <div className="grid gap-4 lg:grid-cols-2">
        <div id="create-request">
          <CreateRequestForm />
        </div>
        <div id="my-requests">
          <MyRequestsList />
        </div>
      </div>
    </div>
  );
}
