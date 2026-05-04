"use client"

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { motion } from 'framer-motion'
import { 
  ShieldCheck, 
  MapPin, 
  PhoneCall, 
  ArrowRight, 
  Users, 
  Activity, 
  ChevronRight,
  LifeBuoy,
  Clock
} from 'lucide-react'
import Cookies from 'js-cookie'
import { useRouter } from 'next/navigation'
import { USER_ROLE_KEY, ROLE_TO_DASHBOARD_PATH, isAppRole } from '@/shared/constants/auth'
import { Button } from '@/components/ui/button'

export default function LandingPage() {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const roleCookie = Cookies.get(USER_ROLE_KEY)
    if (roleCookie && isAppRole(roleCookie)) {
      router.push(ROLE_TO_DASHBOARD_PATH[roleCookie])
    } else {
      setIsLoading(false)
    }
  }, [router])

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50">
        <div className="flex flex-col items-center gap-4">
          <div className="h-12 w-12 animate-spin rounded-full border-4 border-teal-600 border-t-transparent" />
          <p className="text-sm font-medium text-slate-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-white">
      {/* Navigation */}
      <header className="fixed top-0 z-50 w-full border-b bg-white/80 backdrop-blur-md">
        <div className="container mx-auto flex h-16 items-center justify-between px-4 max-w-7xl">
          <Link href="/" className="flex items-center gap-2">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl premium-gradient shadow-lg shadow-teal-600/20">
              <ShieldCheck className="h-6 w-6 text-white" />
            </div>
            <span className="text-xl font-bold tracking-tight text-slate-900">
              Flood<span className="text-teal-600">Rescue</span>
            </span>
          </Link>
          
          <nav className="hidden md:flex items-center gap-8">
            <Link href="#features" className="text-sm font-medium text-slate-600 hover:text-teal-600 transition-colors">Tính năng</Link>
            <Link href="#about" className="text-sm font-medium text-slate-600 hover:text-teal-600 transition-colors">Về hệ thống</Link>
            <Link href="#contact" className="text-sm font-medium text-slate-600 hover:text-teal-600 transition-colors">Hỗ trợ</Link>
          </nav>

          <div className="flex items-center gap-4">
            <Link href="/login" className="text-sm font-semibold text-slate-700 hover:text-teal-600">Đăng nhập</Link>
            <Link href="/register">
              <Button className="rounded-full px-6 premium-gradient hover:opacity-90">
                Đăng ký tham gia
              </Button>
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative pt-32 pb-20 overflow-hidden">
        <div className="absolute top-0 right-0 -z-10 w-1/2 h-full opacity-10 blur-3xl">
          <div className="absolute top-0 right-0 w-full h-full bg-teal-500 rounded-full" />
        </div>
        
        <div className="container mx-auto px-4 max-w-7xl">
          <div className="grid gap-12 lg:grid-cols-2 lg:items-center">
            <motion.div 
              initial={{ opacity: 0, x: -30 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.6 }}
            >
              <span className="inline-flex items-center gap-2 rounded-full bg-teal-50 px-4 py-1.5 text-sm font-semibold text-teal-700 mb-6">
                <Activity className="h-4 w-4" />
                Hệ thống cứu hộ khẩn cấp 24/7
              </span>
              <h1 className="text-5xl font-extrabold tracking-tight text-slate-900 leading-tight mb-6 lg:text-6xl">
                Phản ứng nhanh hơn,<br />
                <span className="text-teal-600">Cứu được nhiều người hơn.</span>
              </h1>
              <p className="text-lg text-slate-600 mb-10 max-w-lg leading-relaxed">
                Nền tảng điều phối cứu hộ lũ lụt tích hợp công nghệ bản đồ số và định vị thời gian thực, kết nối người dân với các lực lượng cứu hộ ngay lập tức.
              </p>
              
              <div className="flex flex-col sm:flex-row gap-4">
                <Link href="/register">
                  <Button size="lg" className="rounded-2xl h-14 px-8 text-lg font-bold premium-gradient shadow-xl shadow-teal-700/20">
                    <span className="flex items-center gap-2">
                      Yêu cầu cứu trợ khẩn cấp <ArrowRight className="h-5 w-5" />
                    </span>
                  </Button>
                </Link>
                <Link href="/login">
                  <Button variant="outline" size="lg" className="rounded-2xl h-14 px-8 text-lg font-bold border-2 border-slate-200 hover:bg-slate-50 text-slate-700">
                    Theo dõi tình trạng
                  </Button>
                </Link>
              </div>

              <div className="mt-12 grid grid-cols-3 gap-6">
                <div>
                  <p className="text-3xl font-bold text-slate-900">500+</p>
                  <p className="text-sm text-slate-500 mt-1">Đội cứu hộ sẵn sàng</p>
                </div>
                <div>
                  <p className="text-3xl font-bold text-slate-900">2k+</p>
                  <p className="text-sm text-slate-500 mt-1">Yêu cầu đã xử lý</p>
                </div>
                <div>
                  <p className="text-3xl font-bold text-slate-900">10ms</p>
                  <p className="text-sm text-slate-500 mt-1">Độ trễ phản hồi</p>
                </div>
              </div>
            </motion.div>

            <motion.div 
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.8, delay: 0.2 }}
              className="relative"
            >
              <div className="relative rounded-3xl overflow-hidden shadow-2xl border-8 border-white">
                <img 
                  src="/flood_rescue_hero_1777901858906.png" 
                  alt="Flood Rescue Illustration" 
                  className="w-full h-auto object-cover"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-slate-900/40 to-transparent" />
              </div>
              
              {/* Floating elements */}
              <div className="absolute -left-6 -bottom-6 glass p-4 rounded-2xl shadow-xl max-w-[200px] animate-bounce-slow">
                <div className="flex items-center gap-3">
                  <div className="h-8 w-8 rounded-full bg-red-100 flex items-center justify-center text-red-600">
                    <PhoneCall className="h-4 w-4" />
                  </div>
                  <div>
                    <p className="text-xs font-bold text-slate-900">SOS Tin mới</p>
                    <p className="text-[10px] text-slate-500">Quận 7, TP.HCM</p>
                  </div>
                </div>
              </div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section id="features" className="py-24 bg-slate-50">
        <div className="container mx-auto px-4 max-w-7xl">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-slate-900 mb-4">Giải pháp toàn diện cho cứu trợ lũ lụt</h2>
            <p className="text-slate-600 max-w-2xl mx-auto">Hệ thống của chúng tôi được thiết kế để hoạt động ổn định trong mọi điều kiện khắc nghiệt nhất.</p>
          </div>

          <div className="grid gap-8 md:grid-cols-3">
            {[
              {
                icon: MapPin,
                title: "Bản đồ thời gian thực",
                description: "Hiển thị trực quan tất cả các điểm nóng và vị trí của đội cứu hộ trên bản đồ số chính xác nhất."
              },
              {
                icon: Users,
                title: "Điều phối thông minh",
                description: "Tự động phân bổ nhiệm vụ cho đội cứu hộ gần nhất dựa trên mức độ khẩn cấp của yêu cầu."
              },
              {
                icon: LifeBuoy,
                title: "Kết nối tức thì",
                description: "Công nghệ thông báo đẩy giúp lực lượng cứu hộ nhận lệnh ngay khi có yêu cầu mới phát sinh."
              },
              {
                icon: Activity,
                title: "Thống kê & Phân tích",
                description: "Cung cấp cái nhìn tổng quan về tình hình thiên tai để ra quyết định ứng phó hiệu quả nhất."
              },
              {
                icon: Clock,
                title: "Theo dõi lịch sử",
                description: "Mọi hoạt động được ghi lại minh bạch, giúp đánh giá và cải thiện quy trình cứu hộ."
              },
              {
                icon: ShieldCheck,
                title: "Bảo mật & Tin cậy",
                description: "Dữ liệu được mã hóa và bảo mật, đảm bảo thông tin cá nhân của người dân luôn an toàn."
              }
            ].map((feature, i) => (
              <motion.div 
                key={i}
                whileHover={{ y: -5 }}
                className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm hover:shadow-xl transition-all group"
              >
                <div className="h-14 w-14 rounded-2xl bg-teal-50 flex items-center justify-center text-teal-600 mb-6 group-hover:premium-gradient group-hover:text-white transition-all">
                  <feature.icon className="h-7 w-7" />
                </div>
                <h3 className="text-xl font-bold text-slate-900 mb-3">{feature.title}</h3>
                <p className="text-slate-500 leading-relaxed">{feature.description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer id="contact" className="py-12 border-t">
        <div className="container mx-auto px-4 max-w-7xl">
          <div className="flex flex-col md:flex-row justify-between items-center gap-8">
            <div className="flex items-center gap-2">
              <ShieldCheck className="h-6 w-6 text-teal-600" />
              <span className="text-lg font-bold text-slate-900">Flood Rescue</span>
            </div>
            <p className="text-sm text-slate-500">© 2026 Flood Rescue Coordination System. All rights reserved.</p>
            <div className="flex gap-6">
              <Link href="#" className="text-slate-400 hover:text-teal-600 transition-colors">Facebook</Link>
              <Link href="#" className="text-slate-400 hover:text-teal-600 transition-colors">Twitter</Link>
              <Link href="#" className="text-slate-400 hover:text-teal-600 transition-colors">GitHub</Link>
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}
