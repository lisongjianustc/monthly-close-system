<template>
  <div class="dashboard">
    <div class="dash-bg">
      <div class="bg-glow bg-glow-1"></div>
      <div class="bg-glow bg-glow-2"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="stats-grid">
      <div
        v-for="(stat, index) in statsData"
        :key="stat.label"
        class="stat-card"
        :style="`--card-delay: ${index * 0.12}s`"
      >
        <div class="stat-orb" :style="`background: radial-gradient(circle, ${stat.orbColor} 0%, transparent 70%)`"></div>
        <div class="stat-line-top" :style="`background: linear-gradient(90deg, transparent, ${stat.accent}, transparent)`"></div>
        <div class="stat-inner">
          <div class="stat-header">
            <div class="stat-icon-mask">
              <div class="stat-icon-ring"></div>
              <div class="stat-icon-wrap" :style="`background: ${stat.iconBg}`">
                <el-icon><component :is="stat.icon" /></el-icon>
              </div>
            </div>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-meta">{{ stat.meta }}</div>
          </div>
          <div class="stat-bar">
            <div class="stat-bar-fill" :style="`width: ${stat.barWidth}; background: ${stat.accent}`"></div>
          </div>
        </div>
        <div class="stat-border-glow" :style="`--accent: ${stat.accent}; --accent-glow: ${stat.accentGlow}`"></div>
      </div>
    </div>

    <div class="dashboard-bottom">
      <div class="panel quick-panel">
        <div class="panel-header">
          <div class="panel-icon">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M3 8h10M8 3l5 5-5 5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
            </svg>
          </div>
          <span class="panel-title">快捷操作</span>
          <span class="panel-badge">{{ quickActions.length }}</span>
        </div>
        <div class="panel-body">
          <div class="action-grid">
            <button
              v-for="action in quickActions"
              :key="action.label"
              class="action-btn"
              :style="`--action-color: ${action.color}`"
              @click="$router.push(action.path)"
            >
              <div class="action-icon-wrap">
                <el-icon><component :is="action.icon" /></el-icon>
              </div>
              <div class="action-text">
                <span class="action-label">{{ action.label }}</span>
                <span class="action-desc">{{ action.desc }}</span>
              </div>
              <svg class="action-arrow" width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M4 8h8M9 4l3 3-3 3" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <div class="panel announce-panel">
        <div class="panel-header">
          <div class="panel-icon">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.3"/>
              <path d="M8 5v3l2 2" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
            </svg>
          </div>
          <span class="panel-title">系统公告</span>
        </div>
        <div class="panel-body">
          <div class="announce-empty">
            <div class="empty-icon">
              <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                <circle cx="20" cy="20" r="16" stroke="rgba(77,171,247,0.15)" stroke-width="1.5"/>
                <circle cx="20" cy="20" r="8" stroke="rgba(77,171,247,0.08)" stroke-width="1"/>
                <circle cx="20" cy="20" r="3" fill="rgba(77,171,247,0.15)"/>
              </svg>
            </div>
            <span class="empty-text">暂无公告</span>
            <span class="empty-sub">系统公告将在此处显示</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { User, Connection, Tickets, Warning, Upload } from '@element-plus/icons-vue'
import request from '@/utils/request'

const stats = ref({ userCount: 0, instanceCount: 0, pendingTaskCount: 0, exceptionCount: 0 })

const loadStats = async () => {
  try {
    const [userRes, instanceRes, taskRes, exceptionRes] = await Promise.all([
      request.get('/system/user/page'),
      request.get('/workflow/instance/list'),
      request.get('/workflow/task/list'),
      request.get('/exception/statistics')
    ])
    stats.value = {
      userCount: userRes.data?.data?.total || 0,
      instanceCount: instanceRes.data?.data?.length || 0,
      pendingTaskCount: taskRes.data?.data?.filter(t => t.status === 'PENDING').length || 0,
      exceptionCount: exceptionRes.data?.data?.TOTAL || 0
    }
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

const statsData = computed(() => [
  {
    label: '用户总数',
    value: stats.value.userCount,
    icon: 'User',
    accent: 'var(--color-primary)',
    accentGlow: 'rgba(77,171,247,0.25)',
    orbColor: 'rgba(77,171,247,0.18)',
    iconBg: 'var(--color-primary-glow)',
    meta: '活跃账户',
    barWidth: Math.min(100, (stats.value.userCount / 100) * 100) + '%'
  },
  {
    label: '运行实例',
    value: stats.value.instanceCount,
    icon: 'Connection',
    accent: 'var(--color-success)',
    accentGlow: 'rgba(81,207,102,0.25)',
    orbColor: 'rgba(81,207,102,0.18)',
    iconBg: 'rgba(81,207,102,0.12)',
    meta: '进行中',
    barWidth: Math.min(100, (stats.value.instanceCount / 50) * 100) + '%'
  },
  {
    label: '待办任务',
    value: stats.value.pendingTaskCount,
    icon: 'Tickets',
    accent: 'var(--color-warning)',
    accentGlow: 'rgba(252,196,25,0.25)',
    orbColor: 'rgba(252,196,25,0.18)',
    iconBg: 'rgba(252,196,25,0.12)',
    meta: '待处理',
    barWidth: Math.min(100, (stats.value.pendingTaskCount / 20) * 100) + '%'
  },
  {
    label: '异常记录',
    value: stats.value.exceptionCount,
    icon: 'Warning',
    accent: 'var(--color-danger)',
    accentGlow: 'rgba(255,107,107,0.25)',
    orbColor: 'rgba(255,107,107,0.18)',
    iconBg: 'rgba(255,107,107,0.12)',
    meta: '需关注',
    barWidth: Math.min(100, (stats.value.exceptionCount / 10) * 100) + '%'
  }
])

const quickActions = [
  { label: '发起流程', desc: '创建新的工作流实例', path: '/workflow/instance', icon: 'Connection', color: 'var(--color-primary)' },
  { label: '数据导入', desc: '批量导入业务数据', path: '/data/import', icon: 'Upload', color: 'var(--color-success)' },
  { label: '处理任务', desc: '查看并审批待办任务', path: '/workflow/task', icon: 'Tickets', color: 'var(--color-warning)' },
  { label: '查看异常', desc: '监控系统异常记录', path: '/exception', icon: 'Warning', color: 'var(--color-danger)' }
]

onMounted(loadStats)
</script>

<style scoped lang="scss">
.dashboard {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
  animation: fadeIn 0.4s ease;
  z-index: 1;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.dash-bg {
  position: absolute;
  inset: -24px;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.bg-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  pointer-events: none;
}

.bg-glow-1 {
  width: 400px;
  height: 300px;
  top: -80px;
  left: 50%;
  transform: translateX(-50%);
  background: radial-gradient(circle, var(--color-primary-glow) 0%, transparent 70%);
}

.bg-glow-2 {
  width: 300px;
  height: 200px;
  bottom: 100px;
  right: -50px;
  background: radial-gradient(circle, var(--color-accent-glow, rgba(201,169,110,0.1)) 0%, transparent 70%);
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(var(--color-border) 1px, transparent 1px),
    linear-gradient(90deg, var(--color-border) 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.5;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  position: relative;
  z-index: 1;
}

.stat-card {
  position: relative;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 2px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: card-enter 0.55s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  animation-delay: var(--card-delay, 0s);
  opacity: 0;

  &:hover {
    border-color: var(--accent, var(--color-primary));
    transform: translateY(-4px);
    box-shadow: var(--shadow-card);

    .stat-border-glow { opacity: 1; }
    .stat-orb { opacity: 0.8; transform: scale(1.2); }
  }
}

@keyframes card-enter {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-orb {
  position: absolute;
  top: -30px;
  right: -20px;
  width: 110px;
  height: 110px;
  border-radius: 50%;
  opacity: 0.5;
  filter: blur(25px);
  pointer-events: none;
  transition: all 0.4s ease;
}

.stat-line-top {
  height: 1px;
  position: absolute;
  top: 0;
  left: 10%;
  right: 10%;
}

.stat-border-glow {
  position: absolute;
  inset: 0;
  border-radius: 16px;
  border: 1px solid var(--accent, var(--color-primary));
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.stat-inner {
  position: relative;
  padding: 20px;
  z-index: 1;
}

.stat-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}

.stat-icon-mask {
  position: relative;
  width: 44px;
  height: 44px;
}

.stat-icon-ring {
  position: absolute;
  inset: -3px;
  border-radius: 10px;
  border: 1px solid var(--accent, var(--color-primary));
  opacity: 0.3;
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-primary);
  font-size: 20px;

  .el-icon { font-size: 20px; }
}

.stat-body { margin-bottom: 12px; }

.stat-value {
  font-family: var(--font-display);
  font-size: 34px;
  font-weight: 800;
  color: var(--color-text-primary);
  line-height: 1;
  letter-spacing: -0.02em;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 500;
  letter-spacing: 0.04em;
  margin-bottom: 3px;
}

.stat-meta {
  font-size: 11px;
  color: var(--color-text-muted);
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.stat-bar {
  height: 2px;
  background: var(--color-border);
  border-radius: 1px;
  overflow: hidden;
}

.stat-bar-fill {
  height: 100%;
  border-radius: 1px;
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.dashboard-bottom {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.panel {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  overflow: hidden;
  animation: card-enter 0.55s cubic-bezier(0.4, 0, 0.2, 1) 0.4s forwards;
  opacity: 0;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--color-surface-glass);
}

.panel-icon {
  width: 28px;
  height: 28px;
  border-radius: 7px;
  background: var(--color-primary-glow);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
}

.panel-title {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.04em;
  flex: 1;
}

.panel-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--color-primary-glow);
  border: 1px solid var(--color-border);
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--color-primary);
  font-weight: 600;
}

.panel-body { padding: 16px; }

.action-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 14px;
  background: var(--color-surface-hover);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.22s ease;
  text-align: left;

  &:hover {
    border-color: var(--action-color);
    transform: translateX(3px);

    .action-icon-wrap {
      background: var(--action-color);
      color: #ffffff;
      transform: scale(1.1);
    }

    .action-arrow {
      color: var(--action-color);
      transform: translateX(3px);
    }

    .action-label { color: var(--action-color); }
  }
}

.action-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--color-surface-glass);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  font-size: 17px;
  flex-shrink: 0;
  transition: all 0.22s ease;
}

.action-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.action-label {
  font-size: 13px;
  color: var(--color-text-primary);
  font-weight: 600;
  letter-spacing: 0.02em;
  transition: color 0.22s ease;
}

.action-desc {
  font-size: 11px;
  color: var(--color-text-muted);
  letter-spacing: 0.02em;
}

.action-arrow {
  color: var(--color-text-muted);
  flex-shrink: 0;
  transition: all 0.22s ease;
}

.announce-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  gap: 8px;
}

.empty-icon {
  margin-bottom: 4px;
  animation: float 4s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.empty-text {
  font-size: 14px;
  color: var(--color-text-muted);
  font-weight: 500;
}

.empty-sub {
  font-size: 12px;
  color: var(--color-text-muted);
  opacity: 0.6;
  letter-spacing: 0.04em;
}
</style>
