import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from '@tanstack/react-form'
import { toast } from 'react-toastify'
import { useNavigate, useRouteContext } from '@tanstack/react-router'

import type { FC } from 'react'

import type { UserResponse } from '@/types/responses/profileResponses'
import type { UserUpdatePayload } from '@/types/payloads/profilePayload'

import { api } from '@/auth/authApi'
import { generalFormErrorHandler } from '@/utils/errorHandling'

interface UserUpdateFormProps {
  initialPayload: UserUpdatePayload
}

const UpdateProfileForm: FC<UserUpdateFormProps> = ({ initialPayload }) => {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const { userId, logout } = useRouteContext({
    from: '__root__',
    select: (ctx) => ({
      userId: ctx.auth.userId,
      logout: ctx.auth.logout,
    }),
  })

  const updateProfileMutation = useMutation({
    mutationKey: ['updateProfile'],
    mutationFn: async (payload: UserUpdatePayload): Promise<UserResponse> => {
      const res = await api.patch<UserResponse>(`users/${userId}`, payload)
      return res.data
    },
    onSuccess: async () => {
      await logout()
      navigate({ to: '/login', search: { redirectLink: '/' } })
    },
  })

  const updateProfileForm = useForm({
    defaultValues: {
      username: initialPayload.username,
      email: initialPayload.email,
    },
    validators: {
      onSubmitAsync: async ({ value, formApi }) => {
        try {
          await updateProfileMutation.mutateAsync(value)
        } catch (err: any) {
          toast.error(err?.response?.data.message)
          return generalFormErrorHandler(
            err,
            formApi,
            'An error occurred while updating the profile. Please try again.',
          )
        }
      },
    },
    onSubmit: () => {
      queryClient.invalidateQueries({
        queryKey: ['profile'],
      })
      toast.success('Update profile successfully')
    },
  })
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault()
        updateProfileForm.handleSubmit()
      }}
    >
      <div className="py-2 gap-4 max-w-2xl">
        <updateProfileForm.Field name="username">
          {(field) => (
            <>
              <label className="flex flex-col mb-2">
                <span className="text-sm font-medium mb-1">Username</span>
                <input
                  name={field.name}
                  type="text"
                  value={field.state.value}
                  onBlur={field.handleBlur}
                  onChange={(e) => field.handleChange(e.target.value)}
                  className="p-2 border rounded-md"
                />
              </label>
            </>
          )}
        </updateProfileForm.Field>
        <updateProfileForm.Field name="email">
          {(field) => (
            <label className="flex flex-col mb-2">
              <span className="text-sm font-medium mb-1">Email</span>
              <input
                name={field.name}
                type="email"
                value={field.state.value}
                onBlur={field.handleBlur}
                onChange={(e) => field.handleChange(e.target.value)}
                className="p-2 border rounded-md"
              />
            </label>
          )}
        </updateProfileForm.Field>
      </div>
      <div className="w-full max-w-2xl mt-4">
        <updateProfileForm.Subscribe
          selector={(s) => [s.canSubmit, s.isSubmitting]}
        >
          {([canSubmit, isSubmitting]) => (
            <button
              type="submit"
              disabled={isSubmitting || !canSubmit}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 cursor-pointer"
            >
              {isSubmitting ? 'Saving changes...' : 'Save changes'}
            </button>
          )}
        </updateProfileForm.Subscribe>
      </div>
    </form>
  )
}

export default UpdateProfileForm
