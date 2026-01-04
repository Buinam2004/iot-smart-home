export default interface BaseResponse<T = unknown> {
  message: string
  code: string
  data: T
}

export interface ErrorResponse<T> {
  message: string
  code: string
  data: T
}
