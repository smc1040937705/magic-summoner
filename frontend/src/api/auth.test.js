import { describe, it, expect, beforeEach, vi } from 'vitest'

// Hoist mock functions so they're available during vi.mock
const { mockPost, mockGet } = vi.hoisted(() => ({
  mockPost: vi.fn(),
  mockGet: vi.fn()
}))

// Mock axios before importing the module
vi.mock('axios', () => ({
  default: {
    create: () => ({
      interceptors: {
        request: { use: vi.fn() }
      },
      post: mockPost,
      get: mockGet
    })
  }
}))

// Import after mock
import { authApi } from './auth'

describe('Auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('login', () => {
    it('should call POST /auth/login with correct data', async () => {
      const mockResponse = { data: { token: 'test-token' } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await authApi.login('testuser', 'password123')

      expect(mockPost).toHaveBeenCalledWith('/auth/login', {
        username: 'testuser',
        password: 'password123'
      })
      expect(result).toEqual(mockResponse)
    })

    it('should handle login error', async () => {
      const error = new Error('Invalid credentials')
      mockPost.mockRejectedValue(error)

      await expect(authApi.login('testuser', 'wrongpass')).rejects.toThrow('Invalid credentials')
    })
  })

  describe('register', () => {
    it('should call POST /auth/register with correct data', async () => {
      const mockResponse = { data: { token: 'new-token' } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await authApi.register('newuser', 'pass123', 'new@example.com', 'New User')

      expect(mockPost).toHaveBeenCalledWith('/auth/register', {
        username: 'newuser',
        password: 'pass123',
        email: 'new@example.com',
        displayName: 'New User'
      })
      expect(result).toEqual(mockResponse)
    })

    it('should handle register error', async () => {
      const error = new Error('Username already exists')
      mockPost.mockRejectedValue(error)

      await expect(authApi.register('existing', 'pass', 'test@test.com', 'Test')).rejects.toThrow('Username already exists')
    })
  })

  describe('getCurrentUser', () => {
    it('should call GET /auth/me', async () => {
      const mockResponse = { data: { id: 1, username: 'testuser' } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await authApi.getCurrentUser()

      expect(mockGet).toHaveBeenCalledWith('/auth/me')
      expect(result).toEqual(mockResponse)
    })

    it('should handle unauthorized error', async () => {
      const error = new Error('Unauthorized')
      error.response = { status: 401 }
      mockGet.mockRejectedValue(error)

      await expect(authApi.getCurrentUser()).rejects.toThrow('Unauthorized')
    })
  })

  describe('logout', () => {
    it('should call POST /auth/logout', async () => {
      const mockResponse = { data: { message: 'Logged out' } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await authApi.logout()

      expect(mockPost).toHaveBeenCalledWith('/auth/logout')
      expect(result).toEqual(mockResponse)
    })
  })
})
