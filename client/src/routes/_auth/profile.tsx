import { useState } from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useQuery } from '@tanstack/react-query'

import type { UserData, UserResponse } from '@/types/responses/profileResponses'

import { api } from '@/auth/authApi'
import ChangePasswordForm from '@/components/pages/profile/ChangePasswordForm'
import UpdateProfileForm from '@/components/pages/profile/UpdateProfileForm'

export const Route = createFileRoute('/_auth/profile')({
  component: RouteComponent,
})

function RouteComponent() {
  const [activeTab, setActiveTab] = useState<'update' | 'password'>('update')

  const {
    data: profile,
    isPending,
    error,
  } = useQuery<UserData>({
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

  return (
    <div className="bg-gray-50 p-6">
      <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Left column - Profile preview */}
        <aside className="md:col-span-1 bg-white rounded-lg shadow p-6 flex flex-col items-center">
          <div className="w-32 h-32 rounded-full overflow-hidden mb-4">
            <img
              src={isPending ? undefined : (profile.avatar ?? undefined)}
              alt="avatar"
              className="w-full h-full object-cover"
            />
          </div>

          <h2 className="text-xl font-semibold">{profile?.displayName}</h2>
          <p className="text-sm text-gray-500 mb-3">@{profile?.username}</p>

          <div className="w-full border-t pt-4">
            <div className="text-xs text-gray-400 uppercase mb-1">Email</div>
            <div className="text-sm text-gray-700 break-words">
              {profile?.email}
            </div>
          </div>
        </aside>

        {/* Right column - Tabs */}
        <section className="md:col-span-2 bg-white rounded-lg shadow p-6">
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-lg font-medium">Account settings</h1>
            <div className="text-sm text-gray-500">
              Manage your profile and password
            </div>
          </div>

          {/* Tabs header */}
          <div className="border-b">
            <nav className="-mb-px flex space-x-6" aria-label="Tabs">
              <button
                onClick={() => setActiveTab('update')}
                className={`py-3 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'update'
                    ? 'border-blue-600 text-blue-600'
                    : 'border-transparent text-gray-600 hover:text-gray-800 hover:border-gray-300 cursor-pointer'
                }`}
              >
                Update profile
              </button>

              <button
                onClick={() => setActiveTab('password')}
                className={`py-3 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'password'
                    ? 'border-blue-600 text-blue-600'
                    : 'border-transparent text-gray-600 hover:text-gray-800 hover:border-gray-300 cursor-pointer'
                }`}
              >
                Change password
              </button>
            </nav>
          </div>
          <div className="mt-6">
            {activeTab === 'update' && (
              <UpdateProfileForm
                initialPayload={{
                  displayName: profile?.displayName ?? '',
                  email: profile?.email ?? '',
                  file: null,
                }}
                avatar={profile?.avatar ?? null}
              />
            )}
            {activeTab === 'password' && <ChangePasswordForm />}
          </div>
        </section>
      </div>
    </div>
  )
}
