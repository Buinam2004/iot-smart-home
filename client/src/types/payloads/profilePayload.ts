export type UserUpdatePayload = {
  displayName: string
  email: string
  file: File | null
}

export type ChangePasswordPayload = {
  oldPassword: string
  newPassword: string
}
