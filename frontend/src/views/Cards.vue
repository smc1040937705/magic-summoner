<template>
  <div class="cards-page">
    <div class="container">
      <div class="page-header">
        <h2>卡牌库</h2>
        <div class="filters">
          <select v-model="filterType">
            <option value="">全部类型</option>
            <option value="CREATURE">生物卡</option>
            <option value="SPELL">法术卡</option>
            <option value="EQUIPMENT">装备卡</option>
          </select>
          <select v-model="filterElement">
            <option value="">全部元素</option>
            <option value="FIRE">火</option>
            <option value="WATER">水</option>
            <option value="EARTH">土</option>
            <option value="WIND">风</option>
            <option value="LIGHT">光</option>
            <option value="DARK">暗</option>
            <option value="NEUTRAL">中立</option>
          </select>
          <select v-model="filterRarity">
            <option value="">全部稀有度</option>
            <option value="COMMON">普通</option>
            <option value="UNCOMMON">优秀</option>
            <option value="RARE">稀有</option>
            <option value="EPIC">史诗</option>
            <option value="LEGENDARY">传说</option>
            <option value="MYTHIC">神话</option>
          </select>
        </div>
      </div>

      <div class="cards-grid">
        <div v-for="card in filteredCards" :key="card.id" class="card-item">
          <div class="card" :class="[card.cardType.toLowerCase(), card.rarity.toLowerCase()]">
            <div class="card-header">
              <span class="card-cost">{{ card.manaCost }}</span>
              <span class="card-rarity" :class="card.rarity.toLowerCase()">
                {{ getRarityName(card.rarity) }}
              </span>
            </div>
            <div class="card-image">{{ card.name?.charAt(0) }}</div>
            <div class="card-name">{{ card.name }}</div>
            <div class="card-type">{{ getTypeName(card.cardType) }}</div>
            <div class="card-desc">{{ card.description }}</div>
            <div class="card-stats" v-if="card.attack !== null">
              <span class="attack">⚔️ {{ card.attack }}</span>
              <span class="health">❤️ {{ card.health }}</span>
            </div>
            <div class="card-keywords" v-if="card.keywords?.length">
              <span v-for="kw in card.keywords" :key="kw" class="keyword">{{ kw }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../stores/auth'

const cards = ref([])
const filterType = ref('')
const filterElement = ref('')
const filterRarity = ref('')

const fetchCards = async () => {
  try {
    const response = await api.get('/cards')
    cards.value = response.data
  } catch (error) {
    console.error('Failed to fetch cards:', error)
  }
}

const filteredCards = computed(() => {
  return cards.value.filter(card => {
    if (filterType.value && card.cardType !== filterType.value) return false
    if (filterElement.value && card.elementType !== filterElement.value) return false
    if (filterRarity.value && card.rarity !== filterRarity.value) return false
    return true
  })
})

const getRarityName = (rarity) => {
  const names = {
    COMMON: '普通',
    UNCOMMON: '优秀',
    RARE: '稀有',
    EPIC: '史诗',
    LEGENDARY: '传说',
    MYTHIC: '神话'
  }
  return names[rarity] || rarity
}

const getTypeName = (type) => {
  const names = {
    CREATURE: '生物卡',
    SPELL: '法术卡',
    EQUIPMENT: '装备卡'
  }
  return names[type] || type
}

onMounted(() => {
  fetchCards()
})
</script>

<style scoped>
.cards-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.filters {
  display: flex;
  gap: 10px;
}

.filters select {
  width: auto;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.card-item {
  perspective: 1000px;
}

.card {
  background: linear-gradient(180deg, #374151, #1f2937);
  border-radius: 12px;
  padding: 15px;
  border: 2px solid #4b5563;
  min-height: 280px;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s;
}

.card:hover {
  transform: translateY(-5px);
}

.card.common { border-color: #9e9e9e; }
.card.uncommon { border-color: #4caf50; }
.card.rare { border-color: #2196f3; }
.card.epic { border-color: #9c27b0; }
.card.legendary { border-color: #ff9800; }
.card.mythic { border-color: #f44336; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.card-cost {
  width: 30px;
  height: 30px;
  background: #3b82f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
}

.card-rarity {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 10px;
}

.card-rarity.common { background: #9e9e9e; }
.card-rarity.uncommon { background: #4caf50; }
.card-rarity.rare { background: #2196f3; }
.card-rarity.epic { background: #9c27b0; }
.card-rarity.legendary { background: #ff9800; }
.card-rarity.mythic { background: #f44336; }

.card-image {
  height: 80px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  margin-bottom: 10px;
}

.card-name {
  font-weight: bold;
  text-align: center;
  margin-bottom: 5px;
}

.card-type {
  font-size: 12px;
  color: var(--text-secondary);
  text-align: center;
  margin-bottom: 10px;
}

.card-desc {
  font-size: 12px;
  color: var(--text-secondary);
  text-align: center;
  flex: 1;
}

.card-stats {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 10px;
  font-size: 14px;
}

.card-keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 10px;
  justify-content: center;
}

.keyword {
  font-size: 10px;
  padding: 2px 6px;
  background: rgba(99, 102, 241, 0.3);
  border-radius: 4px;
}
</style>
