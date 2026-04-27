<template>
  <el-container class="layout-container">
    <!-- Sidebar -->
    <el-aside class="sidebar">
      <!-- Brand -->
      <div class="sidebar-brand">
        <div class="brand-icon">
          <svg width="24" height="24" viewBox="0 0 36 36" fill="none">
            <path d="M18 2L34 10V26L18 34L2 26V10L18 2Z" stroke="url(#sbGrad)" stroke-width="1.5" fill="none"/>
            <path d="M18 8L28 13V23L18 28L8 23V13L18 8Z" stroke="url(#sbGrad)" stroke-width="1" fill="rgba(77,171,247,0.12)"/>
            <circle cx="18" cy="18" r="4" fill="url(#sbGrad)"/>
            <defs>
              <linearGradient id="sbGrad" x1="2" y1="2" x2="34" y2="34" gradientUnits="userSpaceOnUse">
                <stop offset="0%" :stop-color="primaryColor"/>
                <stop offset="100%" :stop-color="accentColor"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <div class="brand-text-wrap">
          <span class="brand-text">月结系统</span>
          <span class="brand-sub">Monthly Close</span>
        </div>
      </div>

      <!-- Navigation -->
      <el-menu :default-active="$route.path" router class="sidebar-nav" :collapse="false">
        <el-menu-item index="/dashboard" class="nav-item">
          <el-icon><HomeFilled /></el-icon>
          <span class="nav-label">首页</span>
        </el-menu-item>

        <el-sub-menu index="/system" class="nav-group">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span class="nav-label">系统管理</span>
          </template>
          <el-menu-item index="/system/org" class="nav-sub-item">组织管理</el-menu-item>
          <el-menu-item index="/system/user" class="nav-sub-item">用户管理</el-menu-item>
          <el-menu-item index="/system/role" class="nav-sub-item">角色管理</el-menu-item>
          <el-menu-item index="/system/period" class="nav-sub-item">期间管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/workflow" class="nav-group">
          <template #title>
            <el-icon><Connection /></el-icon>
            <span class="nav-label">流程引擎</span>
          </template>
          <el-menu-item index="/workflow/template" class="nav-sub-item">流程模板</el-menu-item>
          <el-menu-item index="/workflow/instance" class="nav-sub-item">流程实例</el-menu-item>
          <el-menu-item index="/workflow/task" class="nav-sub-item">任务管理</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/data/import" class="nav-item">
          <el-icon><UploadFilled /></el-icon>
          <span class="nav-label">数据导入</span>
        </el-menu-item>

        <el-menu-item index="/rule" class="nav-item">
          <el-icon><ScaleToOriginal /></el-icon>
          <span class="nav-label">规则配置</span>
        </el-menu-item>

        <el-menu-item index="/exception" class="nav-item">
          <el-icon><Warning /></el-icon>
          <span class="nav-label">异常处理</span>
        </el-menu-item>

        <el-menu-item index="/notification" class="nav-item">
          <el-icon><Bell /></el-icon>
          <span class="nav-label">通知管理</span>
        </el-menu-item>
      </el-menu>

      <!-- Sidebar bottom -->
      <div class="sidebar-bottom">
        <div class="version-tag">v1.0.0</div>
      </div>
    </el-aside>

    <!-- Main Area -->
    <el-container class="main-container">
      <!-- Header -->
      <el-header class="top-header">
        <div class="header-left">
          <div class="page-icon">
            <el-icon><component :is="currentIcon" /></el-icon>
          </div>
          <div class="page-titles">
            <span class="page-title">{{ $route.meta.title || '首页' }}</span>
            <span class="page-path">{{ currentPath }}</span>
          </div>
        </div>

        <div class="header-right">
          <div class="header-time">
            <span class="time-value">{{ currentTime }}</span>
          </div>
          <ThemeSwitcher />
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-menu">
              <div class="user-avatar">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M8 8a3 3 0 100-6 3 3 0 000 6z" stroke="currentColor" stroke-width="1.2"/>
                  <path d="M2 14c0-3.314 2.686-6 6-6s6 2.686 6 6" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="user-info">
                <span class="user-name">{{ authStore.userInfo?.username || '用户' }}</span>
                <span class="user-role">管理员</span>
              </div>
              <el-icon class="user-caret"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown-menu">
                <el-dropdown-item command="logout" divided class="logout-item">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Content -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import {
  HomeFilled, Setting, Connection, UploadFilled,
  ScaleToOriginal, Warning, Bell
} from '@element-plus/icons-vue'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const themeStore = useThemeStore()

authStore.fetchUserInfo()

const primaryColor = computed(() => themeStore.currentTheme === 'light' ? '#1a56db' : themeStore.currentTheme === 'glass' ? '#8b5cf6' : '#4dabf7')
const accentColor = computed(() => themeStore.currentTheme === 'light' ? '#c9a96e' : themeStore.currentTheme === 'glass' ? '#f472b6' : '#c9a96e')

const currentTime = ref('')
let timeInterval = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    hour12: false
  }).replace(/\//g, '-')
}

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  clearInterval(timeInterval)
})

const iconMap = {
  '/dashboard': 'HomeFilled',
  '/system': 'Setting',
  '/workflow': 'Connection',
  '/data/import': 'UploadFilled',
  '/rule': 'ScaleToOriginal',
  '/exception': 'Warning',
  '/notification': 'Bell'
}

const currentIcon = computed(() => {
  const path = route.path
  for (const [key, val] of Object.entries(iconMap)) {
    if (path.startsWith(key)) return val
  }
  return 'HomeFilled'
})

const currentPath = computed(() => {
  const path = route.path
  if (path === '/dashboard') return '首页 / Dashboard'
  if (path.startsWith('/system')) return '系统管理 / System'
  if (path.startsWith('/workflow')) return '流程引擎 / Workflow'
  if (path === '/data/import') return '数据导入 / Data'
  if (path === '/rule') return '规则配置 / Rule'
  if (path === '/exception') return '异常处理 / Exception'
  if (path === '/notification') return '通知管理 / Notify'
  return path
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    await authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  display: flex;
  background: var(--color-bg);
}

/* --- Sidebar --- */
.sidebar {
  width: 250px;
  min-width: 250px;
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--color-border);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 1px;
    height: 100%;
    background: linear-gradient(180deg, transparent, rgba(77,171,247,0.15) 50%, transparent);
    pointer-events: none;
  }
}

.sidebar-brand {
  height: 70px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid var(--color-border);
  position: relative;
}

.brand-icon {
  flex-shrink: 0;
}

.brand-text-wrap {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.brand-text {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.06em;
}

.brand-sub {
  font-size: 10px;
  color: var(--color-primary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  opacity: 0.7;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;

  &::-webkit-scrollbar { width: 3px; }
  &::-webkit-scrollbar-thumb {
    background: var(--color-border);
    border-radius: 2px;
  }
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 46px;
  line-height: 46px;
  background: transparent !important;
  color: var(--color-text-secondary) !important;
  font-family: var(--font-body);
  font-size: 14px;
  font-weight: 450;
  letter-spacing: 0.01em;
  padding-left: 18px !important;
  padding-right: 16px !important;
  transition: all 0.2s ease;
  position: relative;
  margin: 2px 8px;
  border-radius: 8px;

  &:hover {
    background: var(--color-surface-hover) !important;
    color: var(--color-text-primary) !important;
    .el-icon { color: var(--color-primary) !important; }
  }

  .el-icon {
    color: var(--color-text-muted);
    flex-shrink: 0;
    transition: color 0.2s ease;
    font-size: 16px;
    width: 18px;
  }

  .nav-label { margin-left: 10px; }
}

:deep(.el-menu-item.is-active) {
  color: var(--color-primary) !important;
  background: var(--color-primary-glow) !important;
  font-weight: 600;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 24px;
    background: var(--color-primary);
    border-radius: 0 2px 2px 0;
  }

  .el-icon { color: var(--color-primary) !important; }
}

:deep(.el-sub-menu__title:hover) {
  background: var(--color-surface-hover) !important;
  color: var(--color-text-primary) !important;
  .el-icon { color: var(--color-primary) !important; }
}

:deep(.el-sub-menu.is-opened) {
  > .el-sub-menu__title {
    color: var(--color-primary) !important;
    .el-icon { color: var(--color-primary) !important; }
  }
}

:deep(.el-menu--inline) {
  background: transparent !important;
  margin-left: 8px;

  .el-menu-item {
    height: 38px;
    line-height: 38px;
    font-size: 13px;
    color: var(--color-text-muted) !important;
    padding-left: 48px !important;
    margin: 1px 8px;
    border-radius: 6px;

    &:hover {
      color: var(--color-primary) !important;
      background: var(--color-primary-glow) !important;
    }

    &.is-active {
      color: var(--color-primary) !important;
      background: var(--color-primary-glow) !important;
      font-weight: 600;
    }
  }
}

.sidebar-bottom {
  padding: 12px 20px;
  border-top: 1px solid var(--color-border);
}

.version-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  background: var(--color-primary-glow);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--color-text-muted);
  letter-spacing: 0.08em;
}

/* --- Main Container --- */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
  overflow: hidden;
}

/* --- Header --- */
.top-header {
  height: 68px;
  min-height: 68px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: relative;
  backdrop-filter: blur(12px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--color-primary-glow);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  font-size: 16px;
}

.page-titles {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.page-title {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.02em;
}

.page-path {
  font-size: 11px;
  color: var(--color-text-muted);
  letter-spacing: 0.06em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-time {
  .time-value {
    font-family: var(--font-mono);
    font-size: 12px;
    color: var(--color-text-muted);
    letter-spacing: 0.05em;
  }
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid var(--color-border);
  background: var(--color-surface-glass);

  &:hover {
    background: var(--color-surface-hover);
    border-color: var(--color-border-active);
  }
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--color-primary-glow);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.user-name {
  font-size: 13px;
  color: var(--color-text-primary);
  font-weight: 600;
  letter-spacing: 0.02em;
}

.user-role {
  font-size: 10px;
  color: var(--color-primary);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.user-caret {
  color: var(--color-text-muted);
  font-size: 12px;
}

:deep(.user-dropdown-menu) {
  background: var(--color-surface-elevated) !important;
  border: 1px solid var(--color-border) !important;
  border-radius: 10px !important;
  box-shadow: var(--shadow-elevated) !important;
  padding: 6px !important;
  min-width: 160px;

  .el-dropdown-menu__item {
    color: var(--color-text-secondary) !important;
    font-size: 13px;
    border-radius: 6px;
    margin: 2px 0;
    padding: 9px 12px !important;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: all 0.15s ease;

    .el-icon {
      font-size: 14px;
      color: var(--color-text-muted);
      transition: color 0.15s ease;
    }

    &:hover {
      background: var(--color-primary-glow) !important;
      color: var(--color-primary) !important;
      .el-icon { color: var(--color-primary) !important; }
    }

    &.divided {
      border-top: 1px solid var(--color-border) !important;
      margin-top: 4px !important;
      padding-top: 9px !important;
    }
  }

  .logout-item:hover {
    background: rgba(255,107,107,0.08) !important;
    color: var(--color-danger) !important;
    .el-icon { color: var(--color-danger) !important; }
  }
}

/* --- Main Content --- */
.main-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 24px 28px;
  background: var(--color-bg);

  &::-webkit-scrollbar { width: 5px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb {
    background: var(--color-border);
    border-radius: 3px;
    &:hover { background: var(--color-border-active); }
  }
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
