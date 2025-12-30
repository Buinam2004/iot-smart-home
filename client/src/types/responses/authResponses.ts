import type BaseResponse from './BaseResponse'

interface AccessTokenData {
  accessToken: string
}

export type RegisterResponse = BaseResponse<null>

export type LoginResponse = BaseResponse<AccessTokenData>

export type RefreshResponse = BaseResponse<AccessTokenData>

export type LogoutResponse = BaseResponse<null>
