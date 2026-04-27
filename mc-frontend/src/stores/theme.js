import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const currentTheme = ref(localStorage.getItem('mc-theme') || 'dark')

  const themeMeta = {
    light: { name: '白色风', label: 'White', icon: 'sunny' },
    dark: { name: '深蓝科技风', label: 'Dark', icon: 'moon' },
    glass: { name: '毛玻璃苹果风', label: 'Glass', icon: 'MostlyCloudy' }
  }

  const themeConfig = computed(() => themeMeta[currentTheme.value])

  function setTheme(theme) {
    if (!themeMeta[theme]) return
    currentTheme.value = theme
    localStorage.setItem('mc-theme', theme)
    document.documentElement.setAttribute('data-theme', theme)
  }

  function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme)
  }

  applyTheme(currentTheme.value)

  return { currentTheme, themeConfig, setTheme, applyTheme }
})
