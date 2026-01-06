import { createFileRoute } from '@tanstack/react-router'
import { useQuery } from '@tanstack/react-query'

import type { UserResponse } from '@/types/responses/profileResponses'

import { api } from '@/auth/authApi'
import UpdateProfileForm from '@/components/pages/profile/UpdateProfileForm'

export const Route = createFileRoute('/_auth/profile')({
  component: RouteComponent,
})

function RouteComponent() {
  const {
    data: profile,
    isPending,
    error,
  } = useQuery<UserResponse>({
    queryKey: ['profile'],
    queryFn: async () => {
      const res = await api.get<UserResponse>('users')
      return res.data
    },
  })

  if (isPending) {
    ;<div className="p-6">
      <p className="text-black">Loading...</p>
    </div>
  }

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
          <h2 className="text-xl font-semibold">{profile?.username}</h2>

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
            <div className="text-sm text-gray-500">Manage your profile</div>
          </div>

          {/* Tabs header */}
          <div className="border-b"></div>
          <div className="mt-6">
            <UpdateProfileForm
              initialPayload={{
                username: profile?.username ?? '',
                email: profile?.email ?? '',
              }}
            />
          </div>
        </section>
      </div>
    </div>
  )
}
