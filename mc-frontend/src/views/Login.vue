<template>
  <div class="login-container">
    <!-- Theme Switcher (top right) -->
    <div class="login-themer">
      <ThemeSwitcher />
    </div>

    <!-- Layered tech atmosphere -->
    <div class="bg-base"></div>
    <div class="bg-nebula"></div>
    <div class="bg-grid"></div>
    <div class="bg-noise"></div>
    <div class="bg-particles">
      <div v-for="i in 20" :key="i" class="particle" :style="particleStyle(i)"></div>
    </div>

    <!-- Floating orbs -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <div class="login-panel">
      <!-- Top accent bar -->
      <div class="panel-top-bar"></div>

      <!-- Brand -->
      <div class="brand">
        <div class="brand-mark">
          <svg width="36" height="36" viewBox="0 0 36 36" fill="none">
            <path d="M18 2L34 10V26L18 34L2 26V10L18 2Z" stroke="url(#brandGrad)" stroke-width="1.5" fill="none"/>
            <path d="M18 8L28 13V23L18 28L8 23V13L18 8Z" stroke="url(#brandGrad)" stroke-width="1" fill="url(#brandGrad2)"/>
            <circle cx="18" cy="18" r="4" fill="url(#brandGrad)"/>
            <defs>
              <linearGradient id="brandGrad" x1="2" y1="2" x2="34" y2="34" gradientUnits="userSpaceOnUse">
                <stop offset="0%" :stop-color="primaryColor"/>
                <stop offset="100%" :stop-color="accentColor"/>
              </linearGradient>
              <linearGradient id="brandGrad2" x1="8" y1="8" x2="28" y2="28" gradientUnits="userSpaceOnUse">
                <stop offset="0%" :stop-color="primaryColor" stop-opacity="0.15"/>
                <stop offset="100%" :stop-color="accentColor" stop-opacity="0.08"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <div class="brand-text-wrap">
          <span class="brand-name">Monthly Close</span>
          <span class="brand-tagline">月结报表管理系统</span>
        </div>
      </div>

      <!-- Glowing divider -->
      <div class="panel-divider">
        <div class="divider-line"></div>
        <div class="divider-dot"></div>
        <div class="divider-line"></div>
      </div>

      <!-- Heading -->
      <div class="panel-heading">
        <h1 class="panel-title">欢迎登录</h1>
        <p class="panel-subtitle">财务流程管理与数据闭环平台</p>
      </div>

      <!-- Form -->
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin" class="login-form">
        <el-form-item prop="username" class="form-item-animated" style="--delay: 0">
          <div class="field-wrapper">
            <div class="field-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 8a3 3 0 100-6 3 3 0 000 6z" stroke="currentColor" stroke-width="1.3"/>
                <path d="M2 14c0-3.314 2.686-6 6-6s6 2.686 6 6" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="field-input">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                size="large"
                autocomplete="username"
              />
            </div>
          </div>
        </el-form-item>

        <el-form-item prop="password" class="form-item-animated" style="--delay: 1">
          <div class="field-wrapper">
            <div class="field-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <rect x="2" y="7" width="12" height="8" rx="2" stroke="currentColor" stroke-width="1.3"/>
                <path d="M4 7V5a4 4 0 018 0v2" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="field-input">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                autocomplete="current-password"
              />
            </div>
          </div>
        </el-form-item>

        <div class="form-item-animated submit-item" style="--delay: 2">
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            native-type="submit"
            class="submit-btn"
            @click="handleLogin"
          >
            <span v-if="!loading" class="btn-content">
              <span>登录系统</span>
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M3 8h10M9 4l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <span v-else class="btn-content">
              <span>验证中</span>
              <span class="loading-dots">...</span>
            </span>
          </el-button>
        </div>
      </el-form>

      <!-- Footer -->
      <div class="panel-footer">
        <span>默认凭证</span>
        <code>admin / admin123</code>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { ElMessage } from 'element-plus'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const formRef = ref(null)
const loading = ref(false)

const primaryColor = computed(() => themeStore.currentTheme === 'light' ? '#1a56db' : themeStore.currentTheme === 'glass' ? '#8b5cf6' : '#4dabf7')
const accentColor = computed(() => themeStore.currentTheme === 'light' ? '#c9a96e' : themeStore.currentTheme === 'glass' ? '#f472b6' : '#c9a96e')

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const particleStyle = (i) => {
  const size = Math.random() * 3 + 1
  const left = Math.random() * 100
  const delay = Math.random() * 8
  const duration = Math.random() * 10 + 8
  return {
    width: size + 'px',
    height: size + 'px',
    left: left + '%',
    animationDelay: delay + 's',
    animationDuration: duration + 's'
  }
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const success = await authStore.login(form.username, form.password)
    if (success) {
      ElMessage.success({ message: '登录成功', grouping: true })
      router.push('/')
    } else {
      ElMessage.error('用户名或密码错误')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  position: relative;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: var(--login-bg, #080c14);
}

/* --- Theme Switcher on Login --- */
.login-themer {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 100;
  animation: fadeIn 0.5s ease 0.8s forwards;
  opacity: 0;
}

@keyframes fadeIn {
  to { opacity: 1; }
}

.bg-base {
  position: absolute;
  inset: 0;
  background: var(--login-bg-base);
  opacity: var(--login-bg-opacity, 1);
}

.bg-nebula {
  position: absolute;
  inset: 0;
  background: var(--login-bg-nebula);
  pointer-events: none;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image: var(--login-bg-grid);
  background-size: 40px 40px;
  mask-image: radial-gradient(ellipse 80% 80% at 50% 50%, black 0%, transparent 100%);
  -webkit-mask-image: radial-gradient(ellipse 80% 80% at 50% 50%, black 0%, transparent 100%);
  pointer-events: none;
}

.bg-noise {
  position: absolute;
  inset: 0;
  opacity: var(--login-noise-opacity, 0.025);
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  background-repeat: repeat;
  background-size: 256px 256px;
  pointer-events: none;
}

/* --- Floating Orbs --- */
.orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(60px);
}

.orb-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
  background: var(--login-orb1);
  animation: orb-drift 12s ease-in-out infinite;
}

.orb-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -50px;
  background: var(--login-orb2);
  animation: orb-drift 15s ease-in-out infinite reverse;
}

.orb-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: var(--login-orb3);
  animation: orb-drift 10s ease-in-out infinite 2s;
}

@keyframes orb-drift {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(20px, -15px) scale(1.05); }
  66% { transform: translate(-10px, 10px) scale(0.98); }
}

/* --- Particles --- */
.bg-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.particle {
  position: absolute;
  bottom: -4px;
  border-radius: 50%;
  background: var(--login-particle);
  animation: particle-rise linear infinite;
}

@keyframes particle-rise {
  0% { transform: translateY(0) scale(1); opacity: 0; }
  10% { opacity: 0.8; }
  90% { opacity: 0.3; }
  100% { transform: translateY(-100vh) scale(0.5); opacity: 0; }
}

/* --- Login Panel --- */
.login-panel {
  position: relative;
  z-index: 10;
  width: 440px;
  padding: 0;
  background: var(--login-panel-bg);
  border: 1px solid var(--login-panel-border);
  border-radius: 20px;
  box-shadow: var(--login-panel-shadow);
  backdrop-filter: var(--login-panel-blur, blur(24px));
  -webkit-backdrop-filter: var(--login-panel-blur, blur(24px));
  overflow: hidden;
  animation: panel-enter 0.7s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

@keyframes panel-enter {
  from { opacity: 0; transform: translateY(30px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.panel-top-bar {
  height: 3px;
  background: var(--login-topbar);
  background-size: 200% 100%;
  animation: shimmer 4s ease-in-out infinite;
}

@keyframes shimmer {
  0% { background-position: 200% center; }
  100% { background-position: -200% center; }
}

/* --- Brand --- */
.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 28px 36px 0;
  animation: brand-enter 0.5s ease forwards;
  animation-delay: 0.15s;
  opacity: 0;
}

.brand-mark {
  flex-shrink: 0;
  filter: var(--login-brand-filter);
}

.brand-text-wrap {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-name {
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--login-brand-name);
  opacity: 0.9;
}

.brand-tagline {
  font-size: 12px;
  color: var(--login-brand-tagline);
  letter-spacing: 0.06em;
}

@keyframes brand-enter {
  from { opacity: 0; transform: translateY(-10px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* --- Divider --- */
.panel-divider {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 24px 36px 20px;
  animation: heading-enter 0.5s ease 0.2s forwards;
  opacity: 0;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: var(--login-divider);
}

.divider-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--login-divider-dot);
  box-shadow: var(--login-divider-dot-shadow);
}

/* --- Heading --- */
.panel-heading {
  padding: 0 36px;
  margin-bottom: 28px;
  animation: heading-enter 0.5s ease 0.25s forwards;
  opacity: 0;
}

.panel-title {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
  color: var(--login-title);
  letter-spacing: 0.02em;
  line-height: 1.2;
  margin-bottom: 6px;
  background: var(--login-title-bg);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: var(--login-title-fill, transparent);
}

.panel-subtitle {
  font-size: 13px;
  color: var(--login-subtitle);
  letter-spacing: 0.08em;
}

@keyframes heading-enter {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* --- Form --- */
.login-form {
  padding: 0 36px;
  display: flex;
  flex-direction: column;
}

.form-item-animated {
  opacity: 0;
  animation: field-enter 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  animation-delay: calc(0.3s + var(--delay, 0) * 0.1s);
}

@keyframes field-enter {
  from { opacity: 0; transform: translateX(-12px); }
  to   { opacity: 1; transform: translateX(0); }
}

.field-wrapper {
  display: flex;
  align-items: center;
  gap: 0;
  background: var(--login-field-bg);
  border: 1px solid var(--login-field-border);
  border-radius: 10px;
  transition: all 0.25s ease;
  overflow: hidden;

  &:hover {
    border-color: var(--login-field-hover-border);
    background: var(--login-field-hover-bg);
  }

  &:focus-within {
    border-color: var(--login-field-focus-border);
    box-shadow: var(--login-field-focus-shadow);
    background: var(--login-field-focus-bg);
  }
}

.field-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--login-field-icon-color);
  flex-shrink: 0;
  background: var(--login-field-icon-bg);
  border-right: 1px solid var(--login-field-icon-border);
}

.field-input {
  flex: 1;

  :deep(.el-input) {
    .el-input__wrapper {
      background: transparent !important;
      border: none !important;
      box-shadow: none !important;
      padding: 0 12px !important;
      border-radius: 0 !important;

      .el-input__inner {
        color: var(--login-input-text) !important;
        font-size: 15px !important;
        height: 48px !important;
        &::placeholder { color: var(--login-input-placeholder) !important; }
      }
    }
  }
}

.form-item-animated:not(:last-of-type) {
  margin-bottom: 14px;
}

/* --- Submit Button --- */
.submit-item {
  margin-top: 6px;
}

.submit-btn {
  width: 100%;
  height: 50px;
  border-radius: 10px !important;
  background: var(--login-btn-bg) !important;
  border: none !important;
  font-family: var(--font-display) !important;
  font-size: 15px !important;
  font-weight: 700 !important;
  letter-spacing: 0.08em !important;
  color: var(--login-btn-text) !important;
  box-shadow: var(--login-btn-shadow) !important;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1) !important;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: var(--login-btn-shine);
    opacity: 0;
    transition: opacity 0.25s ease;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--login-btn-hover-shadow) !important;
    &::before { opacity: 1; }
  }

  &:active {
    transform: translateY(0);
    box-shadow: var(--login-btn-active-shadow) !important;
  }
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.loading-dots {
  animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

/* --- Footer --- */
.panel-footer {
  margin: 24px 0 0;
  padding: 16px 36px 28px;
  text-align: center;
  font-size: 12px;
  color: var(--login-footer-text);
  letter-spacing: 0.04em;

  code {
    font-family: var(--font-mono);
    color: var(--login-footer-code-color);
    background: var(--login-footer-code-bg);
    padding: 2px 8px;
    border-radius: 4px;
    border: 1px solid var(--login-footer-code-border);
    margin-left: 6px;
  }
}

:deep(.el-message) {
  background: var(--login-msg-bg) !important;
  border-color: var(--login-msg-border) !important;
  color: var(--login-msg-text) !important;
}
</style>
