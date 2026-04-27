<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="orgName" label="所属组织" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '有效' : '无效' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10,20,50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData" @current-change="loadData"
        style="margin-top:16px" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="所属组织" prop="orgId">
          <el-tree-select v-model="form.orgId" :data="orgTree" :props="{ label: 'orgName', value: 'id' }" placeholder="选择组织" clearable />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="选择角色" style="width:100%">
            <el-option v-for="r in roleList" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
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
import { getUserPage, addUser, updateUser, deleteUser, resetPassword, getOrgTree, getRoleList } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const userList = ref([])
const orgTree = ref([])
const roleList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const form = reactive({
  username: '', realName: '', orgId: null, roleIds: [], password: '', status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    if (res.code === 0) {
      userList.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

const loadOrgTree = async () => {
  const res = await getOrgTree()
  if (res.code === 0) orgTree.value = res.data || []
}

const loadRoles = async () => {
  const res = await getRoleList()
  if (res.code === 0) roleList.value = res.data || []
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  Object.assign(form, { username: '', realName: '', orgId: null, roleIds: [], password: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editingId.value = row.id
  dialogTitle.value = '编辑用户'
  Object.assign(form, { username: row.username, realName: row.realName, orgId: row.orgId, roleIds: row.roleIds || [], status: row.status })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '提示', { type: 'warning' })
  const res = await deleteUser(row.id)
  if (res.code === 0) { ElMessage.success('删除成功'); loadData() }
}

const handleResetPwd = async (row) => {
  await ElMessageBox.confirm(`确定重置用户「${row.username}」的密码吗？`, '提示', { type: 'warning' })
  const res = await resetPassword(row.id, 'admin123', 'admin123')
  if (res.code === 0) ElMessage.success('密码已重置为 admin123')
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const payload = { ...form }
  if (isEdit.value) { delete payload.password; payload.id = editingId.value }
  const res = isEdit.value ? await updateUser(payload) : await addUser(payload)
  if (res.code === 0) { ElMessage.success('操作成功'); dialogVisible.value = false; loadData() }
}

onMounted(() => { loadData(); loadOrgTree(); loadRoles() })
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
