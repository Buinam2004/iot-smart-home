import { Outlet, createRootRouteWithContext } from '@tanstack/react-router'
import { TanStackRouterDevtoolsPanel } from '@tanstack/react-router-devtools'
import { TanStackDevtools } from '@tanstack/react-devtools'
import { ToastContainer } from 'react-toastify'
import { formDevtoolsPlugin } from '@tanstack/react-form-devtools'

import TanStackQueryDevtools from '../integrations/devtools'

import type {
  LoginResponse,
  LogoutResponse,
  RefreshResponse,
} from '@/types/responses/authResponses'
import type { QueryClient } from '@tanstack/react-query'
import type { LoginPayload } from '@/types/payloads/authPayloads'

import Footer from '@/components/layout/Footer'
import Header from '@/components/layout/Header'

interface MyRouterContext {
  queryClient: QueryClient
  auth: {
    isAuthenticated: boolean
    userId: string | null
    logout: () => Promise<LogoutResponse>
    login: (payload: LoginPayload) => Promise<LoginResponse>
    refresh: () => Promise<RefreshResponse>
  }
}

export const Route = createRootRouteWithContext<MyRouterContext>()({
  component: () => (
    <>
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <Header />
        <Outlet />
        <Footer />
      </div>
      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={true}
        closeOnClick
        pauseOnHover
        draggable
      />
      <TanStackDevtools
        config={{
          position: 'bottom-right',
        }}
        plugins={[
          {
            name: 'Tanstack Router',
            render: <TanStackRouterDevtoolsPanel />,
          },
          TanStackQueryDevtools,
          formDevtoolsPlugin(),
        ]}
      />
    </>
  ),
})
