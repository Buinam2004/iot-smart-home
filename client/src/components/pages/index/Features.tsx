import {
  BarChart3Icon,
  CheckCircleIcon,
  TrendingUpIcon,
  UsersIcon,
} from 'lucide-react'
import FeatureFragment from './FeatureFragment'

const features = [
  {
    icon: BarChart3Icon,
    title: 'Quản lý dự án hiệu quả',
    description:
      'Theo dõi tiến độ dự án một cách trực quan với biểu đồ Gantt và dashboard tổng quan.',
  },
  {
    icon: CheckCircleIcon,
    title: 'Quản lý task chi tiết',
    description:
      'Tạo, phân công và theo dõi các task với nhiều trạng thái khác nhau.',
  },
  {
    icon: UsersIcon,
    title: 'Cộng tác nhóm',
    description: 'Phân quyền và quản lý thành viên dự án một cách dễ dàng.',
  },
  {
    icon: TrendingUpIcon,
    title: 'Báo cáo tự động',
    description: 'Nhận báo cáo tiến độ tự động qua email theo lịch trình.',
  },
]

const Features = () => {
  return (
    <div className="py-20 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-16">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">
            Tính năng nổi bật
          </h2>
          <p className="text-xl text-gray-600">
            Tất cả những gì bạn cần để quản lý dự án hiệu quả
          </p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {features.map((feature, index) => (
            <FeatureFragment
              key={index}
              icon={feature.icon}
              title={feature.title}
              description={feature.description}
            />
          ))}
        </div>
      </div>
    </div>
  )
}

export default Features
