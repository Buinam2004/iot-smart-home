import { api } from '@/auth/authApi'
import DeviceControl from '@/components/DeviceControl'
import type { DeviceData } from '@/types/responses/sensorResponses'
import { useQuery } from '@tanstack/react-query'

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

  if (isLoading) return <div />
  if (error) return <div className="p-4">Lỗi khi tải danh sách thiết bị.</div>

  return (
    <main className="mx-auto w-full max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900">Điều khiển thiết bị</h1>
      <p className="text-sm text-gray-600 mt-1">
        Giao diện điều khiển thủ công cho các thiết bị trong nhà.
      </p>

      <div className="mt-6">
        <DeviceControl
          initial={{ light: false, fan: false, doorOpen: false, gas: true }}
          deviceIds={deviceIds}
          onChange={(s) => console.log('Device state:', s)}
        />
      </div>
    </main>
  )
}
