<template>
  <div class="role-manager">
    <h2>角色管理</h2>
    <div class="actions">
      <el-button type="primary" @click="addRole">新增角色</el-button>
    </div>
    <el-table :data="roles" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="roleName" label="角色名称" width="180"></el-table-column>
      <el-table-column prop="roleCode" label="角色编码" width="180"></el-table-column>
      <el-table-column prop="description" label="描述"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" size="small" @click="editRole(scope.row)">
            编辑
          </el-button>
          <el-button type="success" size="small" @click="assignPermissions(scope.row)">
            分配权限
          </el-button>
          <el-button type="danger" size="small" @click="deleteRole(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="角色名称" required>
          <el-input v-model="formData.roleName" placeholder="请输入角色名称"></el-input>
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" placeholder="请输入描述" type="textarea"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveRole">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="分配权限">
      <el-tree
        :data="permissionTree"
        show-checkbox
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
        v-model:checkedKeys="checkedPermissionIds"
      >
        <template #default="{ node }">
          <span>{{ node.label }}</span>
        </template>
      </el-tree>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePermissions">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const roles = ref([])
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formData = ref({})
const permissionTree = ref([])
const checkedPermissionIds = ref([])
const currentRoleId = ref(null)

// 加载角色列表
const loadRoles = async () => {
  // 这里应该调用 API 获取角色列表
  // 暂时使用模拟数据
  roles.value = [
    { id: 1, roleName: '管理员', roleCode: 'admin', description: '系统管理员', status: 1, createTime: '2024-01-01 00:00:00' },
    { id: 2, roleName: '普通用户', roleCode: 'user', description: '普通用户', status: 1, createTime: '2024-01-02 00:00:00' }
  ]
}

// 加载权限树
const loadPermissionTree = async () => {
  // 这里应该调用 API 获取权限树
  // 暂时使用模拟数据
  permissionTree.value = [
    {
      id: 1,
      label: '系统管理',
      code: 'system:manage',
      type: 'menu',
      children: [
        {
          id: 2,
          label: '用户管理',
          code: 'user:manage',
          type: 'menu',
          children: [
            { id: 7, label: '用户创建', code: 'user:create', type: 'button' },
            { id: 8, label: '用户查看', code: 'user:read', type: 'button' },
            { id: 9, label: '用户更新', code: 'user:update', type: 'button' },
            { id: 10, label: '用户删除', code: 'user:delete', type: 'button' }
          ]
        },
        { id: 3, label: '角色管理', code: 'role:manage', type: 'menu' },
        { id: 4, label: '权限管理', code: 'permission:manage', type: 'menu' },
        { id: 5, label: '模块管理', code: 'module:manage', type: 'menu' }
      ]
    },
    { id: 6, label: 'CRUD管理', code: 'crud:manage', type: 'menu' }
  ]
}

// 新增角色
const addRole = () => {
  dialogTitle.value = '新增角色'
  formData.value = { status: 1 }
  dialogVisible.value = true
}

// 编辑角色
const editRole = (row) => {
  dialogTitle.value = '编辑角色'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 保存角色
const saveRole = async () => {
  // 这里应该调用 API 保存角色
  console.log('Save role:', formData.value)
  dialogVisible.value = false
  loadRoles()
}

// 删除角色
const deleteRole = (id) => {
  // 这里应该调用 API 删除角色
  console.log('Delete role:', id)
  loadRoles()
}

// 分配权限
const assignPermissions = (role) => {
  currentRoleId.value = role.id
  // 这里应该调用 API 获取角色的权限
  // 暂时使用模拟数据
  checkedPermissionIds.value = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
  loadPermissionTree()
  permissionDialogVisible.value = true
}

// 保存权限
const savePermissions = async () => {
  // 这里应该调用 API 保存权限分配
  console.log('Save permissions for role', currentRoleId.value, ':', checkedPermissionIds.value)
  permissionDialogVisible.value = false
}

// 页面加载时执行
onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.role-manager {
  padding: 20px;
}

.actions {
  margin-bottom: 20px;
}
</style>