import {
  ActivityIcon,
  BellIcon,
  GaugeIcon,
  ShieldCheckIcon,
  WifiIcon,
  WrenchIcon,
} from 'lucide-react'
import FeatureFragment from './FeatureFragment'

const features = [
  {
    icon: WifiIcon,
    title: 'Realtime SSE',
    description:
      'Luồng dữ liệu thời gian thực qua Server-Sent Events, mượt mà và ổn định.',
  },
  {
    icon: GaugeIcon,
    title: 'Đa cảm biến',
    description:
      'DHT (nhiệt/ẩm), PIR (chuyển động), Gas (cảnh báo) và hơn nữa.',
  },
  {
    icon: ActivityIcon,
    title: 'Biểu đồ nhẹ',
    description: 'Sparkline trực quan giúp theo dõi xu hướng nhiệt độ/độ ẩm.',
  },
  {
    icon: WrenchIcon,
    title: 'Điều khiển thiết bị',
    description: 'Quạt, LED PIR… thao tác tức thời và ghi nhận sự kiện.',
  },
  {
    icon: BellIcon,
    title: 'Cảnh báo tức thì',
    description: 'Phát hiện chuyển động, rò rỉ khí, gửi cảnh báo liền tay.',
  },
  {
    icon: ShieldCheckIcon,
    title: 'Bảo mật đầy đủ',
    description: 'Đăng nhập an toàn, làm mới token, phân quyền linh hoạt.',
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
            Mọi thứ bạn cần cho nhà thông minh
          </p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
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
