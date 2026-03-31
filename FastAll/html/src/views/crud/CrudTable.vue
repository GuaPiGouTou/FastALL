<template>
  <div class="crud-table">
    <h2>{{ tableName }} 管理</h2>
    
    <!-- 操作栏 -->
    <div class="actions">
      <el-button type="primary" @click="addData">新增</el-button>
      <el-button type="success" @click="exportData">导出</el-button>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索"
        style="width: 200px; margin-left: 10px"
        clearable
      >
        <template #append>
          <el-button @click="searchData"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
    </div>

    <!-- 数据列表 -->
    <el-table :data="tableData" style="width: 100%">
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <template v-for="field in tableSchema" :key="field.prop">
        <el-table-column
          v-if="field.isShowInList === 1"
          :prop="field.prop"
          :label="field.label"
        ></el-table-column>
      </template>
      <el-table-column prop="createdTime" label="创建时间" width="180"></el-table-column>
      <el-table-column prop="updatedTime" label="更新时间" width="180"></el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button type="primary" size="small" @click="editData(scope.row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="deleteData(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      ></el-pagination>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="formData" label-width="100px">
        <template v-for="field in tableSchema" :key="field.prop">
          <el-form-item :label="field.label">
            <el-input
              v-if="field.uiType === 'Input'"
              v-model="formData[field.prop]"
              :placeholder="`请输入${field.label}`"
            ></el-input>
            <el-input-number
              v-else-if="field.uiType === 'InputNumber'"
              v-model="formData[field.prop]"
              :min="0"
              :max="1000000"
            ></el-input-number>
            <el-date-picker
              v-else-if="field.uiType === 'DatePicker'"
              v-model="formData[field.prop]"
              type="date"
              placeholder="选择日期"
              style="width: 100%"
            ></el-date-picker>
            <el-switch
              v-else-if="field.uiType === 'Switch'"
              v-model="formData[field.prop]"
            ></el-switch>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveData">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const tableName = computed(() => route.params.tableName || 'tb_user')

const tableSchema = ref([])
const tableData = ref([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增数据')
const formData = ref({})

const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

// 加载表结构
const loadTableSchema = async () => {
  // 这里应该调用 API 获取表结构
  // 暂时使用模拟数据
  tableSchema.value = [
    { id: 1, tableName: tableName.value, prop: 'name', label: '姓名', uiType: 'Input', options: null, isShowInList: 1, sortOrder: 1 },
    { id: 2, tableName: tableName.value, prop: 'age', label: '年龄', uiType: 'InputNumber', options: null, isShowInList: 1, sortOrder: 2 },
    { id: 3, tableName: tableName.value, prop: 'email', label: '邮箱', uiType: 'Input', options: null, isShowInList: 1, sortOrder: 3 },
    { id: 4, tableName: tableName.value, prop: 'phone', label: '电话', uiType: 'Input', options: null, isShowInList: 1, sortOrder: 4 }
  ]
}

// 加载数据
const loadTableData = async () => {
  // 这里应该调用 API 获取数据
  // 暂时使用模拟数据
  tableData.value = [
    { id: 1, name: '张三', age: 25, email: 'zhangsan@example.com', phone: '13800138000', createdTime: '2024-01-01 00:00:00', updatedTime: '2024-01-01 00:00:00' },
    { id: 2, name: '李四', age: 30, email: 'lisi@example.com', phone: '13900139000', createdTime: '2024-01-02 00:00:00', updatedTime: '2024-01-02 00:00:00' },
    { id: 3, name: '王五', age: 28, email: 'wangwu@example.com', phone: '13700137000', createdTime: '2024-01-03 00:00:00', updatedTime: '2024-01-03 00:00:00' }
  ]
  pagination.value.total = tableData.value.length
}

// 搜索数据
const searchData = () => {
  // 这里应该调用 API 搜索数据
  console.log('Search keyword:', searchKeyword.value)
  loadTableData()
}

// 新增数据
const addData = () => {
  dialogTitle.value = '新增数据'
  formData.value = {}
  dialogVisible.value = true
}

// 编辑数据
const editData = (row) => {
  dialogTitle.value = '编辑数据'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 保存数据
const saveData = async () => {
  // 这里应该调用 API 保存数据
  console.log('Save data:', formData.value)
  dialogVisible.value = false
  loadTableData()
}

// 删除数据
const deleteData = (id) => {
  // 这里应该调用 API 删除数据
  console.log('Delete data:', id)
  loadTableData()
}

// 导出数据
const exportData = () => {
  // 这里应该调用 API 导出数据
  console.log('Export data')
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.value.size = size
  loadTableData()
}

const handleCurrentChange = (current) => {
  pagination.value.current = current
  loadTableData()
}

// 页面加载时执行
onMounted(() => {
  loadTableSchema()
  loadTableData()
})
</script>

<style scoped>
.crud-table {
  padding: 20px;
}

.actions {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>