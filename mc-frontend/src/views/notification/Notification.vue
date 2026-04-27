<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通知管理</span>
          <el-button type="primary" @click="handleSend">发送通知</el-button>
        </div>
      </template>
      <el-row :gutter="12" style="margin-bottom:12px">
        <el-col :span="6"><div class="stat-box total">总计: {{ stats.TOTAL || 0 }}</div></el-col>
        <el-col :span="6"><div class="stat-box sent">已发送: {{ stats.SENT || 0 }}</div></el-col>
        <el-col :span="6"><div class="stat-box failed">失败: {{ stats.FAILED || 0 }}</div></el-col>
        <el-col :span="6"><div class="stat-box pending">待发送: {{ stats.PENDING || 0 }}</div></el-col>
      </el-row>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="通知记录" name="records">
          <el-table :data="recordList" v-loading="loading" stripe size="small">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="channel" label="渠道" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.channel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recipient" label="接收人" width="120" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="recordStatusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button v-if="row.status === 'FAILED'" link type="primary" size="small" @click="handleRetry(row)">重试</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="模板管理" name="templates">
          <div style="margin-bottom:12px">
            <el-button type="primary" size="small" @click="handleAddTemplate">新增模板</el-button>
          </div>
          <el-table :data="templateList" v-loading="templateLoading" stripe size="small">
            <el-table-column prop="templateCode" label="模板编码" width="150" />
            <el-table-column prop="templateName" label="模板名称" />
            <el-table-column prop="channel" label="渠道" width="100" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleEditTemplate(row)">编辑</el-button>
                <el-button link type="danger" size="small" @click="handleDeleteTemplate(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 发送通知对话框 -->
    <el-dialog v-model="sendVisible" title="发送通知" width="500px">
      <el-form ref="sendFormRef" :model="sendForm" label-width="80px">
        <el-form-item label="渠道" prop="channel">
          <el-select v-model="sendForm.channel" style="width:100%">
            <el-option label="邮件" value="EMAIL" />
            <el-option label="WebSocket" value="WEB_SOCKET" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收人" prop="recipient">
          <el-input v-model="sendForm.recipient" placeholder="请输入接收人" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="sendForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="sendForm.content" type="textarea" :rows="4" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSend">发送</el-button>
      </template>
    </el-dialog>

    <!-- 模板对话框 -->
    <el-dialog v-model="templateVisible" :title="templateDialogTitle" width="500px">
      <el-form ref="templateFormRef" :model="templateForm" label-width="90px">
        <el-form-item label="模板编码" prop="templateCode">
          <el-input v-model="templateForm.templateCode" :disabled="isTemplateEdit" placeholder="请输入模板编码" />
        </el-form-item>
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="templateForm.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="渠道" prop="channel">
          <el-select v-model="sendForm.channel" style="width:100%">
            <el-option label="邮件" value="EMAIL" />
            <el-option label="WebSocket" value="WEB_SOCKET" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="templateForm.templateType" style="width:100%">
            <el-option label="系统通知" value="SYSTEM" />
            <el-option label="业务通知" value="BUSINESS" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="templateForm.status">
            <el-radio label="ACTIVE">启用</el-radio>
            <el-radio label="INACTIVE">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmTemplate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { sendNotification, getRecordList, getStatistics, retrySend, getTemplateList, createTemplate, updateTemplate, deleteTemplate } from '@/api/notification'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('records')
const recordList = ref([])
const templateList = ref([])
const stats = ref({})
const loading = ref(false)
const templateLoading = ref(false)
const sendVisible = ref(false)
const templateVisible = ref(false)
const templateDialogTitle = ref('')
const isTemplateEdit = ref(false)
const sendFormRef = ref(null)
const templateFormRef = ref(null)

const sendForm = reactive({ channel: 'EMAIL', recipient: '', title: '', content: '' })
const templateForm = reactive({ templateCode: '', templateName: '', channel: 'EMAIL', templateType: 'SYSTEM', status: 'ACTIVE' })

const recordStatusType = (s) => ({ SENT: 'success', FAILED: 'danger', PENDING: 'warning' }[s] || 'info')

const loadRecords = async () => {
  loading.value = true
  try {
    const [recordRes, statRes] = await Promise.all([getRecordList(), getStatistics()])
    if (recordRes.code === 0) recordList.value = recordRes.data || []
    if (statRes.code === 0) stats.value = statRes.data || {}
  } finally {
    loading.value = false
  }
}

const loadTemplates = async () => {
  templateLoading.value = true
  try {
    const res = await getTemplateList()
    if (res.code === 0) templateList.value = res.data || []
  } finally {
    templateLoading.value = false
  }
}

const handleSend = () => {
  Object.assign(sendForm, { channel: 'EMAIL', recipient: '', title: '', content: '' })
  sendVisible.value = true
}

const confirmSend = async () => {
  const res = await sendNotification(sendForm)
  if (res.code === 0) { ElMessage.success('发送成功'); sendVisible.value = false; loadRecords() }
}

const handleRetry = async (row) => {
  const res = await retrySend(row.id)
  if (res.code === 0) { ElMessage.success('重试成功'); loadRecords() }
}

const handleAddTemplate = () => {
  isTemplateEdit.value = false
  templateDialogTitle.value = '新增模板'
  Object.assign(templateForm, { templateCode: '', templateName: '', channel: 'EMAIL', templateType: 'SYSTEM', status: 'ACTIVE' })
  templateVisible.value = true
}

const handleEditTemplate = (row) => {
  isTemplateEdit.value = true
  templateDialogTitle.value = '编辑模板'
  Object.assign(templateForm, { id: row.id, templateCode: row.templateCode, templateName: row.templateName, channel: row.channel, templateType: row.templateType, status: row.status })
  templateVisible.value = true
}

const handleDeleteTemplate = async (row) => {
  await ElMessageBox.confirm(`确定删除模板「${row.templateName}」吗？`, '提示', { type: 'warning' })
  const res = await deleteTemplate(row.id)
  if (res.code === 0) { ElMessage.success('删除成功'); loadTemplates() }
}

const confirmTemplate = async () => {
  const res = isTemplateEdit.value ? await updateTemplate(templateForm) : await createTemplate(templateForm)
  if (res.code === 0) { ElMessage.success('操作成功'); templateVisible.value = false; loadTemplates() }
}

onMounted(loadRecords)
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
.stat-box { padding: 12px; border-radius: 4px; text-align: center; font-size: 14px; }
.total { background: rgba(103, 194, 58, 0.15); color: #67c23a; }
.sent { background: rgba(64, 158, 255, 0.15); color: #409eff; }
.failed { background: rgba(230, 162, 60, 0.15); color: #e6a23c; }
.pending { background: rgba(245, 108, 108, 0.15); color: #f56c6c; }
</style>
