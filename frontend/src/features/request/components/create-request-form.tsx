"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";

import { createRescueRequest } from "@/features/request/services/request.service";
import type { CreateRescueRequestPayload } from "@/features/request/types/request.types";

const optionalNumber = z.preprocess(
  (value) => (value === "" || value === null || value === undefined ? undefined : value),
  z.coerce.number().optional()
);

const baseSchema = z.object({
  lat: optionalNumber,
  lng: optionalNumber,
  addressText: z.string().optional(),
  description: z.string().min(10, "Mo ta toi thieu 10 ky tu"),
  numPeople: z.coerce.number().int().min(1),
  urgencyLevel: z.enum(["CRITICAL", "HIGH", "MEDIUM", "LOW"]),
  images: z.custom<FileList | null>().optional(),
});

const schema = baseSchema.refine((value) => {
  const hasGps = value.lat !== undefined && value.lng !== undefined;
  const hasAddress = Boolean(value.addressText?.trim());
  return hasGps || hasAddress;
}, { message: "Can co GPS hoac dia chi thu cong", path: ["addressText"] });


export function CreateRequestForm() {
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<CreateRescueRequestPayload>({
    resolver: zodResolver(schema),
    defaultValues: {
      lat: undefined,
      lng: undefined,
      addressText: "",
      description: "",
      urgencyLevel: "HIGH",
      numPeople: 1,
      images: null,
    },
  });

  const mutation = useMutation({
    mutationFn: createRescueRequest,
    onSuccess: () => {
      toast.success("Da gui yeu cau cuu ho");
      queryClient.invalidateQueries({ queryKey: ["my-requests"] });
      reset();
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Gui yeu cau that bai");
    },
  });

  return (
    <form
      onSubmit={handleSubmit((values) => mutation.mutate(values))}
      className="rounded-2xl border border-slate-200 bg-white p-4"
    >
      <h2 className="text-lg font-semibold text-slate-900">Tao yeu cau cuu ho</h2>
      <div className="mt-4 grid gap-3 md:grid-cols-2">
        <div className="flex flex-col gap-1">
          <input
            {...register("lat")}
            step="any"
            type="number"
            placeholder="Latitude"
            className="w-full rounded-xl border border-slate-300 px-3 py-2 outline-none focus:border-teal-600"
          />
          {errors.lat && (
            <p className="text-xs text-red-500">{errors.lat.message}</p>
          )}
        </div>
        <div className="flex flex-col gap-1">
          <input
            {...register("lng")}
            step="any"
            type="number"
            placeholder="Longitude"
            className="w-full rounded-xl border border-slate-300 px-3 py-2 outline-none focus:border-teal-600"
          />
          {errors.lng && (
            <p className="text-xs text-red-500">{errors.lng.message}</p>
          )}
        </div>
        <div className="flex flex-col gap-1 md:col-span-2">
          <input
            {...register("addressText")}
            placeholder="Dia chi thu cong"
            className="w-full rounded-xl border border-slate-300 px-3 py-2 outline-none focus:border-teal-600"
          />
          {errors.addressText && (
            <p className="text-xs text-red-500">{errors.addressText.message}</p>
          )}
        </div>

        <div className="flex flex-col gap-1 md:col-span-2">
          <textarea
            {...register("description")}
            placeholder="Mo ta tinh trang khan cap (toi thieu 10 ky tu)"
            className="min-h-28 w-full rounded-xl border border-slate-300 px-3 py-2 outline-none focus:border-teal-600"
          />
          {errors.description && (
            <p className="text-xs text-red-500">{errors.description.message}</p>
          )}
        </div>
        <div className="flex flex-col gap-1">
          <input
            {...register("numPeople")}
            type="number"
            min={1}
            placeholder="So nguoi"
            className="w-full rounded-xl border border-slate-300 px-3 py-2 outline-none focus:border-teal-600"
          />
          {errors.numPeople && (
            <p className="text-xs text-red-500">{errors.numPeople.message}</p>
          )}
        </div>

        <div className="flex flex-col gap-1">
          <select
            {...register("urgencyLevel")}
            className="w-full rounded-xl border border-slate-300 px-3 py-2 outline-none focus:border-teal-600"
          >
            <option value="CRITICAL">CRITICAL</option>
            <option value="HIGH">HIGH</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="LOW">LOW</option>
          </select>
          {errors.urgencyLevel && (
            <p className="text-xs text-red-500">{errors.urgencyLevel.message}</p>
          )}
        </div>
        <input
          {...register("images")}
          multiple
          type="file"
          accept="image/*"
          className="rounded-xl border border-slate-300 px-3 py-2 md:col-span-2"
        />
      </div>

      {errors.root || Object.keys(errors).length > 0 ? (
        <div className="mt-4 rounded-xl bg-red-50 p-3 text-sm text-red-600">
          {errors.root?.message || "Vui long kiem tra lai thong tin nhap lieu"}
        </div>
      ) : null}

      <button
        disabled={mutation.isPending}
        type="submit"
        className="mt-4 rounded-xl bg-teal-700 px-4 py-2 font-semibold text-white hover:bg-teal-800 disabled:opacity-60"
      >
        {mutation.isPending ? "Dang gui..." : "Gui yeu cau"}
      </button>
    </form>
  );
}
