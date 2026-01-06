export type UserUpdatePayload = {
  username: string
  email: string
}

export type ChangePasswordPayload = {
  oldPassword: string
  newPassword: string
}
