import { api } from './api'

export async function login(credentials) {
  const { data } = await api.post('/api/auth/login', credentials)
  return data
}

export async function getSession() {
  const { data } = await api.get('/api/auth/session')
  return data
}

export async function logout() {
  await api.post('/api/auth/logout')
}

export async function register(details) {
  const { data } = await api.post('/api/auth/register', details)
  return data
}

export async function verifyEmail(payload) {
  const { data } = await api.post('/api/auth/verify-email', payload)
  return data
}

// Password reset and OTP resend are not exposed by the current backend.
