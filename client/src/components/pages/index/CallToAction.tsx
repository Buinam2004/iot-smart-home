import { Link } from '@tanstack/react-router'
import { ArrowRightIcon } from 'lucide-react'

const CallToAction: React.FC = () => {
  return (
    <div className="bg-blue-600 py-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h2 className="text-3xl font-bold text-white mb-4">
          Sẵn sàng bắt đầu?
        </h2>
        <p className="text-xl text-blue-100 mb-8">
          Đăng ký miễn phí và trải nghiệm ngay
        </p>
        <Link
          to="/register"
          className="inline-flex items-center px-8 py-3 bg-white text-blue-600 rounded-lg text-lg font-medium hover:bg-gray-50 transition-colors"
        >
          Đăng ký miễn phí
          <ArrowRightIcon className="ml-2 h-5 w-5" />
        </Link>
      </div>
    </div>
  )
}

export default CallToAction
