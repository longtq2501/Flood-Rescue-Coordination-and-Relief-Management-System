import Link from 'next/link';
import { Button } from '@/shared/components/ui/button';

export function CTA() {
  return (
    <section className="text-center py-12 bg-gray-100 rounded-xl">
      <h2 className="text-2xl font-bold mb-4">Sẵn sàng bảo vệ bạn và gia đình</h2>
      <Button size="lg" asChild>
        <Link href="/register">Đăng ký ngay</Link>
      </Button>
    </section>
  );
}