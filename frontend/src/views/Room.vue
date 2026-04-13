<template>
  <div class="room-page">
    <div class="container">
      <div class="room-header">
        <button @click="goBack" class="secondary">← 返回大厅</button>
        <h2>房间: {{ room?.name }}</h2>
      </div>

      <div class="room-content">
        <div class="room-info">
          <div class="room-code-display">
            <span class="label">房间码:</span>
            <span class="code">{{ room?.roomCode }}</span>
          </div>
          <div class="room-status">
            <span :class="['status-badge', room?.status?.toLowerCase()]">
              {{ room?.status }}
            </span>
          </div>
        </div>

        <div class="players-area">
          <div class="player-card host">
            <div class="player-avatar">
              {{ room?.host?.username?.charAt(0).toUpperCase() }}
            </div>
            <div class="player-info">
              <div class="player-name">{{ room?.host?.username }}</div>
              <div class="player-label">房主</div>
            </div>
            <div class="player-status ready" v-if="room?.host">
              ✓ 已准备
            </div>
          </div>

          <div class="vs">VS</div>

          <div class="player-card guest">
            <div class="player-avatar">
              {{ room?.guest?.username?.charAt(0).toUpperCase() || '?' }}
            </div>
            <div class="player-info">
              <div class="player-name">{{ room?.guest?.username || '等待对手...' }}</div>
              <div class="player-label">对手</div>
            </div>
            <div class="player-status" :class="room?.guest ? 'ready' : 'waiting'">
              {{ room?.guest ? '✓ 已准备' : '等待中...' }}
            </div>
          </div>
        </div>

        <div class="deck-selection" v-if="!room?.guest">
          <h3>选择卡组</h3>
          <div class="decks-grid">
            <div 
              v-for="deck in decks" 
              :key="deck.id"
              :class="['deck-card', selectedDeck === deck.id ? 'selected' : '']"
              @click="selectedDeck = deck.id"
            >
              <div class="deck-name">{{ deck.name }}</div>
              <div class="deck-cards">{{ deck.cardCount }} 张卡</div>
              <div class="deck-winrate" v-if="deck.wins + deck.losses > 0">
                胜率: {{ Math.round(deck.wins / (deck.wins + deck.losses) * 100) }}%
              </div>
            </div>
          </div>
          <div v-if="decks.length === 0" class="no-decks">
            <p>还没有卡组</p>
            <router-link to="/decks" class="primary">去创建卡组</router-link>
          </div>
        </div>

        <div class="room-actions">
          <button @click="leaveRoom" class="danger">离开房间</button>
          <button 
            v-if="isHost && room?.guest" 
            @click="startGame" 
            class="primary"
            :disabled="!selectedDeck"
          >
            开始游戏
          </button>
        </div>

        <div v-if="!isHost && !room?.guest" class="waiting-message">
          等待房主开始游戏...
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore, api } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const room = ref(null)
const decks = ref([])
const selectedDeck = ref(null)
const polling = ref(null)

const isHost = computed(() => {
  return room.value?.host?.id === authStore.player?.id
})

const fetchRoom = async () => {
  try {
    const response = await api.get(`/rooms/${route.params.id}`)
    room.value = response.data
    
    if (room.value.status === 'PLAYING') {
      router.push(`/game/${room.value.id}`)
    }
  } catch (error) {
    console.error('Failed to fetch room:', error)
    router.push('/lobby')
  }
}

const fetchDecks = async () => {
  try {
    const response = await api.get(`/decks/player/${authStore.player.id}`)
    decks.value = response.data
    if (decks.value.length > 0) {
      selectedDeck.value = decks.value[0].id
    }
  } catch (error) {
    console.error('Failed to fetch decks:', error)
  }
}

const startGame = async () => {
  try {
    await api.post(`/rooms/${room.value.id}/start`)
  } catch (error) {
    alert('开始游戏失败: ' + (error.response?.data?.message || error.message))
  }
}

const leaveRoom = async () => {
  try {
    await api.post(`/rooms/${room.value.id}/leave`)
    router.push('/lobby')
  } catch (error) {
    console.error('Failed to leave room:', error)
    router.push('/lobby')
  }
}

const goBack = () => {
  router.push('/lobby')
}

onMounted(async () => {
  await fetchRoom()
  await fetchDecks()
  
  polling.value = setInterval(fetchRoom, 2000)
})

onUnmounted(() => {
  if (polling.value) {
    clearInterval(polling.value)
  }
})
</script>

<style scoped>
.room-page {
  padding: 20px;
  min-height: calc(100vh - 70px);
}

.room-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.room-content {
  max-width: 800px;
  margin: 0 auto;
}

.room-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.room-code-display .label {
  color: var(--text-secondary);
  margin-right: 10px;
}

.room-code-display .code {
  font-size: 24px;
  font-weight: bold;
  color: var(--primary-color);
  letter-spacing: 2px;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
}

.status-badge.waiting {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
}

.status-badge.playing {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.players-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  margin-bottom: 40px;
}

.player-card {
  background: var(--bg-card);
  padding: 30px;
  border-radius: 16px;
  border: 2px solid var(--border-color);
  text-align: center;
  width: 200px;
}

.player-card.host {
  border-color: var(--primary-color);
}

.player-card.guest {
  border-color: var(--secondary-color);
}

.player-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  margin: 0 auto 15px;
}

.player-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}

.player-label {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 10px;
}

.player-status {
  font-size: 14px;
}

.player-status.ready {
  color: var(--success-color);
}

.player-status.waiting {
  color: var(--text-secondary);
}

.vs {
  font-size: 32px;
  font-weight: bold;
  color: var(--danger-color);
}

.deck-selection {
  margin-bottom: 30px;
}

.deck-selection h3 {
  margin-bottom: 15px;
}

.decks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 15px;
}

.deck-card {
  background: var(--bg-card);
  padding: 15px;
  border-radius: 12px;
  border: 2px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s;
}

.deck-card:hover {
  border-color: var(--primary-color);
}

.deck-card.selected {
  border-color: var(--success-color);
  background: rgba(16, 185, 129, 0.1);
}

.deck-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.deck-cards {
  color: var(--text-secondary);
  font-size: 14px;
}

.deck-winrate {
  color: var(--success-color);
  font-size: 14px;
  margin-top: 5px;
}

.no-decks {
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
}

.no-decks p {
  margin-bottom: 15px;
}

.room-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.waiting-message {
  text-align: center;
  margin-top: 20px;
  color: var(--text-secondary);
}
</style>
