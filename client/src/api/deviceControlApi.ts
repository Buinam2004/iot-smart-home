import { toast } from 'react-toastify'
import { api } from '@/auth/authApi'

export interface DeviceControlPayload {
  deviceId: number
  state: number
}

export interface DoorControlPayload extends DeviceControlPayload {
  action: string
}

export interface FanStateResponse {
  deviceId: number
  state: number
  createdAt: string
}

export interface LedPirStateResponse {
  deviceId: number
  state: number
  createdAt: string
}

export interface DoorStateResponse {
  deviceId: number
  action: string
  receiveAt: string
}

export async function controlLed(payload: DeviceControlPayload) {
  try {
    await api.post('led_pir/publish', payload)
  } catch {
    toast.error('Toggle unsuccessfully')
  }
}
export async function controlFan(payload: DeviceControlPayload) {
  try {
    await api.post('fan/publish', payload)
  } catch {
    toast.error('Toggle unsuccessfully')
  }
}

export async function controlDoor(payload: DoorControlPayload) {
  try {
    await api.post('door/publish', payload)
  } catch {
    toast.error('Toggle unsuccessfully')
  }
}

export async function clearGas(deviceId: number) {
  try {
    await api.post('gas/publish', deviceId)
  } catch {
    toast.error('Toggle unsuccessfully')
  }
}

function isNotFoundError(err: unknown): boolean {
  return (
    typeof err === 'object' &&
    err !== null &&
    'response' in err &&
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (err as any).response?.status === 404
  )
}

export async function getFanState(deviceId: number) {
  try {
    const res = await api.get<FanStateResponse>('fan', { params: { deviceId } })
    return res.data
  } catch (err) {
    if (isNotFoundError(err)) return null
    throw err
  }
}

export async function getLedPirState(deviceId: number) {
  try {
    const res = await api.get<LedPirStateResponse>('led_pir', {
      params: { deviceId },
    })
    return res.data
  } catch (err) {
    if (isNotFoundError(err)) return null
    throw err
  }
}

export async function getDoorState(deviceId: number) {
  try {
    const res = await api.get<DoorStateResponse>('door', {
      params: { deviceId },
    })
    return res.data
  } catch (err) {
    if (isNotFoundError(err)) return null
    throw err
  }
}
