import { api } from './api'

export async function getMyTokens() {
  const { data } = await api.get('/api/tokens/my-tokens')
  return data
}

export async function bookToken(request) {
  const { data } = await api.post('/api/tokens', request)
  return data
}

export async function cancelToken(id) {
  const { data } = await api.patch(`/api/tokens/${id}/cancel`)
  return data
}
