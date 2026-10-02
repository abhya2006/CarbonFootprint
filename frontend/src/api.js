import axios from 'axios'

const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api' })
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('ecotrack.token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
api.interceptors.response.use((response) => response, (error) => {
  if (error.response?.status === 401) {
    sessionStorage.removeItem('ecotrack.token')
    window.dispatchEvent(new Event('ecotrack:unauthorized'))
  }
  return Promise.reject(error)
})

export function apiError(error) {
  return error.response?.data?.message || (!error.response ? 'Cannot reach EcoTrack API' : error.message) || 'Something went wrong'
}

export default api