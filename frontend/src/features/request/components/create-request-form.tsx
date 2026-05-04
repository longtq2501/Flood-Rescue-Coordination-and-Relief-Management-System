"use client";

import { useState, useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { X, Loader2, ImagePlus, MapPin, Navigation } from "lucide-react";
import Image from "next/image";
import dynamic from "next/dynamic";

import { createRescueRequest } from "@/features/request/services/request.service";
import type { CreateRescueRequestPayload } from "@/features/request/types/request.types";
import { useAuthStore } from "@/features/auth/store/auth.store";

// Dynamic import for the map to avoid SSR issues
const LocationPickerMap = dynamic(() => import("./location-picker-map"), {
  ssr: false,
  loading: () => (
    <div className="flex h-64 w-full items-center justify-center rounded-xl border border-slate-200 bg-slate-50">
      <Loader2 className="h-6 w-6 animate-spin text-slate-400" />
    </div>
  ),
});

const optionalNumber = z.preprocess(
  (value) => (value === "" || value === null || value === undefined ? undefined : value),
  z.coerce.number().optional()
);

const baseSchema = z.object({
  lat: optionalNumber,
  lng: optionalNumber,
  addressText: z.string().optional(),
  description: z.string().min(10, "Mô tả tối thiểu 10 ký tự"),
  numPeople: z.coerce.number().int().min(1, "Tối thiểu 1 người"),
  urgencyLevel: z.enum(["CRITICAL", "HIGH", "MEDIUM", "LOW"]),
  images: z.custom<File[] | null>().optional()
    .refine((files) => !files || files.length <= 5, "Tối đa 5 hình ảnh")
    .refine(
      (files) => !files || files.every((file) => file.size <= 5 * 1024 * 1024),
      "Kích thước mỗi ảnh tối đa 5MB"
    )
    .refine(
      (files) => !files || files.every((file) => ["image/jpeg", "image/jpg", "image/png", "image/webp"].includes(file.type)),
      "Định dạng không hỗ trợ. Chỉ nhận JPEG, PNG, WEBP."
    ),
});

const schema = baseSchema.refine((value) => {
  const hasGps = value.lat !== undefined && value.lng !== undefined;
  const hasAddress = Boolean(value.addressText?.trim());
  return hasGps || hasAddress;
}, { message: "Cần có GPS hoặc địa chỉ thủ công", path: ["addressText"] });

export function CreateRequestForm() {
  const queryClient = useQueryClient();
  const role = useAuthStore((state) => state.role);
  const hydrated = useAuthStore((state) => state.hydrated);
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    setValue,
    watch,
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
  const [locationMessage, setLocationMessage] = useState<{ type: "idle" | "pending" | "success" | "error"; text: string }>({
    type: "idle",
    text: "",
  });
  const watchLat = watch("lat");
  const watchLng = watch("lng");
  const isCitizen = hydrated ? role === "CITIZEN" || !role : true;
  const showRoleHint = hydrated && isCitizen;
  const showUrgencySelect = hydrated && !isCitizen;

  useEffect(() => {
    const urls = selectedFiles.map(file => URL.createObjectURL(file));
    setPreviewUrls(urls);
    return () => {
      urls.forEach(url => URL.revokeObjectURL(url));
    };
  }, [selectedFiles]);

  const fetchAddress = async (lat: number, lng: number) => {
    try {
      const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`);
      const data = await res.json();
      if (data.display_name) {
        setValue("addressText", data.display_name, { shouldValidate: true });
      }
    } catch (error) {
      console.warn("Không thể lấy địa chỉ tự động:", error);
    }
  };

  const handleGetLocation = () => {
    if (!navigator.geolocation) {
      setLocationMessage({
        type: "error",
        text: "Trình duyệt không hỗ trợ định vị. Hãy nhập vị trí thủ công.",
      });
      return;
    }

    setLocationMessage({
      type: "pending",
      text: "Đang lấy vị trí hiện tại...",
    });

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords;
        setValue("lat", latitude);
        setValue("lng", longitude);
        fetchAddress(latitude, longitude);
        setLocationMessage({
          type: "success",
          text: "Đã lấy vị trí hiện tại.",
        });
        toast.success("Đã lấy vị trí hiện tại");
      },
      (err) => {
        setLocationMessage({
          type: "error",
          text: err.message === "User denied Geolocation"
            ? "Bạn đã từ chối cấp quyền vị trí. Hãy nhập địa chỉ hoặc ghim trên bản đồ."
            : `Không thể lấy vị trí: ${err.message}`,
        });
      }
    );
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const newFiles = Array.from(e.target.files);
      const combined = [...selectedFiles, ...newFiles].slice(0, 5);
      setSelectedFiles(combined);
      setValue("images", combined, { shouldValidate: true });
    }
    e.target.value = "";
  };

  const removeFile = (index: number) => {
    const updated = selectedFiles.filter((_, i) => i !== index);
    setSelectedFiles(updated);
    setValue("images", updated, { shouldValidate: true });
  };

  const mutation = useMutation({
    mutationFn: createRescueRequest,
    onSuccess: () => {
      toast.success("Đã gửi yêu cầu cứu hộ");
      queryClient.invalidateQueries({ queryKey: ["my-requests"] });
      setSelectedFiles([]);
      setPreviewUrls([]);
      reset();
    },
    onError: (error: any) => {
      const errorMessage = error.response?.data?.message || error.message || "Gửi yêu cầu thất bại";
      toast.error(errorMessage);
    },
  });

  return (
    <form
      onSubmit={handleSubmit((values) => mutation.mutate(values))}
      className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
    >
      <div className="mb-6">
        <h2 className="text-xl font-bold text-slate-900">Tạo yêu cầu cứu hộ</h2>
        <p className="text-sm text-slate-500">Vui lòng cung cấp vị trí và thông tin tình trạng để chúng tôi hỗ trợ kịp thời.</p>
      </div>

      <div className="grid gap-6">
        {/* Location Section */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <label className="flex items-center gap-2 text-sm font-semibold text-slate-700">
              <MapPin className="h-4 w-4 text-teal-600" />
              Vị trí của bạn
            </label>
            <button
              type="button"
              onClick={handleGetLocation}
              className="flex items-center gap-1.5 text-xs font-medium text-teal-600 hover:text-teal-700"
            >
              <Navigation className="h-3 w-3" />
              Sử dụng vị trí hiện tại
            </button>
          </div>

          {locationMessage.type !== "idle" && (
            <p
              className={
                locationMessage.type === "error"
                  ? "text-xs font-medium text-rose-600"
                  : locationMessage.type === "success"
                    ? "text-xs font-medium text-emerald-600"
                    : "text-xs font-medium text-slate-500"
              }
            >
              {locationMessage.text}
            </p>
          )}

          <LocationPickerMap
            lat={watchLat}
            lng={watchLng}
            onChange={(lat, lng) => {
              setValue("lat", lat);
              setValue("lng", lng);
              fetchAddress(lat, lng);
              setLocationMessage({
                type: "success",
                text: "Đã ghim vị trí trên bản đồ.",
              });
            }}
          />

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-1.5">
              <span className="text-xs font-medium text-slate-500">Kinh độ (Latitude)</span>
              <input
                {...register("lat")}
                step="any"
                type="number"
                placeholder="Ví dụ: 10.762"
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-teal-600 focus:bg-white transition-all"
              />
            </div>
            <div className="space-y-1.5">
              <span className="text-xs font-medium text-slate-500">Vĩ độ (Longitude)</span>
              <input
                {...register("lng")}
                step="any"
                type="number"
                placeholder="Ví dụ: 106.660"
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-teal-600 focus:bg-white transition-all"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-medium text-slate-500">Địa chỉ chi tiết (nếu có)</label>
            <input
              {...register("addressText")}
              placeholder="Số nhà, tên đường, phường/xã..."
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-teal-600 focus:bg-white transition-all"
            />
            {errors.addressText && (
              <p className="text-xs text-red-500">{errors.addressText.message}</p>
            )}
          </div>
        </div>

        {/* Details Section */}
        <div className="grid gap-4 md:grid-cols-2">
          <div className="space-y-1.5 md:col-span-2">
            <label className="text-sm font-semibold text-slate-700">Tình trạng khẩn cấp</label>
            <textarea
              {...register("description")}
              placeholder="Hãy mô tả chi tiết tình hình hiện tại (số người mắc kẹt, tình trạng sức khỏe, mực nước...)"
              className="min-h-24 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-teal-600 focus:bg-white transition-all"
            />
            {errors.description && (
              <p className="text-xs text-red-500">{errors.description.message}</p>
            )}
          </div>

          <div className="space-y-1.5">
            <label className="text-sm font-semibold text-slate-700">Số người cần cứu hộ</label>
            <input
              {...register("numPeople")}
              type="number"
              min={1}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-teal-600 focus:bg-white transition-all"
            />
            {errors.numPeople && (
              <p className="text-xs text-red-500">{errors.numPeople.message}</p>
            )}
          </div>

          {showRoleHint ? (
            <div className="space-y-1.5 rounded-xl border border-teal-100 bg-teal-50/70 px-4 py-3 text-sm text-teal-800 md:col-span-1">
              <p className="font-semibold">Mức độ ưu tiên do hệ thống xử lý</p>
              <p className="text-xs leading-5 text-teal-700/90">
                Với tài khoản citizen, mức độ ưu tiên sẽ được điều phối viên đánh giá từ nội dung và vị trí bạn cung cấp.
              </p>
            </div>
          ) : showUrgencySelect ? (
            <div className="space-y-1.5">
              <label className="text-sm font-semibold text-slate-700">Mức độ ưu tiên</label>
              <select
                {...register("urgencyLevel")}
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none transition-all focus:border-teal-600 focus:bg-white"
              >
                <option value="CRITICAL">NGUY CẤP (Đe dọa tính mạng)</option>
                <option value="HIGH">CAO (Cần cứu hộ gấp)</option>
                <option value="MEDIUM">TRUNG BÌNH (Mắc kẹt, an toàn)</option>
                <option value="LOW">THẤP (Cần hỗ trợ nhu yếu phẩm)</option>
              </select>
              {errors.urgencyLevel && (
                <p className="text-xs text-red-500">{errors.urgencyLevel.message}</p>
              )}
            </div>
          ) : (
            <div className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-500 md:col-span-1">
              Đang đồng bộ quyền tài khoản...
            </div>
          )}
        </div>

        {/* Images Section */}
        <div className="space-y-3">
          <label className="flex items-center gap-2 text-sm font-semibold text-slate-700">
            <ImagePlus className="h-4 w-4 text-teal-600" />
            Hình ảnh hiện trường
          </label>
          <div className="flex flex-wrap gap-3">
            {previewUrls.map((url, i) => (
              <div key={i} className="relative h-20 w-20 overflow-hidden rounded-xl border border-slate-200 shadow-sm">
                <Image 
                  src={url} 
                  alt={`Preview ${i}`} 
                  width={80}
                  height={80}
                  className="h-full w-full object-cover" 
                  unoptimized
                />
                <button
                  type="button"
                  onClick={() => removeFile(i)}
                  className="absolute right-1 top-1 rounded-full bg-black/50 p-1 text-white hover:bg-black/70 transition-colors"
                >
                  <X className="h-2.5 w-2.5" />
                </button>
              </div>
            ))}
            {selectedFiles.length < 5 && (
              <label className="flex h-20 w-20 cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed border-slate-200 bg-slate-50 hover:bg-slate-100 hover:border-teal-600 transition-all">
                <ImagePlus className="h-5 w-5 text-slate-400" />
                <span className="mt-1 text-[10px] font-medium text-slate-500 text-center px-1">Thêm ảnh</span>
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
            <p className="text-xs text-red-500">{errors.images.message}</p>
          )}
        </div>
      </div>

      <div className="mt-8 border-t border-slate-100 pt-6">
        <button
          disabled={mutation.isPending}
          type="submit"
          className="flex w-full items-center justify-center gap-2 rounded-xl bg-teal-700 px-6 py-3.5 font-bold text-white shadow-lg shadow-teal-700/20 transition-all hover:bg-teal-800 hover:translate-y-[-1px] active:translate-y-[0px] disabled:opacity-60"
        >
          {mutation.isPending ? (
            <>
              <Loader2 className="h-5 w-5 animate-spin" />
              ĐANG GỬI YÊU CẦU...
            </>
          ) : (
            "GỬI YÊU CẦU CỨU HỘ"
          )}
        </button>
      </div>
    </form>
  );
}
