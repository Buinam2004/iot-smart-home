import { Store } from '@tanstack/react-store'

type AuthState = {
  accessToken: string | null
  isAuthenticated: boolean
  userId: string | null
}

export const authStore = new Store<AuthState>({
  accessToken: null,
  isAuthenticated: false,
  userId: null,
})

const saved = window.localStorage.getItem('authStore')
if (saved) {
  try {
    const state = JSON.parse(saved)
    authStore.setState(state)
  } catch (e) {
    console.error('Invalid store state in localStorage', e)
  }
}

// Subscribe to store changes and save to localStorage
authStore.subscribe((authStoreObject) => {
  window.localStorage.setItem(
    'authStore',
    JSON.stringify(authStoreObject.currentVal),
  )
})

export function setAuth(token: string, userId: string) {
  authStore.setState({ accessToken: token, isAuthenticated: true, userId })
}

export function clearAuth() {
  authStore.setState({
    accessToken: null,
    isAuthenticated: false,
    userId: null,
  })
}
