import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const authApi = {
  login(username, password) {
    return api.post('/auth/login', { username, password })
  },

  register(username, password, email, displayName) {
    return api.post('/auth/register', { username, password, email, displayName })
  },

  getCurrentUser() {
    return api.get('/auth/me')
  },

  logout() {
    return api.post('/auth/logout')
  }
}

export default authApi
