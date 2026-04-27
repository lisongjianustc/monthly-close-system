<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>异常处理</span>
          <el-button type="primary" size="small" @click="handleRecord">记录异常</el-button>
        </div>
      </template>
      <el-row :gutter="12" style="margin-bottom:12px">
        <el-col :span="6"><div class="stat-box total">待处理: {{ stats.PENDING || 0 }}</div></el-col>
        <el-col :span="6"><div class="stat-box resolved">已处理: {{ stats.RESOLVED || 0 }}</div></el-col>
        <el-col :span="6"><div class="stat-box ignored">已忽略: {{ stats.IGNORED || 0 }}</div></el-col>
        <el-col :span="6"><div class="stat-box total">总计: {{ stats.TOTAL || 0 }}</div></el-col>
      </el-row>
      <el-table :data="exceptionList" v-loading="loading" stripe>
        <el-table-column prop="exceptionCode" label="异常编码" width="150" />
        <el-table-column prop="exceptionType" label="异常类型" width="120" />
        <el-table-column prop="severity" label="严重程度">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severity)">{{ row.severity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="message" label="异常信息" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button link type="success" size="small" @click="handleAction(row, 'handle')">处理</el-button>
              <el-button link type="warning" size="small" @click="handleAction(row, 'ignore')">忽略</el-button>
            </template>
            <el-button link type="info" size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="异常详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="异常编码">{{ currentRecord?.exceptionCode }}</el-descriptions-item>
        <el-descriptions-item label="异常类型">{{ currentRecord?.exceptionType }}</el-descriptions-item>
        <el-descriptions-item label="严重程度">
          <el-tag :type="severityType(currentRecord?.severity)">{{ currentRecord?.severity }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentRecord?.module }}</el-descriptions-item>
        <el-descriptions-item label="业务ID">{{ currentRecord?.businessId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(currentRecord?.status)">{{ currentRecord?.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="异常信息" :span="2">{{ currentRecord?.message }}</el-descriptions-item>
        <el-descriptions-item label="堆栈信息" :span="2">
          <pre style="max-height:200px;overflow:auto;font-size:12px">{{ currentRecord?.stackTrace }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="actionVisible" :title="actionTitle" width="400px">
      <el-form label-width="80px">
        <el-form-item label="处理备注">
          <el-input v-model="actionComment" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAction">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const exceptionList = ref([])
const stats = ref({})
const loading = ref(false)
const detailVisible = ref(false)
const actionVisible = ref(false)
const currentRecord = ref(null)
const currentAction = ref('')
const actionComment = ref('')

const severityType = (s) => ({ LOW: 'info', MEDIUM: 'warning', HIGH: 'danger' }[s] || 'info')
const statusType = (s) => ({ PENDING: 'warning', RESOLVED: 'success', IGNORED: 'info' }[s] || 'info')

const loadData = async () => {
  loading.value = true
  try {
    const [listRes, statRes] = await Promise.all([
      request.get('/exception/pending'),
      request.get('/exception/statistics')
    ])
    if (listRes.code === 0) exceptionList.value = listRes.data || []
    if (statRes.code === 0) stats.value = statRes.data || {}
  } finally {
    loading.value = false
  }
}

const handleDetail = (row) => {
  currentRecord.value = row
  detailVisible.value = true
}

const handleAction = (row, action) => {
  currentRecord.value = row
  currentAction.value = action
  actionComment.value = ''
  actionVisible.value = true
}

const confirmAction = async () => {
  const record = currentRecord.value
  if (currentAction.value === 'handle') {
    const res = await request.post(`/exception/handle/${record.id}?handlerId=1&handlerName=admin&comment=${encodeURIComponent(actionComment.value)}`)
    if (res.code === 0) { ElMessage.success('处理成功'); actionVisible.value = false; loadData() }
  } else {
    const res = await request.post(`/exception/ignore/${record.id}?reason=${encodeURIComponent(actionComment.value)}`)
    if (res.code === 0) { ElMessage.success('已忽略'); actionVisible.value = false; loadData() }
  }
}

const handleRecord = () => {
  ElMessage.info('记录异常功能开发中')
}

const actionTitle = computed(() => currentAction.value === 'handle' ? '处理异常' : '忽略异常')

onMounted(loadData)
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
.stat-box { padding: 12px; border-radius: 4px; text-align: center; font-size: 14px; }
.total { background: rgba(103, 194, 58, 0.15); color: #67c23a; }
.resolved { background: rgba(64, 158, 255, 0.15); color: #409eff; }
.ignored { background: rgba(230, 162, 60, 0.15); color: #e6a23c; }
</style>
