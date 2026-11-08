import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || ''
let csrfToken
let csrfRequest
let unauthorizedHandler = () => {}

export const api = axios.create({
  baseURL,
  timeout: 15000,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

async function getCsrfToken(forceRefresh = false) {
  if (forceRefresh) {
    csrfToken = undefined
    csrfRequest = undefined
  }
  if (!csrfToken) {
    csrfRequest ||= axios.get('/api/auth/csrf', {
      baseURL,
      timeout: 15000,
      withCredentials: true,
    }).then(({ data }) => {
      if (!data?.token) throw new Error('The server did not provide a CSRF token.')
      csrfToken = data.token
      return csrfToken
    }).finally(() => {
      csrfRequest = undefined
    })
  }
  return csrfRequest || csrfToken
}

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

api.interceptors.request.use(async (config) => {
  const method = config.method?.toUpperCase()
  if (method && !['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    config.headers['X-XSRF-TOKEN'] = await getCsrfToken()
  }
  return config
})

api.interceptors.response.use(
  async (response) => {
    const url = response.config?.url || ''
    if (url.endsWith('/api/auth/login') || url.endsWith('/api/auth/logout')) {
      await getCsrfToken(true)
    }
    return response
  },
  async (error) => {
    const config = error.config
    const url = config?.url || ''
    const isLoginRequest = url.endsWith('/api/auth/login')

    if (error.response?.status === 403 && isLoginRequest && config && !config._csrfRetry) {
      config._csrfRetry = true
      config.headers['X-XSRF-TOKEN'] = await getCsrfToken(true)
      return api.request(config)
    }

    if (error.response?.status === 401 && !isLoginRequest) unauthorizedHandler()
    return Promise.reject(error)
  },
)

export function getErrorMessage(error) {
  const status = error.response?.status
  const url = error.config?.url || ''
  if (!error.response) return 'We could not reach the server. Check your connection and try again.'
  if (status === 401) return 'We could not verify your sign-in. Check your credentials or sign in again.'
  if (status === 403 && url.endsWith('/api/auth/login')) return 'We could not verify the sign-in request. Refresh the page and try again.'
  if (status === 403) return 'You do not have permission to access this information.'
  if (status === 410) return 'This verification code or registration has expired. Please register again to request a new code.'
  if (status === 423) return 'Your account is temporarily locked after too many sign-in attempts. Please try again later.'
  if (status === 429) return 'Too many verification attempts. Please register again or try again later.'
  if (status >= 500) return 'The service is temporarily unavailable. Please try again later.'
  if (status === 400 || status === 409 || status === 422) return 'Please check the information and try again.'
  return 'Something went wrong. Please try again.'
}
