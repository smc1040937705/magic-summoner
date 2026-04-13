import { vi } from 'vitest'
import { config } from '@vue/test-utils'

vi.stubGlobal('localStorage', {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
})

config.global.mocks = {
  $router: {
    push: vi.fn()
  }
}
