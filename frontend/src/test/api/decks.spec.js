import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { decksApi } from '../../api/decks'
import { api } from '../../api/auth'

vi.mock('../../api/auth', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

describe('Decks API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  describe('getByPlayer', () => {
    it('应该获取玩家的所有卡组', async () => {
      const mockResponse = {
        data: [
          { id: 1, name: 'Aggro Deck', cardCount: 40 },
          { id: 2, name: 'Control Deck', cardCount: 40 }
        ]
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await decksApi.getByPlayer(1)

      expect(api.get).toHaveBeenCalledWith('/decks/player/1')
      expect(result.data).toHaveLength(2)
      expect(result.data[0].name).toBe('Aggro Deck')
    })

    it('获取卡组失败应该抛出错误', async () => {
      const mockError = new Error('Player not found')
      api.get.mockRejectedValueOnce(mockError)

      await expect(decksApi.getByPlayer(999)).rejects.toThrow('Player not found')
    })
  })

  describe('getById', () => {
    it('应该根据ID获取卡组详情', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'Test Deck',
          cardCount: 40,
          cards: [
            { card: { id: 1, name: 'Fire Dragon' }, quantity: 2 }
          ]
        }
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await decksApi.getById(1)

      expect(api.get).toHaveBeenCalledWith('/decks/1')
      expect(result.data.name).toBe('Test Deck')
      expect(result.data.cards).toHaveLength(1)
    })

    it('卡组不存在应该抛出错误', async () => {
      const mockError = new Error('Deck not found')
      api.get.mockRejectedValueOnce(mockError)

      await expect(decksApi.getById(999)).rejects.toThrow('Deck not found')
    })
  })

  describe('create', () => {
    it('应该创建新卡组', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'New Deck',
          description: 'A new deck',
          cardCount: 0
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await decksApi.create(1, 'New Deck', 'A new deck')

      expect(api.post).toHaveBeenCalledWith('/decks/player/1', {
        name: 'New Deck',
        description: 'A new deck'
      })
      expect(result.data.id).toBe(1)
    })

    it('创建卡组失败应该抛出错误', async () => {
      const mockError = new Error('Max decks reached')
      api.post.mockRejectedValueOnce(mockError)

      await expect(decksApi.create(1, 'Deck', 'Desc')).rejects.toThrow('Max decks reached')
    })
  })

  describe('update', () => {
    it('应该更新卡组', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'Updated Deck',
          description: 'Updated description'
        }
      }
      api.put.mockResolvedValueOnce(mockResponse)

      const result = await decksApi.update(1, 1, 'Updated Deck', 'Updated description')

      expect(api.put).toHaveBeenCalledWith('/decks/1', {
        playerId: 1,
        name: 'Updated Deck',
        description: 'Updated description'
      })
      expect(result.data.name).toBe('Updated Deck')
    })

    it('更新卡组失败应该抛出错误', async () => {
      const mockError = new Error('Not authorized')
      api.put.mockRejectedValueOnce(mockError)

      await expect(decksApi.update(1, 2, 'Name', 'Desc')).rejects.toThrow('Not authorized')
    })
  })

  describe('delete', () => {
    it('应该删除卡组', async () => {
      const mockResponse = { data: { message: 'Deck deleted' } }
      api.delete.mockResolvedValueOnce(mockResponse)

      const result = await decksApi.delete(1, 1)

      expect(api.delete).toHaveBeenCalledWith('/decks/1', { data: { playerId: 1 } })
      expect(result.data.message).toBe('Deck deleted')
    })

    it('删除卡组失败应该抛出错误', async () => {
      const mockError = new Error('Deck not found')
      api.delete.mockRejectedValueOnce(mockError)

      await expect(decksApi.delete(999, 1)).rejects.toThrow('Deck not found')
    })
  })

  describe('addCard', () => {
    it('应该添加卡牌到卡组', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'Test Deck',
          cardCount: 41
        }
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await decksApi.addCard(1, 1, 100, 2)

      expect(api.post).toHaveBeenCalledWith('/decks/1/cards', {
        playerId: 1,
        cardId: 100,
        quantity: 2
      })
      expect(result.data.cardCount).toBe(41)
    })

    it('添加卡牌失败应该抛出错误', async () => {
      const mockError = new Error('Card limit reached')
      api.post.mockRejectedValueOnce(mockError)

      await expect(decksApi.addCard(1, 1, 100, 1)).rejects.toThrow('Card limit reached')
    })
  })

  describe('removeCard', () => {
    it('应该从卡组移除卡牌', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'Test Deck',
          cardCount: 39
        }
      }
      api.delete.mockResolvedValueOnce(mockResponse)

      const result = await decksApi.removeCard(1, 1, 100)

      expect(api.delete).toHaveBeenCalledWith('/decks/1/cards/100', { data: { playerId: 1 } })
      expect(result.data.cardCount).toBe(39)
    })

    it('移除卡牌失败应该抛出错误', async () => {
      const mockError = new Error('Card not in deck')
      api.delete.mockRejectedValueOnce(mockError)

      await expect(decksApi.removeCard(1, 1, 999)).rejects.toThrow('Card not in deck')
    })
  })

  describe('validate', () => {
    it('应该验证卡组', async () => {
      const mockResponse = {
        data: {
          valid: true,
          errors: []
        }
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await decksApi.validate(1)

      expect(api.get).toHaveBeenCalledWith('/decks/1/validate')
      expect(result.data.valid).toBe(true)
    })

    it('验证失败应该返回错误', async () => {
      const mockResponse = {
        data: {
          valid: false,
          errors: ['Deck must have exactly 40 cards']
        }
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await decksApi.validate(1)

      expect(result.data.valid).toBe(false)
      expect(result.data.errors).toHaveLength(1)
    })
  })

  describe('getBestDecks', () => {
    it('应该获取玩家最佳卡组', async () => {
      const mockResponse = {
        data: [
          { id: 1, name: 'Best Deck', wins: 100, losses: 20 }
        ]
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await decksApi.getBestDecks(1)

      expect(api.get).toHaveBeenCalledWith('/decks/player/1/best')
      expect(result.data[0].wins).toBe(100)
    })
  })
})
