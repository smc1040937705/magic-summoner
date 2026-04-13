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

export const decksApi = {
  getMyDecks() {
    return api.get('/decks/my')
  },

  getDeckById(deckId) {
    return api.get(`/decks/${deckId}`)
  },

  createDeck(name, description) {
    return api.post('/decks', { name, description })
  },

  updateDeck(deckId, name, description) {
    return api.put(`/decks/${deckId}`, { name, description })
  },

  deleteDeck(deckId) {
    return api.delete(`/decks/${deckId}`)
  },

  addCardToDeck(deckId, cardId, quantity) {
    return api.post(`/decks/${deckId}/cards`, { cardId, quantity })
  },

  removeCardFromDeck(deckId, cardId) {
    return api.delete(`/decks/${deckId}/cards/${cardId}`)
  },

  validateDeck(deckId) {
    return api.get(`/decks/${deckId}/validate`)
  },

  getBestDecks() {
    return api.get('/decks/best')
  }
}

export default decksApi
