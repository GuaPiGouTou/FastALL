<template>
  <div class="file-log-container">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><DataLine /></el-icon> 文件合规归档审计
        </h2>
        <p class="page-subtitle">GxP 全链路审计跟踪 · 元数据存证 · 变更记录</p>
      </div>
      <div class="header-actions">
        <el-button @click="fetchData" :loading="loading" plain round icon="Refresh">刷新数据</el-button>
        <el-button type="primary" color="#6366f1" round icon="Download">导出报告</el-button>
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" :style="{ backgroundColor: stat.color + '20', color: stat.color }">
              <el-icon><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">{{ stat.label }}</div>
              <div class="stat-value">{{ stat.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主表格区域 -->
    <el-card class="main-card">
      <div class="filter-bar">
        <el-input
          v-model="queryParams.fileName"
          placeholder="搜索文件名..."
          prefix-icon="Search"
          style="width: 260px"
          clearable
          @change="handleQuery"
        />
        <el-select v-model="queryParams.fileExt" placeholder="后缀过滤" clearable style="width: 150px" @change="handleQuery">
          <el-option label="PDF" value="pdf" />
          <el-option label="DOCX" value="docx" />
          <el-option label="EXCEL" value="xlsx" />
          <el-option label="ZIP" value="zip" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 320px"
          @change="handleDateChange"
        />
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        class="premium-table"
        :header-cell-style="{ backgroundColor: '#f8fafc', fontWeight: 'bold' }"
      >
        <el-table-column type="index" width="60" align="center" label="ID" />
        
        <el-table-column label="归档文件" min-width="280">
          <template #default="scope">
            <div class="file-info-cell">
              <div class="file-icon-box">
                <el-icon :color="getFileColor(scope.row.fileExt)"><Document /></el-icon>
              </div>
              <div class="file-text">
                <div class="file-name">{{ scope.row.fileName }}</div>
                <div class="file-md5">Fingerprint: {{ scope.row.fileMd5.substring(0, 16) }}...</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="存储路径" min-width="200">
          <template #default="scope">
            <el-link type="primary" :underline="false" :href="scope.row.filePath" target="_blank" class="path-link">
              {{ scope.row.filePath }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column label="大小" width="120" align="center">
          <template #default="scope">
            <span class="size-tag">{{ formatSize(scope.row.fileSize) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="归档时间" width="180" align="center">
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDate(scope.row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="合规状态" width="120" align="center">
          <template #default="scope">
            <el-tag type="success" effect="light" class="status-tag">
              <el-icon><Checked /></el-icon> 已固化
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-dropdown trigger="click">
              <el-button type="primary" link icon="More">详情</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item icon="Download" @click="handleDownload(scope.row)">下载原始副本</el-dropdown-item>
                  <el-dropdown-item icon="Link" @click="copyUrl(scope.row.filePath)">复制访问链接</el-dropdown-item>
                  <el-dropdown-item icon="Search" @click="viewHash(scope.row)">查看哈希摘要</el-dropdown-item>
                  <el-dropdown-item @click="openRenameDialog(scope.row)" icon="Edit">重命名</el-dropdown-item>
                  <el-dropdown-item divided @click="handleDelete(scope.row)" icon="Delete" style="color: #ef4444">删除归档</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 重命名对话框 -->
    <el-dialog v-model="renameDialogVisible" title="重命名归档文件" width="400px" round>
      <el-form :model="renameForm" label-position="top">
        <el-form-item label="新文件名">
          <el-input v-model="renameForm.fileName" placeholder="请输入新文件名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="renameDialogVisible = false">取消</el-button>
          <el-button type="primary" color="#6366f1" @click="submitRename" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { DataLine, Document, Clock, Checked, Refresh, Download, Search, More, Delete, Edit, Link, Box, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { getFileList, deleteFile, updateFile, downloadFile } from '@/api/file'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  fileName: '',
  fileExt: '',
  beginTime: '',
  endTime: ''
})

// 重命名相关状态
const renameDialogVisible = ref(false)
const submitLoading = ref(false)
const renameForm = reactive({
  id: null,
  fileName: ''
})

const stats = ref([
  { label: '累计归档文件', value: '0', icon: 'Document', color: '#6366f1' },
  { label: '总存储占用', value: '0 MB', icon: 'Box', color: '#10b981' },
  { label: '今日新增', value: '0', icon: 'Plus', color: '#f59e0b' },
  { label: '审计合规率', value: '100%', icon: 'Checked', color: '#ec4899' }
])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getFileList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
    
    // 更新简单统计信息 (mock values for now)
    stats.value[0].value = total.value
    let totalSize = tableData.value.reduce((acc, curr) => acc + curr.fileSize, 0)
    stats.value[1].value = formatSize(totalSize)
  } catch (error) {
    console.error('Fetch file list error:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  fetchData()
}

const handleDateChange = (val) => {
  if (val && val.length === 2) {
    // 确保日期对象有效
    const start = new Date(val[0])
    const end = new Date(val[1])
    if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
      queryParams.beginTime = formatDate(start)
      const endOfDay = new Date(end)
      endOfDay.setHours(23, 59, 59, 999)
      queryParams.endTime = formatDate(endOfDay)
    }
  } else {
    queryParams.beginTime = null // 改为 null，确保后端识别
    queryParams.endTime = null
  }
  handleQuery()
}

// 复制链接
const copyUrl = (url) => {
  const input = document.createElement('input')
  input.setAttribute('value', url)
  document.body.appendChild(input)
  input.select()
  document.execCommand('copy')
  document.body.removeChild(input)
  ElMessage.success('访问链接已复制到剪贴板')
}

// 打开重命名弹窗
const openRenameDialog = (row) => {
  renameForm.id = row.id
  renameForm.fileName = row.fileName
  renameDialogVisible.value = true
}

// 提交重命名
const submitRename = async () => {
  if (!renameForm.fileName.trim()) {
    return ElMessage.warning('文件名不能为空')
  }
  submitLoading.value = true
  try {
    await updateFile(renameForm)
    ElMessage.success('重命名成功')
    renameDialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error('重命名失败')
  } finally {
    submitLoading.value = false
  }
}

// 删除归档
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要永久删除文件 [${row.fileName}] 吗？此操作将同步删除云端存储中的物理文件，不可撤销。`,
    '高危审计警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      buttonSize: 'default',
      confirmButtonClass: 'el-button--danger',
      center: true
    }
  ).then(async () => {
    try {
      await deleteFile(row.id)
      ElMessage.success('物理文件及归档记录已彻底删除')
      fetchData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 下载处理
const handleDownload = async (row) => {
  const notify = ElNotification({
    title: '正在准备下载',
    message: `正在从存储引擎安全调取 ${row.fileName}...`,
    duration: 0
  })
  try {
    const res = await downloadFile(row.id)
    const blob = new Blob([res]) // request.js 已经返回了 data 部分
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    notify.close()
    ElMessage.success('下载任务已完成')
  } catch (error) {
    console.error('Download error:', error)
    notify.close()
    ElMessage.error('无法从云端获取该文件，请检查存储服务的访问权限')
  }
}

// 查看哈希
const viewHash = (row) => {
  ElMessageBox.alert(
    `<div style="word-break: break-all; font-family: monospace; background: #f1f5f9; padding: 10px; border-radius: 4px;">${row.fileMd5}</div>`,
    '文件 MD5 指纹 (GxP 存证)',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      customClass: 'hash-alert'
    }
  )
}

const formatSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}:${s}`
}

const getFileColor = (ext) => {
  const colors = {
    pdf: '#ef4444',
    docx: '#3b82f6',
    xlsx: '#10b981',
    zip: '#f59e0b',
    txt: '#64748b'
  }
  return colors[ext?.toLowerCase()] || '#cbd5e1'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.file-log-container {
  animation: fadeIn 0.4s ease-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.page-title {
  font-size: 26px;
  font-weight: 800;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, #0f172a, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-subtitle {
  color: #64748b;
  margin: 0;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.stat-row {
  margin-bottom: 24px;
}

.stat-card {
  border: none;
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -10px rgba(0, 0, 0, 0.1);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.main-card {
  border: none;
  border-radius: 20px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
}

.file-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon-box {
  width: 40px;
  height: 40px;
  background: #f1f5f9;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.file-name {
  font-weight: 600;
  color: #334155;
  font-size: 14px;
}

.file-md5 {
  font-size: 12px;
  color: #94a3b8;
  font-family: 'Monaco', 'Consolas', monospace;
}

.path-link {
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.size-tag {
  color: #64748b;
  font-weight: 500;
}

.time-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
}

.status-tag {
  border: none;
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

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

:deep(.el-table__inner-wrapper::before) {
  display: none;
}
</style>
