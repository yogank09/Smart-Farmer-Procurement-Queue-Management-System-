import { api } from './api'

export async function getProcurements() {
  const { data } = await api.get('/api/procurements')
  return data
}

export async function getProcurement(id) {
  const { data } = await api.get(`/api/procurements/${id}`)
  return data
}
