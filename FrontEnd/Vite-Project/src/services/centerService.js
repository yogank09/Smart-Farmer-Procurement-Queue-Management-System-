import { api } from './api'

export async function getCenters() {
  const { data } = await api.get('/api/centers')
  return data
}

export async function searchCentersByDistrict(district) {
  const { data } = await api.get('/api/centers/search/district', { params: { district } })
  return data
}

export async function getCenter(id) {
  const { data } = await api.get(`/api/centers/${id}`)
  return data
}
