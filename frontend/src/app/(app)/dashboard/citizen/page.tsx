import { CreateRequestForm } from "@/features/request/components/create-request-form";
import { MyRequestsList } from "@/features/request/components/my-requests-list";

export default function CitizenDashboardPage() {
  return (
    <div className="grid gap-4 lg:grid-cols-2">
      <CreateRequestForm />
      <MyRequestsList />
    </div>
  );
}
