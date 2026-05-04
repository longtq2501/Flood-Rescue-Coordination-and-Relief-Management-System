"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createWarehouse } from "@/features/resource/services/resource.service";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ArrowLeft, Save } from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";

export function CreateWarehouseForm() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState({
    name: "",
    location: "",
    capacity: 0,
  });

  const mutation = useMutation({
    mutationFn: createWarehouse,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["warehouses"] });
      toast.success("Warehouse created successfully!");
      router.push("/dashboard/manager/warehouses");
    },
    onError: (error: Error) => {
      toast.error(error.message || "Failed to create warehouse");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.capacity <= 0) {
      toast.error("Capacity must be greater than 0");
      return;
    }
    mutation.mutate(formData);
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/dashboard/manager/warehouses">
          <Button variant="ghost" icon={ArrowLeft}>Back</Button>
        </Link>
        <h1 className="text-2xl font-bold text-slate-900">Create New Warehouse</h1>
      </div>

      <form onSubmit={handleSubmit} className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm space-y-6">
        <div className="space-y-2">
          <label htmlFor="name" className="text-sm font-semibold text-slate-700">
            Warehouse Name
          </label>
          <Input
            id="name"
            placeholder="e.g. Central Hub A"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            required
            className="w-full"
          />
        </div>

        <div className="space-y-2">
          <label htmlFor="location" className="text-sm font-semibold text-slate-700">
            Location Address
          </label>
          <Input
            id="location"
            placeholder="e.g. 123 River Road, District 7"
            value={formData.location}
            onChange={(e) => setFormData({ ...formData, location: e.target.value })}
            required
            className="w-full"
          />
        </div>

        <div className="space-y-2">
          <label htmlFor="capacity" className="text-sm font-semibold text-slate-700">
            Capacity (units)
          </label>
          <Input
            id="capacity"
            type="number"
            placeholder="5000"
            value={formData.capacity || ""}
            onChange={(e) => setFormData({ ...formData, capacity: parseInt(e.target.value) || 0 })}
            required
            className="w-full"
          />
        </div>

        <div className="pt-4 flex justify-end">
          <Button
            type="submit"
            icon={Save}
            disabled={mutation.isPending}
            className="w-full sm:w-auto px-8"
          >
            {mutation.isPending ? "Creating..." : "Save Warehouse"}
          </Button>
        </div>
      </form>
    </div>
  );
}
