<template>
  <div class="audit-log">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><Shield /></el-icon> 数据安全与风控墙
        </h2>
        <p class="page-subtitle">21 CFR Part 11 合规日志 · 自动化操作审计 · 切面隔离采集</p>
      </div>
    </div>

    <el-card class="box-card main-card">
      <!-- 搜索条件 -->
      <div class="search-bar">
        <el-form :inline="true" :model="queryParams" class="custom-form">
          <el-form-item label="操作行为">
            <el-input v-model="queryParams.title" placeholder="请输入核心模块或行为" clearable class="custom-input" />
          </el-form-item>
          <el-form-item label="操作结果">
            <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="custom-input" style="width: 140px">
              <el-option label="执行成功" :value="0" />
              <el-option label="异常拦截" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" color="#6366f1" @click="handleSearch" class="search-btn">
              <template #icon><el-icon><Search /></el-icon></template>
              精准查询
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 日志表格 -->
      <el-table 
        :data="logList" 
        style="width: 100%" 
        v-loading="loading"
        class="custom-table"
      >
        <el-table-column prop="id" label="追踪编号" width="100">
          <template #default="scope">
            <span class="trace-id">#{{ scope.row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="指令源/行为" width="160">
          <template #default="scope">
            <span class="module-name">{{ scope.row.title || '未知操作' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="操作方式" width="120">
          <template #default="scope">
            <el-tag 
              :type="scope.row.status === 0 ? 'success' : 'danger'"
              effect="light"
              round
              class="action-tag"
            >
              {{ scope.row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operUrl" label="请求路由" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <span style="color: #64748b; font-family: 'Monaco', monospace; font-size: 13px;">{{ scope.row.operUrl }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operIp" label="源终端 IP" width="160">
          <template #default="scope">
            <span class="ip-address">
              <el-icon><Monitor /></el-icon> {{ scope.row.operIp }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="时间戳" width="180">
          <template #default="scope">
            <span class="time-text">{{ formatTime(scope.row.operTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="详情查验" align="center" width="100" fixed="right">
          <template #default="scope">
            <el-button 
              link 
              type="primary" 
              class="action-text-btn"
              @click="showDetail(scope.row)"
            >
              <el-icon><View /></el-icon> 审计
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 详情抽屉 -->
      <el-drawer 
        v-model="drawerVisible" 
        title="指令完整追踪记录 (21 CFR Part 11)" 
        size="75%"
        class="custom-drawer"
      >
        <div v-if="currentLog" class="drawer-content">
          <div class="trace-header">
            <div class="trace-title">执行快照 <span>#{{ currentLog.id }}</span></div>
            <el-tag :type="currentLog.status === 0 ? 'success' : 'danger'" class="trace-status" effect="dark" round>
              {{ currentLog.status === 0 ? '执行成功' : '异常风控拦截' }}
            </el-tag>
          </div>

          <el-descriptions title="上下文元数据" :column="2" border>
            <el-descriptions-item label="操作 IP">{{ currentLog.operIp }}</el-descriptions-item>
            <el-descriptions-item label="请求 URI">{{ currentLog.operUrl }}</el-descriptions-item>
            <el-descriptions-item label="HTTP Method">{{ currentLog.requestMethod }}</el-descriptions-item>
            <el-descriptions-item label="时间戳">{{ formatTime(currentLog.operTime) }}</el-descriptions-item>
          </el-descriptions>
          
          <div class="code-block-wrapper mt-4" v-if="currentLog.errorMsg">
            <h4 class="block-title" style="color: #ef4444;"><el-icon><Warning /></el-icon> 异常原因分析 (Stack Trace / Message)</h4>
            <div class="mock-window" style="background: #450a0a; border: 1px solid #7f1d1d;">
              <div class="window-header" style="background: #280505;">
                <span class="dot red"></span><span class="dot yellow"></span><span class="dot green"></span>
              </div>
              <pre class="json-code" style="color: #fca5a5;"><code>{{ currentLog.errorMsg }}</code></pre>
            </div>
          </div>

          <div class="code-block-wrapper mt-4">
            <div class="block-header-row">
              <h4 class="block-title"><el-icon><Finished /></el-icon> 响应业务报文 (JSON Response)</h4>
              <el-button link type="primary" size="small" @click="copyJson(currentLog.jsonResult)">
                <el-icon><CopyDocument /></el-icon> 复制内容
              </el-button>
            </div>
            <div class="mock-window code-display-window">
              <div class="window-header">
                <span class="dot red"></span><span class="dot yellow"></span><span class="dot green"></span>
                <span class="window-title">response_body.json</span>
              </div>
              <pre class="json-code"><code>{{ formatJson(currentLog.jsonResult) }}</code></pre>
            </div>
          </div>
        </div>
      </el-drawer>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOperLogList } from '@/api/system/log'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const logList = ref([])
const total = ref(0)

const queryParams = ref({ 
  current: 1, 
  size: 10,
  title: '', 
  status: '' 
})

const drawerVisible = ref(false)
const currentLog = ref(null)

const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await getOperLogList(queryParams.value)
    if (res.code === 200 && res.data) {
      logList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.msg || '获取日志失败')
    }
  } catch (error) {
    ElMessage.error('网络异常，无法获取审计日志')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.value.current = 1
  fetchLogs()
}

const handleSizeChange = (val) => {
  queryParams.value.size = val
  fetchLogs()
}

const handleCurrentChange = (val) => {
  queryParams.value.current = val
  fetchLogs()
}

const showDetail = (row) => {
  currentLog.value = row
  drawerVisible.value = true
}

const formatJson = (jsonStr) => {
  if (!jsonStr) return 'No Content'
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch {
    return jsonStr
  }
}

const formatTime = (timeArr) => {
  if (!timeArr || !Array.isArray(timeArr)) return timeArr;
  const pad = (n) => n.toString().padStart(2, '0');
  return `${timeArr[0]}-${pad(timeArr[1])}-${pad(timeArr[2])} ${pad(timeArr[3])}:${pad(timeArr[4])}:${pad(timeArr[5])}`;
}

const copyJson = (jsonStr) => {
  if (!jsonStr) return
  navigator.clipboard.writeText(jsonStr).then(() => {
    ElMessage.success('JSON 已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.audit-log {
  width: 100%;
  animation: fadeIn 0.4s ease;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  padding: 0 10px;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, #1e293b, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-subtitle {
  color: #64748b;
  margin: 0;
  font-size: 14px;
}

.main-card {
  border: none;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(16px);
  box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.05);
  min-height: 600px;
}

.search-bar {
  background: rgba(248, 250, 252, 0.8);
  padding: 20px 20px 0 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  border: 1px solid rgba(226, 232, 240, 0.8);
}

:deep(.custom-input .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.search-btn {
  border-radius: 8px;
  font-weight: 600;
}

/* Table Styles */
:deep(.custom-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-border-color: transparent;
  border-radius: 12px;
  overflow: hidden;
}

:deep(.custom-table .el-table__row) {
  transition: all 0.2s ease;
}

:deep(.custom-table .el-table__row:hover > td) {
  background-color: #f1f5f9 !important;
}

.trace-id {
  font-family: 'Monaco', monospace;
  color: #94a3b8;
  font-size: 13px;
}

.module-name {
  font-weight: 600;
  color: #334155;
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
}

.action-tag {
  font-weight: 600;
  letter-spacing: 0.5px;
}

.operator-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #1e293b;
}

.ip-address {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-family: 'Monaco', monospace;
  font-size: 13px;
}

.time-text {
  color: #64748b;
  font-size: 13px;
}

.action-text-btn {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* Drawer & Code Styles */
.drawer-content {
  padding: 0 24px;
}

.trace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.trace-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.trace-title span {
  color: #6366f1;
  font-family: 'Monaco', monospace;
}

.block-title {
  color: #334155;
  margin: 0 0 12px 0;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.mt-4 {
  margin-top: 24px;
}

.mock-window {
  background: #0f172a;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 24px -4px rgba(0, 0, 0, 0.15);
}

.window-header {
  height: 32px;
  background: #1e293b;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 8px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot.red { background: #ef4444; }
.dot.yellow { background: #f59e0b; }
.dot.green { background: #10b981; }

.block-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.code-display-window {
  border: 1px solid #1e293b;
}

.window-title {
  color: #94a3b8;
  font-size: 12px;
  margin-left: auto;
  font-family: 'Monaco', monospace;
}

.json-code {
  margin: 0;
  padding: 20px;
  color: #a5b4fc;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
  overflow-x: auto;
  max-height: 500px;
  overflow-y: auto;
  background: #0f172a;
}

.json-code code {
  white-space: pre-wrap;
  word-wrap: break-word;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
