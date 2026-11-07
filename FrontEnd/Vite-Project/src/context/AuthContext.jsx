import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { getSession, login, logout } from '../services/authService'
import { setUnauthorizedHandler } from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [ready, setReady] = useState(false)
  const [initializationError, setInitializationError] = useState('')

  const signOut = useCallback(() => {
    setUser(null)
  }, [])

  const restoreSession = useCallback(async () => {
    setReady(false)
    setInitializationError('')
    try {
      setUser(await getSession())
    } catch (error) {
      if (error.response?.status === 401) {
        setUser(null)
      } else {
        setInitializationError('Unable to verify your session. Check your connection and retry.')
      }
    } finally {
      setReady(true)
    }
  }, [])

  useEffect(() => {
    let active = true
    setUnauthorizedHandler(signOut)
    Promise.resolve().then(() => active && restoreSession())
    return () => {
      active = false
      setUnauthorizedHandler(() => {})
    }
  }, [signOut, restoreSession])

  const signIn = useCallback(async (credentials) => {
    const response = await login(credentials)
    if (!response?.userId || !response?.role || !response?.email) {
      throw new Error('The server returned an incomplete sign-in response.')
    }
    const sessionUser = {
      userId: response.userId,
      fullName: response.fullName,
      email: response.email,
      phoneNumber: response.phoneNumber,
      role: response.role,
      dateOfBirth: response.dateOfBirth,
    }
    setUser(sessionUser)
    setInitializationError('')
    setReady(true)
    return sessionUser
  }, [])

  const endSession = useCallback(async () => {
    await logout()
    signOut()
  }, [signOut])

  const value = useMemo(() => ({
    user,
    ready,
    initializationError,
    retrySession: restoreSession,
    signIn,
    signOut: endSession,
  }), [user, ready, initializationError, restoreSession, signIn, endSession])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// The provider and hook are exported together as the authentication context API.
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}
