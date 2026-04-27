<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据导入</span>
          <el-radio-group v-model="importType" size="small">
            <el-radio-button label="EXCEL">Excel</el-radio-button>
            <el-radio-button label="CSV">CSV</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-form label-width="100px">
        <el-form-item label="上传文件">
          <el-upload ref="uploadRef" :auto-upload="false" :limit="1" :on-change="handleFileChange" :file-list="fileList">
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 .xlsx, .xls, .csv 文件</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="success" :loading="uploading" @click="handleUpload">上传</el-button>
          <el-button v-if="batchId" type="warning" :loading="validating" @click="handleValidate">校验数据</el-button>
          <el-button v-if="batchId" type="primary" :loading="executing" @click="handleExecute">执行导入</el-button>
        </el-form-item>
      </el-form>

      <el-divider v-if="progress" content-position="left">导入进度</el-divider>
      <el-descriptions v-if="progress" :column="3" border style="margin-top:12px">
        <el-descriptions-item label="总记录数">{{ progress.total }}</el-descriptions-item>
        <el-descriptions-item label="成功">{{ progress.success }}</el-descriptions-item>
        <el-descriptions-item label="失败">{{ progress.fail }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ progress.status }}</el-descriptions-item>
        <el-descriptions-item label="进度">{{ progressPercent }}%</el-descriptions-item>
      </el-descriptions>

      <el-progress v-if="progress" :percentage="progressPercent" :status="progressStatus" style="margin-top:8px" />

      <el-divider v-if="failRecords.length > 0" content-position="left">失败记录</el-divider>
      <el-table v-if="failRecords.length > 0" :data="failRecords" stripe size="small" max-height="300">
        <el-table-column prop="rowNum" label="行号" width="80" />
        <el-table-column prop="errorMsg" label="错误信息" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { uploadFile, executeImport, getImportProgress, getFailRecords, validateData } from '@/api/data'
import { ElMessage } from 'element-plus'

const importType = ref('EXCEL')
const uploading = ref(false)
const executing = ref(false)
const validating = ref(false)
const fileList = ref([])
const batchId = ref(null)
const progress = ref(null)
const failRecords = ref([])
const uploadRef = ref(null)
let pollingTimer = null

const progressPercent = computed(() => {
  if (!progress.value || progress.value.total === 0) return 0
  return Math.round((progress.value.success + progress.value.fail) / progress.value.total * 100)
})

const progressStatus = computed(() => {
  if (!progress.value) return undefined
  if (progress.value.fail > 0) return 'exception'
  if (progress.value.total > 0 && (progress.value.success + progress.value.fail) >= progress.value.total) return 'success'
  return undefined
})

const handleFileChange = (file) => {
  fileList.value = [file]
  batchId.value = null
  progress.value = null
  failRecords.value = []
}

const handleUpload = async () => {
  if (fileList.value.length === 0) { ElMessage.warning('请先选择文件'); return }
  const file = fileList.value[0].raw
  const formData = new FormData()
  formData.append('file', file)
  formData.append('importType', importType.value)
  uploading.value = true
  try {
    const res = await uploadFile(formData)
    if (res.code === 0) {
      batchId.value = res.data
      ElMessage.success('上传成功')
      startPolling()
    }
  } finally {
    uploading.value = false
  }
}

const startPolling = () => {
  stopPolling()
  pollingTimer = setInterval(async () => {
    if (!batchId.value) return
    const res = await getImportProgress(batchId.value)
    if (res.code === 0) {
      progress.value = res.data
      if (['COMPLETED', 'FAILED', 'PARTIAL_SUCCESS'].includes(progress.value.status)) {
        stopPolling()
        if (progress.value.fail > 0) {
          const failRes = await getFailRecords(batchId.value)
          if (failRes.code === 0) failRecords.value = failRes.data || []
        }
      }
    }
  }, 2000)
}

const stopPolling = () => {
  if (pollingTimer) { clearInterval(pollingTimer); pollingTimer = null }
}

const handleValidate = async () => {
  if (!batchId.value) return
  validating.value = true
  try {
    const res = await validateData(batchId.value)
    if (res.code === 0) ElMessage.success('校验完成')
  } finally {
    validating.value = false
  }
}

const handleExecute = async () => {
  if (!batchId.value) return
  executing.value = true
  try {
    const res = await executeImport(batchId.value, '')
    if (res.code === 0) {
      ElMessage.success('导入执行完成')
      startPolling()
    }
  } finally {
    executing.value = false
  }
}
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
