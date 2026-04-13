import { vi, describe, beforeEach, it, expect } from 'vitest'
import { getMockAxios, initRequestInterceptor } from '../setup'

describe('API Module Tests', () => {
  let mockApi

  beforeEach(() => {
    mockApi = getMockAxios()
    initRequestInterceptor()
  })

  describe('Auth API', () => {
    it('should call login endpoint with correct credentials', async () => {
      const credentials = { username: 'testuser', password: 'password123' }
      const mockResponse = { data: { token: 'jwt-token', player: { id: 1 } } }
      mockApi.post.mockResolvedValue(mockResponse)

      const result = await mockApi.post('/auth/login', credentials)

      expect(mockApi.post).toHaveBeenCalledWith('/auth/login', credentials)
      expect(result).toEqual(mockResponse)
    })

    it('should call register endpoint with user data', async () => {
      const userData = {
        username: 'newuser',
        password: 'password123',
        email: 'test@example.com',
        displayName: 'Test User'
      }
      mockApi.post.mockResolvedValue({ data: { success: true } })

      await mockApi.post('/auth/register', userData)

      expect(mockApi.post).toHaveBeenCalledWith('/auth/register', userData)
    })

    it('should call current user endpoint', async () => {
      mockApi.get.mockResolvedValue({ data: { id: 1, username: 'testuser' } })

      await mockApi.get('/auth/me')

      expect(mockApi.get).toHaveBeenCalledWith('/auth/me')
    })

    it('should handle auth API errors', async () => {
      const errorMessage = 'Invalid credentials'
      mockApi.post.mockRejectedValue(new Error(errorMessage))

      await expect(mockApi.post('/auth/login', {})).rejects.toThrow(errorMessage)
    })
  })

  describe('Cards API', () => {
    it('should fetch all cards', async () => {
      const mockCards = [
        { id: 1, name: 'Fireball', cardType: 'SPELL', manaCost: 4 },
        { id: 2, name: 'Warrior', cardType: 'CREATURE', manaCost: 3 }
      ]
      mockApi.get.mockResolvedValue({ data: mockCards })

      const result = await mockApi.get('/cards')

      expect(mockApi.get).toHaveBeenCalledWith('/cards')
      expect(result.data).toHaveLength(2)
      expect(result.data[0].name).toBe('Fireball')
    })

    it('should fetch card by id', async () => {
      const cardId = 1
      const mockCard = { id: 1, name: 'Fireball', cardType: 'SPELL' }
      mockApi.get.mockResolvedValue({ data: mockCard })

      await mockApi.get(`/cards/${cardId}`)

      expect(mockApi.get).toHaveBeenCalledWith('/cards/1')
    })

    it('should fetch cards by filters', async () => {
      const filters = { cardType: 'CREATURE', rarity: 'RARE', minMana: 2, maxMana: 5 }
      mockApi.get.mockResolvedValue({ data: [], total: 0 })

      await mockApi.get('/cards/filters', { params: filters })

      expect(mockApi.get).toHaveBeenCalledWith('/cards/filters', { params: filters })
    })

    it('should search cards by keyword', async () => {
      const keyword = 'fire'
      mockApi.get.mockResolvedValue({ data: [] })

      await mockApi.get('/cards/search', { params: { keyword } })

      expect(mockApi.get).toHaveBeenCalledWith('/cards/search', { params: { keyword: 'fire' } })
    })

    it('should open card pack', async () => {
      const packSize = 5
      mockApi.post.mockResolvedValue({ data: [] })

      await mockApi.post('/cards/pack', { packSize })

      expect(mockApi.post).toHaveBeenCalledWith('/cards/pack', { packSize: 5 })
    })
  })

  describe('Decks API', () => {
    it('should fetch player decks', async () => {
      const playerId = 1
      const mockDecks = [
        { id: 1, name: 'Fire Deck', cardCount: 30 },
        { id: 2, name: 'Ice Deck', cardCount: 28 }
      ]
      mockApi.get.mockResolvedValue({ data: mockDecks })

      await mockApi.get(`/decks/player/${playerId}`)

      expect(mockApi.get).toHaveBeenCalledWith('/decks/player/1')
    })

    it('should fetch deck by id', async () => {
      const deckId = 1
      mockApi.get.mockResolvedValue({ data: { id: 1, name: 'Test Deck' } })

      await mockApi.get(`/decks/${deckId}`)

      expect(mockApi.get).toHaveBeenCalledWith('/decks/1')
    })

    it('should create new deck', async () => {
      const deckData = { name: 'New Deck', description: 'My new deck' }
      mockApi.post.mockResolvedValue({ data: { id: 3, ...deckData } })

      await mockApi.post('/decks', deckData)

      expect(mockApi.post).toHaveBeenCalledWith('/decks', deckData)
    })

    it('should update deck', async () => {
      const deckId = 1
      const updateData = { name: 'Updated Name' }
      mockApi.put.mockResolvedValue({ data: { success: true } })

      await mockApi.put(`/decks/${deckId}`, updateData)

      expect(mockApi.put).toHaveBeenCalledWith('/decks/1', { name: 'Updated Name' })
    })

    it('should add card to deck', async () => {
      const deckId = 1
      const cardData = { cardId: 5, quantity: 2 }
      mockApi.post.mockResolvedValue({ data: { success: true } })

      await mockApi.post(`/decks/${deckId}/cards`, cardData)

      expect(mockApi.post).toHaveBeenCalledWith('/decks/1/cards', { cardId: 5, quantity: 2 })
    })

    it('should remove card from deck', async () => {
      const deckId = 1
      const cardId = 5
      mockApi.delete.mockResolvedValue({ data: { success: true } })

      await mockApi.delete(`/decks/${deckId}/cards/${cardId}`)

      expect(mockApi.delete).toHaveBeenCalledWith('/decks/1/cards/5')
    })

    it('should delete deck', async () => {
      const deckId = 1
      mockApi.delete.mockResolvedValue({ data: { success: true } })

      await mockApi.delete(`/decks/${deckId}`)

      expect(mockApi.delete).toHaveBeenCalledWith('/decks/1')
    })
  })

  describe('Game Rooms API', () => {
    it('should fetch all game rooms', async () => {
      const mockRooms = [
        { id: 1, name: 'Room 1', host: 'player1', status: 'WAITING' },
        { id: 2, name: 'Room 2', host: 'player2', status: 'IN_GAME' }
      ]
      mockApi.get.mockResolvedValue({ data: mockRooms })

      await mockApi.get('/game-rooms')

      expect(mockApi.get).toHaveBeenCalledWith('/game-rooms')
      expect(mockRooms).toHaveLength(2)
    })

    it('should fetch room by id', async () => {
      const roomId = 1
      mockApi.get.mockResolvedValue({ data: { id: 1, name: 'Room 1' } })

      await mockApi.get(`/game-rooms/${roomId}`)

      expect(mockApi.get).toHaveBeenCalledWith('/game-rooms/1')
    })

    it('should create game room', async () => {
      const roomData = { name: 'Battle Arena', maxPlayers: 2 }
      mockApi.post.mockResolvedValue({ data: { id: 3, ...roomData } })

      await mockApi.post('/game-rooms', roomData)

      expect(mockApi.post).toHaveBeenCalledWith('/game-rooms', roomData)
    })

    it('should join game room', async () => {
      const roomId = 1
      mockApi.post.mockResolvedValue({ data: { success: true } })

      await mockApi.post(`/game-rooms/${roomId}/join`)

      expect(mockApi.post).toHaveBeenCalledWith('/game-rooms/1/join')
    })

    it('should leave game room', async () => {
      const roomId = 1
      mockApi.post.mockResolvedValue({ data: { success: true } })

      await mockApi.post(`/game-rooms/${roomId}/leave`)

      expect(mockApi.post).toHaveBeenCalledWith('/game-rooms/1/leave')
    })

    it('should start game', async () => {
      const roomId = 1
      mockApi.post.mockResolvedValue({ data: { gameId: 'game-123' } })

      await mockApi.post(`/game-rooms/${roomId}/start`)

      expect(mockApi.post).toHaveBeenCalledWith('/game-rooms/1/start')
    })
  })

  describe('API Request Interceptor', () => {
    it('should add auth token to requests when available', () => {
      localStorage.getItem.mockReturnValue('test-token')
      const config = { headers: {} }
      
      const axios = getMockAxios()
      const interceptor = axios.interceptors.request.use.mock.results[0].value
      const result = interceptor(config)

      expect(result.headers.Authorization).toBe('Bearer test-token')
    })

    it('should not add auth token when not available', () => {
      localStorage.getItem.mockReturnValue(null)
      const config = { headers: {} }
      
      const axios = getMockAxios()
      const interceptor = axios.interceptors.request.use.mock.results[0].value
      const result = interceptor(config)

      expect(result.headers.Authorization).toBeUndefined()
    })
  })
})
