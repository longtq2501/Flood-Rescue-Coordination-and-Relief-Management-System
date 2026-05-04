import { WarehouseDetails } from "@/features/resource/components/warehouse/WarehouseDetails";

export default async function WarehouseDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const warehouseId = parseInt(id);

  return (
    <div className="container mx-auto py-6">
      <WarehouseDetails id={warehouseId} />
    </div>
  );
}
