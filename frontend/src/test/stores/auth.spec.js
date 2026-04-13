import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../../stores/auth'
import axios from 'axios'

vi.mock('axios', () => {
  const mockAxios = {
    create: vi.fn(() => mockAxios),
    post: vi.fn(),
    get: vi.fn(),
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
    default: mockAxios
  }
})

describe('Auth Store', () => {
  let authStore

  beforeEach(() => {
    setActivePinia(createPinia())
    authStore = useAuthStore()
    vi.clearAllMocks()
    localStorage.clear()
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  describe('初始状态', () => {
    it('应该有正确的初始状态', () => {
      expect(authStore.token).toBeNull()
      expect(authStore.player).toBeNull()
      expect(authStore.isLoading).toBe(false)
      expect(authStore.error).toBeNull()
    })

    it('isLoggedIn getter 应该正确返回登录状态', () => {
      expect(authStore.isLoggedIn).toBe(false)
      authStore.token = 'test-token'
      expect(authStore.isLoggedIn).toBe(true)
    })
  })

  describe('login action', () => {
    it('登录成功应该设置token和player', async () => {
      const mockResponse = {
        data: {
          token: 'test-jwt-token',
          player: {
            id: 1,
            username: 'testuser',
            email: 'test@example.com',
            displayName: 'Test User'
          }
        }
      }

      const mockAxios = await import('axios')
      mockAxios.default.post.mockResolvedValueOnce(mockResponse)

      const result = await authStore.login('testuser', 'password123')

      expect(result).toBe(true)
      expect(authStore.token).toBe('test-jwt-token')
      expect(authStore.player).toEqual(mockResponse.data.player)
      expect(authStore.isLoading).toBe(false)
      expect(authStore.error).toBeNull()
    })

    it('登录失败应该设置错误信息', async () => {
      const mockError = {
        response: {
          data: {
            message: '用户名或密码错误'
          }
        }
      }

      const mockAxios = await import('axios')
      mockAxios.default.post.mockRejectedValueOnce(mockError)

      const result = await authStore.login('testuser', 'wrongpassword')

      expect(result).toBe(false)
      expect(authStore.error).toBe('用户名或密码错误')
      expect(authStore.token).toBeNull()
      expect(authStore.isLoading).toBe(false)
    })

    it('登录失败且无错误消息时应该使用默认消息', async () => {
      const mockAxios = await import('axios')
      mockAxios.default.post.mockRejectedValueOnce({})

      const result = await authStore.login('testuser', 'password')

      expect(result).toBe(false)
      expect(authStore.error).toBe('登录失败')
    })

    it('登录过程中isLoading应该正确变化', async () => {
      const mockAxios = await import('axios')
      mockAxios.default.post.mockImplementation(() => {
        return new Promise(resolve => {
          setTimeout(() => {
            resolve({
              data: {
                token: 'token',
                player: { id: 1, username: 'test' }
              }
            })
          }, 100)
        })
      })

      const promise = authStore.login('testuser', 'password')
      
      expect(authStore.isLoading).toBe(true)
      
      await promise
      
      expect(authStore.isLoading).toBe(false)
    })
  })

  describe('register action', () => {
    it('注册成功应该设置token和player', async () => {
      const mockResponse = {
        data: {
          token: 'new-user-token',
          player: {
            id: 2,
            username: 'newuser',
            email: 'new@example.com',
            displayName: 'New User'
          }
        }
      }

      const mockAxios = await import('axios')
      mockAxios.default.post.mockResolvedValueOnce(mockResponse)

      const result = await authStore.register('newuser', 'password123', 'new@example.com', 'New User')

      expect(result).toBe(true)
      expect(authStore.token).toBe('new-user-token')
      expect(authStore.player).toEqual(mockResponse.data.player)
      expect(authStore.error).toBeNull()
    })

    it('注册失败应该设置错误信息', async () => {
      const mockError = {
        response: {
          data: {
            message: '用户名已存在'
          }
        }
      }

      const mockAxios = await import('axios')
      mockAxios.default.post.mockRejectedValueOnce(mockError)

      const result = await authStore.register('existinguser', 'password', 'email@test.com', 'Display')

      expect(result).toBe(false)
      expect(authStore.error).toBe('用户名已存在')
    })

    it('注册失败且无错误消息时应该使用默认消息', async () => {
      const mockAxios = await import('axios')
      mockAxios.default.post.mockRejectedValueOnce({})

      const result = await authStore.register('user', 'pass', 'email', 'name')

      expect(result).toBe(false)
      expect(authStore.error).toBe('注册失败')
    })
  })

  describe('fetchCurrentUser action', () => {
    it('有token时应该获取当前用户信息', async () => {
      authStore.token = 'valid-token'

      const mockResponse = {
        data: {
          id: 1,
          username: 'testuser',
          email: 'test@example.com'
        }
      }

      const mockAxios = await import('axios')
      mockAxios.default.get.mockResolvedValueOnce(mockResponse)

      await authStore.fetchCurrentUser()

      expect(authStore.player).toEqual(mockResponse.data)
    })

    it('无token时应该不发送请求', async () => {
      const mockAxios = await import('axios')
      
      await authStore.fetchCurrentUser()

      expect(mockAxios.default.get).not.toHaveBeenCalled()
    })

    it('获取用户信息失败应该调用logout', async () => {
      authStore.token = 'invalid-token'
      authStore.player = { id: 1, username: 'test' }

      const mockAxios = await import('axios')
      mockAxios.default.get.mockRejectedValueOnce(new Error('Unauthorized'))

      const logoutSpy = vi.spyOn(authStore, 'logout')

      await authStore.fetchCurrentUser()

      expect(logoutSpy).toHaveBeenCalled()
    })
  })

  describe('logout action', () => {
    it('logout应该清除所有状态', () => {
      authStore.token = 'test-token'
      authStore.player = { id: 1, username: 'testuser' }

      authStore.logout()

      expect(authStore.token).toBeNull()
      expect(authStore.player).toBeNull()
    })
  })
})
