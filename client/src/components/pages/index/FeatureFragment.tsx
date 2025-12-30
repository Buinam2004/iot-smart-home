import type { LucideIcon } from 'lucide-react'

interface FeatureFragmentProps {
  icon: LucideIcon
  title: string
  description: string
}

const FeatureFragment: React.FC<FeatureFragmentProps> = ({
  icon: Icon,
  title,
  description,
}) => {
  return (
    <div className="text-center">
      <div className="bg-blue-100 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
        <Icon className="h-8 w-8 text-blue-600" />
      </div>
      <h3 className="text-lg font-semibold text-gray-900 mb-2">{title}</h3>
      <p className="text-gray-600">{description}</p>
    </div>
  )
}

export default FeatureFragment
