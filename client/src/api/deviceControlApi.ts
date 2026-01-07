import { toast } from 'react-toastify'
import { api } from '@/auth/authApi'

export interface DeviceControlPayload {
  deviceId: number
  state: number
}

export interface DoorControlPayload extends DeviceControlPayload {
  action: string
}

export async function controlLed(payload: DeviceControlPayload) {
  try {
    const res = await api.post('led_pir/publish', payload)
  } catch {
    toast.error('Toggle unsuccessfully')
  }
}
export async function controlFan(payload: DeviceControlPayload) {
  try {
    const res = await api.post('fan/publish', payload)
  } catch {
    toast.error('Toggle unsuccessfully')
  }
}

export async function controlDoor(payload: DoorControlPayload) {
  try {
    const res = await api.post('door/publish', payload)
  } catch {
    toast.error('Toggle unsuccessfully')
  }
}

export async function clearGas(deviceId: number) {
  try {
    const res = await api.post('gas/publish', deviceId)
  } catch {
    toast.error('Toggle unsuccessfully')
  }
}
