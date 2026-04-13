import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const cardsApi = {
  getAllCards() {
    return api.get('/cards')
  },

  getCardById(id) {
    return api.get(`/cards/${id}`)
  },

  getCardsByType(cardType) {
    return api.get('/cards/type', { params: { cardType } })
  },

  getCardsByElement(elementType) {
    return api.get('/cards/element', { params: { elementType } })
  },

  getCardsByRarity(rarity) {
    return api.get('/cards/rarity', { params: { rarity } })
  },

  searchCards(keyword, page = 0, size = 20) {
    return api.get('/cards/search', { params: { keyword, page, size } })
  },

  getCardsByFilters(filters, page = 0, size = 20) {
    return api.get('/cards/filter', { params: { ...filters, page, size } })
  },

  openCardPack() {
    return api.post('/cards/pack/open')
  },

  openSingleCard() {
    return api.post('/cards/single/open')
  }
}

export default cardsApi
