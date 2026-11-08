import { api } from './api'

export async function getPayments() {
  const { data } = await api.get('/api/payments')
  return data
}

export async function getMyPayments() {
  const { data } = await api.get('/api/payments/my-history')
  return data
}
