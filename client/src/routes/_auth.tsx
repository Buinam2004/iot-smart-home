import { Outlet, createFileRoute, redirect } from '@tanstack/react-router'
import { toast } from 'react-toastify'

export const Route = createFileRoute('/_auth')({
  beforeLoad: async ({ context, location }) => {
    if (!context.auth.isAuthenticated) {
      try {
        await context.auth.refresh()
      } catch (error) {
        toast.info('You must be logged in before accessing this page.')
        throw redirect({
          to: '/login',
          search: {
            // Save current location for redirect after login
            redirectLink: location.href,
          },
        })
      }
    }
  },
  component: () => <Outlet />,
})
