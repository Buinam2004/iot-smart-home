export interface PageResponse<T> {
  content: T
}

export enum GasStatus {
  ALERT = 'alert',
  CLEAR = 'clear',
}

export interface BaseSensorData {
  id: number
  deviceId: number
  type: string
  sensor: string
  receivedAt: Date
}

export interface User {
  id: null
  username: string
  email: string
  createdAt: Date
  role: string
}

export interface Device {
  id: number
  userId: number
  name: string
  macAddress: string
  isOnline: boolean
  createdAt: Date
  user: User
}

export interface DhtData extends BaseSensorData {
  temperature: number
  humidity: number
  device: Device | null
}

export interface PirData extends BaseSensorData {
  motion: number // 1 : motion detected, 0 : no motion
  light: number
  device: Device | null
}

export interface GasData extends BaseSensorData {
  event: GasStatus
  value: number
  state: number // 0 if clear or 1 if alert
}

export interface FanData {
  id: number
  deviceId: number
  createdAt: number
  state: number
  device: Device | null
}

export interface LedPirData {
  id: number
  deviceId: number
  createdAt: Date
  device: Device | null
}

export interface DeviceData {
  id: number
  userId: number
  name: string
  macAddress: string
  isOnline: boolean
  createdAt: Date | null
  creatorName: string | null
}
