<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>
      <el-table :data="roleList" v-loading="loading" stripe>
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '有效' : '无效' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="isEdit" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">有效</el-radio>
            <el-radio :label="0">无效</el-radio>
          </el-radio-group>
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
import { getRolePage, addRole, updateRole, deleteRole } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const roleList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({ roleName: '', roleCode: '', description: '', status: 1 })

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRolePage()
    if (res.code === 0) roleList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增角色'
  Object.assign(form, { roleName: '', roleCode: '', description: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  Object.assign(form, { id: row.id, roleName: row.roleName, roleCode: row.roleCode, description: row.description, status: row.status })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
  const res = await deleteRole(row.id)
  if (res.code === 0) { ElMessage.success('删除成功'); loadData() }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const res = isEdit.value ? await updateRole(form) : await addRole(form)
  if (res.code === 0) { ElMessage.success('操作成功'); dialogVisible.value = false; loadData() }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
