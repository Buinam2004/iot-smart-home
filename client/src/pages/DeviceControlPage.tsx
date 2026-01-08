import { api } from '@/auth/authApi'
import DeviceControl from '@/components/DeviceControl'
import type { DeviceData } from '@/types/responses/sensorResponses'
import { useQuery } from '@tanstack/react-query'
import { getDoorState, getFanState, getLedPirState } from '@/api/deviceControlApi'

export default function DeviceControlPage() {
  const {
    data: devices,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['devices'],
    queryFn: async () => {
      const res = await api.get<Array<DeviceData>>('devices/user')
      return res.data
    },
  })

  const deviceIds = devices?.map((e) => e.id)
  const firstDeviceId = deviceIds?.[0]

  const { data: fanState, isLoading: isFanLoading } = useQuery({
    queryKey: ['fan-state', firstDeviceId],
    enabled: typeof firstDeviceId === 'number',
    queryFn: async () => getFanState(firstDeviceId as number),
  })

  const { data: ledPirState, isLoading: isLedPirLoading } = useQuery({
    queryKey: ['led-pir-state', firstDeviceId],
    enabled: typeof firstDeviceId === 'number',
    queryFn: async () => getLedPirState(firstDeviceId as number),
  })

  const { data: doorState, isLoading: isDoorLoading } = useQuery({
    queryKey: ['door-state', firstDeviceId],
    enabled: typeof firstDeviceId === 'number',
    queryFn: async () => getDoorState(firstDeviceId as number),
  })

  if (isLoading) return <div />
  if (error) return <div className="p-4">Lỗi khi tải danh sách thiết bị.</div>
  if (!deviceIds || deviceIds.length === 0)
    return <div className="p-4">Không có thiết bị để điều khiển.</div>

  if (isFanLoading || isLedPirLoading || isDoorLoading) return <div />

  const doorOpen =
    (doorState?.action ?? '').toLowerCase() === 'open' ||
    (doorState?.action ?? '').toLowerCase() === 'opened'

  return (
    <main className="mx-auto w-full max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900">Điều khiển thiết bị</h1>
      <p className="text-sm text-gray-600 mt-1">
        Giao diện điều khiển thủ công cho các thiết bị trong nhà.
      </p>

      <div className="mt-6">
        <DeviceControl
          initial={{
            light: (ledPirState?.state ?? 0) === 1,
            fan: (fanState?.state ?? 0) === 1,
            doorOpen,
            gas: true,
          }}
          deviceIds={deviceIds}
          onChange={(s) => console.log('Device state:', s)}
        />
      </div>
    </main>
  )
}
