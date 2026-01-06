import { useState } from 'react'
import {
  Link,
  createFileRoute,
  redirect,
  useNavigate,
} from '@tanstack/react-router'
import { useMutation } from '@tanstack/react-query'
import { BarChart3Icon, EyeIcon, EyeOffIcon } from 'lucide-react'
import { toast } from 'react-toastify'
import { revalidateLogic, useForm } from '@tanstack/react-form'

import type { RegisterPayload } from '@/types/payloads/authPayloads'
import { register } from '@/auth/authApi'
import { generalFormErrorHandler } from '@/utils/errorHandling'

export const Route = createFileRoute('/register')({
  beforeLoad: ({ context }) => {
    if (context.auth.isAuthenticated) throw redirect({ to: '/dashboard' })
  },
  component: RegisterPage,
})

function RegisterPage() {
  const navigate = useNavigate()
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  const mutation = useMutation({
    mutationFn: (payload: RegisterPayload) => register(payload),
  })

  // Include confirmPassword in defaultValues
  const form = useForm({
    defaultValues: {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
    } as RegisterPayload & { confirmPassword: string },
    validationLogic: revalidateLogic({
      mode: 'submit',
      modeAfterSubmission: 'change',
    }),
    validators: {
      onSubmitAsync: async ({ value, formApi }) => {
        // Strip confirmPassword
        const { username, email, password } = value
        try {
          await mutation.mutateAsync({ username, email, password })
          return undefined
        } catch (e: any) {
          console.error(e)
          return generalFormErrorHandler(
            e,
            formApi,
            'Đã xảy ra lỗi trong quá trình đăng ký. Vui lòng thử lại.',
          )
        }
      },
    },
    onSubmit: () => {
      navigate({ to: '/login', search: { redirectLink: '/dashboard' } })
      toast.success('Successfully registered!')
    },
  })

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center">
          <BarChart3Icon className="h-12 w-12 text-blue-600" />
        </div>
        <h2 className="mt-6 text-center text-3xl font-bold text-gray-900">
          Tạo tài khoản mới
        </h2>
        <p className="mt-2 text-center text-sm text-gray-600">
          Hoặc{' '}
          <Link
            to="/login"
            search={{ redirectLink: '/dashboard' }}
            className="font-medium text-blue-600 hover:text-blue-500"
          >
            đăng nhập vào tài khoản có sẵn
          </Link>
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          {form.state.errorMap.onSubmit?.form && (
            <div className="mb-4 text-red-600 text-center">
              {form.state.errorMap.onSubmit.form}
            </div>
          )}
          <form
            className="space-y-6"
            onSubmit={(e) => {
              e.preventDefault()
              form.handleSubmit()
            }}
          >
            {/* Username */}
            <form.Field
              name="username"
              validators={{
                onDynamic: ({ value }) =>
                  !value ? 'Tên đăng nhập là bắt buộc' : undefined,
              }}
            >
              {(field) => (
                <>
                  <label
                    htmlFor={field.name}
                    className="block text-sm font-medium text-gray-700"
                  >
                    Tên đăng nhập
                  </label>
                  <div className="mt-1">
                    <input
                      id={field.name}
                      name={field.name}
                      type="text"
                      value={field.state.value}
                      onChange={(e) => field.handleChange(e.target.value)}
                      onBlur={field.handleBlur}
                      className={`appearance-none block w-full px-3 py-2 border rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm ${
                        !field.state.meta.isValid
                          ? 'border-red-300'
                          : 'border-gray-300'
                      }`}
                      placeholder="Nhập tên đăng nhập"
                    />
                    {!field.state.meta.isValid && (
                      <p className="mt-2 text-sm text-red-600">
                        {field.state.meta.errors.join(', ')}
                      </p>
                    )}
                  </div>
                </>
              )}
            </form.Field>

            {/* Email */}
            <form.Field
              name="email"
              validators={{
                onDynamic: ({ value }) => {
                  if (!value) return 'Email là bắt buộc'
                  if (!/\S+@\S+\.\S+/.test(value)) return 'Email không hợp lệ'
                  return undefined
                },
              }}
            >
              {(field) => (
                <>
                  <label
                    htmlFor={field.name}
                    className="block text-sm font-medium text-gray-700"
                  >
                    Email
                  </label>
                  <div className="mt-1">
                    <input
                      id={field.name}
                      name={field.name}
                      type="email"
                      value={field.state.value}
                      onChange={(e) => field.handleChange(e.target.value)}
                      onBlur={field.handleBlur}
                      className={`appearance-none block w-full px-3 py-2 border rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm ${
                        !field.state.meta.isValid
                          ? 'border-red-300'
                          : 'border-gray-300'
                      }`}
                      placeholder="Nhập email của bạn"
                    />
                    {!field.state.meta.isValid && (
                      <p className="mt-2 text-sm text-red-600">
                        {field.state.meta.errors.join(', ')}
                      </p>
                    )}
                  </div>
                </>
              )}
            </form.Field>

            {/* Password */}
            <form.Field
              name="password"
              validators={{
                onDynamic: ({ value }) =>
                  !value ? 'Mật khẩu là bắt buộc' : undefined,
              }}
            >
              {(field) => (
                <>
                  <label
                    htmlFor={field.name}
                    className="block text-sm font-medium text-gray-700"
                  >
                    Mật khẩu
                  </label>
                  <div className="mt-1 relative">
                    <input
                      id={field.name}
                      name={field.name}
                      type={showPassword ? 'text' : 'password'}
                      value={field.state.value}
                      onChange={(e) => field.handleChange(e.target.value)}
                      onBlur={field.handleBlur}
                      className={`appearance-none block w-full px-3 py-2 border rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm pr-10 ${
                        !field.state.meta.isValid
                          ? 'border-red-300'
                          : 'border-gray-300'
                      }`}
                      placeholder="Nhập mật khẩu"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword((v) => !v)}
                      className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    >
                      {showPassword ? (
                        <EyeOffIcon className="h-5 w-5 text-gray-400" />
                      ) : (
                        <EyeIcon className="h-5 w-5 text-gray-400" />
                      )}
                    </button>
                    {!field.state.meta.isValid && (
                      <p className="mt-2 text-sm text-red-600">
                        {field.state.meta.errors.join(', ')}
                      </p>
                    )}
                  </div>
                </>
              )}
            </form.Field>

            {/* Confirm Password */}
            <form.Field
              name="confirmPassword"
              validators={{
                // re-validate confirmPassword when password changes
                onChangeListenTo: ['password'],
                onChange: ({ value, fieldApi }) => {
                  const pwd = fieldApi.form.getFieldValue('password')
                  if (value !== pwd && fieldApi.state.meta.isDirty) {
                    return 'Mật khẩu không khớp'
                  }
                  return undefined
                },
              }}
            >
              {(field) => (
                <>
                  <label
                    htmlFor={field.name}
                    className="block text-sm font-medium text-gray-700"
                  >
                    Xác nhận mật khẩu
                  </label>
                  <div className="mt-1 relative">
                    <input
                      id={field.name}
                      name={field.name}
                      type={showConfirmPassword ? 'text' : 'password'}
                      value={field.state.value}
                      onChange={(e) => field.handleChange(e.target.value)}
                      onBlur={field.handleBlur}
                      className={`appearance-none block w-full px-3 py-2 border rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm pr-10 ${
                        !field.state.meta.isValid
                          ? 'border-red-300'
                          : 'border-gray-300'
                      }`}
                      placeholder="Nhập lại mật khẩu"
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword((v) => !v)}
                      className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    >
                      {showConfirmPassword ? (
                        <EyeOffIcon className="h-5 w-5 text-gray-400" />
                      ) : (
                        <EyeIcon className="h-5 w-5 text-gray-400" />
                      )}
                    </button>
                    {!field.state.meta.isValid && (
                      <p className="mt-2 text-sm text-red-600">
                        {field.state.meta.errors.join(', ')}
                      </p>
                    )}
                  </div>
                </>
              )}
            </form.Field>

            {/* Submit button */}
            <form.Subscribe selector={(s) => [s.canSubmit, s.isSubmitting]}>
              {([canSubmit, isSubmitting]) => (
                <button
                  type="submit"
                  disabled={isSubmitting || !canSubmit}
                  className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-60"
                >
                  {isSubmitting ? 'Đang đăng ký…' : 'Đăng ký'}
                </button>
              )}
            </form.Subscribe>
          </form>
        </div>
      </div>
    </div>
  )
}

export default RegisterPage
