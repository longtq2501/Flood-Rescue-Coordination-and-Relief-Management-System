import Link from "next/link";

import { RequestDetailCard } from "@/features/request/components/request-detail-card";

type Props = {
  params: Promise<{ id: string }>;
};

export default async function CitizenRequestDetailPage({ params }: Props) {
  const { id } = await params;

  return (
    <div className="space-y-4">
      <Link href="/dashboard/citizen" className="text-sm font-semibold text-teal-700">
        &larr; Quay lai danh sach
      </Link>
      <RequestDetailCard id={id} />
    </div>
  );
}
