import { Link } from '@tanstack/react-router'
import { ArrowRightIcon, Wifi, Gauge, Bell, Shield } from 'lucide-react'

const Hero = () => {
  return (
    <div className="bg-gradient-to-br from-blue-50 to-emerald-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div className="text-center">
          <h1 className="text-4xl md:text-6xl font-extrabold text-gray-900 mb-6">
            IoT Smart Home
            <span className="text-blue-600"> Realtime Dashboard</span>
          </h1>
          <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
            Giám sát cảm biến, điều khiển thiết bị và nhận cảnh báo tức thời qua
            SSE. Giao diện hiện đại, mượt mà trên mọi thiết bị.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/register"
              className="inline-flex items-center px-8 py-3 bg-blue-600 text-white rounded-lg text-lg font-medium hover:bg-blue-700 transition-colors"
            >
              Bắt đầu miễn phí
              <ArrowRightIcon className="ml-2 h-5 w-5" />
            </Link>
            <Link
              to="/login"
              search={{ redirectLink: '/dashboard' }}
              className="inline-flex items-center px-8 py-3 bg-white text-blue-600 border border-blue-600 rounded-lg text-lg font-medium hover:bg-blue-50 transition-colors"
            >
              Đăng nhập
            </Link>
          </div>

          <div className="mt-12 grid grid-cols-2 sm:grid-cols-4 gap-4 max-w-3xl mx-auto text-sm">
            <div className="flex items-center justify-center gap-2 rounded-lg bg-white/70 p-3 ring-1 ring-gray-200">
              <Wifi className="h-4 w-4 text-blue-600" /> Realtime SSE
            </div>
            <div className="flex items-center justify-center gap-2 rounded-lg bg-white/70 p-3 ring-1 ring-gray-200">
              <Gauge className="h-4 w-4 text-emerald-600" /> DHT · PIR · Gas
            </div>
            <div className="flex items-center justify-center gap-2 rounded-lg bg-white/70 p-3 ring-1 ring-gray-200">
              <Bell className="h-4 w-4 text-amber-600" /> Cảnh báo tức thời
            </div>
            <div className="flex items-center justify-center gap-2 rounded-lg bg-white/70 p-3 ring-1 ring-gray-200">
              <Shield className="h-4 w-4 text-violet-600" /> Bảo mật đăng nhập
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Hero
