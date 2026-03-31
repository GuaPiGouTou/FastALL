<template>
  <div class="audit-log">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><Monitor /></el-icon> 系统登录与访问控制拦截
        </h2>
        <p class="page-subtitle">识别异常访问流量 · 审计用户登录行为</p>
      </div>
    </div>

    <el-card class="box-card main-card">
      <!-- 搜索条件 -->
      <div class="search-bar">
        <el-form :inline="true" :model="queryParams" class="custom-form">
          <el-form-item label="登入用户">
            <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable class="custom-input" />
          </el-form-item>
          <el-form-item label="登录状态">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="custom-input" style="width: 160px">
              <el-option label="登录成功" :value="0" />
              <el-option label="登录失败" :value="1" />
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
        <el-table-column prop="id" label="记录 ID" width="100">
          <template #default="scope">
            <span class="trace-id">#{{ scope.row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="登入账户" width="140">
          <template #default="scope">
            <div class="operator-cell">
              <el-avatar :size="24" :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${scope.row.username}`" />
              <span>{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="终端 IP" width="150">
          <template #default="scope">
            <span class="ip-address">
              <el-icon><Connection /></el-icon> {{ scope.row.ipAddress }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="browser" label="浏览器标识" width="160" show-overflow-tooltip/>
        <el-table-column prop="os" label="操作系统" width="160" show-overflow-tooltip/>
        <el-table-column prop="status" label="访问结果" width="120">
          <template #default="scope">
            <el-tag 
              :type="scope.row.status === 0 ? 'success' : 'danger'"
              effect="light"
              round
              class="action-tag"
            >
              {{ scope.row.status === 0 ? '准入成功' : '风控拦截' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="msg" label="反馈信息" min-width="160" show-overflow-tooltip/>
        <el-table-column prop="loginTime" label="发生时间" width="180">
          <template #default="scope">
            <span class="time-text">{{ formatTime(scope.row.loginTime) }}</span>
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLoginLogList } from '@/api/system/log'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const queryParams = ref({ 
  current: 1, 
  size: 10,
  username: '', 
  status: '' 
})

const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await getLoginLogList(queryParams.value)
    if (res.code === 200 && res.data) {
      logList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.msg || '获取列表失败')
    }
  } catch (error) {
    ElMessage.error('网络异常，请稍后重试')
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

const formatTime = (timeArr) => {
  if (!timeArr || !Array.isArray(timeArr)) return timeArr;
  const pad = (n) => n.toString().padStart(2, '0');
  return `${timeArr[0]}-${pad(timeArr[1])}-${pad(timeArr[2])} ${pad(timeArr[3])}:${pad(timeArr[4])}:${pad(timeArr[5])}`;
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

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
