import { defineStore } from 'pinia'
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

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    player: null,
    isLoading: false,
    error: null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token
  },

  actions: {
    async login(username, password) {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.post('/auth/login', { username, password })
        this.token = response.data.token
        this.player = response.data.player
        localStorage.setItem('token', this.token)
        api.defaults.headers.common['Authorization'] = `Bearer ${this.token}`
        return true
      } catch (error) {
        this.error = error.response?.data?.message || '登录失败'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async register(username, password, email, displayName) {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.post('/auth/register', { 
          username, password, email, displayName 
        })
        this.token = response.data.token
        this.player = response.data.player
        localStorage.setItem('token', this.token)
        api.defaults.headers.common['Authorization'] = `Bearer ${this.token}`
        return true
      } catch (error) {
        this.error = error.response?.data?.message || '注册失败'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async fetchCurrentUser() {
      if (!this.token) return
      try {
        const response = await api.get('/auth/me')
        this.player = response.data
      } catch (error) {
        this.logout()
      }
    },

    logout() {
      this.token = null
      this.player = null
      localStorage.removeItem('token')
      delete api.defaults.headers.common['Authorization']
    }
  }
})

export { api }
