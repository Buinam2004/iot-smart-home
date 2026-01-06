export interface RefreshResponse {
  userId: number
  username: string
  role: string
  accessToken: string
  expiresIn: number
}

export interface RegisterResponse {
  userId: number
  username: string
  email: string
  role: string
}

export interface LoginResponse {
  userId: number
  username: string
  role: string
  accessToken: string
  expiresIn: number
}
