import { api } from './api'

export async function getSlots() {
  const { data } = await api.get('/api/slots')
  return data
}

export async function getSlotsForCenter(centerId) {
  const { data } = await api.get(`/api/slots/center/${centerId}`)
  return data
}

export async function getSlotsForCenterAndDate(centerId, slotDate) {
  const { data } = await api.get(`/api/slots/center/${centerId}/date`, { params: { slotDate } })
  return data
}
