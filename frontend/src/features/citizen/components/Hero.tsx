import Link from 'next/link';
import { Button } from '@/shared/components/ui/button';

export function Hero() {
  return (
    <section className="text-center py-20 bg-gradient-to-r from-red-500 to-orange-500 rounded-xl text-white">
      <h1 className="text-5xl font-bold mb-4">Cứu hộ khẩn cấp 24/7</h1>
      <p className="text-xl mb-8">Kết nối nhanh chóng với đội cứu hộ trong tình huống khẩn cấp</p>
      <div className="space-x-4">
        <Button size="lg" asChild>
          <Link href="/register">Đăng ký ngay</Link>
        </Button>
        <Button size="lg" variant="outline" className="bg-white text-red-500" asChild>
          <Link href="/login">Đăng nhập</Link>
        </Button>
      </div>
    </section>
  );
}