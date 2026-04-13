import { api } from './auth'

export const gameRoomsApi = {
  getAvailable: () => 
    api.get('/rooms/available'),
  
  getById: (roomId) => 
    api.get(`/rooms/${roomId}`),
  
  getByCode: (roomCode) => 
    api.get(`/rooms/code/${roomCode}`),
  
  getByStatus: (status) => 
    api.get(`/rooms/status/${status}`),
  
  create: (hostId, name, isPrivate, password, turnTimeLimit) => 
    api.post('/rooms', { hostId, name, isPrivate, password, turnTimeLimit }),
  
  join: (roomId, playerId, password) => 
    api.post(`/rooms/${roomId}/join`, { playerId, password }),
  
  joinByCode: (roomCode, playerId, password) => 
    api.post(`/rooms/code/${roomCode}/join`, { playerId, password }),
  
  leave: (roomId, playerId) => 
    api.post(`/rooms/${roomId}/leave`, { playerId }),
  
  startGame: (roomId, hostId) => 
    api.post(`/rooms/${roomId}/start`, { hostId }),
  
  endGame: (roomId, winnerId) => 
    api.post(`/rooms/${roomId}/end`, { winnerId }),
  
  getActiveGame: (playerId) => 
    api.get(`/rooms/active/${playerId}`)
}
