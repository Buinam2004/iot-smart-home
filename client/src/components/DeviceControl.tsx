import { useEffect, useState } from 'react'

export interface DeviceState {
  light: boolean
  fan: boolean
  doorOpen: boolean
}

export interface DeviceControlProps {
  initial?: Partial<DeviceState>
  onChange?: (s: DeviceState) => void
}

export default function DeviceControl({ initial, onChange }: DeviceControlProps) {
  const [state, setState] = useState<DeviceState>(() => ({
    light: initial?.light ?? false,
    fan: initial?.fan ?? false,
    doorOpen: initial?.doorOpen ?? false,
  }))

  useEffect(() => {
    onChange?.(state)
  }, [state, onChange])

  function toggle<K extends keyof DeviceState>(key: K) {
    setState((s) => ({ ...s, [key]: !s[key] }))
  }

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-gray-900">Điều khiển thiết bị</h3>
      </div>

      <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {/* Light */}
        <div className="flex flex-col gap-2">
          <div className="text-xs font-medium text-gray-600">Đèn</div>
          <button
            onClick={() => toggle('light')}
            aria-pressed={state.light}
            className={`rounded-md px-3 py-2 text-sm font-semibold transition-colors ${
              state.light
                ? 'bg-yellow-100 text-yellow-800 border border-yellow-200'
                : 'bg-gray-50 text-gray-700 border border-gray-200'
            }`}
          >
            {state.light ? 'ON' : 'OFF'}
          </button>
        </div>

        {/* Fan */}
        <div className="flex flex-col gap-2">
          <div className="text-xs font-medium text-gray-600">Quạt</div>
          <button
            onClick={() => toggle('fan')}
            aria-pressed={state.fan}
            className={`rounded-md px-3 py-2 text-sm font-semibold transition-colors ${
              state.fan
                ? 'bg-emerald-100 text-emerald-800 border border-emerald-200'
                : 'bg-gray-50 text-gray-700 border border-gray-200'
            }`}
          >
            {state.fan ? 'ON' : 'OFF'}
          </button>
        </div>

        {/* Door */}
        <div className="flex flex-col gap-2">
          <div className="text-xs font-medium text-gray-600">Cửa</div>
          <button
            onClick={() => toggle('doorOpen')}
            aria-pressed={state.doorOpen}
            className={`rounded-md px-3 py-2 text-sm font-semibold transition-colors ${
              state.doorOpen
                ? 'bg-sky-100 text-sky-800 border border-sky-200'
                : 'bg-gray-50 text-gray-700 border border-gray-200'
            }`}
          >
            {state.doorOpen ? 'OPEN' : 'CLOSE'}
          </button>
        </div>
      </div>
    </div>
  )
}
