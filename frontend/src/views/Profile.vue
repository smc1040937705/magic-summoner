<template>
  <div class="profile-page">
    <div class="container">
      <div class="profile-header">
        <div class="profile-avatar">
          {{ player?.username?.charAt(0).toUpperCase() }}
        </div>
        <div class="profile-info">
          <h2>{{ player?.username }}</h2>
          <p class="level">等级 {{ player?.level }}</p>
        </div>
      </div>

      <div class="profile-stats">
        <div class="stat-item">
          <span class="stat-label">金币</span>
          <span class="stat-value gold">💰 {{ player?.gold }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">宝石</span>
          <span class="stat-value gems">💎 {{ player?.gems }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">段位积分</span>
          <span class="stat-value rank">⭐ {{ player?.rankPoints }}</span>
        </div>
      </div>

      <div class="profile-battle-stats">
        <h3>对战统计</h3>
        <div class="battle-stats-grid">
          <div class="stat-card">
            <div class="stat-value">{{ player?.wins }}</div>
            <div class="stat-label">胜利</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ player?.losses }}</div>
            <div class="stat-label">失败</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ player?.draws }}</div>
            <div class="stat-label">平局</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ winRate }}%</div>
            <div class="stat-label">胜率</div>
          </div>
        </div>
      </div>

      <div class="profile-progress">
        <h3>经验值</h3>
        <div class="exp-bar">
          <div class="exp-fill" :style="{ width: expProgress + '%' }"></div>
        </div>
        <p class="exp-text">{{ player?.experience }} / {{ requiredExp }} XP</p>
      </div>

      <div class="profile-actions">
        <button @click="openPack" class="primary">购买卡包</button>
        <button @click="refreshProfile" class="secondary">刷新数据</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore, api } from '../stores/auth'

const authStore = useAuthStore()

const player = computed(() => authStore.player)

const winRate = computed(() => {
  const total = (player.value?.wins || 0) + (player.value?.losses || 0)
  if (total === 0) return 0
  return Math.round(player.value.wins / total * 100)
})

const requiredExp = computed(() => {
  return player.value?.level * 100
})

const expProgress = computed(() => {
  if (!player.value) return 0
  return (player.value.experience / requiredExp.value) * 100
})

const refreshProfile = async () => {
  await authStore.fetchCurrentUser()
}

const openPack = async () => {
  if (!confirm('花费100金币购买卡包？')) return
  
  try {
    const response = await api.post('/cards/open-pack?packSize=5')
    alert(`恭喜获得 ${response.data.length} 张卡牌！\n` + 
      response.data.map(c => c.name).join('\n'))
    await refreshProfile()
  } catch (error) {
    alert('购买失败: ' + (error.response?.data?.message || error.message))
  }
}

onMounted(() => {
  refreshProfile()
})
</script>

<style scoped>
.profile-page {
  padding: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 40px;
}

.profile-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: bold;
}

.profile-info h2 {
  font-size: 28px;
  margin-bottom: 5px;
}

.profile-info .level {
  color: var(--primary-color);
  font-size: 18px;
}

.profile-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.stat-item {
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
  font-size: 28px;
  font-weight: bold;
}

.stat-value.gold { color: #f59e0b; }
.stat-value.gems { color: #8b5cf6; }
.stat-value.rank { color: #10b981; }

.profile-battle-stats {
  margin-bottom: 40px;
}

.profile-battle-stats h3 {
  margin-bottom: 20px;
}

.battle-stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: var(--bg-card);
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  border: 1px solid var(--border-color);
}

.stat-card .stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-card .stat-label {
  color: var(--text-secondary);
}

.profile-progress {
  margin-bottom: 40px;
}

.profile-progress h3 {
  margin-bottom: 15px;
}

.exp-bar {
  height: 20px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 10px;
  overflow: hidden;
}

.exp-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  transition: width 0.3s;
}

.exp-text {
  text-align: center;
  margin-top: 10px;
  color: var(--text-secondary);
}

.profile-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
}
</style>
