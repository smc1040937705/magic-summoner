import { api } from './auth'

export const decksApi = {
  getByPlayer: (playerId) => 
    api.get(`/decks/player/${playerId}`),
  
  getById: (deckId) => 
    api.get(`/decks/${deckId}`),
  
  create: (playerId, name, description) => 
    api.post(`/decks/player/${playerId}`, { name, description }),
  
  update: (deckId, playerId, name, description) => 
    api.put(`/decks/${deckId}`, { playerId, name, description }),
  
  delete: (deckId, playerId) => 
    api.delete(`/decks/${deckId}`, { data: { playerId } }),
  
  addCard: (deckId, playerId, cardId, quantity) => 
    api.post(`/decks/${deckId}/cards`, { playerId, cardId, quantity }),
  
  removeCard: (deckId, playerId, cardId) => 
    api.delete(`/decks/${deckId}/cards/${cardId}`, { data: { playerId } }),
  
  validate: (deckId) => 
    api.get(`/decks/${deckId}/validate`),
  
  getBestDecks: (playerId) => 
    api.get(`/decks/player/${playerId}/best`)
}
