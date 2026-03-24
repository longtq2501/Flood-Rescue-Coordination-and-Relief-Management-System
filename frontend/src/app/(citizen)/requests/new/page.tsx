'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Input } from '@/shared/components/ui/input';
import { Textarea } from '@/shared/components/ui/textarea';
import { Button } from '@/shared/components/ui/button';
import { Label } from '@/shared/components/ui/label';
import { useFileUpload } from '@/features/citizen/hooks/useFileUpload';
import { toast } from 'sonner';
import { useRouter } from 'next/navigation';

const createRequestSchema = z.object({
  title: z.string().min(5, 'Tiêu đề phải có ít nhất 5 ký tự'),
  description: z.string().min(10, 'Mô tả phải có ít nhất 10 ký tự'),
  location: z.string().min(5, 'Vui lòng nhập địa chỉ'),
  urgency: z.enum(['low', 'medium', 'high', 'critical']),
  peopleCount: z.number().min(1).max(50).optional(),
});

type CreateRequestForm = z.infer<typeof createRequestSchema>;

export default function NewRequestPage() {
  const router = useRouter();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<CreateRequestForm>({
    resolver: zodResolver(createRequestSchema),
    defaultValues: {
      urgency: 'medium',
      peopleCount: 1,
    },
  });

  const { files, previews, addFiles, removeFile, clearFiles } = useFileUpload(5);

  const onSubmit = async (data: CreateRequestForm) => {
    try {
      // TODO: gọi API tạo request với data và files
      console.log({ ...data, files });
      toast.success('Yêu cầu đã được gửi!');
      router.push('/requests');
    } catch (error) {
      toast.error('Gửi yêu cầu thất bại, vui lòng thử lại.');
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      addFiles(e.target.files);
      e.target.value = '';
    }
  };

  return (
    <div className="max-w-2xl mx-auto py-8">
      <h1 className="text-3xl font-bold mb-6">Gửi yêu cầu cứu hộ</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <div>
          <Label htmlFor="title">Tiêu đề</Label>
          <Input id="title" {...register('title')} />
          {errors.title && <p className="text-red-500 text-sm">{errors.title.message}</p>}
        </div>

        <div>
          <Label htmlFor="description">Mô tả chi tiết</Label>
          <Textarea id="description" rows={4} {...register('description')} />
          {errors.description && <p className="text-red-500 text-sm">{errors.description.message}</p>}
        </div>

        <div>
          <Label htmlFor="location">Địa chỉ</Label>
          <Input id="location" {...register('location')} />
          {errors.location && <p className="text-red-500 text-sm">{errors.location.message}</p>}
        </div>

        <div>
          <Label htmlFor="urgency">Mức độ khẩn cấp</Label>
          <select
            id="urgency"
            {...register('urgency')}
            className="w-full border rounded-md p-2"
          >
            <option value="low">Thấp</option>
            <option value="medium">Trung bình</option>
            <option value="high">Cao</option>
            <option value="critical">Nguy cấp</option>
          </select>
          {errors.urgency && <p className="text-red-500 text-sm">{errors.urgency.message}</p>}
        </div>

        <div>
          <Label htmlFor="peopleCount">Số người cần hỗ trợ (tùy chọn)</Label>
          <Input
            id="peopleCount"
            type="number"
            {...register('peopleCount', { valueAsNumber: true })}
          />
          {errors.peopleCount && <p className="text-red-500 text-sm">{errors.peopleCount.message}</p>}
        </div>

        <div>
          <Label>Hình ảnh (tối đa 5)</Label>
          <Input type="file" multiple accept="image/*" onChange={handleFileChange} />
          <div className="flex flex-wrap gap-2 mt-2">
            {previews.map((src, idx) => (
              <div key={idx} className="relative w-20 h-20 border rounded">
                <img src={src} alt="preview" className="w-full h-full object-cover rounded" />
                <button
                  type="button"
                  onClick={() => removeFile(idx)}
                  className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 text-xs"
                >
                  ×
                </button>
              </div>
            ))}
          </div>
          {files.length > 0 && (
            <button type="button" onClick={clearFiles} className="text-red-500 text-sm mt-1">
              Xóa tất cả
            </button>
          )}
        </div>

        <Button type="submit" className="w-full" disabled={isSubmitting}>
          {isSubmitting ? 'Đang gửi...' : 'Gửi yêu cầu'}
        </Button>
      </form>
    </div>
  );
}