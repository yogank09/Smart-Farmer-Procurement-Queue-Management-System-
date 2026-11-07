import { api } from './api'

export async function getMyQueue() {
  const { data } = await api.get('/api/queue/my-queue')
  return data
}

export async function joinQueue(tokenId) {
  const { data } = await api.post('/api/queue/join', { tokenId })
  return data
}

export async function getQueueForSlot(slotId) {
  const { data } = await api.get(`/api/queue/slot/${slotId}`)
  return data
}

export async function callNextFarmer(slotId) {
  const { data } = await api.post('/api/queue/call-next', { slotId })
  return data
}

export async function startProcessing(queueId) {
  const { data } = await api.patch(`/api/queue/${queueId}/start-processing`)
  return data
}

export async function completeProcurement(queueId) {
  const { data } = await api.patch(`/api/queue/${queueId}/complete`)
  return data
}
