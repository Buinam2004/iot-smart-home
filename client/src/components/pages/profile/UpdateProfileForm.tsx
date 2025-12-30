import { useEffect, useRef, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from '@tanstack/react-form'
import { toast } from 'react-toastify'

import type { FC } from 'react'

import type { UserResponse } from '@/types/responses/profileResponses'
import type { UserUpdatePayload } from '@/types/payloads/profilePayload'

import { api } from '@/auth/authApi'
import { generalFormErrorHandler } from '@/utils/errorHandling'

interface UserUpdateFormProps {
  initialPayload: UserUpdatePayload
  avatar: string | null
}

const UpdateProfileForm: FC<UserUpdateFormProps> = ({
  initialPayload,
  avatar,
}) => {
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null)
  const hiddenFileInputRef = useRef<HTMLInputElement | null>(null)
  const queryClient = useQueryClient()

  const updateProfileMutation = useMutation({
    mutationKey: ['updateProfile'],
    mutationFn: async (payload: UserUpdatePayload): Promise<UserResponse> => {
      const formData = new FormData()
      formData.append(
        'payload',
        new Blob(
          [
            JSON.stringify({
              displayName: payload.displayName,
              email: payload.email,
            }),
          ],
          { type: 'application/json' },
        ),
      )
      if (payload.file) formData.append('file', payload.file)

      const res = await api.put<UserResponse>('user', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      return res.data
    },
  })

  const updateProfileForm = useForm({
    defaultValues: {
      displayName: initialPayload.displayName,
      email: initialPayload.email,
      file: initialPayload.file,
    } as {
      displayName: string
      email: string
      file: File | null
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

  useEffect(() => {
    setAvatarPreview(avatar)
  }, [avatar])

  function handleFileChange(file: File | null) {
    if (!file) return
    const url = URL.createObjectURL(file)
    setAvatarPreview(url)
  }

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault()
        updateProfileForm.handleSubmit()
      }}
    >
      <h2 className="text-md font-semibold mb-4">Update profile</h2>
      <div className="flex flex-col items-center mb-6">
        <div className="w-28 h-28 rounded-full overflow-hidden mb-3 border-2">
          <img
            src={avatarPreview ?? avatar ?? undefined}
            alt="avatar center"
            className="w-full h-full object-cover"
          />
        </div>
        <p className="text-sm text-gray-500">Avatar Preview</p>
      </div>
      <div className="py-2 border-t gap-4 max-w-2xl">
        <updateProfileForm.Field name="displayName">
          {(field) => (
            <>
              <label className="flex flex-col mb-2">
                <span className="text-sm font-medium mb-1">Name</span>
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

      <div className="w-full max-w-2xl">
        <button
          type="button"
          onClick={() => hiddenFileInputRef.current?.click()}
          className="w-full px-4 py-2 bg-gray-300 text-black rounded-md hover:bg-gray-200 cursor-pointer"
        >
          Edit avatar - PNG or JPG
        </button>
        {/* Hidden file input */}
        <updateProfileForm.Field name="file">
          {(field) => (
            <input
              name={field.name}
              ref={hiddenFileInputRef}
              type="file"
              accept="image/*"
              onChange={(e) => {
                const file = e.target.files ? e.target.files[0] : null
                updateProfileForm.setFieldValue('file', file)
                handleFileChange(file)
              }}
              className="hidden"
            />
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
