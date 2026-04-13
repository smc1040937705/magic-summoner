import { describe, it, expect, beforeEach, vi } from 'vitest'

// Hoist mock functions so they're available during vi.mock
const { mockGet, mockPost } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn()
}))

// Mock axios before importing the module
vi.mock('axios', () => ({
  default: {
    create: () => ({
      interceptors: {
        request: { use: vi.fn() }
      },
      get: mockGet,
      post: mockPost
    })
  }
}))

// Import after mock
import { cardsApi } from './cards'

describe('Cards API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getAllCards', () => {
    it('should call GET /cards', async () => {
      const mockResponse = { data: [{ id: 1, name: 'Fire Dragon' }] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await cardsApi.getAllCards()

      expect(mockGet).toHaveBeenCalledWith('/cards')
      expect(result).toEqual(mockResponse)
    })

    it('should handle empty card list', async () => {
      const mockResponse = { data: [] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await cardsApi.getAllCards()

      expect(result.data).toEqual([])
    })
  })

  describe('getCardById', () => {
    it('should call GET /cards/:id', async () => {
      const mockResponse = { data: { id: 1, name: 'Fire Dragon' } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await cardsApi.getCardById(1)

      expect(mockGet).toHaveBeenCalledWith('/cards/1')
      expect(result).toEqual(mockResponse)
    })

    it('should handle card not found', async () => {
      const error = new Error('Card not found')
      error.response = { status: 404 }
      mockGet.mockRejectedValue(error)

      await expect(cardsApi.getCardById(999)).rejects.toThrow('Card not found')
    })
  })

  describe('getCardsByType', () => {
    it('should call GET /cards/type with type param', async () => {
      const mockResponse = { data: [{ id: 1, type: 'CREATURE' }] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await cardsApi.getCardsByType('CREATURE')

      expect(mockGet).toHaveBeenCalledWith('/cards/type', { params: { cardType: 'CREATURE' } })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('getCardsByElement', () => {
    it('should call GET /cards/element with element param', async () => {
      const mockResponse = { data: [{ id: 1, element: 'FIRE' }] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await cardsApi.getCardsByElement('FIRE')

      expect(mockGet).toHaveBeenCalledWith('/cards/element', { params: { elementType: 'FIRE' } })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('getCardsByRarity', () => {
    it('should call GET /cards/rarity with rarity param', async () => {
      const mockResponse = { data: [{ id: 1, rarity: 'LEGENDARY' }] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await cardsApi.getCardsByRarity('LEGENDARY')

      expect(mockGet).toHaveBeenCalledWith('/cards/rarity', { params: { rarity: 'LEGENDARY' } })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('searchCards', () => {
    it('should call GET /cards/search with keyword and pagination', async () => {
      const mockResponse = { data: { content: [{ id: 1, name: 'Dragon' }], totalPages: 1 } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await cardsApi.searchCards('dragon', 0, 10)

      expect(mockGet).toHaveBeenCalledWith('/cards/search', { params: { keyword: 'dragon', page: 0, size: 10 } })
      expect(result).toEqual(mockResponse)
    })

    it('should use default pagination values', async () => {
      const mockResponse = { data: { content: [], totalPages: 0 } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await cardsApi.searchCards('test')

      expect(mockGet).toHaveBeenCalledWith('/cards/search', { params: { keyword: 'test', page: 0, size: 20 } })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('getCardsByFilters', () => {
    it('should call GET /cards/filter with filter params', async () => {
      const mockResponse = { data: [{ id: 1, element: 'FIRE', rarity: 'RARE' }] }
      mockGet.mockResolvedValue(mockResponse)

      const filters = { element: 'FIRE', rarity: 'RARE' }
      const result = await cardsApi.getCardsByFilters(filters)

      expect(mockGet).toHaveBeenCalledWith('/cards/filter', { params: { element: 'FIRE', rarity: 'RARE', page: 0, size: 20 } })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('openCardPack', () => {
    it('should call POST /cards/pack/open', async () => {
      const mockResponse = { data: [{ id: 1, name: 'New Card' }] }
      mockPost.mockResolvedValue(mockResponse)

      const result = await cardsApi.openCardPack('NORMAL')

      expect(mockPost).toHaveBeenCalledWith('/cards/pack/open')
      expect(result).toEqual(mockResponse)
    })
  })

  describe('openSingleCard', () => {
    it('should call POST /cards/single/open', async () => {
      const mockResponse = { data: { id: 1, name: 'Rare Card' } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await cardsApi.openSingleCard()

      expect(mockPost).toHaveBeenCalledWith('/cards/single/open')
      expect(result).toEqual(mockResponse)
    })
  })
})
