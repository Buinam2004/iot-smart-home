import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate, useRouteContext } from '@tanstack/react-router'
import { BarChart3Icon, LogOutIcon, UserIcon } from 'lucide-react'

import type { UserData, UserResponse } from '@/types/responses/profileResponses'

import { api } from '@/auth/authApi'

export default function Header() {
  const { isAuthenticated, logout } = useRouteContext({
    from: '__root__',
    select: (ctx) => ({
      isAuthenticated: ctx.auth.isAuthenticated,
      logout: ctx.auth.logout,
    }),
  })
  const navigate = useNavigate()

  const { data: profile, error } = useQuery<UserData>({
    queryKey: ['profile'],
    queryFn: async () => {
      const res = await api.get<UserResponse>('user')
      return res.data.data
    },
  })

  if (error) {
    return (
      <div className="p-6">
        <p className="text-red-600">Error loading profile: {String(error)}</p>
      </div>
    )
  }

  const handleLogout = async () => {
    await logout()
    navigate({ to: '/' })
  }

  return (
    <header className="bg-white">
      <div className="mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center space-x-4">
            <Link to="/" className="flex items-center space-x-2">
              <BarChart3Icon className="h-8 w-8 text-blue-600" />
              <h1 className="text-xl font-bold text-gray-900">
                Project Manager
              </h1>
            </Link>
          </div>

          {isAuthenticated && (
            <div className="flex space-x-6">
              <Link
                to="/dashboard"
                className="text-gray-700 hover:text-blue-600 font-medium"
              >
                Dashboard
              </Link>
            </div>
          )}

          <div className="flex items-center space-x-4">
            {isAuthenticated ? (
              <>
                <div className="flex items-center space-x-2">
                  <Link to="/profile" className="relative group">
                    {profile?.avatar ? (
                      <img
                        src={profile.avatar}
                        alt={profile.displayName}
                        className="h-8 w-8 rounded-full object-cover cursor-pointer"
                      />
                    ) : (
                      <div className="h-8 w-8 rounded-full bg-gray-100 flex items-center justify-center cursor-pointer">
                        <UserIcon className="h-5 w-5 text-gray-500" />
                      </div>
                    )}

                    {/* Tooltip */}
                    <div
                      className="pointer-events-none absolute left-1/2 top-full mt-2 -translate-x-1/2 w-max hidden group-hover:block group-focus-within:block z-20"
                      aria-hidden
                    >
                      <div className="rounded-md bg-white shadow-md py-2 px-3 text-sm border border-gray-200 whitespace-nowrap">
                        <div className="font-medium text-gray-800">
                          {profile?.displayName ?? 'No name'}
                        </div>
                        <div className="text-gray-500">
                          @{profile?.username ?? 'Unknown'}
                        </div>
                      </div>
                    </div>
                  </Link>
                </div>

                <button
                  onClick={handleLogout}
                  className="cursor-pointer flex items-center space-x-1 text-gray-500 hover:text-gray-700"
                >
                  <LogOutIcon className="h-5 w-5" />
                  <span className="">Đăng xuất</span>
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  search={{ redirectLink: '/dashboard' }}
                  className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                >
                  Đăng nhập
                </Link>
                <Link
                  to="/register"
                  className="bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700 transition-colors"
                >
                  Đăng ký
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}
