<template>
  <div class="crud-dashboard">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="left">
        <h2>CRUD 管理</h2>
        <el-tag :type="connectionStatus ? 'success' : 'danger'" effect="dark">
          {{ connectionStatus ? '已连接' : '未连接' }}
        </el-tag>
      </div>
      <div class="right">
        <el-select v-model="selectedDbType" placeholder="选择数据库类型" style="width: 150px" @change="handleDbTypeChange">
          <el-option label="MySQL" value="mysql" />
          <el-option label="PostgreSQL" value="postgresql" />
          <el-option label="SQLite" value="sqlite" />
          <el-option label="MongoDB" value="mongodb" />
        </el-select>
        <el-button type="primary" @click="showConnectDialog">
          <el-icon><Connection /></el-icon>
          连接数据库
        </el-button>
      </div>
    </div>

    <!-- 系统总览卡片 -->
    <div class="overview-section" v-show="!hideDashboard">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon" style="color: #409EFF"><Table /></el-icon>
              <div class="stat-info">
                <div class="stat-value">{{ overview.tableCount }}</div>
                <div class="stat-label">数据表总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon" style="color: #67C23A"><DataLine /></el-icon>
              <div class="stat-info">
                <div class="stat-value">{{ overview.totalRecords }}</div>
                <div class="stat-label">总记录数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon" style="color: #E6A23C"><Coin /></el-icon>
              <div class="stat-info">
                <div class="stat-value">{{ overview.storageUsed }}</div>
                <div class="stat-label">存储占用</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon" style="color: #F56C6C"><TrendCharts /></el-icon>
              <div class="stat-info">
                <div class="stat-value">{{ overview.weeklyNew }}</div>
                <div class="stat-label">本周新增</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <el-row :gutter="20">
        <!-- 左侧：数据排行和系统配额 -->
        <el-col :span="6" v-show="!hideDashboard">
          <!-- 数据排行 -->
          <el-card class="ranking-card">
            <template #header>
              <div class="card-header">
                <span>数据量排行 Top5</span>
              </div>
            </template>
            <div class="ranking-list">
              <div v-for="(item, index) in rankingList" :key="item.tableName" class="ranking-item">
                <div class="ranking-index" :class="'top-' + (index + 1)">{{ index + 1 }}</div>
                <div class="ranking-info">
                  <div class="ranking-name">{{ item.tableName }}</div>
                  <el-progress :percentage="item.percentage" :show-text="false" />
                </div>
                <div class="ranking-count">{{ item.count }} 条</div>
              </div>
            </div>
          </el-card>

          <!-- 系统配额 -->
          <el-card class="quota-card">
            <template #header>
              <div class="card-header">
                <span>系统配额</span>
              </div>
            </template>
            <div class="quota-info">
              <div class="quota-item">
                <span>表数量上限</span>
                <span>{{ quota.tableLimit }}</span>
              </div>
              <div class="quota-item">
                <span>已使用</span>
                <span>{{ quota.tableUsed }}</span>
              </div>
              <el-progress :percentage="quota.usagePercentage" :format="format" />
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：快速构建和数据表管理 -->
        <el-col :span="hideDashboard ? 24 : 18">
          <!-- 快速构建 -->
          <el-card class="quick-build-card">
            <template #header>
              <div class="card-header">
                <span>快速构建</span>
              </div>
            </template>
            <div class="build-options">
              <div class="build-option" @click="importExcel">
                <el-icon size="32"><Document /></el-icon>
                <span>导入 Excel</span>
              </div>
              <div class="build-option" @click="importCSV">
                <el-icon size="32"><Document /></el-icon>
                <span>导入 CSV</span>
              </div>
              <div class="build-option" @click="importJSON">
                <el-icon size="32"><Document /></el-icon>
                <span>导入 JSON</span>
              </div>
              <div class="build-option" @click="importFromDb">
                <el-icon size="32"><Database /></el-icon>
                <span>从数据库导入</span>
              </div>
              <div class="build-option" @click="createEmptyTable">
                <el-icon size="32"><Plus /></el-icon>
                <span>创建空白表</span>
              </div>
            </div>
          </el-card>

          <!-- 数据表管理 -->
          <el-card class="table-list-card">
            <template #header>
              <div class="card-header">
                <span>数据表管理</span>
                <el-button type="primary" size="small" @click="refreshTables">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </template>
            <el-table :data="tableList" style="width: 100%">
              <el-table-column prop="tableName" label="表名" width="180" />
              <el-table-column prop="moduleName" label="模块名称" width="150" />
              <el-table-column prop="description" label="描述" />
              <el-table-column prop="recordCount" label="记录数" width="100" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                    {{ scope.row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="manageTable(scope.row)">
                    管理
                  </el-button>
                  <el-button type="warning" size="small" @click="editTable(scope.row)">
                    编辑
                  </el-button>
                  <el-button type="danger" size="small" @click="deleteTable(scope.row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 隐藏仪表盘按钮 -->
    <el-button 
      class="toggle-dashboard" 
      :icon="hideDashboard ? ArrowUp : ArrowDown" 
      circle 
      @click="hideDashboard = !hideDashboard"
    />

    <!-- 连接数据库对话框 -->
    <el-dialog v-model="connectDialogVisible" title="连接数据库" width="500px">
      <el-form :model="connectForm" label-width="100px">
        <el-form-item label="连接方式">
          <el-radio-group v-model="connectForm.connectionType">
            <el-radio label="local">本地数据库</el-radio>
            <el-radio label="remote">远程数据库</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="主机地址" v-if="connectForm.connectionType === 'remote'">
          <el-input v-model="connectForm.host" placeholder="请输入主机地址" />
        </el-form-item>
        <el-form-item label="端口" v-if="connectForm.connectionType === 'remote'">
          <el-input v-model="connectForm.port" placeholder="请输入端口" />
        </el-form-item>
        <el-form-item label="数据库名">
          <el-input v-model="connectForm.database" placeholder="请输入数据库名" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="connectForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="connectForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="文件路径" v-if="connectForm.connectionType === 'local' && (selectedDbType === 'sqlite' || selectedDbType === 'mongodb')">
          <el-input v-model="connectForm.filePath" placeholder="请输入文件路径" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="connectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConnect">连接</el-button>
      </template>
    </el-dialog>

    <!-- 导入文件对话框 -->
    <el-dialog v-model="importDialogVisible" :title="importDialogTitle" width="500px">
      <el-upload
        drag
        :action="importAction"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :before-upload="beforeImportUpload"
        :auto-upload="false"
        ref="uploadRef"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">
            {{ importTip }}
          </div>
        </template>
      </el-upload>
      <el-form :model="importForm" label-width="100px" style="margin-top: 20px">
        <el-form-item label="模块名称">
          <el-input v-model="importForm.moduleName" placeholder="请输入模块名称" />
        </el-form-item>
        <el-form-item label="表名（可选）">
          <el-input v-model="importForm.tableName" placeholder="不填写则自动生成" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitImport">确定导入</el-button>
      </template>
    </el-dialog>

    <!-- 创建空白表对话框 -->
    <el-dialog v-model="createTableDialogVisible" title="创建空白表" width="600px">
      <el-form :model="createTableForm" label-width="100px">
        <el-form-item label="表名">
          <el-input v-model="createTableForm.tableName" placeholder="请输入表名" />
        </el-form-item>
        <el-form-item label="模块名称">
          <el-input v-model="createTableForm.moduleName" placeholder="请输入模块名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createTableForm.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="字段设计">
          <el-button type="primary" size="small" @click="addField">添加字段</el-button>
          <el-table :data="createTableForm.fields" style="width: 100%; margin-top: 10px">
            <el-table-column prop="name" label="字段名" width="120">
              <template #default="scope">
                <el-input v-model="scope.row.name" size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="120">
              <template #default="scope">
                <el-select v-model="scope.row.type" size="small">
                  <el-option label="VARCHAR" value="VARCHAR" />
                  <el-option label="INT" value="INT" />
                  <el-option label="BIGINT" value="BIGINT" />
                  <el-option label="DECIMAL" value="DECIMAL" />
                  <el-option label="TEXT" value="TEXT" />
                  <el-option label="DATETIME" value="DATETIME" />
                  <el-option label="BOOLEAN" value="BOOLEAN" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="length" label="长度" width="80">
              <template #default="scope">
                <el-input v-model="scope.row.length" size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="nullable" label="可空" width="60">
              <template #default="scope">
                <el-checkbox v-model="scope.row.nullable" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="scope">
                <el-button type="danger" size="small" @click="removeField(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createTableDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTable">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Connection, Table, DataLine, Coin, TrendCharts, Document, Database, Plus, Refresh,
  ArrowUp, ArrowDown, UploadFilled
} from '@element-plus/icons-vue'
import { getModuleList, deleteModule } from '@/api/crud'
import request from '@/utils/request'

const router = useRouter()

// 状态
const connectionStatus = ref(true)
const hideDashboard = ref(false)
const selectedDbType = ref('mysql')

// 系统总览数据
const overview = ref({
  tableCount: 0,
  totalRecords: 0,
  storageUsed: '0 MB',
  weeklyNew: 0
})

// 数据排行
const rankingList = ref([])

// 系统配额
const quota = ref({
  tableLimit: 100,
  tableUsed: 0,
  usagePercentage: 0
})

// 数据表列表
const tableList = ref([])

// 连接对话框
const connectDialogVisible = ref(false)
const connectForm = ref({
  connectionType: 'remote',
  host: '',
  port: '',
  database: '',
  username: '',
  password: '',
  filePath: ''
})

// 导入对话框
const importDialogVisible = ref(false)
const importDialogTitle = ref('')
const importAction = ref('')
const importTip = ref('')
const importForm = ref({
  moduleName: '',
  tableName: ''
})
const uploadRef = ref(null)

// 创建表对话框
const createTableDialogVisible = ref(false)
const createTableForm = ref({
  tableName: '',
  moduleName: '',
  description: '',
  fields: []
})

// 加载系统总览数据
const loadOverview = async () => {
  try {
    const res = await request({ url: '/api/crud/overview', method: 'get' })
    if (res.code === 200 || res.code === 0) {
      overview.value = res.data
    }
  } catch (error) {
    console.error('加载系统总览失败:', error)
  }
}

// 加载数据排行
const loadRanking = async () => {
  try {
    const res = await request({ url: '/api/crud/ranking', method: 'get' })
    if (res.code === 200 || res.code === 0) {
      rankingList.value = res.data || []
    }
  } catch (error) {
    console.error('加载数据排行失败:', error)
  }
}

// 加载系统配额
const loadQuota = async () => {
  try {
    const res = await request({ url: '/api/crud/quota', method: 'get' })
    if (res.code === 200 || res.code === 0) {
      quota.value = res.data
    }
  } catch (error) {
    console.error('加载系统配额失败:', error)
  }
}

// 加载数据表列表
const loadTables = async () => {
  try {
    const res = await getModuleList()
    if (res.code === 200 || res.code === 0) {
      tableList.value = res.data || []
    }
  } catch (error) {
    console.error('加载数据表列表失败:', error)
  }
}

// 刷新数据表
const refreshTables = () => {
  loadTables()
  loadOverview()
  loadRanking()
  loadQuota()
}

// 显示连接对话框
const showConnectDialog = () => {
  connectDialogVisible.value = true
}

// 处理数据库类型变化
const handleDbTypeChange = (type) => {
  if (type === 'mysql') {
    connectForm.value.port = '3306'
  } else if (type === 'postgresql') {
    connectForm.value.port = '5432'
  } else if (type === 'mongodb') {
    connectForm.value.port = '27017'
  }
}

// 处理连接
const handleConnect = async () => {
  try {
    const res = await request({
      url: '/api/crud/connect',
      method: 'post',
      data: {
        dbType: selectedDbType.value,
        ...connectForm.value
      }
    })
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('连接成功')
      connectionStatus.value = true
      connectDialogVisible.value = false
      refreshTables()
    }
  } catch (error) {
    ElMessage.error('连接失败')
  }
}

// 导入 Excel
const importExcel = () => {
  importDialogTitle.value = '导入 Excel'
  importAction.value = '/api/file/import-excel'
  importTip.value = '请上传 Excel 文件，系统将自动创建表结构和导入数据'
  importDialogVisible.value = true
}

// 导入 CSV
const importCSV = () => {
  importDialogTitle.value = '导入 CSV'
  importAction.value = '/api/file/import-csv'
  importTip.value = '请上传 CSV 文件，系统将自动创建表结构和导入数据'
  importDialogVisible.value = true
}

// 导入 JSON
const importJSON = () => {
  importDialogTitle.value = '导入 JSON'
  importAction.value = '/api/file/import-json'
  importTip.value = '请上传 JSON 文件，系统将自动创建表结构和导入数据'
  importDialogVisible.value = true
}

// 从数据库导入
const importFromDb = () => {
  ElMessage.info('从数据库导入功能开发中...')
}

// 创建空白表
const createEmptyTable = () => {
  createTableForm.value = {
    tableName: '',
    moduleName: '',
    description: '',
    fields: []
  }
  createTableDialogVisible.value = true
}

// 添加字段
const addField = () => {
  createTableForm.value.fields.push({
    name: '',
    type: 'VARCHAR',
    length: '',
    nullable: true
  })
}

// 移除字段
const removeField = (index) => {
  createTableForm.value.fields.splice(index, 1)
}

// 处理创建表
const handleCreateTable = async () => {
  try {
    const res = await request({
      url: '/api/crud/table/create',
      method: 'post',
      data: createTableForm.value
    })
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('创建成功')
      createTableDialogVisible.value = false
      refreshTables()
    }
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

// 上传前处理
const beforeImportUpload = (file) => {
  return true
}

// 导入成功
const handleImportSuccess = (response) => {
  if (response.code === 200 || response.code === 0) {
    ElMessage.success('导入成功')
    importDialogVisible.value = false
    refreshTables()
  } else {
    ElMessage.error('导入失败: ' + response.msg)
  }
}

// 导入失败
const handleImportError = (error) => {
  ElMessage.error('导入失败')
}

// 提交导入
const submitImport = () => {
  if (uploadRef.value) {
    uploadRef.value.submit()
  }
}

// 管理表
const manageTable = (row) => {
  router.push(`/crud/${row.tableName}`)
}

// 编辑表
const editTable = (row) => {
  ElMessage.info('编辑功能开发中...')
}

// 删除表
const deleteTable = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该表吗？删除后数据将无法恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteModule(row.tableName)
    ElMessage.success('删除成功')
    refreshTables()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 格式化进度条
const format = (percentage) => `${percentage}%`

// 页面加载时执行
onMounted(() => {
  loadOverview()
  loadRanking()
  loadQuota()
  loadTables()
})
</script>

<style scoped>
.crud-dashboard {
  padding: 20px;
  position: relative;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.toolbar .left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.toolbar .right {
  display: flex;
  gap: 10px;
}

.overview-section {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 48px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.main-content {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ranking-card, .quota-card {
  margin-bottom: 20px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ranking-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  background: #909399;
  color: #fff;
}

.ranking-index.top-1 {
  background: #FFD700;
}

.ranking-index.top-2 {
  background: #C0C0C0;
}

.ranking-index.top-3 {
  background: #CD7F32;
}

.ranking-info {
  flex: 1;
}

.ranking-name {
  font-size: 14px;
  margin-bottom: 5px;
}

.ranking-count {
  font-size: 14px;
  font-weight: bold;
  color: #409EFF;
}

.quota-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quota-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.quick-build-card {
  margin-bottom: 20px;
}

.build-options {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.build-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border: 2px dashed #DCDFE6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 120px;
}

.build-option:hover {
  border-color: #409EFF;
  background: #ECF5FF;
}

.table-list-card {
  margin-bottom: 20px;
}

.toggle-dashboard {
  position: fixed;
  bottom: 100px;
  right: 30px;
  z-index: 100;
}
</style>
