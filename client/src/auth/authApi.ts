import axios from 'axios'
import { decode } from 'jsonwebtoken'
import { toast } from 'react-toastify'

import { authStore, clearAuth, setAuth } from './authStore'
import type {
  LoginResponse,
  LogoutResponse,
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
      'public/refresh',
      {},
      { withCredentials: true },
    )

    const accessToken = response.data.data.accessToken

    const decoded = decode(accessToken, { complete: true }) as {
      payload?: { sub?: string | (() => string) }
    } | null
    let userId = ''
    if (decoded?.payload?.sub) {
      const sub = decoded.payload.sub
      userId = typeof sub === 'function' ? sub() : sub
    }
    setAuth(accessToken, userId)

    return response.data
  } catch (error: any) {
    const originalRequest = error.config
    const errMessage = error.response.data.message as string
    if (!errMessage) return Promise.reject(error)
    if (
      (errMessage.toLocaleLowerCase().includes('missing cookie rt_refresh') ||
        errMessage.toLocaleLowerCase().includes('invalid refresh token')) &&
      !originalRequest._retry
    ) {
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
    const errMessage = error.response.data.message as string
    if (!errMessage) return Promise.reject(error)
    if (
      errMessage.toLocaleLowerCase().includes('invalid access token') &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true
      await refreshAccessToken()
      return api(originalRequest)
    }
    return Promise.reject(error)
  },
)

export async function register(
  payload: RegisterPayload,
): Promise<RegisterResponse> {
  const response = await publicApi.post<RegisterResponse>(
    'public/register',
    payload,
  )
  return response.data
}

export async function login(payload: LoginPayload): Promise<LoginResponse> {
  const response = await publicApi.post<LoginResponse>(
    'public/login',
    payload,
    { withCredentials: true },
  )
  const accessToken = response.data.data.accessToken

  const decoded = decode(accessToken, { complete: true }) as {
    payload?: { sub?: string | (() => string) }
  } | null
  let userId = ''
  if (decoded?.payload?.sub) {
    const sub = decoded.payload.sub
    userId = typeof sub === 'function' ? sub() : sub
  }
  setAuth(accessToken, userId)

  return response.data
}

export async function logout(): Promise<LogoutResponse> {
  try {
    const response = await api.post<LogoutResponse>(
      'user/logout',
      {},
      { withCredentials: true },
    )
    clearAuth()
    return response.data
  } catch (error: any) {
    const originalRequest = error.config
    const errMessage = error.response.data.message as string
    if (!errMessage) return Promise.reject(error)
    if (
      errMessage.toLocaleLowerCase().includes('missing cookie rt_logout') &&
      !originalRequest._retry
    ) {
      clearAuth()
      window.location.href = '/'
    }
    return Promise.reject(error)
  }
}
