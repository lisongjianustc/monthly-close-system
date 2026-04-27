<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程模板</span>
          <el-button type="primary" @click="handleAdd">新增模板</el-button>
        </div>
      </template>
      <el-table :data="templateList" v-loading="loading" stripe>
        <el-table-column prop="name" label="模板名称" />
        <el-table-column prop="code" label="模板编码" width="150" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'DRAFT'" link type="success" size="small" @click="handlePublish(row)">发布</el-button>
            <el-button link type="warning" size="small" @click="handleView(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板编码" prop="code">
          <el-input v-model="form.code" :disabled="isEdit" placeholder="请输入模板编码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="节点配置" prop="nodeConfig">
          <el-input v-model="form.nodeConfig" type="textarea" :rows="6" placeholder='如: [{"nodeCode":"start","nodeName":"开始","type":"START"},{"nodeCode":"task1","nodeName":"录入","type":"TASK","slaHours":24,"dependencies":["start"]}]' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="模板详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模板名称">{{ currentTemplate?.name }}</el-descriptions-item>
        <el-descriptions-item label="模板编码">{{ currentTemplate?.code }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ currentTemplate?.version }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(currentTemplate?.status)">{{ currentTemplate?.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentTemplate?.description }}</el-descriptions-item>
        <el-descriptions-item label="节点配置" :span="2">
          <pre style="font-size:12px;max-height:300px;overflow:auto">{{ formatJson(currentTemplate?.nodeConfig) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTemplateList, createTemplate, updateTemplate, publishTemplate, getTemplateById } from '@/api/workflow'
import { ElMessage } from 'element-plus'

const templateList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const currentTemplate = ref(null)

const form = reactive({ name: '', code: '', description: '', nodeConfig: '[]' })

const rules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入模板编码', trigger: 'blur' }]
}

const statusType = (status) => {
  const map = { DRAFT: 'info', PUBLISHED: 'success', ARCHIVED: 'warning' }
  return map[status] || 'info'
}

const formatJson = (str) => {
  try { return JSON.stringify(JSON.parse(str), null, 2) } catch { return str }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTemplateList()
    if (res.code === 0) templateList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增模板'
  Object.assign(form, { name: '', code: '', description: '', nodeConfig: '[]' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑模板'
  Object.assign(form, { id: row.id, name: row.name, code: row.code, description: row.description, nodeConfig: row.nodeConfig || '[]' })
  dialogVisible.value = true
}

const handleView = async (row) => {
  const res = await getTemplateById(row.id)
  if (res.code === 0) currentTemplate.value = res.data
  detailVisible.value = true
}

const handlePublish = async (row) => {
  const res = await publishTemplate(row.id)
  if (res.code === 0) { ElMessage.success('发布成功'); loadData() }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  let nodeConfig = form.nodeConfig
  if (typeof nodeConfig === 'string') {
    try { JSON.parse(nodeConfig) } catch { ElMessage.error('节点配置必须是合法JSON'); return }
  }
  const payload = { ...form }
  if (isEdit.value) { delete payload.code }
  const res = isEdit.value ? await updateTemplate(payload) : await createTemplate(payload)
  if (res.code === 0) { ElMessage.success('操作成功'); dialogVisible.value = false; loadData() }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
