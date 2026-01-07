export type DeviceControlPayload = {
  deviceId: number
  state: number
}

import { authStore } from '../auth/authStore'

const BASE_API_URL: string = import.meta.env.VITE_API_URL || '/api/'
const BASE = BASE_API_URL.endsWith('/') ? BASE_API_URL : `${BASE_API_URL}/`

const inFlight = new Map<string, Promise<any>>()

async function postJson<T = any>(url: string, body: unknown): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }

  // Prefer in-memory authStore token, fallback to localStorage
  const token = authStore?.state?.accessToken ?? (() => {
    try {
      const saved = window.localStorage.getItem('authStore')
      if (!saved) return null
      const parsed = JSON.parse(saved)
      return parsed?.accessToken ?? null
    } catch (e) {
      return null
    }
  })()

  if (token) headers.Authorization = `Bearer ${token}`

  const key = `${url}|${JSON.stringify(body)}`
  if (inFlight.has(key)) {
    // Return the existing promise to avoid duplicate network calls
    return inFlight.get(key) as Promise<T>
  }

  const p = (async (): Promise<T> => {
    const res = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(body),
    })

    if (!res.ok) {
      const text = await res.text().catch(() => '')
      throw new Error(`POST ${url} failed: ${res.status} ${res.statusText} ${text}`)
    }

    const ct = res.headers.get('content-type') || ''
    if (ct.includes('application/json')) return res.json()
    return undefined as unknown as T
  })()

  inFlight.set(key, p)
  // cleanup when done
  p.finally(() => inFlight.delete(key))
  return p
}

/**
 * Control LED (PIR) device
 * POST /api/led_pir
 * body: { deviceId: number, state: number }
 */
export function controlLed(payload: DeviceControlPayload) {
  return postJson(`${BASE}led_pir`, payload)
}

/**
 * Control fan device
 * POST /api/fan/publish
 * body: { deviceId: number, state: number }
 */
export function controlFan(payload: DeviceControlPayload) {
  return postJson(`${BASE}fan/publish`, payload)
}

export default {
  controlLed,
  controlFan,
}
