<template>
  <div id="app">
    <nav v-if="isLoggedIn" class="navbar">
      <div class="nav-content container">
        <div class="nav-brand">
          <h1>魔法召唤师</h1>
        </div>
        <div class="nav-links">
          <router-link to="/lobby">游戏大厅</router-link>
          <router-link to="/cards">卡牌库</router-link>
          <router-link to="/decks">卡组</router-link>
          <router-link to="/profile">个人资料</router-link>
          <button @click="logout" class="secondary">退出登录</button>
        </div>
        <div class="nav-user">
          <span>{{ player?.username }}</span>
          <span class="gold">💰 {{ player?.gold }}</span>
          <span class="gems">💎 {{ player?.gems }}</span>
        </div>
      </div>
    </nav>
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const isLoggedIn = computed(() => authStore.isLoggedIn)
const player = computed(() => authStore.player)

const logout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  padding: 15px 0;
}

.nav-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-brand h1 {
  font-size: 24px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-links {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-links a {
  color: var(--text-secondary);
  text-decoration: none;
  padding: 8px 15px;
  border-radius: 6px;
  transition: all 0.2s;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: var(--text-primary);
  background: var(--border-color);
}

.nav-user {
  display: flex;
  gap: 15px;
  align-items: center;
}

.gold {
  color: #f59e0b;
}

.gems {
  color: #8b5cf6;
}
</style>
