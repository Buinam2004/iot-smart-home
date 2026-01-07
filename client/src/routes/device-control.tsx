import { createFileRoute } from '@tanstack/react-router'
import DeviceControlPage from '@/pages/DeviceControlPage'

export const Route = createFileRoute('/device-control')({
  component: DeviceControlPage,
})
