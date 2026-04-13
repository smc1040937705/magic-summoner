<template>
  <div class="game-page">
    <div class="game-container">
      <div class="game-header">
        <div class="turn-indicator">
          <span>回合 {{ gameState?.turnNumber }}</span>
          <span :class="['current-player', isMyTurn ? 'my-turn' : '']">
            {{ isMyTurn ? '你的回合' : '对手回合' }}
          </span>
        </div>
        <div class="game-timer">
          ⏱️ {{ remainingTime }}s
        </div>
        <button @click="surrender" class="danger">投降</button>
      </div>

      <div class="game-board">
        <div class="opponent-area">
          <div class="opponent-info">
            <div class="player-avatar">{{ opponent?.username?.charAt(0) }}</div>
            <div class="player-details">
              <div class="player-name">{{ opponent?.username }}</div>
              <div class="player-health">
                <span class="health-icon">❤️</span>
                <div class="health-bar">
                  <div class="health-fill" :style="{ width: (opponentHealth / 30 * 100) + '%' }"></div>
                </div>
                <span class="health-text">{{ opponentHealth }}/30</span>
              </div>
              <div class="player-mana">
                <span class="mana-icon">💧</span>
                <span>{{ opponent?.mana }}/{{ opponent?.maxMana }}</span>
              </div>
            </div>
          </div>
          <div class="opponent-hand">
            <div v-for="n in opponentHandCount" :key="n" class="card-back"></div>
          </div>
          <div class="opponent-board">
            <div 
              v-for="creature in opponentBoard" 
              :key="creature.instanceId"
              class="board-creature"
              :class="{ 'can-attack': creature.canAttack, 'taunt': creature.hasTaunt }"
            >
              <div class="creature-image">{{ creature.card?.name?.charAt(0) }}</div>
              <div class="creature-stats">
                <span class="attack">{{ creature.attack }}</span>
                <span class="health">{{ creature.health }}</span>
              </div>
              <div class="creature-effects">
                <span v-if="creature.hasTaunt" class="effect taunt">🛡️</span>
                <span v-if="creature.hasDivineShield" class="effect shield">✨</span>
                <span v-if="creature.hasStealth" class="effect stealth">👻</span>
              </div>
            </div>
          </div>
        </div>

        <div class="divider"></div>

        <div class="my-board">
          <div 
            v-for="creature in myBoard" 
            :key="creature.instanceId"
            class="board-creature"
            :class="{ 'can-attack': creature.canAttack && isMyTurn, 'selected': selectedCreature?.instanceId === creature.instanceId }"
            @click="selectCreature(creature)"
          >
            <div class="creature-image">{{ creature.card?.name?.charAt(0) }}</div>
            <div class="creature-stats">
              <span class="attack">{{ creature.attack }}</span>
              <span class="health">{{ creature.health }}</span>
            </div>
            <div class="creature-effects">
              <span v-if="creature.hasTaunt" class="effect taunt">🛡️</span>
              <span v-if="creature.hasDivineShield" class="effect shield">✨</span>
              <span v-if="creature.hasLifesteal" class="effect lifesteal">💚</span>
            </div>
          </div>
        </div>

        <div class="my-area">
          <div class="my-board-info">
            <div class="my-info">
              <div class="player-avatar">{{ myPlayer?.username?.charAt(0) }}</div>
              <div class="player-details">
                <div class="player-name">{{ myPlayer?.username }}</div>
                <div class="player-health">
                  <span class="health-icon">❤️</span>
                  <div class="health-bar">
                    <div class="health-fill" :style="{ width: (myHealth / 30 * 100) + '%' }"></div>
                  </div>
                  <span class="health-text">{{ myHealth }}/30</span>
                </div>
                <div class="player-mana">
                  <span class="mana-icon">💧</span>
                  <span>{{ myPlayer?.mana }}/{{ myPlayer?.maxMana }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="my-hand">
            <div 
              v-for="card in myHand" 
              :key="card.id"
              class="hand-card"
              :class="{ 'can-play': canPlayCard(card) && isMyTurn }"
              @click="playCard(card)"
            >
              <div class="card-cost">{{ card.manaCost }}</div>
              <div class="card-name">{{ card.name }}</div>
              <div class="card-image">{{ card.name?.charAt(0) }}</div>
              <div class="card-stats" v-if="card.attack !== null">
                <span class="attack">⚔️ {{ card.attack }}</span>
                <span class="health">❤️ {{ card.health }}</span>
              </div>
              <div class="card-desc">{{ card.description }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="game-actions">
        <button 
          @click="endTurn" 
          class="primary end-turn-btn" 
          :disabled="!isMyTurn"
        >
          结束回合
        </button>
      </div>

      <div class="event-log">
        <div v-for="(event, index) in events" :key="index" class="event">
          {{ event }}
        </div>
      </div>
    </div>

    <div v-if="gameOver" class="game-over-modal">
      <div class="modal-content">
        <h2>{{ isWinner ? '🎉 胜利!' : '💀 失败' }}</h2>
        <p>{{ gameOverMessage }}</p>
        <button @click="backToLobby" class="primary">返回大厅</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore, api } from '../stores/auth'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const gameState = ref(null)
const myPlayer = ref(null)
const opponent = ref(null)
const myHand = ref([])
const opponentHandCount = ref(0)
const myBoard = ref([])
const opponentBoard = ref([])
const events = ref([])
const selectedCreature = ref(null)
const remainingTime = ref(120)
const gameOver = ref(false)
const isWinner = ref(false)
const gameOverMessage = ref('')
const stompClient = ref(null)
const polling = ref(null)

const myHealth = computed(() => myPlayer.value?.health || 0)
const opponentHealth = computed(() => opponent.value?.health || 0)

const isMyTurn = computed(() => {
  return gameState.value?.currentPlayerId === authStore.player?.id
})

const canPlayCard = (card) => {
  return myPlayer.value?.mana >= card.manaCost
}

const fetchGameState = async () => {
  try {
    const response = await api.get(`/game/${route.params.roomId}/state`)
    gameState.value = response.data
    
    const myId = authStore.player.id
    if (gameState.value.player1.playerId === myId) {
      myPlayer.value = gameState.value.player1
      opponent.value = gameState.value.player2
      myHand.value = gameState.value.player1.hand || []
      opponentHandCount.value = gameState.value.player2.hand?.length || 0
      myBoard.value = gameState.value.player1.board || []
      opponentBoard.value = gameState.value.player2.board || []
    } else {
      myPlayer.value = gameState.value.player2
      opponent.value = gameState.value.player1
      myHand.value = gameState.value.player2.hand || []
      opponentHandCount.value = gameState.value.player1.hand?.length || 0
      myBoard.value = gameState.value.player2.board || []
      opponentBoard.value = gameState.value.player1.board || []
    }
    
    events.value = gameState.value.eventLog || []
    
    if (gameState.value.isGameOver) {
      gameOver.value = true
      isWinner.value = gameState.value.winnerId === myId
      gameOverMessage.value = isWinner.value ? '恭喜你获得了胜利！' : '很遗憾，你输了。'
    }
    
    remainingTime.value = gameState.value.remainingTime || 120
  } catch (error) {
    console.error('Failed to fetch game state:', error)
  }
}

const playCard = async (card) => {
  if (!isMyTurn.value || !canPlayCard(card)) return
  
  try {
    await api.post(`/game/${route.params.roomId}/play`, null, {
      params: { cardId: card.id }
    })
    await fetchGameState()
  } catch (error) {
    console.error('Failed to play card:', error)
  }
}

const selectCreature = (creature) => {
  if (!isMyTurn.value) return
  
  if (selectedCreature.value?.instanceId === creature.instanceId) {
    selectedCreature.value = null
  } else {
    selectedCreature.value = creature
  }
}

const attack = async (targetId) => {
  if (!isMyTurn.value || !selectedCreature.value) return
  
  try {
    await api.post(`/game/${route.params.roomId}/attack`, null, {
      params: { 
        attackerId: selectedCreature.value.instanceId,
        targetId: targetId
      }
    })
    selectedCreature.value = null
    await fetchGameState()
  } catch (error) {
    console.error('Failed to attack:', error)
  }
}

const endTurn = async () => {
  if (!isMyTurn.value) return
  
  try {
    await api.post(`/game/${route.params.roomId}/endTurn`)
    await fetchGameState()
  } catch (error) {
    console.error('Failed to end turn:', error)
  }
}

const surrender = async () => {
  if (confirm('确定要投降吗？')) {
    try {
      await api.post(`/game/${route.params.roomId}/surrender`)
      await fetchGameState()
    } catch (error) {
      console.error('Failed to surrender:', error)
    }
  }
}

const backToLobby = () => {
  router.push('/lobby')
}

const connectWebSocket = () => {
  stompClient.value = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    onConnect: () => {
      stompClient.value.subscribe(`/topic/game/${route.params.roomId}`, (message) => {
        const data = JSON.parse(message.body)
        if (data.type === 'STATE_UPDATE') {
          fetchGameState()
        }
      })
    }
  })
  
  stompClient.value.activate()
}

onMounted(async () => {
  await fetchGameState()
  polling.value = setInterval(fetchGameState, 3000)
  connectWebSocket()
})

onUnmounted(() => {
  if (polling.value) {
    clearInterval(polling.value)
  }
  if (stompClient.value) {
    stompClient.value.deactivate()
  }
})
</script>

<style scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  padding: 20px;
}

.game-container {
  max-width: 1200px;
  margin: 0 auto;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: rgba(30, 41, 59, 0.8);
  border-radius: 12px;
  margin-bottom: 20px;
}

.turn-indicator {
  display: flex;
  gap: 15px;
  align-items: center;
}

.current-player {
  padding: 5px 15px;
  border-radius: 20px;
  background: rgba(100, 100, 100, 0.3);
}

.current-player.my-turn {
  background: rgba(16, 185, 129, 0.3);
  color: #10b981;
}

.game-timer {
  font-size: 24px;
  font-weight: bold;
}

.game-board {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.opponent-area, .my-area {
  background: rgba(30, 41, 59, 0.5);
  border-radius: 12px;
  padding: 15px;
}

.opponent-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.player-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
}

.player-details {
  flex: 1;
}

.player-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.player-health, .player-mana {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.health-bar {
  width: 100px;
  height: 10px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 5px;
  overflow: hidden;
}

.health-fill {
  height: 100%;
  background: linear-gradient(90deg, #ef4444, #f87171);
  transition: width 0.3s;
}

.opponent-hand {
  display: flex;
  gap: 5px;
  justify-content: center;
  margin: 10px 0;
}

.card-back {
  width: 40px;
  height: 55px;
  background: linear-gradient(135deg, #374151, #4b5563);
  border-radius: 4px;
  border: 2px solid #6b7280;
}

.opponent-board, .my-board {
  display: flex;
  gap: 10px;
  justify-content: center;
  min-height: 100px;
  flex-wrap: wrap;
}

.divider {
  height: 2px;
  background: linear-gradient(90deg, transparent, #6366f1, transparent);
  margin: 10px 0;
}

.board-creature {
  width: 80px;
  height: 100px;
  background: rgba(55, 65, 81, 0.8);
  border-radius: 8px;
  border: 2px solid #4b5563;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.board-creature.can-attack {
  border-color: #10b981;
  box-shadow: 0 0 10px rgba(16, 185, 129, 0.5);
}

.board-creature.selected {
  border-color: #f59e0b;
  box-shadow: 0 0 15px rgba(245, 158, 11, 0.5);
}

.board-creature.taunt {
  border-color: #f59e0b;
}

.creature-image {
  font-size: 24px;
}

.creature-stats {
  display: flex;
  gap: 10px;
  font-size: 12px;
  margin-top: 5px;
}

.creature-stats .attack {
  color: #f87171;
}

.creature-stats .health {
  color: #10b981;
}

.creature-effects {
  position: absolute;
  top: 2px;
  right: 2px;
  display: flex;
  gap: 2px;
}

.effect {
  font-size: 10px;
}

.my-hand {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 10px;
}

.hand-card {
  width: 100px;
  height: 140px;
  background: linear-gradient(180deg, #374151, #1f2937);
  border-radius: 8px;
  border: 2px solid #4b5563;
  padding: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.hand-card.can-play:hover {
  transform: translateY(-10px);
  border-color: #6366f1;
  box-shadow: 0 5px 20px rgba(99, 102, 241, 0.5);
}

.card-cost {
  position: absolute;
  top: -8px;
  left: -8px;
  width: 25px;
  height: 25px;
  background: #3b82f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.card-name {
  font-size: 12px;
  font-weight: bold;
  text-align: center;
  margin-top: 20px;
}

.card-image {
  font-size: 32px;
  margin: 5px 0;
}

.card-stats {
  display: flex;
  gap: 10px;
  font-size: 12px;
}

.card-desc {
  font-size: 10px;
  color: #9ca3af;
  text-align: center;
  margin-top: auto;
}

.game-actions {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.end-turn-btn {
  padding: 15px 40px;
  font-size: 18px;
}

.event-log {
  margin-top: 20px;
  padding: 15px;
  background: rgba(30, 41, 59, 0.5);
  border-radius: 8px;
  max-height: 150px;
  overflow-y: auto;
}

.event {
  padding: 5px;
  font-size: 14px;
  color: #9ca3af;
}

.game-over-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--bg-card);
  padding: 40px;
  border-radius: 16px;
  text-align: center;
}

.modal-content h2 {
  font-size: 36px;
  margin-bottom: 20px;
}

.modal-content p {
  font-size: 18px;
  margin-bottom: 30px;
  color: var(--text-secondary);
}
</style>
