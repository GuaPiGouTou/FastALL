<template>
  <div class="dashboard-container">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><DataAnalysis /></el-icon> 系统总览
        </h2>
        <p class="page-subtitle">数据概览 · 系统监控 · 资源管理</p>
      </div>
      <div class="header-actions">
        <el-switch v-model="dashboardVisible" active-text="显示仪表盘" inactive-text="隐藏仪表盘" />
        <el-button @click="refreshData" :loading="loading" plain round :icon="Refresh">刷新数据</el-button>
      </div>
    </div>

    <template v-if="dashboardVisible">
      <!-- 系统总览卡片 -->
      <el-row :gutter="20" class="overview-row">
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card shadow="never" class="overview-card">
            <div class="overview-item">
              <div class="overview-icon" style="background-color: #6366f120; color: #6366f1">
                <el-icon><Files /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-label">数据表总数</div>
                <div class="overview-value">{{ overviewData.tableCount }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card shadow="never" class="overview-card">
            <div class="overview-item">
              <div class="overview-icon" style="background-color: #10b98120; color: #10b981">
                <el-icon><DataLine /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-label">总记录数</div>
                <div class="overview-value">{{ overviewData.totalRecords }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card shadow="never" class="overview-card">
            <div class="overview-item">
              <div class="overview-icon" style="background-color: #f59e0b20; color: #f59e0b">
                <el-icon><MessageBox /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-label">存储占用</div>
                <div class="overview-value">{{ overviewData.storageUsage }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card shadow="never" class="overview-card">
            <div class="overview-item">
              <div class="overview-icon" style="background-color: #ef444420; color: #ef4444">
                <el-icon><DocumentAdd /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-label">本周新增数据量</div>
                <div class="overview-value">{{ overviewData.weeklyNewData }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="content-row">
        <!-- 数据排行 -->
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="content-card">
            <template #header>
              <div class="card-header">
                <span><el-icon><TrendCharts /></el-icon> 数据量排行 Top 5</span>
              </div>
            </template>
            <div class="ranking-container">
              <div v-for="(item, index) in dataRanking" :key="item.tableName" class="ranking-item">
                <div class="ranking-index" :class="getRankingClass(index)">{{ index + 1 }}</div>
                <div class="ranking-info">
                  <div class="ranking-name">{{ item.tableName }}</div>
                  <div class="ranking-desc">{{ item.description }}</div>
                </div>
                <div class="ranking-value">{{ item.recordCount }}</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 系统配额 -->
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="content-card">
            <template #header>
              <div class="card-header">
                <span><el-icon><AlarmClock /></el-icon> 系统配额</span>
              </div>
            </template>
            <div class="quota-container">
              <div v-for="item in systemQuota" :key="item.name" class="quota-item">
                <div class="quota-info">
                  <div class="quota-name">{{ item.name }}</div>
                  <div class="quota-usage">{{ item.usage }} / {{ item.limit }}</div>
                </div>
                <el-progress
                    :percentage="item.percentage"
                    :color="getQuotaColor(item.percentage)"
                    :stroke-width="8"
                    :show-text="false"
                />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 数据库类型选择和连接方式 -->
    <el-card shadow="never" class="db-config-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><Database /></el-icon> 数据库配置</span>
        </div>
      </template>
      <div class="db-config-content">
        <el-row :gutter="20">
          <el-col :xs="24" :lg="12">
            <el-form :model="dbConfig" label-width="120px">
              <el-form-item label="数据库类型">
                <el-select v-model="dbConfig.type" style="width: 100%">
                  <el-option label="MySQL" value="mysql" />
                  <el-option label="PostgreSQL" value="postgresql" />
                  <el-option label="SQLite" value="sqlite" />
                  <el-option label="MongoDB" value="mongodb" />
                </el-select>
              </el-form-item>
              <el-form-item label="连接方式">
                <el-radio-group v-model="dbConfig.connectionType">
                  <el-radio label="local">本地数据库</el-radio>
                  <el-radio label="remote">远程数据库</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="dbConfig.connectionType === 'remote'" label="连接地址">
                <el-input v-model="dbConfig.host" placeholder="数据库主机地址" />
              </el-form-item>
              <el-form-item v-if="dbConfig.connectionType === 'remote'" label="端口">
                <el-input-number v-model="dbConfig.port" :min="1" :max="65535" style="width: 100%" />
              </el-form-item>
              <el-form-item label="数据库名称">
                <el-input v-model="dbConfig.database" placeholder="数据库名称" />
              </el-form-item>
              <el-form-item v-if="dbConfig.connectionType === 'remote'" label="用户名">
                <el-input v-model="dbConfig.username" placeholder="数据库用户名" />
              </el-form-item>
              <el-form-item v-if="dbConfig.connectionType === 'remote'" label="密码">
                <el-input v-model="dbConfig.password" type="password" placeholder="数据库密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="testConnection" :loading="connectionLoading">测试连接</el-button>
                <el-button @click="saveDbConfig">保存配置</el-button>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-card shadow="never" class="connection-status-card">
              <template #header>
                <div class="card-header">
                  <span><el-icon><Link /></el-icon> 实时连接状态</span>
                </div>
              </template>
              <div class="connection-status">
                <div class="status-item">
                  <div class="status-label">连接状态</div>
                  <div class="status-value">
                    <el-tag :type="connectionStatus.status === 'connected' ? 'success' : 'danger'">
                      {{ connectionStatus.status === 'connected' ? '已连接' : '未连接' }}
                    </el-tag>
                  </div>
                </div>
                <div class="status-item">
                  <div class="status-label">数据库类型</div>
                  <div class="status-value">{{ connectionStatus.dbType || '未配置' }}</div>
                </div>
                <div class="status-item">
                  <div class="status-label">响应时间</div>
                  <div class="status-value">{{ connectionStatus.responseTime || '-' }}</div>
                </div>
                <div class="status-item">
                  <div class="status-label">最后连接时间</div>
                  <div class="status-value">{{ connectionStatus.lastConnected || '-' }}</div>
                </div>
              </div>
            </el-card>

            <el-card shadow="never" class="quick-build-card">
              <template #header>
                <div class="card-header">
                  <span><el-icon><MagicStick /></el-icon> 快速构建</span>
                </div>
              </template>
              <div class="quick-build-options">
                <el-button type="primary" :icon="Upload" class="build-btn">
                  导入 Excel/CSV/JSON
                </el-button>
                <el-button :icon="Database" class="build-btn">
                  从现有数据库导入
                </el-button>
                <el-button :icon="Plus" class="build-btn">
                  手动创建空白表
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 数据表管理 -->
    <el-card shadow="never" class="table-management-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><Grid /></el-icon> 数据表管理</span>
          <el-button type="primary" :icon="Plus" @click="createTable">创建新表</el-button>
        </div>
      </template>
      <el-table :data="tables" style="width: 100%" border>
        <el-table-column prop="tableName" label="表名" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="recordCount" label="记录数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" :icon="View" @click="viewTable(scope.row)">查看</el-button>
              <el-button size="small" :icon="Edit" @click="editTable(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" plain :icon="Delete" @click="deleteTable(scope.row)">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  DataAnalysis, Refresh, Files, DataLine, MessageBox, DocumentAdd,
  TrendCharts, AlarmClock, Database, Link, MagicStick, Upload,
  Plus, Grid, View, Edit, Delete
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const dashboardVisible = ref(true)
const connectionLoading = ref(false)

// 系统总览数据
const overviewData = reactive({
  tableCount: 12,
  totalRecords: '1,245,369',
  storageUsage: '128.5 MB',
  weeklyNewData: '24,567'
})

// 数据量排行
const dataRanking = reactive([
  { tableName: 'tb_user', description: '用户表', recordCount: '324,567' },
  { tableName: 'tb_order', description: '订单表', recordCount: '289,123' },
  { tableName: 'tb_product', description: '产品表', recordCount: '156,789' },
  { tableName: 'tb_log', description: '日志表', recordCount: '123,456' },
  { tableName: 'tb_device', description: '设备表', recordCount: '89,234' }
])

// 系统配额
const systemQuota = reactive([
  { name: '表数量上限', usage: 12, limit: 50, percentage: 24 },
  { name: '存储空间', usage: '128.5 MB', limit: '500 MB', percentage: 25.7 },
  { name: '并发连接数', usage: 8, limit: 50, percentage: 16 },
  { name: '查询速率', usage: '120 QPS', limit: '500 QPS', percentage: 24 }
])

// 数据库配置
const dbConfig = reactive({
  type: 'mysql',
  connectionType: 'local',
  host: 'localhost',
  port: 3306,
  database: 'ecmo',
  username: 'root',
  password: ''
})

// 连接状态
const connectionStatus = reactive({
  status: 'connected',
  dbType: 'MySQL',
  responseTime: '12ms',
  lastConnected: '2026-03-26 14:30:45'
})

// 数据表列表
const tables = reactive([
  { tableName: 'tb_user', description: '用户信息表', recordCount: 324567, createTime: '2026-01-01 00:00:00' },
  { tableName: 'tb_order', description: '订单信息表', recordCount: 289123, createTime: '2026-01-02 00:00:00' },
  { tableName: 'tb_product', description: '产品信息表', recordCount: 156789, createTime: '2026-01-03 00:00:00' },
  { tableName: 'tb_log', description: '系统日志表', recordCount: 123456, createTime: '2026-01-04 00:00:00' },
  { tableName: 'tb_device', description: '设备信息表', recordCount: 89234, createTime: '2026-01-05 00:00:00' }
])

const refreshData = async () => {
  loading.value = true
  try {
    // 模拟数据刷新
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

const getRankingClass = (index) => {
  if (index === 0) return 'first'
  if (index === 1) return 'second'
  if (index === 2) return 'third'
  return ''
}

const getQuotaColor = (percentage) => {
  if (percentage < 50) return '#10b981'
  if (percentage < 80) return '#f59e0b'
  return '#ef4444'
}

const testConnection = async () => {
  connectionLoading.value = true
  try {
    // 模拟连接测试
    await new Promise(resolve => setTimeout(resolve, 1500))
    connectionStatus.status = 'connected'
    connectionStatus.dbType = dbConfig.type === 'mysql' ? 'MySQL' :
        dbConfig.type === 'postgresql' ? 'PostgreSQL' :
            dbConfig.type === 'sqlite' ? 'SQLite' : 'MongoDB'
    connectionStatus.responseTime = Math.floor(Math.random() * 20 + 5) + 'ms'
    connectionStatus.lastConnected = new Date().toLocaleString()
    ElMessage.success('连接测试成功')
  } catch (error) {
    connectionStatus.status = 'disconnected'
    ElMessage.error('连接测试失败')
  } finally {
    connectionLoading.value = false
  }
}

const saveDbConfig = () => {
  ElMessage.success('数据库配置保存成功')
}

const createTable = () => {
  ElMessage.info('创建新表功能开发中')
}

const viewTable = (row) => {
  ElMessage.info(`查看表 ${row.tableName}`)
}

const editTable = (row) => {
  ElMessage.info(`编辑表 ${row.tableName}`)
}

const deleteTable = (row) => {
  ElMessage.info(`删除表 ${row.tableName}`)
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  background-color: #f8fafc;
  min-height: calc(100vh - 84px);
  box-sizing: border-box;
}

.page-header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
}

.page-subtitle {
  color: #64748b;
  margin: 4px 0 0 0;
  font-size: 14px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.overview-row {
  margin-bottom: 24px;
}

.overview-card {
  border: none;
  border-radius: 16px;
  transition: all 0.3s ease;
  height: 100%;
}

.overview-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px -10px rgba(0,0,0,0.1);
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 100%;
}

.overview-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.overview-info {
  flex: 1;
}

.overview-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.overview-value {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.content-row {
  margin-bottom: 24px;
}

.content-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  min-height: 300px;
  height: 100%;
}

.card-header {
  font-weight: 600;
  color: #1e293b;
  font-size: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ranking-container {
  padding: 16px 0;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.ranking-item:last-child {
  border-bottom: none;
}

.ranking-index {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  margin-right: 16px;
  font-size: 14px;
  flex-shrink: 0;
}

.ranking-index.first {
  background-color: #f59e0b;
  color: white;
}

.ranking-index.second {
  background-color: #94a3b8;
  color: white;
}

.ranking-index.third {
  background-color: #d97706;
  color: white;
}

.ranking-info {
  flex: 1;
}

.ranking-name {
  font-weight: 600;
  color: #334155;
  font-size: 14px;
}

.ranking-desc {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.ranking-value {
  font-weight: 700;
  color: #1e293b;
  font-size: 16px;
}

.quota-container {
  padding: 16px 0;
}

.quota-item {
  margin-bottom: 16px;
}

.quota-item:last-child {
  margin-bottom: 0;
}

.quota-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.quota-name {
  font-weight: 600;
  color: #334155;
  font-size: 14px;
}

.quota-usage {
  font-size: 13px;
  color: #64748b;
}

.db-config-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.db-config-content {
  padding: 20px 0;
}

.connection-status-card {
  border: none;
  border-radius: 12px;
  margin-bottom: 20px;
  background-color: #f8fafc;
}

.connection-status {
  padding: 16px 0;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.status-item:last-child {
  border-bottom: none;
}

.status-label {
  font-weight: 600;
  color: #334155;
  font-size: 14px;
}

.status-value {
  font-size: 13px;
  color: #64748b;
}

.quick-build-card {
  border: none;
  border-radius: 12px;
  background-color: #f8fafc;
}

.quick-build-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 0;
}

.build-btn {
  width: 100%;
  justify-content: flex-start;
  padding: 12px 16px;
}

.table-management-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}
</style>