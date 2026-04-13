import { describe, it, expect, beforeEach, vi } from 'vitest'

// Hoist mock functions so they're available during vi.mock
const { mockGet, mockPost, mockPut, mockDelete } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn(),
  mockPut: vi.fn(),
  mockDelete: vi.fn()
}))

// Mock axios before importing the module
vi.mock('axios', () => ({
  default: {
    create: () => ({
      interceptors: {
        request: { use: vi.fn() }
      },
      get: mockGet,
      post: mockPost,
      put: mockPut,
      delete: mockDelete
    })
  }
}))

// Import after mock
import { decksApi } from './decks'

describe('Decks API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getMyDecks', () => {
    it('should call GET /decks/my', async () => {
      const mockResponse = { data: [{ id: 1, name: 'My Deck' }] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await decksApi.getMyDecks()

      expect(mockGet).toHaveBeenCalledWith('/decks/my')
      expect(result).toEqual(mockResponse)
    })

    it('should return empty array when no decks', async () => {
      const mockResponse = { data: [] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await decksApi.getMyDecks()

      expect(result.data).toEqual([])
    })
  })

  describe('getDeckById', () => {
    it('should call GET /decks/:id', async () => {
      const mockResponse = { data: { id: 1, name: 'My Deck', cards: [] } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await decksApi.getDeckById(1)

      expect(mockGet).toHaveBeenCalledWith('/decks/1')
      expect(result).toEqual(mockResponse)
    })

    it('should handle deck not found', async () => {
      const error = new Error('Deck not found')
      error.response = { status: 404 }
      mockGet.mockRejectedValue(error)

      await expect(decksApi.getDeckById(999)).rejects.toThrow('Deck not found')
    })
  })

  describe('createDeck', () => {
    it('should call POST /decks with name and description', async () => {
      const mockResponse = { data: { id: 1, name: 'New Deck', description: 'Test deck' } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await decksApi.createDeck('New Deck', 'Test deck')

      expect(mockPost).toHaveBeenCalledWith('/decks', { name: 'New Deck', description: 'Test deck' })
      expect(result).toEqual(mockResponse)
    })

    it('should create deck with null description', async () => {
      const mockResponse = { data: { id: 1, name: 'New Deck', description: null } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await decksApi.createDeck('New Deck', null)

      expect(mockPost).toHaveBeenCalledWith('/decks', { name: 'New Deck', description: null })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('updateDeck', () => {
    it('should call PUT /decks/:id with updated data', async () => {
      const mockResponse = { data: { id: 1, name: 'Updated Deck' } }
      mockPut.mockResolvedValue(mockResponse)

      const result = await decksApi.updateDeck(1, 'Updated Deck', undefined)

      expect(mockPut).toHaveBeenCalledWith('/decks/1', { name: 'Updated Deck', description: undefined })
      expect(result).toEqual(mockResponse)
    })

    it('should handle unauthorized update', async () => {
      const error = new Error('Unauthorized')
      error.response = { status: 403 }
      mockPut.mockRejectedValue(error)

      await expect(decksApi.updateDeck(1, 'Hacked', null)).rejects.toThrow('Unauthorized')
    })
  })

  describe('deleteDeck', () => {
    it('should call DELETE /decks/:id', async () => {
      const mockResponse = { data: { message: 'Deck deleted' } }
      mockDelete.mockResolvedValue(mockResponse)

      const result = await decksApi.deleteDeck(1)

      expect(mockDelete).toHaveBeenCalledWith('/decks/1')
      expect(result).toEqual(mockResponse)
    })

    it('should handle delete non-existent deck', async () => {
      const error = new Error('Deck not found')
      error.response = { status: 404 }
      mockDelete.mockRejectedValue(error)

      await expect(decksApi.deleteDeck(999)).rejects.toThrow('Deck not found')
    })
  })

  describe('addCardToDeck', () => {
    it('should call POST /decks/:id/cards with card data', async () => {
      const mockResponse = { data: { id: 1, cards: [{ id: 100, count: 1 }] } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await decksApi.addCardToDeck(1, 100, 2)

      expect(mockPost).toHaveBeenCalledWith('/decks/1/cards', { cardId: 100, quantity: 2 })
      expect(result).toEqual(mockResponse)
    })

    it('should handle deck full error', async () => {
      const error = new Error('Deck is full')
      error.response = { status: 400, data: { message: 'Maximum 30 cards allowed' } }
      mockPost.mockRejectedValue(error)

      await expect(decksApi.addCardToDeck(1, 100, 1)).rejects.toThrow('Deck is full')
    })
  })

  describe('removeCardFromDeck', () => {
    it('should call DELETE /decks/:id/cards/:cardId', async () => {
      const mockResponse = { data: { id: 1, cards: [] } }
      mockDelete.mockResolvedValue(mockResponse)

      const result = await decksApi.removeCardFromDeck(1, 100)

      expect(mockDelete).toHaveBeenCalledWith('/decks/1/cards/100')
      expect(result).toEqual(mockResponse)
    })
  })

  describe('validateDeck', () => {
    it('should call GET /decks/:id/validate', async () => {
      const mockResponse = { data: { valid: true, errors: [] } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await decksApi.validateDeck(1)

      expect(mockGet).toHaveBeenCalledWith('/decks/1/validate')
      expect(result).toEqual(mockResponse)
    })

    it('should return invalid for incomplete deck', async () => {
      const mockResponse = { data: { valid: false, errors: ['Not enough cards'] } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await decksApi.validateDeck(1)

      expect(result.data.valid).toBe(false)
    })
  })

  describe('getBestDecks', () => {
    it('should call GET /decks/best', async () => {
      const mockResponse = { data: [{ id: 1, name: 'Top Deck', winRate: 75 }] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await decksApi.getBestDecks()

      expect(mockGet).toHaveBeenCalledWith('/decks/best')
      expect(result).toEqual(mockResponse)
    })
  })
})
