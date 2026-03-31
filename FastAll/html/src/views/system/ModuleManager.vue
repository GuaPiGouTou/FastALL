<template>
  <div class="module-manager">
    <h2>模块管理</h2>
    <div class="actions">
      <el-button type="primary" @click="addModule">新增模块</el-button>
    </div>
    <el-table :data="modules" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="moduleName" label="模块名称" width="180"></el-table-column>
      <el-table-column prop="tableName" label="表名" width="180"></el-table-column>
      <el-table-column prop="description" label="描述"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button type="primary" size="small" @click="editModule(scope.row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="deleteModule(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="模块名称" required>
          <el-input v-model="formData.moduleName" placeholder="请输入模块名称"></el-input>
        </el-form-item>
        <el-form-item label="表名" required>
          <el-input v-model="formData.tableName" placeholder="请输入表名"></el-input>
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
          <el-button type="primary" @click="saveModule">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const modules = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增模块')
const formData = ref({})

// 加载模块列表
const loadModules = async () => {
  // 这里应该调用 API 获取模块列表
  // 暂时使用模拟数据
  modules.value = [
    { id: 1, moduleName: '用户管理', tableName: 'tb_user', description: '用户信息管理', status: 1, createTime: '2024-01-01 00:00:00', updateTime: '2024-01-01 00:00:00' },
    { id: 3, moduleName: '订单管理', tableName: 'tb_order', description: '订单信息管理', status: 1, createTime: '2024-01-03 00:00:00', updateTime: '2024-01-03 00:00:00' }
  ]
}

// 新增模块
const addModule = () => {
  dialogTitle.value = '新增模块'
  formData.value = { status: 1 }
  dialogVisible.value = true
}

// 编辑模块
const editModule = (row) => {
  dialogTitle.value = '编辑模块'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 保存模块
const saveModule = async () => {
  // 这里应该调用 API 保存模块
  console.log('Save module:', formData.value)
  dialogVisible.value = false
  loadModules()
}

// 删除模块
const deleteModule = (id) => {
  // 这里应该调用 API 删除模块
  console.log('Delete module:', id)
  loadModules()
}

// 页面加载时执行
onMounted(() => {
  loadModules()
})
</script>

<style scoped>
.module-manager {
  padding: 20px;
}

.actions {
  margin-bottom: 20px;
}
</style>