import type { FormApi } from '@tanstack/react-form'

export function generalFormErrorHandler(
  e: any,
  formApi: FormApi<any, any, any, any, any, any, any, any, any, any, any, any>,
  fallbackMessage: string,
): any {
  const err = e?.response?.data
  if (err) {
    const { message, data } = err
    const errorMap: any = { form: message }
    if (data && typeof data === 'object') {
      errorMap.fields = {}
      for (const key in data) {
        errorMap.fields[key] = data[key]
      }
    }
    formApi.setErrorMap({ onSubmit: errorMap })
    return errorMap
  }
  const fallback = { form: fallbackMessage }
  formApi.setErrorMap({ onSubmit: fallback })
  return fallback
}
