import axios from 'axios'
import { toast } from 'react-toastify'

import { authStore, clearAuth, setAuth } from './authStore'
import type {
  LoginResponse,
  RefreshResponse,
  RegisterResponse,
} from '@/types/responses/authResponses'
import type {
  LoginPayload,
  RegisterPayload,
} from '@/types/payloads/authPayloads'

const BASE_API_URL: string = import.meta.env.VITE_API_URL

export const api = axios.create({
  baseURL: BASE_API_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

export const publicApi = axios.create({
  baseURL: BASE_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export async function refreshAccessToken(): Promise<RefreshResponse> {
  try {
    const response = await publicApi.post<RefreshResponse>(
      'auth/refresh-token',
      {},
      { withCredentials: true },
    )

    const accessToken = response.data.accessToken
    setAuth(accessToken, response.data.userId)

    return response.data
  } catch (error: any) {
    const originalRequest = error.config

    if (!originalRequest._retry) {
      clearAuth()
      window.location.href = '/'
      toast.error('Something goes wrong! Please try to login!')
    }
    return Promise.reject(error)
  }
}

api.interceptors.request.use((request) => {
  const token = authStore.state.accessToken
  if (token) request.headers.Authorization = `Bearer ${token}`
  return request
})

api.interceptors.response.use(
  (response) => {
    return response
  },
  async (error): Promise<any> => {
    const originalRequest = error.config
    if (!originalRequest._retry) {
      originalRequest._retry = true
      await refreshAccessToken()
      return api(originalRequest)
    }
    clearAuth()
    window.location.href = '/'
    return Promise.reject(error)
  },
)

export async function register(
  payload: RegisterPayload,
): Promise<RegisterResponse> {
  const response = await publicApi.post<RegisterResponse>('users', payload)
  return response.data
}

export async function login(payload: LoginPayload): Promise<LoginResponse> {
  const response = await publicApi.post<LoginResponse>('auth/login', payload, {
    withCredentials: true,
  })
  const accessToken = response.data.accessToken
  setAuth(accessToken, response.data.userId)

  return response.data
}

export async function logout(): Promise<string> {
  try {
    const response = await api.post<string>(
      'auth/logout',
      {},
      { withCredentials: true },
    )
    clearAuth()
    return response.data
  } catch (error: any) {
    const originalRequest = error.config
    const errMessage = error.response.data.message as string
    if (!errMessage) return Promise.reject(error)
    if (!originalRequest._retry) {
      clearAuth()
      window.location.href = '/'
    }
    return Promise.reject(error)
  }
}
