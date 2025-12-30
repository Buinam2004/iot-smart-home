import { revalidateLogic, useForm } from '@tanstack/react-form'
import { useMutation } from '@tanstack/react-query'
import { toast } from 'react-toastify'

import type { FC } from 'react'

import type { ChangePasswordPayload } from '@/types/payloads/profilePayload'
import type BaseResponse from '@/types/responses/BaseResponse'

import { api } from '@/auth/authApi'
import { generalFormErrorHandler } from '@/utils/errorHandling'

const ChangePasswordForm: FC = () => {
  const changePasswordMutation = useMutation({
    mutationKey: ['changePassword'],
    mutationFn: (
      payload: ChangePasswordPayload,
    ): Promise<BaseResponse<null>> => {
      return api.post('user/change-password', payload)
    },
  })

  const changePasswordForm = useForm({
    defaultValues: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    } as ChangePasswordPayload & { confirmPassword: string },
    validationLogic: revalidateLogic({
      mode: 'submit',
      modeAfterSubmission: 'change',
    }),
    validators: {
      onSubmitAsync: async ({ value, formApi }) => {
        const { oldPassword, newPassword } = value
        try {
          await changePasswordMutation.mutateAsync({ oldPassword, newPassword })
        } catch (err: any) {
          toast.error(err?.response?.data.message)
          return generalFormErrorHandler(
            err,
            formApi,
            'An error occurred while changing the password. Please try again.',
          )
        }
      },
    },
  })

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault()
        changePasswordForm.handleSubmit()
      }}
    >
      <h2 className="text-md font-semibold mb-4">Change password</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 max-w-xl">
        <changePasswordForm.Field
          name="oldPassword"
          validators={{
            onDynamic: ({ value }) =>
              !value ? 'Please enter your current password' : undefined,
          }}
        >
          {(field) => (
            <label className="flex flex-col md:col-span-2">
              <span className="text-sm font-medium mb-1">Current password</span>
              <input
                name={field.name}
                type="password"
                value={field.state.value}
                onChange={(e) => field.handleChange(e.target.value)}
                onBlur={field.handleBlur}
                className={`p-2 border rounded-md ${!field.state.meta.isValid && 'border-red-300'}`}
              />
            </label>
          )}
        </changePasswordForm.Field>

        <changePasswordForm.Field
          name="newPassword"
          validators={{
            onDynamic: ({ value }) =>
              !value ? 'Please enter your new password' : undefined,
          }}
        >
          {(field) => (
            <label className="flex flex-col">
              <span className="text-sm font-medium mb-1">New password</span>
              <input
                name={field.name}
                type="password"
                value={field.state.value}
                onChange={(e) => field.handleChange(e.target.value)}
                onBlur={field.handleBlur}
                className={`p-2 border rounded-md ${!field.state.meta.isValid && 'border-red-300'}`}
              />
              {!field.state.meta.isValid && (
                <p className="mt-2 text-sm text-red-600">
                  {field.state.meta.errors.join(', ')}
                </p>
              )}
            </label>
          )}
        </changePasswordForm.Field>

        <changePasswordForm.Field
          name="confirmPassword"
          validators={{
            onChangeListenTo: ['newPassword'],
            onChange: ({ value, fieldApi }) => {
              const pwd = fieldApi.form.getFieldValue('newPassword')
              if (value !== pwd && fieldApi.state.meta.isDirty) {
                return 'Mật khẩu không khớp'
              }
              return undefined
            },
          }}
        >
          {(field) => (
            <label className="flex flex-col">
              <span className="text-sm font-medium mb-1">
                Confirm new password
              </span>
              <input
                name={field.name}
                type="password"
                value={field.state.value}
                onChange={(e) => field.handleChange(e.target.value)}
                onBlur={field.handleBlur}
                className={`p-2 border rounded-md ${!field.state.meta.isValid && 'border-red-300'}`}
              />
              {!field.state.meta.isValid && (
                <p className="mt-2 text-sm text-red-600">
                  {field.state.meta.errors.join(', ')}
                </p>
              )}
            </label>
          )}
        </changePasswordForm.Field>
      </div>

      <changePasswordForm.Subscribe
        selector={(s) => [s.canSubmit, s.isSubmitting]}
      >
        {([canSubmit, isSubmitting]) => (
          <div className="mt-6 flex items-center gap-3">
            <button
              type="submit"
              disabled={isSubmitting || !canSubmit}
              className="px-4 py-2 bg-rose-600 text-white rounded-md hover:bg-rose-700 cursor-pointer"
            >
              {isSubmitting ? 'Changing password…' : 'Change password'}
            </button>
          </div>
        )}
      </changePasswordForm.Subscribe>
    </form>
  )
}

export default ChangePasswordForm
