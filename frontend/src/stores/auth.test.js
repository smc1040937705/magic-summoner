import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import axios from 'axios'

// Mock axios before importing the store
vi.mock('axios', () => {
  return {
    default: {
      create: vi.fn(() => ({
        interceptors: {
          request: { use: vi.fn() }
        },
        post: vi.fn(),
        get: vi.fn(),
        defaults: {
          headers: {
            common: {}
          }
        }
      }))
    }
  }
})

// Import after mock
import { useAuthStore, api } from './auth'

describe('Auth Store', () => {
  let store
  let mockApi

  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear()
    vi.clearAllMocks()
    
    // Create a fresh Pinia instance
    setActivePinia(createPinia())
    
    // Get the store instance
    store = useAuthStore()
    mockApi = api
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('State', () => {
    it('should have correct initial state', () => {
      expect(store.token).toBeNull()
      expect(store.player).toBeNull()
      expect(store.isLoading).toBe(false)
      expect(store.error).toBeNull()
    })

    it('should initialize token from localStorage', () => {
      // Set token in localStorage before creating store
      localStorage.setItem('token', 'test-token')
      
      // Create a fresh store to read from localStorage
      const freshPinia = createPinia()
      setActivePinia(freshPinia)
      const newStore = useAuthStore()
      
      // The store should read the token from localStorage
      expect(newStore.token).toBe('test-token')
    })
  })

  describe('Getters', () => {
    it('isLoggedIn should return false when token is null', () => {
      store.token = null
      expect(store.isLoggedIn).toBe(false)
    })

    it('isLoggedIn should return true when token exists', () => {
      store.token = 'test-token'
      expect(store.isLoggedIn).toBe(true)
    })
  })

  describe('Login', () => {
    it('should login successfully', async () => {
      const mockResponse = {
        data: {
          token: 'test-token',
          player: { id: 1, username: 'testuser' }
        }
      }
      mockApi.post.mockResolvedValue(mockResponse)

      const result = await store.login('testuser', 'password123')

      expect(result).toBe(true)
      expect(store.token).toBe('test-token')
      expect(store.player).toEqual({ id: 1, username: 'testuser' })
      expect(localStorage.getItem('token')).toBe('test-token')
    })

    it('should handle login failure', async () => {
      const error = new Error('Login failed')
      error.response = { data: { message: 'Invalid credentials' } }
      mockApi.post.mockRejectedValue(error)

      const result = await store.login('testuser', 'wrongpass')

      expect(result).toBe(false)
      expect(store.error).toBe('Invalid credentials')
      expect(store.token).toBeNull()
    })

    it('should set loading state during login', async () => {
      const mockResponse = {
        data: { token: 'token', player: {} }
      }
      mockApi.post.mockResolvedValue(mockResponse)

      const loginPromise = store.login('testuser', 'password')
      expect(store.isLoading).toBe(true)

      await loginPromise
      expect(store.isLoading).toBe(false)
    })

    it('should handle network error', async () => {
      mockApi.post.mockRejectedValue(new Error('Network error'))

      const result = await store.login('testuser', 'password')

      expect(result).toBe(false)
      expect(store.error).toBe('登录失败')
    })
  })

  describe('Register', () => {
    it('should register successfully', async () => {
      const mockResponse = {
        data: {
          token: 'new-token',
          player: { id: 2, username: 'newuser' }
        }
      }
      mockApi.post.mockResolvedValue(mockResponse)

      const result = await store.register('newuser', 'pass123', 'new@test.com', 'New User')

      expect(result).toBe(true)
      expect(store.token).toBe('new-token')
      expect(store.player).toEqual({ id: 2, username: 'newuser' })
    })

    it('should handle register failure', async () => {
      const error = new Error('Register failed')
      error.response = { data: { message: 'Username exists' } }
      mockApi.post.mockRejectedValue(error)

      const result = await store.register('existing', 'pass', 'test@test.com', 'Test')

      expect(result).toBe(false)
      expect(store.error).toBe('Username exists')
    })

    it('should handle duplicate username', async () => {
      const error = new Error('Duplicate')
      error.response = { data: { message: 'Username already taken' } }
      mockApi.post.mockRejectedValue(error)

      const result = await store.register('taken', 'pass', 'email@test.com', 'Name')

      expect(result).toBe(false)
      expect(store.error).toBe('Username already taken')
    })
  })

  describe('Fetch Current User', () => {
    it('should fetch current user when token exists', async () => {
      store.token = 'test-token'
      const mockResponse = {
        data: { id: 1, username: 'testuser', displayName: 'Test User' }
      }
      mockApi.get.mockResolvedValue(mockResponse)

      await store.fetchCurrentUser()

      expect(store.player).toEqual(mockResponse.data)
    })

    it('should not fetch when token is null', async () => {
      store.token = null

      await store.fetchCurrentUser()

      expect(mockApi.get).not.toHaveBeenCalled()
    })

    it('should logout on fetch failure', async () => {
      store.token = 'invalid-token'
      mockApi.get.mockRejectedValue(new Error('Unauthorized'))

      await store.fetchCurrentUser()

      expect(store.token).toBeNull()
      expect(store.player).toBeNull()
    })
  })

  describe('Logout', () => {
    it('should clear all auth state', () => {
      store.token = 'test-token'
      store.player = { id: 1, username: 'testuser' }
      localStorage.setItem('token', 'test-token')

      store.logout()

      expect(store.token).toBeNull()
      expect(store.player).toBeNull()
      expect(localStorage.getItem('token')).toBeNull()
    })

    it('should handle logout when already logged out', () => {
      store.token = null
      store.player = null

      store.logout()

      expect(store.token).toBeNull()
      expect(store.player).toBeNull()
    })
  })
})
