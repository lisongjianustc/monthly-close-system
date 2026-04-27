<template>
  <div class="theme-switcher">
    <button
      v-for="theme in themes"
      :key="theme.key"
      class="theme-btn"
      :class="{ active: currentTheme === theme.key }"
      :title="theme.name"
      @click="setTheme(theme.key)"
    >
      <div class="theme-icon" v-html="theme.svg"></div>
      <span class="theme-label">{{ theme.label }}</span>
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'

const store = useThemeStore()
const currentTheme = computed(() => store.currentTheme)

const themes = [
  {
    key: 'light',
    name: '白色风',
    label: 'White',
    svg: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="12" r="4" stroke="currentColor" stroke-width="1.8"/>
      <path d="M12 2v2M12 20v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M2 12h2M20 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
    </svg>`
  },
  {
    key: 'dark',
    name: '深蓝科技风',
    label: 'Dark',
    svg: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none">
      <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`
  },
  {
    key: 'glass',
    name: '毛玻璃苹果风',
    label: 'Glass',
    svg: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none">
      <path d="M18 8A6 6 0 106 8c0 5.4 6 9 6 9s6-3.6 6-9z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
      <path d="M12 2v2M4.22 4.22l1.42 1.42M2 12h2M4.22 19.78l1.42-1.42" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
    </svg>`
  }
]

function setTheme(theme) {
  store.setTheme(theme)
}
</script>

<style scoped lang="scss">
.theme-switcher {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: var(--shadow-card);
}

.theme-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 8px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  transition: all 0.22s ease;
  color: var(--color-text-muted);
  position: relative;

  &:hover {
    background: var(--color-surface-hover);
    color: var(--color-text-secondary);
    transform: translateY(-1px);
  }

  &.active {
    background: var(--color-primary);
    color: #ffffff;
    box-shadow: 0 2px 10px var(--shadow-primary);

    .theme-icon {
      color: #ffffff;
    }

    .theme-label {
      color: #ffffff;
    }
  }
}

.theme-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: inherit;
  transition: color 0.22s ease;
}

.theme-label {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: inherit;
  transition: color 0.22s ease;
  font-family: 'Inter', sans-serif;
}
</style>
