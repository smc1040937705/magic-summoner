import { createPinia, setActivePinia } from 'pinia'
import { vi, describe, beforeEach, it, expect } from 'vitest'
import { getMockAxios } from '../setup'

describe('Auth Store', () => {
  let useAuthStore
  let mockApi

  beforeEach(async () => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
    
    mockApi = getMockAxios()
    
    const authModule = await import('../../stores/auth')
    useAuthStore = authModule.useAuthStore
  })

  it('should have default initial state', () => {
    const store = useAuthStore()

    expect(store.user).toBeNull()
    expect(store.token).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(store.isLoading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('login should successfully authenticate user', async () => {
    const mockResponse = {
      data: {
        token: 'jwt-token-123',
        player: { id: 1, username: 'testuser', displayName: 'Test User' }
      }
    }
    mockApi.post.mockResolvedValue(mockResponse)

    const store = useAuthStore()
    await store.login({ username: 'testuser', password: 'password123' })

    expect(mockApi.post).toHaveBeenCalledWith('/auth/login', {
      username: 'testuser',
      password: 'password123'
    })
    expect(store.token).toBe('jwt-token-123')
    expect(store.user).toEqual(mockResponse.data.player)
    expect(store.isAuthenticated).toBe(true)
    expect(store.error).toBeNull()
    expect(localStorage.setItem).toHaveBeenCalledWith('token', 'jwt-token-123')
  })

  it('login should handle authentication error', async () => {
    const errorMessage = 'Invalid credentials'
    mockApi.post.mockRejectedValue(new Error(errorMessage))

    const store = useAuthStore()
    await store.login({ username: 'wrong', password: 'wrong' })

    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(store.error).toBe(errorMessage)
  })

  it('register should successfully create user', async () => {
    mockApi.post.mockResolvedValue({ data: { success: true } })

    const store = useAuthStore()
    await store.register({
      username: 'newuser',
      password: 'password123',
      email: 'test@example.com',
      displayName: 'New User'
    })

    expect(mockApi.post).toHaveBeenCalledWith('/auth/register', {
      username: 'newuser',
      password: 'password123',
      email: 'test@example.com',
      displayName: 'New User'
    })
    expect(store.error).toBeNull()
  })

  it('register should handle registration error', async () => {
    const errorMessage = 'Username already exists'
    mockApi.post.mockRejectedValue(new Error(errorMessage))

    const store = useAuthStore()
    await store.register({ username: 'existing', password: 'pass', email: 'e@e.com', displayName: 'E' })

    expect(store.error).toBe(errorMessage)
  })

  it('fetchUser should get current user info', async () => {
    const mockUser = { id: 1, username: 'testuser' }
    mockApi.get.mockResolvedValue({ data: mockUser })

    const store = useAuthStore()
    store.token = 'valid-token'
    await store.fetchUser()

    expect(mockApi.get).toHaveBeenCalledWith('/auth/me')
    expect(store.user).toEqual(mockUser)
    expect(store.isAuthenticated).toBe(true)
  })

  it('fetchUser should not call API when no token', async () => {
    const store = useAuthStore()
    store.token = null
    await store.fetchUser()

    expect(mockApi.get).not.toHaveBeenCalled()
  })

  it('fetchUser should handle error and clear auth', async () => {
    mockApi.get.mockRejectedValue(new Error('Token expired'))

    const store = useAuthStore()
    store.token = 'expired-token'
    await store.fetchUser()

    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(localStorage.removeItem).toHaveBeenCalledWith('token')
  })

  it('logout should clear all auth data', () => {
    const store = useAuthStore()
    store.token = 'token'
    store.user = { id: 1, username: 'test' }

    store.logout()

    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(localStorage.removeItem).toHaveBeenCalledWith('token')
  })

  it('clearError should reset error state', () => {
    const store = useAuthStore()
    store.error = 'Some error'

    store.clearError()

    expect(store.error).toBeNull()
  })

  it('should manage loading state during login', async () => {
    let resolvePromise
    const promise = new Promise(resolve => {
      resolvePromise = resolve
    })
    mockApi.post.mockReturnValue(promise)

    const store = useAuthStore()
    const loginPromise = store.login({ username: 'test', password: 'test' })

    expect(store.isLoading).toBe(true)

    resolvePromise({ data: { token: 't', player: {} } })
    await loginPromise

    expect(store.isLoading).toBe(false)
  })

  it('should manage loading state during error', async () => {
    let rejectPromise
    const promise = new Promise((_, reject) => {
      rejectPromise = reject
    })
    mockApi.post.mockReturnValue(promise)

    const store = useAuthStore()
    const loginPromise = store.login({ username: 'test', password: 'test' })

    expect(store.isLoading).toBe(true)

    rejectPromise(new Error('fail'))
    try {
      await loginPromise
    } catch (e) {}

    expect(store.isLoading).toBe(false)
  })
})
