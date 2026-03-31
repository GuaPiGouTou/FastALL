<template>
  <div class="permission-manager">
    <h2>权限管理</h2>
    <div class="actions">
      <el-button type="primary" @click="addPermission">新增权限</el-button>
    </div>
    <el-table :data="permissions" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="permissionName" label="权限名称" width="180"></el-table-column>
      <el-table-column prop="permissionCode" label="权限编码" width="180"></el-table-column>
      <el-table-column prop="resourceType" label="资源类型" width="120"></el-table-column>
      <el-table-column prop="resourceUrl" label="资源路径"></el-table-column>
      <el-table-column prop="parentId" label="父级ID" width="100"></el-table-column>
      <el-table-column prop="icon" label="图标" width="100"></el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button type="primary" size="small" @click="editPermission(scope.row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="deletePermission(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="权限名称" required>
          <el-input v-model="formData.permissionName" placeholder="请输入权限名称"></el-input>
        </el-form-item>
        <el-form-item label="权限编码" required>
          <el-input v-model="formData.permissionCode" placeholder="请输入权限编码"></el-input>
        </el-form-item>
        <el-form-item label="资源类型" required>
          <el-select v-model="formData.resourceType" placeholder="请选择资源类型">
            <el-option label="菜单" value="menu"></el-option>
            <el-option label="按钮" value="button"></el-option>
            <el-option label="API" value="api"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="资源路径">
          <el-input v-model="formData.resourceUrl" placeholder="请输入资源路径"></el-input>
        </el-form-item>
        <el-form-item label="父级ID">
          <el-input v-model="formData.parentId" placeholder="请输入父级ID" type="number"></el-input>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="formData.icon" placeholder="请输入图标"></el-input>
        </el-form-item>
        <el-form-item label="排序">
          <el-input v-model="formData.sortOrder" placeholder="请输入排序" type="number"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePermission">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const permissions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增权限')
const formData = ref({})

// 加载权限列表
const loadPermissions = async () => {
  // 这里应该调用 API 获取权限列表
  // 暂时使用模拟数据
  permissions.value = [
    { id: 1, permissionName: '系统管理', permissionCode: 'system:manage', resourceType: 'menu', resourceUrl: '/system', parentId: 0, icon: 'System', sortOrder: 1, status: 1, createTime: '2024-01-01 00:00:00' },
    { id: 2, permissionName: '用户管理', permissionCode: 'user:manage', resourceType: 'menu', resourceUrl: '/system/users', parentId: 1, icon: 'User', sortOrder: 2, status: 1, createTime: '2024-01-01 00:00:00' },
    { id: 3, permissionName: '角色管理', permissionCode: 'role:manage', resourceType: 'menu', resourceUrl: '/system/roles', parentId: 1, icon: 'Role', sortOrder: 3, status: 1, createTime: '2024-01-01 00:00:00' },
    { id: 4, permissionName: '权限管理', permissionCode: 'permission:manage', resourceType: 'menu', resourceUrl: '/system/permissions', parentId: 1, icon: 'Permission', sortOrder: 4, status: 1, createTime: '2024-01-01 00:00:00' },
    { id: 5, permissionName: '模块管理', permissionCode: 'module:manage', resourceType: 'menu', resourceUrl: '/system/modules', parentId: 1, icon: 'Module', sortOrder: 5, status: 1, createTime: '2024-01-01 00:00:00' },
    { id: 6, permissionName: 'CRUD管理', permissionCode: 'crud:manage', resourceType: 'menu', resourceUrl: '/crud', parentId: 0, icon: 'Crud', sortOrder: 6, status: 1, createTime: '2024-01-01 00:00:00' },
    { id: 7, permissionName: '用户创建', permissionCode: 'user:create', resourceType: 'button', resourceUrl: '/api/users', parentId: 2, icon: '', sortOrder: 1, status: 1, createTime: '2024-01-01 00:00:00' },
    { id: 8, permissionName: '用户查看', permissionCode: 'user:read', resourceType: 'button', resourceUrl: '/api/users', parentId: 2, icon: '', sortOrder: 2, status: 1, createTime: '2024-01-01 00:00:00' },
    { id: 9, permissionName: '用户更新', permissionCode: 'user:update', resourceType: 'button', resourceUrl: '/api/users', parentId: 2, icon: '', sortOrder: 3, status: 1, createTime: '2024-01-01 00:00:00' },
    { id: 10, permissionName: '用户删除', permissionCode: 'user:delete', resourceType: 'button', resourceUrl: '/api/users', parentId: 2, icon: '', sortOrder: 4, status: 1, createTime: '2024-01-01 00:00:00' }
  ]
}

// 新增权限
const addPermission = () => {
  dialogTitle.value = '新增权限'
  formData.value = { status: 1, parentId: 0, sortOrder: 0 }
  dialogVisible.value = true
}

// 编辑权限
const editPermission = (row) => {
  dialogTitle.value = '编辑权限'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 保存权限
const savePermission = async () => {
  // 这里应该调用 API 保存权限
  console.log('Save permission:', formData.value)
  dialogVisible.value = false
  loadPermissions()
}

// 删除权限
const deletePermission = (id) => {
  // 这里应该调用 API 删除权限
  console.log('Delete permission:', id)
  loadPermissions()
}

// 页面加载时执行
onMounted(() => {
  loadPermissions()
})
</script>

<style scoped>
.permission-manager {
  padding: 20px;
}

.actions {
  margin-bottom: 20px;
}
</style>