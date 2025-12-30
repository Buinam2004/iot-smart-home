import { Link } from '@tanstack/react-router'
import { ArrowRightIcon } from 'lucide-react'

const Hero = () => {
  return (
    <div className="bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div className="text-center">
          <h1 className="text-4xl md:text-6xl font-bold text-gray-900 mb-6">
            Quản lý dự án
            <span className="text-blue-600"> hiệu quả</span>
          </h1>
          <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
            Hệ thống quản lý dự án toàn diện giúp bạn theo dõi tiến độ, quản lý
            task và tạo báo cáo một cách chuyên nghiệp.
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
              search={{ redirect: '/dashboard' }}
              className="inline-flex items-center px-8 py-3 bg-white text-blue-600 border border-blue-600 rounded-lg text-lg font-medium hover:bg-blue-50 transition-colors"
            >
              Đăng nhập
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Hero
