<template>
  <div class="decks-page">
    <div class="container">
      <div class="page-header">
        <h2>我的卡组</h2>
        <button @click="showCreateModal = true" class="primary">创建卡组</button>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="decks.length === 0" class="empty-state">
        <p>还没有卡组</p>
        <button @click="showCreateModal = true" class="primary">创建第一个卡组</button>
      </div>

      <div v-else class="decks-grid">
        <div 
          v-for="deck in decks" 
          :key="deck.id" 
          class="deck-card"
          @click="selectDeck(deck)"
        >
          <div class="deck-header">
            <div class="deck-name">{{ deck.name }}</div>
            <div class="deck-cards">{{ deck.cardCount }} / 40</div>
          </div>
          <div class="deck-stats">
            <span class="wins">胜: {{ deck.wins }}</span>
            <span class="losses">负: {{ deck.losses }}</span>
            <span class="winrate" v-if="deck.wins + deck.losses > 0">
              {{ Math.round(deck.wins / (deck.wins + deck.losses) * 100) }}%
            </span>
          </div>
          <div class="deck-actions">
            <button @click.stop="editDeck(deck)" class="secondary">编辑</button>
            <button @click.stop="deleteDeck(deck)" class="danger">删除</button>
          </div>
        </div>
      </div>

      <div v-if="selectedDeck" class="deck-detail">
        <div class="detail-header">
          <h3>{{ selectedDeck.name }}</h3>
          <button @click="selectedDeck = null" class="secondary">关闭</button>
        </div>
        <div class="deck-cards-list">
          <div v-for="card in selectedDeck.cards" :key="card.id" class="deck-card-item">
            <span class="card-cost">{{ card.card.manaCost }}</span>
            <span class="card-name">{{ card.card.name }}</span>
            <span class="card-quantity">x{{ card.quantity }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>创建卡组</h3>
          <button @click="showCreateModal = false" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>卡组名称</label>
            <input v-model="newDeck.name" type="text" placeholder="输入卡组名称" />
          </div>
          <div class="form-group">
            <label>描述 (可选)</label>
            <input v-model="newDeck.description" type="text" placeholder="卡组描述" />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showCreateModal = false" class="secondary">取消</button>
          <button @click="createDeck" class="primary">创建</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore, api } from '../stores/auth'

const authStore = useAuthStore()

const decks = ref([])
const loading = ref(false)
const selectedDeck = ref(null)
const showCreateModal = ref(false)
const newDeck = ref({ name: '', description: '' })

const fetchDecks = async () => {
  loading.value = true
  try {
    const response = await api.get(`/decks/player/${authStore.player.id}`)
    decks.value = response.data
  } catch (error) {
    console.error('Failed to fetch decks:', error)
  } finally {
    loading.value = false
  }
}

const createDeck = async () => {
  if (!newDeck.value.name) return
  
  try {
    await api.post('/decks', null, {
      params: {
        name: newDeck.value.name,
        description: newDeck.value.description
      }
    })
    showCreateModal.value = false
    newDeck.value = { name: '', description: '' }
    await fetchDecks()
  } catch (error) {
    alert('创建失败: ' + (error.response?.data?.message || error.message))
  }
}

const selectDeck = async (deck) => {
  try {
    const response = await api.get(`/decks/${deck.id}`)
    selectedDeck.value = response.data
  } catch (error) {
    console.error('Failed to fetch deck details:', error)
  }
}

const editDeck = (deck) => {
  alert('编辑卡组功能开发中')
}

const deleteDeck = async (deck) => {
  if (!confirm(`确定要删除卡组 "${deck.name}" 吗？`)) return
  
  try {
    await api.delete(`/decks/${deck.id}`)
    await fetchDecks()
  } catch (error) {
    alert('删除失败: ' + (error.response?.data?.message || error.message))
  }
}

onMounted(() => {
  fetchDecks()
})
</script>

<style scoped>
.decks-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
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
}

.empty-state p {
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.decks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.deck-card {
  background: var(--bg-card);
  padding: 20px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s;
}

.deck-card:hover {
  border-color: var(--primary-color);
}

.deck-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.deck-name {
  font-size: 18px;
  font-weight: bold;
}

.deck-cards {
  color: var(--text-secondary);
}

.deck-stats {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.wins { color: var(--success-color); }
.losses { color: var(--danger-color); }
.winrate { color: var(--primary-color); }

.deck-actions {
  display: flex;
  gap: 10px;
}

.deck-actions button {
  flex: 1;
  padding: 8px;
  font-size: 12px;
}

.deck-detail {
  margin-top: 30px;
  background: var(--bg-card);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--border-color);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.deck-cards-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.deck-card-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.card-cost {
  width: 25px;
  height: 25px;
  background: #3b82f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.card-name {
  flex: 1;
}

.card-quantity {
  color: var(--text-secondary);
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
  display: block;
  margin-bottom: 8px;
  color: var(--text-secondary);
}
</style>
