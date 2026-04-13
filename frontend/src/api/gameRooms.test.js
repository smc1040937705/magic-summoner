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
import { gameRoomsApi } from './gameRooms'

describe('GameRooms API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getAvailableRooms', () => {
    it('should call GET /rooms/available', async () => {
      const mockResponse = { data: [{ id: 1, name: 'Room 1' }] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.getAvailableRooms()

      expect(mockGet).toHaveBeenCalledWith('/rooms/available')
      expect(result).toEqual(mockResponse)
    })

    it('should return empty array when no rooms available', async () => {
      const mockResponse = { data: [] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.getAvailableRooms()

      expect(result.data).toEqual([])
    })
  })

  describe('getRoomById', () => {
    it('should call GET /rooms/:id', async () => {
      const mockResponse = { data: { id: 1, name: 'Room 1', host: { id: 1 } } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.getRoomById(1)

      expect(mockGet).toHaveBeenCalledWith('/rooms/1')
      expect(result).toEqual(mockResponse)
    })

    it('should handle room not found', async () => {
      const error = new Error('Room not found')
      error.response = { status: 404 }
      mockGet.mockRejectedValue(error)

      await expect(gameRoomsApi.getRoomById(999)).rejects.toThrow('Room not found')
    })
  })

  describe('getRoomByCode', () => {
    it('should call GET /rooms/code/:code', async () => {
      const mockResponse = { data: { id: 1, roomCode: 'ABC123' } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.getRoomByCode('ABC123')

      expect(mockGet).toHaveBeenCalledWith('/rooms/code/ABC123')
      expect(result).toEqual(mockResponse)
    })

    it('should handle invalid room code', async () => {
      const error = new Error('Room not found')
      error.response = { status: 404 }
      mockGet.mockRejectedValue(error)

      await expect(gameRoomsApi.getRoomByCode('INVALID')).rejects.toThrow('Room not found')
    })
  })

  describe('createRoom', () => {
    it('should call POST /rooms with room data', async () => {
      const mockResponse = { data: { id: 1, name: 'New Room', isPrivate: false } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.createRoom('New Room', false, null, 120)

      expect(mockPost).toHaveBeenCalledWith('/rooms', { name: 'New Room', isPrivate: false, password: null, turnTimeLimit: 120 })
      expect(result).toEqual(mockResponse)
    })

    it('should create private room with password', async () => {
      const mockResponse = { data: { id: 1, name: 'Private Room', isPrivate: true } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.createRoom('Private Room', true, 'secret123', 120)

      expect(mockPost).toHaveBeenCalledWith('/rooms', { name: 'Private Room', isPrivate: true, password: 'secret123', turnTimeLimit: 120 })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('joinRoom', () => {
    it('should call POST /rooms/:id/join with password', async () => {
      const mockResponse = { data: { id: 1, guest: { id: 2 } } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.joinRoom(1, 'password123')

      expect(mockPost).toHaveBeenCalledWith('/rooms/1/join', { password: 'password123' })
      expect(result).toEqual(mockResponse)
    })

    it('should join public room without password', async () => {
      const mockResponse = { data: { id: 1, guest: { id: 2 } } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.joinRoom(1, null)

      expect(mockPost).toHaveBeenCalledWith('/rooms/1/join', { password: null })
      expect(result).toEqual(mockResponse)
    })

    it('should handle wrong password', async () => {
      const error = new Error('Invalid password')
      error.response = { status: 403 }
      mockPost.mockRejectedValue(error)

      await expect(gameRoomsApi.joinRoom(1, 'wrongpass')).rejects.toThrow('Invalid password')
    })

    it('should handle room full', async () => {
      const error = new Error('Room is full')
      error.response = { status: 400 }
      mockPost.mockRejectedValue(error)

      await expect(gameRoomsApi.joinRoom(1, null)).rejects.toThrow('Room is full')
    })
  })

  describe('joinRoomByCode', () => {
    it('should call POST /rooms/join with room code', async () => {
      const mockResponse = { data: { id: 1, roomCode: 'ABC123', guest: { id: 2 } } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.joinRoomByCode('ABC123', null)

      expect(mockPost).toHaveBeenCalledWith('/rooms/join', { roomCode: 'ABC123', password: null })
      expect(result).toEqual(mockResponse)
    })

    it('should join private room by code with password', async () => {
      const mockResponse = { data: { id: 1, roomCode: 'ABC123', isPrivate: true } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.joinRoomByCode('ABC123', 'secret123')

      expect(mockPost).toHaveBeenCalledWith('/rooms/join', { roomCode: 'ABC123', password: 'secret123' })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('leaveRoom', () => {
    it('should call POST /rooms/:id/leave', async () => {
      const mockResponse = { data: { id: 1, guest: null } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.leaveRoom(1)

      expect(mockPost).toHaveBeenCalledWith('/rooms/1/leave')
      expect(result).toEqual(mockResponse)
    })

    it('should handle leave room as host', async () => {
      const mockResponse = { data: { id: 1, status: 'ABANDONED' } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.leaveRoom(1)

      expect(mockPost).toHaveBeenCalledWith('/rooms/1/leave')
      expect(result).toEqual(mockResponse)
    })
  })

  describe('startGame', () => {
    it('should call POST /rooms/:id/start', async () => {
      const mockResponse = { data: { id: 1, status: 'PLAYING' } }
      mockPost.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.startGame(1)

      expect(mockPost).toHaveBeenCalledWith('/rooms/1/start')
      expect(result).toEqual(mockResponse)
    })

    it('should handle not host trying to start', async () => {
      const error = new Error('Only host can start')
      error.response = { status: 403 }
      mockPost.mockRejectedValue(error)

      await expect(gameRoomsApi.startGame(1)).rejects.toThrow('Only host can start')
    })

    it('should handle room not full', async () => {
      const error = new Error('Room is not full')
      error.response = { status: 400 }
      mockPost.mockRejectedValue(error)

      await expect(gameRoomsApi.startGame(1)).rejects.toThrow('Room is not full')
    })
  })

  describe('getMyActiveGame', () => {
    it('should call GET /rooms/my/active', async () => {
      const mockResponse = { data: { id: 1, status: 'PLAYING' } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.getMyActiveGame()

      expect(mockGet).toHaveBeenCalledWith('/rooms/my/active')
      expect(result).toEqual(mockResponse)
    })

    it('should return null when no active game', async () => {
      const mockResponse = { data: null }
      mockGet.mockResolvedValue(mockResponse)

      const result = await gameRoomsApi.getMyActiveGame()

      expect(result.data).toBeNull()
    })
  })
})
