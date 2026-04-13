import { config } from '@vue/test-utils'
import { vi, beforeEach } from 'vitest'

config.global.stubs = {}

const mockLocalStorage = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}

Object.defineProperty(window, 'localStorage', {
  value: mockLocalStorage,
  writable: true
})

let mockAxiosInstance = null
export let requestInterceptor = null

const mockCreate = vi.fn(() => {
  mockAxiosInstance = {
    interceptors: {
      request: {
        use: vi.fn((callback) => {
          requestInterceptor = callback
          return callback
        })
      },
      response: { use: vi.fn() }
    },
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    defaults: {
      headers: {
        common: {}
      }
    }
  }
  return mockAxiosInstance
})

vi.mock('axios', () => {
  return {
    default: {
      create: mockCreate
    }
  }
})

export function getMockAxios() {
  if (!mockAxiosInstance) {
    mockCreate()
  }
  return mockAxiosInstance
}

export function initRequestInterceptor() {
  if (!requestInterceptor) {
    const axios = getMockAxios()
    axios.interceptors.request.use((config) => {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    })
  }
}

beforeEach(() => {
  vi.clearAllMocks()
  mockLocalStorage.getItem.mockReset()
  mockLocalStorage.setItem.mockReset()
  mockLocalStorage.removeItem.mockReset()
  mockLocalStorage.clear.mockReset()
})
