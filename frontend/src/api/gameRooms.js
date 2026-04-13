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

export const gameRoomsApi = {
  getAvailableRooms() {
    return api.get('/rooms/available')
  },

  getRoomById(roomId) {
    return api.get(`/rooms/${roomId}`)
  },

  getRoomByCode(roomCode) {
    return api.get(`/rooms/code/${roomCode}`)
  },

  createRoom(name, isPrivate, password, turnTimeLimit) {
    return api.post('/rooms', { name, isPrivate, password, turnTimeLimit })
  },

  joinRoom(roomId, password) {
    return api.post(`/rooms/${roomId}/join`, { password })
  },

  joinRoomByCode(roomCode, password) {
    return api.post('/rooms/join', { roomCode, password })
  },

  leaveRoom(roomId) {
    return api.post(`/rooms/${roomId}/leave`)
  },

  startGame(roomId) {
    return api.post(`/rooms/${roomId}/start`)
  },

  getMyActiveGame() {
    return api.get('/rooms/my/active')
  }
}

export default gameRoomsApi
