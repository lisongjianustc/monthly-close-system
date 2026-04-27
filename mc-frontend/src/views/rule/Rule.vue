<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>规则配置</span>
          <el-button type="primary" @click="handleAdd">新增规则</el-button>
        </div>
      </template>
      <el-table :data="ruleList" v-loading="loading" stripe>
        <el-table-column prop="ruleCode" label="规则编码" width="150" />
        <el-table-column prop="ruleName" label="规则名称" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="expression" label="表达式" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status !== 'PUBLISHED'" link type="success" size="small" @click="handlePublish(row)">发布</el-button>
            <el-button link type="warning" size="small" @click="handleTest(row)">测试</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="form.ruleCode" :disabled="isEdit" placeholder="请输入规则编码" />
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="请输入分类" />
        </el-form-item>
        <el-form-item label="表达式" prop="expression">
          <el-input v-model="form.expression" type="textarea" :rows="4" placeholder="如: amount > 0" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 测试对话框 -->
    <el-dialog v-model="testVisible" title="测试规则" width="500px">
      <el-form label-width="100px">
        <el-form-item label="规则">
          <div style="font-size:13px;color:#666;word-break:break-all">{{ currentRule?.expression }}</div>
        </el-form-item>
        <el-form-item label="输入参数">
          <el-input v-model="testParams" type="textarea" :rows="4" placeholder='如: {"amount": 100}' />
        </el-form-item>
        <el-form-item label="测试结果">
          <el-tag v-if="testResult !== null" :type="testResult ? 'success' : 'danger'">
            {{ testResult ? '结果: true (通过)' : '结果: false (不通过)' }}
          </el-tag>
          <span v-else-if="testError" style="color:#f56c6c;font-size:13px">{{ testError }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testVisible = false">关闭</el-button>
        <el-button type="primary" @click="confirmTest">执行测试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRuleList, createRule, updateRule, publishRule, executeRule } from '@/api/rule'
import { ElMessage } from 'element-plus'

const ruleList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const testVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const currentRule = ref(null)
const testParams = ref('')
const testResult = ref(null)
const testError = ref('')

const form = reactive({ ruleCode: '', ruleName: '', category: '', expression: '', description: '' })

const rules = {
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  expression: [{ required: true, message: '请输入表达式', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRuleList()
    if (res.code === 0) ruleList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增规则'
  Object.assign(form, { ruleCode: '', ruleName: '', category: '', expression: '', description: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑规则'
  Object.assign(form, { id: row.id, ruleCode: row.ruleCode, ruleName: row.ruleName, category: row.category, expression: row.expression, description: row.description })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const payload = { ...form }
  if (isEdit.value) { delete payload.ruleCode }
  const res = isEdit.value ? await updateRule(payload) : await createRule(payload)
  if (res.code === 0) { ElMessage.success('操作成功'); dialogVisible.value = false; loadData() }
}

const handlePublish = async (row) => {
  const res = await publishRule(row.id)
  if (res.code === 0) { ElMessage.success('发布成功'); loadData() }
}

const handleTest = (row) => {
  currentRule.value = row
  testParams.value = ''
  testResult.value = null
  testError.value = ''
  testVisible.value = true
}

const confirmTest = async () => {
  let params = {}
  try { params = JSON.parse(testParams.value) } catch { testError.value = '参数必须是合法JSON'; return }
  testResult.value = null
  testError.value = ''
  const res = await executeRule({ ruleCode: currentRule.value.ruleCode, params })
  if (res.code === 0) {
    testResult.value = res.data?.success
    if (res.data?.error) testError.value = res.data.error
  } else {
    testError.value = res.message || '执行失败'
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
