<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程实例</span>
          <el-button type="primary" @click="handleInstantiate">发起流程</el-button>
        </div>
      </template>
      <el-table :data="instanceList" v-loading="loading" stripe>
        <el-table-column prop="id" label="实例ID" width="80" />
        <el-table-column prop="templateName" label="模板名称" />
        <el-table-column prop="periodCode" label="期间" width="100" />
        <el-table-column prop="unitName" label="单位" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status === 'RUNNING'" link type="danger" size="small" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="发起流程" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="流程模板" prop="templateId">
          <el-select v-model="form.templateId" placeholder="选择模板" style="width:100%">
            <el-option v-for="t in templateList" :key="t.id" :label="t.name + ' (v' + t.version + ')'" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="会计期间" prop="periodId">
          <el-select v-model="form.periodId" placeholder="选择期间" style="width:100%">
            <el-option v-for="p in periodList" :key="p.id" :label="p.code" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位" prop="unitId">
          <el-tree-select v-model="form.unitId" :data="orgTree" :props="{ label: 'orgName', value: 'id' }" placeholder="选择单位" clearable style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定发起</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getInstanceList, instantiate } from '@/api/workflow'
import { getTemplateList } from '@/api/workflow'
import { getPeriodList, getOrgTree } from '@/api/system'
import { ElMessage } from 'element-plus'

const instanceList = ref([])
const templateList = ref([])
const periodList = ref([])
const orgTree = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({ templateId: null, periodId: null, unitId: null })

const rules = {
  templateId: [{ required: true, message: '请选择模板', trigger: 'change' }],
  periodId: [{ required: true, message: '请选择期间', trigger: 'change' }],
  unitId: [{ required: true, message: '请选择单位', trigger: 'change' }]
}

const statusType = (status) => {
  const map = { RUNNING: 'success', COMPLETED: 'info', CANCELLED: 'danger' }
  return map[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const [instanceRes, templateRes, periodRes, orgRes] = await Promise.all([
      getInstanceList(),
      getTemplateList(),
      getPeriodList(),
      getOrgTree()
    ])
    if (instanceRes.code === 0) instanceList.value = instanceRes.data || []
    if (templateRes.code === 0) templateList.value = (templateRes.data || []).filter(t => t.status === 'PUBLISHED')
    if (periodRes.code === 0) periodList.value = periodRes.data || []
    if (orgRes.code === 0) orgTree.value = orgRes.data || []
  } finally {
    loading.value = false
  }
}

const handleInstantiate = () => {
  form.templateId = null; form.periodId = null; form.unitId = null
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const res = await instantiate(form.templateId, form.periodId, form.unitId)
  if (res.code === 0) { ElMessage.success('流程发起成功'); dialogVisible.value = false; loadData() }
}

const handleCancel = async (row) => {
  const res = await ElMessageBox.confirm(`确定取消该流程实例吗？`, '提示', { type: 'warning' })
  if (res) {
    // cancel API would go here if available
    ElMessage.info('取消功能待实现')
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
