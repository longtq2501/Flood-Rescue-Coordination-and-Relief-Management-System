"use client";

import { useState, useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { X, Loader2, ImagePlus } from "lucide-react";
import Image from "next/image";

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
  images: z.custom<File[] | null>().optional()
    .refine((files) => !files || files.length <= 5, "Toi da 5 hinh anh")
    .refine(
      (files) => !files || files.every((file) => file.size <= 5 * 1024 * 1024),
      "Kich thuoc moi anh toi da 5MB"
    )
    .refine(
      (files) => !files || files.every((file) => ["image/jpeg", "image/jpg", "image/png", "image/webp"].includes(file.type)),
      "Dinh dang khong ho tro. Chi nhan JPEG, PNG, WEBP."
    ),
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
    setValue,
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

  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [previewUrls, setPreviewUrls] = useState<string[]>([]);

  useEffect(() => {
    const urls = selectedFiles.map(file => URL.createObjectURL(file));
    setPreviewUrls(urls);
    return () => {
      urls.forEach(url => URL.revokeObjectURL(url));
    };
  }, [selectedFiles]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const newFiles = Array.from(e.target.files);
      const combined = [...selectedFiles, ...newFiles].slice(0, 5);
      setSelectedFiles(combined);
      setValue("images", combined, { shouldValidate: true });
    }
    e.target.value = ""; // Reset to allow selecting the same file again if removed
  };

  const removeFile = (index: number) => {
    const updated = selectedFiles.filter((_, i) => i !== index);
    setSelectedFiles(updated);
    setValue("images", updated, { shouldValidate: true });
  };

  const mutation = useMutation({
    mutationFn: createRescueRequest,
    onSuccess: () => {
      toast.success("Da gui yeu cau cuu ho");
      queryClient.invalidateQueries({ queryKey: ["my-requests"] });
      setSelectedFiles([]);
      setPreviewUrls([]);
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
        <div className="md:col-span-2">
          <label className="mb-2 flex items-center gap-2 text-sm font-medium text-slate-700">
            <ImagePlus className="h-4 w-4" />
            Hinh anh hien truong (Toi da 5 anh, 5MB/anh)
          </label>
          <div className="flex flex-wrap gap-4">
            {previewUrls.map((url, i) => (
              <div key={i} className="relative h-24 w-24 overflow-hidden rounded-xl border border-slate-200">
                <Image 
                  src={url} 
                  alt={`Preview ${i}`} 
                  width={96}
                  height={96}
                  className="h-full w-full object-cover" 
                  unoptimized
                />
                <button
                  type="button"
                  onClick={() => removeFile(i)}
                  className="absolute right-1 top-1 rounded-full bg-black/50 p-1 text-white hover:bg-black/70"
                >
                  <X className="h-3 w-3" />
                </button>
              </div>
            ))}
            {selectedFiles.length < 5 && (
              <label className="flex h-24 w-24 cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed border-slate-300 bg-slate-50 hover:bg-slate-100 transition-colors">
                <ImagePlus className="h-6 w-6 text-slate-400" />
                <span className="mt-1 text-xs text-slate-500">Them anh</span>
                <input
                  type="file"
                  multiple
                  accept="image/jpeg,image/png,image/jpg,image/webp"
                  className="hidden"
                  onChange={handleFileChange}
                />
              </label>
            )}
          </div>
          {errors.images && (
            <p className="mt-1 text-xs text-red-500">{errors.images.message}</p>
          )}
        </div>
      </div>

      {errors.root || Object.keys(errors).length > 0 ? (
        <div className="mt-4 rounded-xl bg-red-50 p-3 text-sm text-red-600">
          {errors.root?.message || "Vui long kiem tra lai thong tin nhap lieu"}
        </div>
      ) : null}

      <button
        disabled={mutation.isPending}
        type="submit"
        className="mt-4 flex w-full items-center justify-center gap-2 rounded-xl bg-teal-700 px-4 py-3 font-semibold text-white transition-colors hover:bg-teal-800 disabled:opacity-60 md:w-auto"
      >
        {mutation.isPending ? (
          <>
            <Loader2 className="h-5 w-5 animate-spin" />
            Dang gui yeu cau...
          </>
        ) : (
          "Gui yeu cau"
        )}
      </button>
    </form>
  );
}
