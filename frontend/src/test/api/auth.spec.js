import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { authApi, api } from '../../api/auth'
import axios from 'axios'

vi.mock('axios', () => {
  const mockApi = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    interceptors: {
      request: {
        use: vi.fn()
      }
    },
    defaults: {
      headers: {
        common: {}
      }
    }
  }
  return {
    default: {
      create: vi.fn(() => mockApi)
    }
  }
})

describe('Auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  describe('login', () => {
    it('应该发送登录请求', async () => {
      const mockResponse = {
        data: {
          token: 'test-token',
          player: { id: 1, username: 'testuser' }
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await authApi.login('testuser', 'password123')

      expect(api.post).toHaveBeenCalledWith('/auth/login', {
        username: 'testuser',
        password: 'password123'
      })
      expect(result.data.token).toBe('test-token')
    })

    it('登录失败应该抛出错误', async () => {
      const mockError = new Error('Invalid credentials')
      api.post.mockRejectedValueOnce(mockError)

      await expect(authApi.login('testuser', 'wrongpassword')).rejects.toThrow('Invalid credentials')
    })
  })

  describe('register', () => {
    it('应该发送注册请求', async () => {
      const mockResponse = {
        data: {
          token: 'new-user-token',
          player: { id: 2, username: 'newuser' }
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await authApi.register('newuser', 'password', 'test@example.com', 'Display Name')

      expect(api.post).toHaveBeenCalledWith('/auth/register', {
        username: 'newuser',
        password: 'password',
        email: 'test@example.com',
        displayName: 'Display Name'
      })
      expect(result.data.token).toBe('new-user-token')
    })

    it('注册失败应该抛出错误', async () => {
      const mockError = new Error('Username already exists')
      api.post.mockRejectedValueOnce(mockError)

      await expect(authApi.register('existinguser', 'password', 'email', 'name')).rejects.toThrow('Username already exists')
    })
  })

  describe('getCurrentUser', () => {
    it('应该获取当前用户信息', async () => {
      const mockResponse = {
        data: {
          id: 1,
          username: 'testuser',
          email: 'test@example.com'
        }
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await authApi.getCurrentUser()

      expect(api.get).toHaveBeenCalledWith('/auth/me')
      expect(result.data.username).toBe('testuser')
    })

    it('获取用户信息失败应该抛出错误', async () => {
      const mockError = new Error('Unauthorized')
      api.get.mockRejectedValueOnce(mockError)

      await expect(authApi.getCurrentUser()).rejects.toThrow('Unauthorized')
    })
  })

  describe('logout', () => {
    it('应该发送登出请求', async () => {
      const mockResponse = { data: { message: 'Logged out successfully' } }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await authApi.logout()

      expect(api.post).toHaveBeenCalledWith('/auth/logout')
      expect(result.data.message).toBe('Logged out successfully')
    })

    it('登出失败应该抛出错误', async () => {
      const mockError = new Error('Logout failed')
      api.post.mockRejectedValueOnce(mockError)

      await expect(authApi.logout()).rejects.toThrow('Logout failed')
    })
  })
})
