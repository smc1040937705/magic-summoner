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
  login: (username, password) => 
    api.post('/auth/login', { username, password }),
  
  register: (username, password, email, displayName) => 
    api.post('/auth/register', { username, password, email, displayName }),
  
  getCurrentUser: () => 
    api.get('/auth/me'),
  
  logout: () => 
    api.post('/auth/logout')
}

export { api }
