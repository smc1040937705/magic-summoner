<template>
  <div class="lobby-page">
    <div class="container">
      <div class="lobby-header">
        <h2>游戏大厅</h2>
        <div class="lobby-actions">
          <button @click="showCreateModal = true" class="primary">创建房间</button>
          <button @click="refreshRooms" class="secondary">刷新</button>
        </div>
      </div>

      <div class="lobby-stats">
        <div class="stat-card">
          <span class="stat-label">在线玩家</span>
          <span class="stat-value">{{ onlineCount }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">进行中</span>
          <span class="stat-value">{{ playingCount }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">等待中</span>
          <span class="stat-value">{{ waitingCount }}</span>
        </div>
      </div>

      <div class="rooms-section">
        <h3>可用房间</h3>
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="rooms.length === 0" class="empty-state">
          <p>暂无可用房间</p>
          <button @click="showCreateModal = true" class="primary">创建第一个房间</button>
        </div>
        <div v-else class="rooms-grid">
          <div 
            v-for="room in rooms" 
            :key="room.id" 
            class="room-card"
            @click="joinRoom(room)"
          >
            <div class="room-header">
              <span class="room-name">{{ room.name }}</span>
              <span :class="['room-status', room.status.toLowerCase()]">
                {{ room.status }}
              </span>
            </div>
            <div class="room-info">
              <div class="host-info">
                <span class="label">房主:</span>
                <span>{{ room.host?.username }}</span>
              </div>
              <div v-if="room.guest" class="guest-info">
                <span class="label">对手:</span>
                <span>{{ room.guest.username }}</span>
              </div>
              <div v-else class="guest-info waiting">
                <span class="label">等待对手加入...</span>
              </div>
            </div>
            <div class="room-code">
              房间码: {{ room.roomCode }}
            </div>
          </div>
        </div>
      </div>

      <div class="join-section">
        <h3>输入房间码加入</h3>
        <div class="join-form">
          <input 
            v-model="joinCode" 
            type="text" 
            placeholder="请输入6位房间码"
            maxlength="6"
          />
          <button @click="joinByCode" class="primary" :disabled="!joinCode">
            加入房间
          </button>
        </div>
      </div>
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>创建房间</h3>
          <button @click="showCreateModal = false" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>房间名称</label>
            <input v-model="newRoom.name" type="text" placeholder="可选，默认用户名+的房间" />
          </div>
          <div class="form-group">
            <label>
              <input v-model="newRoom.isPrivate" type="checkbox" />
              设为私人房间
            </label>
          </div>
          <div v-if="newRoom.isPrivate" class="form-group">
            <label>房间密码</label>
            <input v-model="newRoom.password" type="password" placeholder="请输入密码" />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showCreateModal = false" class="secondary">取消</button>
          <button @click="createRoom" class="primary">创建</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../stores/auth'

const router = useRouter()

const rooms = ref([])
const loading = ref(false)
const showCreateModal = ref(false)
const joinCode = ref('')
const onlineCount = ref(0)
const playingCount = ref(0)
const waitingCount = ref(0)

const newRoom = ref({
  name: '',
  isPrivate: false,
  password: ''
})

const refreshRooms = async () => {
  loading.value = true
  try {
    const response = await api.get('/rooms')
    rooms.value = response.data
    
    playingCount.value = rooms.value.filter(r => r.status === 'PLAYING').length
    waitingCount.value = rooms.value.filter(r => r.status === 'WAITING').length
  } catch (error) {
    console.error('Failed to fetch rooms:', error)
  } finally {
    loading.value = false
  }
}

const fetchOnlinePlayers = async () => {
  try {
    const response = await api.get('/players/online')
    onlineCount.value = response.data.length
  } catch (error) {
    console.error('Failed to fetch online players:', error)
  }
}

const createRoom = async () => {
  try {
    const response = await api.post('/rooms', null, {
      params: {
        name: newRoom.value.name || undefined,
        isPrivate: newRoom.value.isPrivate,
        password: newRoom.value.isPrivate ? newRoom.value.password : undefined
      }
    })
    showCreateModal.value = false
    router.push(`/rooms/${response.data.id}`)
  } catch (error) {
    alert('创建房间失败: ' + (error.response?.data?.message || error.message))
  }
}

const joinRoom = async (room) => {
  if (room.status !== 'WAITING') {
    alert('房间已开始游戏')
    return
  }
  try {
    const response = await api.post(`/rooms/${room.id}/join`)
    router.push(`/rooms/${room.id}`)
  } catch (error) {
    alert('加入房间失败: ' + (error.response?.data?.message || error.message))
  }
}

const joinByCode = async () => {
  try {
    const response = await api.post(`/rooms/join/${joinCode.value.toUpperCase()}`)
    router.push(`/rooms/${response.data.id}`)
  } catch (error) {
    alert('加入房间失败: ' + (error.response?.data?.message || error.message))
  }
}

onMounted(() => {
  refreshRooms()
  fetchOnlinePlayers()
})
</script>

<style scoped>
.lobby-page {
  padding: 20px;
  min-height: calc(100vh - 70px);
}

.lobby-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.lobby-header h2 {
  font-size: 28px;
}

.lobby-actions {
  display: flex;
  gap: 10px;
}

.lobby-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: var(--bg-card);
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  border: 1px solid var(--border-color);
}

.stat-label {
  display: block;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: var(--primary-color);
}

.rooms-section h3 {
  margin-bottom: 20px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
}

.empty-state {
  text-align: center;
  padding: 60px;
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.empty-state p {
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.room-card {
  background: var(--bg-card);
  padding: 20px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s;
}

.room-card:hover {
  border-color: var(--primary-color);
  transform: translateY(-2px);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.room-name {
  font-size: 18px;
  font-weight: bold;
}

.room-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.room-status.waiting {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
}

.room-status.playing {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.room-info {
  margin-bottom: 15px;
}

.host-info, .guest-info {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.host-info .label, .guest-info .label {
  color: var(--text-secondary);
}

.guest-info.waiting {
  color: var(--text-secondary);
  font-style: italic;
}

.room-code {
  font-size: 12px;
  color: var(--text-secondary);
  text-align: center;
  padding: 8px;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 6px;
}

.join-section {
  margin-top: 40px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.join-section h3 {
  margin-bottom: 15px;
}

.join-form {
  display: flex;
  gap: 10px;
}

.join-form input {
  flex: 1;
  max-width: 300px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: var(--bg-card);
  border-radius: 16px;
  width: 100%;
  max-width: 400px;
  border: 1px solid var(--border-color);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--text-secondary);
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid var(--border-color);
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
}

.form-group input[type="checkbox"] {
  width: auto;
}
</style>
