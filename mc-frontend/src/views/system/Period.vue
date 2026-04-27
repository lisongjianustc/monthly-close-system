<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>期间管理</span>
          <el-button type="primary" @click="handleAdd">新增期间</el-button>
        </div>
      </template>
      <el-table :data="periodList" v-loading="loading" stripe>
        <el-table-column prop="code" label="期间编码" width="120" />
        <el-table-column prop="year" label="年度" width="80" />
        <el-table-column prop="month" label="月份" width="80" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="isCurrent" label="当前期间">
          <template #default="{ row }">
            <el-tag :type="row.isCurrent === 1 ? 'success' : 'info'">{{ row.isCurrent === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'CLOSED' ? 'danger' : 'warning'">
              {{ row.status === 'ACTIVE' ? '开放' : row.status === 'CLOSED' ? '关闭' : '未开放' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status !== 'CLOSED'" link type="warning" size="small" @click="handleClose(row)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="期间编码" prop="code">
          <el-input v-model="form.code" :disabled="isEdit" placeholder="如: 2024-01" />
        </el-form-item>
        <el-form-item label="年度" prop="year">
          <el-input-number v-model="form.year" :min="2000" :max="2100" />
        </el-form-item>
        <el-form-item label="月份" prop="month">
          <el-input-number v-model="form.month" :min="1" :max="12" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="当前期间" prop="isCurrent">
          <el-switch v-model="form.isCurrent" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="开放" value="ACTIVE" />
            <el-option label="未开放" value="PENDING" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPeriodList, addPeriod, updatePeriod, closePeriod } from '@/api/system'
import { ElMessage } from 'element-plus'

const periodList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({ code: '', year: new Date().getFullYear(), month: 1, startDate: '', endDate: '', isCurrent: 0, status: 'ACTIVE' })

const rules = {
  code: [{ required: true, message: '请输入期间编码', trigger: 'blur' }],
  year: [{ required: true, message: '请输入年度', trigger: 'blur' }],
  month: [{ required: true, message: '请输入月份', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPeriodList()
    if (res.code === 0) periodList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增期间'
  Object.assign(form, { code: '', year: new Date().getFullYear(), month: 1, startDate: '', endDate: '', isCurrent: 0, status: 'ACTIVE' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑期间'
  Object.assign(form, { id: row.id, code: row.code, year: row.year, month: row.month, startDate: row.startDate, endDate: row.endDate, isCurrent: row.isCurrent, status: row.status })
  dialogVisible.value = true
}

const handleClose = async (row) => {
  const res = await closePeriod(row.id)
  if (res.code === 0) { ElMessage.success('期间已关闭'); loadData() }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const res = isEdit.value ? await updatePeriod(form) : await addPeriod(form)
  if (res.code === 0) { ElMessage.success('操作成功'); dialogVisible.value = false; loadData() }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
