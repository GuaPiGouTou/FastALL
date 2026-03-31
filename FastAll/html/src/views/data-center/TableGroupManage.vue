<template>
  <div class="group-manage-container">
    <div class="group-header">
      <div class="header-left">
        <h3 class="section-title">
          <el-icon><Folder /></el-icon>
          分组管理
        </h3>
        <span class="group-count">共 {{ groups.length }} 个分组</span>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="showAddDialog">新增分组</el-button>
      </div>
    </div>

    <div class="group-content">
      <div class="group-tree">
        <el-tree
          :data="groupTreeData"
          :props="{ label: 'groupName', children: 'children' }"
          node-key="id"
          :current-node-key="selectedGroupId"
          @node-click="handleGroupClick"
          highlight-current
          default-expand-all
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="node-info">
                <el-icon class="folder-icon"><FolderOpened /></el-icon>
                <span class="node-name">{{ data.groupName }}</span>
                <el-badge :value="getTableCountByGroup(data.id)" :max="99" class="node-badge" />
              </div>
              <div class="node-actions" @click.stop>
                <el-dropdown trigger="click">
                  <el-button type="primary" link size="small">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="showEditDialog(data)">
                        <el-icon><Edit /></el-icon>编辑分组
                      </el-dropdown-item>
                      <el-dropdown-item @click="showMoveTableDialog(data)" v-if="getTableCountByGroup(data.id) > 0">
                        <el-icon><Sort /></el-icon>移动表
                      </el-dropdown-item>
                      <el-dropdown-item divided @click="handleDeleteGroup(data)" :disabled="getTableCountByGroup(data.id) > 0">
                        <el-icon><Delete /></el-icon>删除分组
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </template>
        </el-tree>
      </div>

      <div class="group-tables" v-if="selectedGroup">
        <div class="tables-header">
          <h4>{{ selectedGroup.groupName }} - 数据表</h4>
          <el-button size="small" @click="showBatchMoveDialog" :disabled="selectedTables.length === 0">
            批量移动 ({{ selectedTables.length }})
          </el-button>
        </div>
        <el-table
          :data="currentGroupTables"
          style="width: 100%"
          size="small"
          @selection-change="handleTableSelection"
        >
          <el-table-column type="selection" width="40" />
          <el-table-column prop="tableName" label="表名" min-width="120" />
          <el-table-column prop="description" label="描述" min-width="150" />
          <el-table-column prop="recordCount" label="记录数" width="80" />
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button type="primary" link size="small" @click="moveSingleTable(scope.row)">
                移动
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="currentGroupTables.length === 0" description="该分组下暂无数据表" :image-size="60" />
      </div>
      <div class="group-tables empty" v-else>
        <el-empty description="请选择一个分组查看数据表" :image-size="80" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分组' : '新增分组'" width="450px">
      <el-form :model="groupForm" label-width="80px">
        <el-form-item label="分组名称" required>
          <el-input v-model="groupForm.groupName" placeholder="请输入分组名称" />
        </el-form-item>
        <el-form-item label="分组编码">
          <el-input v-model="groupForm.groupCode" placeholder="请输入分组编码（可选）" />
        </el-form-item>
        <el-form-item label="父级分组">
          <el-cascader
            v-model="groupForm.parentPath"
            :options="groupOptions"
            :props="{ checkStrictly: true, value: 'id', label: 'groupName', emitPath: false }"
            clearable
            placeholder="选择父级分组（可选）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="groupForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="moveDialogVisible" title="移动数据表" width="500px">
      <div class="move-dialog-content">
        <div class="move-info">
          <span>将 </span>
          <el-tag v-for="t in tablesToMove" :key="t.tableName" style="margin: 2px">{{ t.tableName }}</el-tag>
          <span> 移动到：</span>
        </div>
        <el-select v-model="targetGroupId" placeholder="请选择目标分组" style="width: 100%; margin-top: 16px">
          <el-option label="未分组" :value="null" />
          <el-option v-for="g in groups" :key="g.id" :label="g.groupName" :value="g.id" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="moveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleMoveTables" :loading="moving">确定移动</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, FolderOpened, Plus, Edit, Delete, MoreFilled, Sort } from '@element-plus/icons-vue'
import { getTableGroups, createTableGroup, updateTableGroup, deleteTableGroup, updateTableGroupForTable } from '@/api/dataCenter'

const props = defineProps({
  tables: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['refresh'])

const groups = ref([])
const selectedGroupId = ref(null)
const selectedGroup = ref(null)
const selectedTables = ref([])
const tablesToMove = ref([])
const targetGroupId = ref(null)

const dialogVisible = ref(false)
const moveDialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const moving = ref(false)

const groupForm = ref({
  id: null,
  groupName: '',
  groupCode: '',
  parentId: null,
  parentPath: [],
  sortOrder: 0
})

const groupTreeData = computed(() => {
  const map = {}
  const roots = []
  
  groups.value.forEach(g => {
    map[g.id] = { ...g, children: [] }
  })
  
  groups.value.forEach(g => {
    const node = map[g.id]
    if (g.parentId && map[g.parentId]) {
      map[g.parentId].children.push(node)
    } else {
      roots.push(node)
    }
  })
  
  return roots
})

const groupOptions = computed(() => {
  return groupTreeData.value
})

const currentGroupTables = computed(() => {
  if (!selectedGroupId.value) return []
  return props.tables.filter(t => t.groupId === selectedGroupId.value)
})

const getTableCountByGroup = (groupId) => {
  return props.tables.filter(t => t.groupId === groupId).length
}

const loadGroups = async () => {
  try {
    const res = await getTableGroups()
    if (res.code === 200 || res.code === 0) {
      groups.value = res.data || []
    }
  } catch (error) {
    console.error('加载分组失败:', error)
  }
}

const handleGroupClick = (data) => {
  selectedGroupId.value = data.id
  selectedGroup.value = data
  selectedTables.value = []
}

const showAddDialog = () => {
  isEdit.value = false
  groupForm.value = {
    id: null,
    groupName: '',
    groupCode: '',
    parentId: null,
    parentPath: [],
    sortOrder: 0
  }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  groupForm.value = {
    id: row.id,
    groupName: row.groupName,
    groupCode: row.groupCode,
    parentId: row.parentId,
    parentPath: row.parentId ? [row.parentId] : [],
    sortOrder: row.sortOrder || 0
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!groupForm.value.groupName || !groupForm.value.groupName.trim()) {
    ElMessage.warning('请输入分组名称')
    return
  }
  
  saving.value = true
  try {
    const data = {
      groupName: groupForm.value.groupName,
      groupCode: groupForm.value.groupCode,
      parentId: groupForm.value.parentPath && groupForm.value.parentPath.length > 0 
        ? groupForm.value.parentPath[groupForm.value.parentPath.length - 1] 
        : null,
      sortOrder: groupForm.value.sortOrder
    }
    
    let res
    if (isEdit.value) {
      res = await updateTableGroup(groupForm.value.id, data)
    } else {
      res = await createTableGroup(data)
    }
    
    if (res.code === 200 || res.code === 0) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      await loadGroups()
      emit('refresh')
    }
  } catch (error) {
    console.error('保存分组失败:', error)
  } finally {
    saving.value = false
  }
}

const handleDeleteGroup = async (row) => {
  const tableCount = getTableCountByGroup(row.id)
  if (tableCount > 0) {
    ElMessage.warning(`该分组下有 ${tableCount} 个数据表，请先移动或删除这些表`)
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除分组 "${row.groupName}" 吗？`,
      '删除确认',
      { type: 'warning' }
    )
    
    const res = await deleteTableGroup(row.id)
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('删除成功')
      await loadGroups()
      emit('refresh')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分组失败:', error)
    }
  }
}

const handleTableSelection = (selection) => {
  selectedTables.value = selection
}

const showMoveTableDialog = (group) => {
  tablesToMove.value = props.tables.filter(t => t.groupId === group.id)
  targetGroupId.value = null
  moveDialogVisible.value = true
}

const showBatchMoveDialog = () => {
  tablesToMove.value = [...selectedTables.value]
  targetGroupId.value = null
  moveDialogVisible.value = true
}

const moveSingleTable = (table) => {
  tablesToMove.value = [table]
  targetGroupId.value = null
  moveDialogVisible.value = true
}

const handleMoveTables = async () => {
  if (tablesToMove.value.length === 0) {
    ElMessage.warning('请选择要移动的表')
    return
  }
  
  moving.value = true
  try {
    const targetGroup = groups.value.find(g => g.id === targetGroupId.value)
    
    for (const table of tablesToMove.value) {
      await updateTableGroupForTable(table.tableName, {
        groupId: targetGroupId.value,
        groupName: targetGroup ? targetGroup.groupName : null
      })
    }
    
    ElMessage.success(`成功移动 ${tablesToMove.value.length} 个表`)
    moveDialogVisible.value = false
    selectedTables.value = []
    emit('refresh')
  } catch (error) {
    console.error('移动表失败:', error)
    ElMessage.error('移动失败')
  } finally {
    moving.value = false
  }
}

onMounted(() => {
  loadGroups()
})

defineExpose({
  loadGroups
})
</script>

<style scoped>
.group-manage-container {
  background: #fff;
  border-radius: 12px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.group-count {
  font-size: 13px;
  color: #9ca3af;
}

.group-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.group-tree {
  width: 280px;
  border-right: 1px solid #f0f0f0;
  overflow-y: auto;
  padding: 12px;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 8px;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.folder-icon {
  color: #f59e0b;
}

.node-name {
  font-size: 14px;
}

.node-badge {
  margin-left: 4px;
}

.group-tables {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.group-tables.empty {
  display: flex;
  align-items: center;
  justify-content: center;
}

.tables-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.tables-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.move-dialog-content {
  padding: 8px 0;
}

.move-info {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

:deep(.el-tree-node__content) {
  height: 36px;
  border-radius: 6px;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f3f4f6;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #eef2ff;
}
</style>
