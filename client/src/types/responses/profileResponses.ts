import type BaseResponse from './BaseResponse'

export interface UserData {
  username: string
  displayName: string
  email: string
  avatar: string | null
}

export type UserResponse = BaseResponse<UserData>
