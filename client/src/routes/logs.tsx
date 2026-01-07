import { createFileRoute, redirect } from '@tanstack/react-router'
import { toast } from 'react-toastify'
import Logs from '@/pages/Logs'

export const Route = createFileRoute('/logs')({
  beforeLoad: async ({ context, location }) => {
    if (!context.auth.isAuthenticated) {
      try {
        await context.auth.refresh()
      } catch (error) {
        toast.info('You must be logged in before accessing this page.')
        throw redirect({
          to: '/login',
          search: { redirectLink: location.href },
        })
      }
    }
  },
  component: Logs,
})
