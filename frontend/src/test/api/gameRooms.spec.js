import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { gameRoomsApi } from '../../api/gameRooms'
import { api } from '../../api/auth'

vi.mock('../../api/auth', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

describe('GameRooms API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  describe('getAvailable', () => {
    it('应该获取可用房间列表', async () => {
      const mockResponse = {
        data: [
          { id: 1, name: 'Room 1', status: 'WAITING' },
          { id: 2, name: 'Room 2', status: 'WAITING' }
        ]
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.getAvailable()

      expect(api.get).toHaveBeenCalledWith('/rooms/available')
      expect(result.data).toHaveLength(2)
      expect(result.data[0].status).toBe('WAITING')
    })

    it('获取房间失败应该抛出错误', async () => {
      const mockError = new Error('Network error')
      api.get.mockRejectedValueOnce(mockError)

      await expect(gameRoomsApi.getAvailable()).rejects.toThrow('Network error')
    })
  })

  describe('getById', () => {
    it('应该根据ID获取房间详情', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'Test Room',
          roomCode: 'ABC123',
          status: 'WAITING',
          host: { id: 1, username: 'host' }
        }
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.getById(1)

      expect(api.get).toHaveBeenCalledWith('/rooms/1')
      expect(result.data.roomCode).toBe('ABC123')
    })

    it('房间不存在应该抛出错误', async () => {
      const mockError = new Error('Room not found')
      api.get.mockRejectedValueOnce(mockError)

      await expect(gameRoomsApi.getById(999)).rejects.toThrow('Room not found')
    })
  })

  describe('getByCode', () => {
    it('应该根据房间码获取房间', async () => {
      const mockResponse = {
        data: {
          id: 1,
          roomCode: 'ABC123',
          name: 'Test Room'
        }
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.getByCode('ABC123')

      expect(api.get).toHaveBeenCalledWith('/rooms/code/ABC123')
      expect(result.data.roomCode).toBe('ABC123')
    })

    it('房间码无效应该抛出错误', async () => {
      const mockError = new Error('Invalid room code')
      api.get.mockRejectedValueOnce(mockError)

      await expect(gameRoomsApi.getByCode('INVALID')).rejects.toThrow('Invalid room code')
    })
  })

  describe('getByStatus', () => {
    it('应该根据状态获取房间列表', async () => {
      const mockResponse = {
        data: [
          { id: 1, name: 'Room 1', status: 'PLAYING' },
          { id: 2, name: 'Room 2', status: 'PLAYING' }
        ]
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.getByStatus('PLAYING')

      expect(api.get).toHaveBeenCalledWith('/rooms/status/PLAYING')
      expect(result.data).toHaveLength(2)
    })
  })

  describe('create', () => {
    it('应该创建公开房间', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'New Room',
          roomCode: 'XYZ789',
          isPrivate: false,
          status: 'WAITING'
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.create(1, 'New Room', false, null, 120)

      expect(api.post).toHaveBeenCalledWith('/rooms', {
        hostId: 1,
        name: 'New Room',
        isPrivate: false,
        password: null,
        turnTimeLimit: 120
      })
      expect(result.data.roomCode).toBe('XYZ789')
    })

    it('应该创建私有房间', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'Private Room',
          roomCode: 'PRIV01',
          isPrivate: true
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.create(1, 'Private Room', true, 'secret123', 90)

      expect(api.post).toHaveBeenCalledWith('/rooms', {
        hostId: 1,
        name: 'Private Room',
        isPrivate: true,
        password: 'secret123',
        turnTimeLimit: 90
      })
      expect(result.data.isPrivate).toBe(true)
    })

    it('创建房间失败应该抛出错误', async () => {
      const mockError = new Error('Player not found')
      api.post.mockRejectedValueOnce(mockError)

      await expect(gameRoomsApi.create(999, 'Room', false, null, 120)).rejects.toThrow('Player not found')
    })
  })

  describe('join', () => {
    it('应该加入房间', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'Test Room',
          guest: { id: 2, username: 'guest' }
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.join(1, 2, null)

      expect(api.post).toHaveBeenCalledWith('/rooms/1/join', {
        playerId: 2,
        password: null
      })
      expect(result.data.guest.username).toBe('guest')
    })

    it('加入私有房间需要密码', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'Private Room',
          guest: { id: 2, username: 'guest' }
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.join(1, 2, 'secret123')

      expect(api.post).toHaveBeenCalledWith('/rooms/1/join', {
        playerId: 2,
        password: 'secret123'
      })
    })

    it('加入房间失败应该抛出错误', async () => {
      const mockError = new Error('Room is full')
      api.post.mockRejectedValueOnce(mockError)

      await expect(gameRoomsApi.join(1, 2, null)).rejects.toThrow('Room is full')
    })

    it('密码错误应该抛出错误', async () => {
      const mockError = new Error('Invalid password')
      api.post.mockRejectedValueOnce(mockError)

      await expect(gameRoomsApi.join(1, 2, 'wrong')).rejects.toThrow('Invalid password')
    })
  })

  describe('joinByCode', () => {
    it('应该通过房间码加入房间', async () => {
      const mockResponse = {
        data: {
          id: 1,
          roomCode: 'ABC123',
          guest: { id: 2, username: 'guest' }
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.joinByCode('ABC123', 2, null)

      expect(api.post).toHaveBeenCalledWith('/rooms/code/ABC123/join', {
        playerId: 2,
        password: null
      })
      expect(result.data.roomCode).toBe('ABC123')
    })
  })

  describe('leave', () => {
    it('应该离开房间', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'Test Room',
          status: 'ABANDONED'
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.leave(1, 1)

      expect(api.post).toHaveBeenCalledWith('/rooms/1/leave', { playerId: 1 })
      expect(result.data.status).toBe('ABANDONED')
    })

    it('离开房间失败应该抛出错误', async () => {
      const mockError = new Error('Room not found')
      api.post.mockRejectedValueOnce(mockError)

      await expect(gameRoomsApi.leave(999, 1)).rejects.toThrow('Room not found')
    })
  })

  describe('startGame', () => {
    it('应该开始游戏', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'Test Room',
          status: 'PLAYING'
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.startGame(1, 1)

      expect(api.post).toHaveBeenCalledWith('/rooms/1/start', { hostId: 1 })
      expect(result.data.status).toBe('PLAYING')
    })

    it('开始游戏失败应该抛出错误', async () => {
      const mockError = new Error('Only host can start')
      api.post.mockRejectedValueOnce(mockError)

      await expect(gameRoomsApi.startGame(1, 2)).rejects.toThrow('Only host can start')
    })

    it('房间未满应该抛出错误', async () => {
      const mockError = new Error('Room is not full')
      api.post.mockRejectedValueOnce(mockError)

      await expect(gameRoomsApi.startGame(1, 1)).rejects.toThrow('Room is not full')
    })
  })

  describe('endGame', () => {
    it('应该结束游戏并指定胜者', async () => {
      const mockResponse = {
        data: {
          id: 1,
          status: 'FINISHED',
          winner: { id: 1, username: 'winner' }
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.endGame(1, 1)

      expect(api.post).toHaveBeenCalledWith('/rooms/1/end', { winnerId: 1 })
      expect(result.data.status).toBe('FINISHED')
    })

    it('应该结束游戏无胜者（平局）', async () => {
      const mockResponse = {
        data: {
          id: 1,
          status: 'FINISHED',
          winner: null
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.endGame(1, null)

      expect(api.post).toHaveBeenCalledWith('/rooms/1/end', { winnerId: null })
      expect(result.data.winner).toBeNull()
    })
  })

  describe('getActiveGame', () => {
    it('应该获取玩家当前活跃游戏', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'Active Game',
          status: 'PLAYING'
        }
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.getActiveGame(1)

      expect(api.get).toHaveBeenCalledWith('/rooms/active/1')
      expect(result.data.status).toBe('PLAYING')
    })

    it('无活跃游戏应该返回null', async () => {
      const mockResponse = { data: null }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await gameRoomsApi.getActiveGame(1)

      expect(result.data).toBeNull()
    })
  })
})
