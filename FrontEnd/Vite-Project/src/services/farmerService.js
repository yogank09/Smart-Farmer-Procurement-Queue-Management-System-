import { api } from './api'

export async function getFarmerProfile() {
  const { data } = await api.get('/api/farmers/profile')
  return data
}

export async function createFarmerProfile(profile) {
  const { data } = await api.post('/api/farmers/profile', profile)
  return data
}

export async function updateFarmerProfile(profile) {
  const { data } = await api.put('/api/farmers/profile', profile)
  return data
}
