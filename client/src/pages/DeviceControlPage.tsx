import DeviceControl from '@/components/DeviceControl'

export default function DeviceControlPage() {
  return (
    <main className="mx-auto w-full max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900">Điều khiển thiết bị</h1>
      <p className="text-sm text-gray-600 mt-1">Giao diện điều khiển thủ công cho các thiết bị trong nhà.</p>

      <div className="mt-6">
        <DeviceControl
          initial={{ light: false, fan: false, doorOpen: false, auto: false }}
          onChange={(s) => console.log('Device state:', s)}
        />
      </div>
    </main>
  )
}
