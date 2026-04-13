import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { cardsApi } from '../../api/cards'
import { api } from '../../api/auth'

vi.mock('../../api/auth', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

describe('Cards API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  describe('getAll', () => {
    it('应该获取所有卡牌', async () => {
      const mockResponse = {
        data: [
          { id: 1, name: 'Fire Dragon', cardType: 'CREATURE' },
          { id: 2, name: 'Ice Golem', cardType: 'CREATURE' }
        ]
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await cardsApi.getAll()

      expect(api.get).toHaveBeenCalledWith('/cards')
      expect(result.data).toHaveLength(2)
      expect(result.data[0].name).toBe('Fire Dragon')
    })

    it('获取卡牌失败应该抛出错误', async () => {
      const mockError = new Error('Network error')
      api.get.mockRejectedValueOnce(mockError)

      await expect(cardsApi.getAll()).rejects.toThrow('Network error')
    })
  })

  describe('getById', () => {
    it('应该根据ID获取卡牌', async () => {
      const mockResponse = {
        data: { id: 1, name: 'Fire Dragon', attack: 6, health: 5 }
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await cardsApi.getById(1)

      expect(api.get).toHaveBeenCalledWith('/cards/1')
      expect(result.data.name).toBe('Fire Dragon')
    })

    it('卡牌不存在应该抛出错误', async () => {
      const mockError = new Error('Card not found')
      api.get.mockRejectedValueOnce(mockError)

      await expect(cardsApi.getById(999)).rejects.toThrow('Card not found')
    })
  })

  describe('getByType', () => {
    it('应该根据类型获取卡牌', async () => {
      const mockResponse = {
        data: [
          { id: 1, name: 'Fire Dragon', cardType: 'CREATURE' }
        ]
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await cardsApi.getByType('CREATURE')

      expect(api.get).toHaveBeenCalledWith('/cards/type/CREATURE')
      expect(result.data).toHaveLength(1)
    })
  })

  describe('getByElement', () => {
    it('应该根据元素获取卡牌', async () => {
      const mockResponse = {
        data: [
          { id: 1, name: 'Fire Dragon', elementType: 'FIRE' }
        ]
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await cardsApi.getByElement('FIRE')

      expect(api.get).toHaveBeenCalledWith('/cards/element/FIRE')
      expect(result.data[0].elementType).toBe('FIRE')
    })
  })

  describe('getByRarity', () => {
    it('应该根据稀有度获取卡牌', async () => {
      const mockResponse = {
        data: [
          { id: 1, name: 'Fire Dragon', rarity: 'RARE' }
        ]
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await cardsApi.getByRarity('RARE')

      expect(api.get).toHaveBeenCalledWith('/cards/rarity/RARE')
      expect(result.data[0].rarity).toBe('RARE')
    })
  })

  describe('search', () => {
    it('应该搜索卡牌', async () => {
      const mockResponse = {
        data: {
          content: [
            { id: 1, name: 'Fire Dragon' }
          ],
          totalElements: 1
        }
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await cardsApi.search('Dragon', 0, 20)

      expect(api.get).toHaveBeenCalledWith('/cards/search', {
        params: { keyword: 'Dragon', page: 0, size: 20 }
      })
      expect(result.data.content).toHaveLength(1)
    })

    it('应该使用默认分页参数', async () => {
      const mockResponse = { data: { content: [] } }
      api.get.mockResolvedValueOnce(mockResponse)

      await cardsApi.search('Dragon')

      expect(api.get).toHaveBeenCalledWith('/cards/search', {
        params: { keyword: 'Dragon', page: 0, size: 20 }
      })
    })
  })

  describe('getFiltered', () => {
    it('应该根据过滤器获取卡牌', async () => {
      const mockResponse = {
        data: {
          content: [
            { id: 1, name: 'Fire Dragon', cardType: 'CREATURE', elementType: 'FIRE' }
          ]
        }
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const filters = { cardType: 'CREATURE', elementType: 'FIRE', minMana: 1, maxMana: 10 }
      const result = await cardsApi.getFiltered(filters)

      expect(api.get).toHaveBeenCalledWith('/cards/filter', { params: filters })
      expect(result.data.content).toHaveLength(1)
    })
  })

  describe('openPack', () => {
    it('应该打开卡包', async () => {
      const mockResponse = {
        data: [
          { id: 1, name: 'Card 1' },
          { id: 2, name: 'Card 2' },
          { id: 3, name: 'Card 3' },
          { id: 4, name: 'Card 4' },
          { id: 5, name: 'Card 5' }
        ]
      }
      api.post.mockResolvedValueOnce(mockResponse)

      const result = await cardsApi.openPack(5)

      expect(api.post).toHaveBeenCalledWith('/cards/pack', { packSize: 5 })
      expect(result.data).toHaveLength(5)
    })

    it('应该使用默认卡包大小', async () => {
      const mockResponse = { data: [] }
      api.post.mockResolvedValueOnce(mockResponse)

      await cardsApi.openPack()

      expect(api.post).toHaveBeenCalledWith('/cards/pack', { packSize: 5 })
    })
  })

  describe('openSingleCard', () => {
    it('应该打开单张卡牌', async () => {
      const mockResponse = {
        data: { id: 1, name: 'Rare Card', rarity: 'RARE' }
      }
      api.get.mockResolvedValueOnce(mockResponse)

      const result = await cardsApi.openSingleCard()

      expect(api.get).toHaveBeenCalledWith('/cards/pack/single')
      expect(result.data.name).toBe('Rare Card')
    })
  })
})
