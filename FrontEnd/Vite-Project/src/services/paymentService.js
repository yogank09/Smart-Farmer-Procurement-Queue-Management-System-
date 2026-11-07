import { api } from './api'

export async function getPayments() {
  const { data } = await api.get('/api/payments')
  return data
}
