<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>任务管理</span>
          <el-radio-group v-model="filterStatus" size="small">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="PENDING">待办</el-radio-button>
            <el-radio-button label="IN_PROGRESS">进行中</el-radio-button>
            <el-radio-button label="COMPLETED">已完成</el-radio-button>
            <el-radio-button label="OVERDUE">已逾期</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-table :data="filteredTasks" v-loading="loading" stripe>
        <el-table-column prop="taskCode" label="任务编码" width="120" />
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="instanceName" label="所属实例" />
        <el-table-column prop="assigneeName" label="处理人" width="100" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="slaDeadline" label="SLA截止" width="160" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button link type="success" size="small" @click="handleAction(row, 'start')">开始</el-button>
            </template>
            <template v-else-if="row.status === 'IN_PROGRESS'">
              <el-button link type="success" size="small" @click="handleAction(row, 'approve')">通过</el-button>
              <el-button link type="danger" size="small" @click="handleAction(row, 'reject')">退回</el-button>
            </template>
            <el-button link type="info" size="small" @click="handleTrace(row)">轨迹</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="traceVisible" title="任务轨迹" width="600px">
      <el-timeline>
        <el-timeline-item v-for="t in traceList" :key="t.id" :timestamp="t.createTime" placement="top">
          <p><strong>{{ t.action }}</strong> - {{ t.operatorName }}</p>
          <p v-if="t.comment" style="color:#666">{{ t.comment }}</p>
        </el-timeline-item>
      </el-timeline>
      <div v-if="traceList.length === 0" style="text-align:center;color:#999">暂无轨迹记录</div>
    </el-dialog>

    <el-dialog v-model="commentVisible" :title="commentTitle" width="400px">
      <el-input v-model="comment" type="textarea" :rows="3" placeholder="请输入备注（可选）" />
      <template #footer>
        <el-button @click="commentVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAction">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getTaskList, startTask, approveTask, rejectTask, getTaskTrace } from '@/api/workflow'
import { ElMessage } from 'element-plus'

const taskList = ref([])
const traceList = ref([])
const loading = ref(false)
const filterStatus = ref('')
const traceVisible = ref(false)
const commentVisible = ref(false)
const commentTitle = ref('')
const comment = ref('')
const currentTask = ref(null)
const currentAction = ref('')

const filteredTasks = computed(() => {
  if (!filterStatus.value) return taskList.value
  return taskList.value.filter(t => t.status === filterStatus.value)
})

const statusType = (status) => {
  const map = { PENDING: 'warning', IN_PROGRESS: 'primary', COMPLETED: 'success', REJECTED: 'danger', OVERDUE: 'danger' }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = { PENDING: '待办', IN_PROGRESS: '进行中', COMPLETED: '已完成', REJECTED: '已退回', OVERDUE: '已逾期' }
  return map[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTaskList()
    if (res.code === 0) taskList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAction = (row, action) => {
  currentTask.value = row
  currentAction.value = action
  comment.value = ''
  commentTitle.value = action === 'start' ? '开始任务' : action === 'approve' ? '复核通过' : '复核退回'
  commentVisible.value = true
}

const confirmAction = async () => {
  const { id } = currentTask.value
  const cm = comment.value || null
  let res
  if (currentAction.value === 'start') res = await startTask(id)
  else if (currentAction.value === 'approve') res = await approveTask(id, cm)
  else if (currentAction.value === 'reject') res = await rejectTask(id, cm)
  if (res && res.code === 0) {
    ElMessage.success('操作成功')
    commentVisible.value = false
    loadData()
  }
}

const handleTrace = async (row) => {
  const res = await getTaskTrace(row.id)
  if (res.code === 0) traceList.value = res.data || []
  traceVisible.value = true
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
