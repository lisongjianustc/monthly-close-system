<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>组织管理</span>
          <el-button type="primary" @click="handleAdd">新增组织</el-button>
        </div>
      </template>
      <el-tree :data="orgTree" :props="{ label: 'orgName', children: 'children' }" node-key="id" default-expand-all>
        <template #default="{ node, data }">
          <span class="tree-node">
            <span>{{ node.label }}</span>
            <span class="tree-actions">
              <el-button link type="primary" size="small" @click="handleEdit(data)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(data)">删除</el-button>
              <el-button link type="success" size="small" @click="handleAddChild(data)">新增子组织</el-button>
            </span>
          </span>
        </template>
      </el-tree>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="组织名称" prop="orgName">
          <el-input v-model="form.orgName" placeholder="请输入组织名称" />
        </el-form-item>
        <el-form-item label="组织编码" prop="orgCode">
          <el-input v-model="form.orgCode" placeholder="请输入组织编码" />
        </el-form-item>
        <el-form-item label="上级组织" prop="parentId">
          <el-tree-select v-model="form.parentId" :data="orgTree" :props="{ label: 'orgName', value: 'id' }" placeholder="选择上级组织" clearable check-strictly />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">有效</el-radio>
            <el-radio :label="0">无效</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
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
import { getOrgTree, addOrg, updateOrg, deleteOrg } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const orgTree = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const isEdit = ref(false)
const editingId = ref(null)

const form = reactive({
  orgName: '',
  orgCode: '',
  parentId: null,
  status: 1,
  sortOrder: 0
})

const rules = {
  orgName: [{ required: true, message: '请输入组织名称', trigger: 'blur' }],
  orgCode: [{ required: true, message: '请输入组织编码', trigger: 'blur' }]
}

const loadTree = async () => {
  const res = await getOrgTree()
  if (res.code === 0) {
    orgTree.value = res.data || []
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增组织'
  Object.assign(form, { orgName: '', orgCode: '', parentId: null, status: 1, sortOrder: 0 })
  dialogVisible.value = true
}

const handleAddChild = (data) => {
  isEdit.value = false
  dialogTitle.value = '新增子组织'
  Object.assign(form, { orgName: '', orgCode: '', parentId: data.id, status: 1, sortOrder: 0 })
  dialogVisible.value = true
}

const handleEdit = (data) => {
  isEdit.value = true
  editingId.value = data.id
  dialogTitle.value = '编辑组织'
  Object.assign(form, {
    orgName: data.orgName,
    orgCode: data.orgCode,
    parentId: data.parentId,
    status: data.status,
    sortOrder: data.sortOrder || 0
  })
  dialogVisible.value = true
}

const handleDelete = async (data) => {
  await ElMessageBox.confirm(`确定删除组织「${data.orgName}」吗？`, '提示', { type: 'warning' })
  const res = await deleteOrg(data.id)
  if (res.code === 0) {
    ElMessage.success('删除成功')
    loadTree()
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const res = isEdit.value ? await updateOrg({ id: editingId.value, ...form }) : await addOrg(form)
  if (res.code === 0) {
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    loadTree()
  }
}

onMounted(loadTree)
</script>

<style scoped lang="scss">
.card-header { display: flex; justify-content: space-between; align-items: center; }
.tree-node { display: flex; justify-content: space-between; align-items: center; width: 100%; padding-right: 8px; }
.tree-actions { display: flex; gap: 4px; }
</style>
