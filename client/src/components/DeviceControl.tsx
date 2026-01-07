import { useEffect, useState } from 'react'
import {
  clearGas,
  controlDoor,
  controlFan,
  controlLed,
} from '../api/deviceControlApi'

export interface DeviceState {
  light: boolean
  fan: boolean
  doorOpen: boolean
  gas: boolean
}

export interface DeviceControlProps {
  initial?: Partial<DeviceState>
  onChange?: (s: DeviceState) => void
  deviceIds: Array<number>
}

export default function DeviceControl({
  initial,
  onChange,
  deviceIds,
}: DeviceControlProps) {
  const [state, setState] = useState<DeviceState>(() => ({
    light: initial?.light ?? false,
    fan: initial?.fan ?? false,
    doorOpen: initial?.doorOpen ?? false,
    gas: initial?.gas ?? false,
  }))
  const [pending, setPending] = useState<
    Partial<Record<keyof DeviceState, boolean>>
  >({})

  useEffect(() => {
    onChange?.(state)
  }, [state, onChange])

  function toggle<K extends keyof DeviceState>(key: K) {
    // Prevent duplicate requests for the same control
    if (pending[key]) return

    setState((prev) => {
      const next = { ...prev, [key]: !prev[key] } as DeviceState

      setPending((p) => ({ ...p, [key]: true }))
      ;(async () => {
        try {
          for (const deviceId of deviceIds) {
            if (key === 'light') {
              await controlLed({
                deviceId: deviceId,
                state: next.light ? 1 : 0,
              })
            } else if (key === 'fan') {
              await controlFan({ deviceId: deviceId, state: next.fan ? 1 : 0 })
            } else if (key === 'doorOpen') {
              await controlDoor({
                deviceId: deviceId,
                state: next.doorOpen ? 1 : 0,
                action: next.doorOpen ? 'open' : 'deny',
              })
            } else if (key === 'gas') {
              await clearGas({})
            }
          }
        } catch (err) {
          console.error('Device control failed', err)
          // revert optimistic update
          setState((s) => ({ ...s, [key]: !next[key] }))
        } finally {
          setPending((p) => ({ ...p, [key]: false }))
        }
      })()

      return next
    })
  }

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-gray-900">
          Điều khiển thiết bị
        </h3>
      </div>

      <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-2">
        {/* Light */}
        <div className="flex flex-col gap-2">
          <div className="text-xs font-medium text-gray-600">Đèn</div>
          <button
            onClick={() => toggle('light')}
            aria-pressed={state.light}
            className={`rounded-md px-3 py-2 text-sm font-semibold transition-colors cursor-pointer ${
              state.light
                ? 'bg-yellow-100 text-yellow-800 border border-yellow-200 hover:bg-yellow-200'
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
            className={`rounded-md px-3 py-2 text-sm font-semibold transition-colors cursor-pointer ${
              state.fan
                ? 'bg-emerald-100 text-emerald-800 border border-emerald-200 hover:bg-emerald-200'
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
            className={`rounded-md px-3 py-2 text-sm font-semibold transition-colors cursor-pointer ${
              state.doorOpen
                ? 'bg-sky-100 text-sky-800 border border-sky-200 hover:bg-sky-200'
                : 'bg-gray-50 text-gray-700 border border-gray-200'
            }`}
          >
            {state.doorOpen ? 'OPEN' : 'CLOSE'}
          </button>
        </div>
        <div className="flex flex-col gap-2">
          <div className="text-xs font-medium text-gray-600">Clear Khí gas</div>
          <button
            onClick={() => {
              for (const deviceId of deviceIds) clearGas(deviceId)
            }}
            aria-pressed={state.gas}
            className={`rounded-md px-3 py-2 text-sm font-semibold transition-colors ${'bg-sky-100 hover:bg-sky-200 text-sky-800 border border-sky-200'} cursor-pointer`}
          >
            CLEAR GAS
          </button>
        </div>
      </div>
    </div>
  )
}
