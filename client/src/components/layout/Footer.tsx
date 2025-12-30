import { Link } from '@tanstack/react-router'
import { BarChart3Icon } from 'lucide-react'

export default function Footer() {
  return (
    <footer className="bg-white border-t border-gray-200 mt-auto">
      <div className="mx-auto px-4 sm:px-6 lg:px-8 py-4">
        <div className="flex align-center justify-between">
          <Link to="/">
            <div className="flex items-center space-x-2">
              <BarChart3Icon className="h-6 w-6 text-blue-600" />
              <span className="text-lg font-bold text-gray-900">
                Project Manager
              </span>
            </div>
          </Link>
          <div className="text-center">
            <p className="text-md text-gray-500">&#169; 2025 Project Manager</p>
          </div>
        </div>
      </div>
    </footer>
  )
}
