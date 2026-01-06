import { useState } from 'react'
import {
  Link,
  createFileRoute,
  redirect,
  useNavigate,
  useRouteContext,
} from '@tanstack/react-router'
import { useMutation } from '@tanstack/react-query'
import { BarChart3Icon, EyeIcon, EyeOffIcon } from 'lucide-react'
import { revalidateLogic, useForm } from '@tanstack/react-form'
import { toast } from 'react-toastify'

import type { LoginPayload } from '@/types/payloads/authPayloads'
import type { LoginResponse } from '@/types/responses/authResponses'
import { generalFormErrorHandler } from '@/utils/errorHandling'

export const Route = createFileRoute('/login')({
  validateSearch: (search) => ({
    redirectLink: (search.redirect as string) || '/dashboard',
  }),
  beforeLoad: ({ context, search }) => {
    if (context.auth.isAuthenticated)
      throw redirect({ to: search.redirectLink, replace: true })
  },
  component: Login,
})

function Login() {
  const login = useRouteContext({
    from: '__root__',
    select: (context) => context.auth.login,
  })
  const navigate = useNavigate()
  const { redirectLink } = Route.useSearch()
  const [showPassword, setShowPassword] = useState(false)

  const mutation = useMutation<LoginResponse, unknown, LoginPayload>({
    mutationFn: (payload) => login(payload),
    onError: () => {
      toast.error('Logging in Fail')
    },
  })

  const form = useForm({
    defaultValues: {
      username: '',
      password: '',
    } as LoginPayload,
    validationLogic: revalidateLogic({
      mode: 'submit',
      modeAfterSubmission: 'change',
    }),
    validators: {
      onSubmitAsync: async ({ value, formApi }) => {
        try {
          await mutation.mutateAsync(value)
          return undefined
        } catch (e: any) {
          toast.error(e?.response?.data.message)
          return generalFormErrorHandler(
            e,
            formApi,
            'Lỗi đăng nhập. Vui lòng thử lại.',
          )
        }
      },
    },
    onSubmit: () => {
      // Called after onSubmitAsync is done and does not return truthy value
      navigate({ to: redirectLink })
    },
  })

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center">
          <BarChart3Icon className="h-12 w-12 text-blue-600" />
        </div>
        <h2 className="mt-6 text-center text-3xl font-bold text-gray-900">
          Đăng nhập vào tài khoản
        </h2>
        <p className="mt-2 text-center text-sm text-gray-600">
          Hoặc{' '}
          <Link
            to="/register"
            className="font-medium text-blue-600 hover:text-blue-500"
          >
            tạo tài khoản mới
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
            <form.Field
              name="username"
              validators={{
                onDynamic: ({ value }) => {
                  return !value ? 'Tên đăng nhập là bắt buộc' : undefined
                },
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
                      value={field.state.value}
                      type="text"
                      autoComplete="username"
                      onChange={(e) => field.handleChange(e.target.value)}
                      className={`appearance-none block w-full px-3 py-2 border rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm ${
                        !field.state.meta.isValid
                          ? 'border-red-300'
                          : 'border-gray-300'
                      }`}
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
                onDynamic: ({ value }) => {
                  return !value ? 'Mật khẩu là bắt buộc' : undefined
                },
              }}
            >
              {(field) => (
                <>
                  <label
                    htmlFor="password"
                    className="block text-sm font-medium text-gray-700"
                  >
                    Mật khẩu
                  </label>
                  <div className="mt-1 relative">
                    <input
                      id={field.name}
                      name={field.name}
                      type={showPassword ? 'text' : 'password'}
                      autoComplete="current-password"
                      value={field.state.value}
                      onChange={(e) => field.handleChange(e.target.value)}
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
                      className="absolute inset-y-0 right-0 pr-3 flex items-center cursor-pointer"
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

            {/* Submit */}
            <form.Subscribe
              selector={(state) => [state.canSubmit, state.isSubmitting]}
              children={([canSubmit, isSubmitting]) => (
                <button
                  type="submit"
                  disabled={isSubmitting || !canSubmit}
                  className="cursor-pointer w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-60"
                >
                  {isSubmitting ? 'Đang đăng nhập...' : 'Đăng nhập'}
                </button>
              )}
            />
          </form>
        </div>
      </div>
    </div>
  )
}
