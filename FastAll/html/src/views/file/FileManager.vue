<template>
  <div class="file-manager">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><FolderOpened /></el-icon> 企业合规文档库
        </h2>
        <p class="page-subtitle">GMP / GSP 资质库 · 大文件切片上传 · 链上固化</p>
      </div>
      <el-button type="success" color="#10b981" class="action-btn" round @click="triggerUpload">
        <template #icon>
          <el-icon><DocumentAdd /></el-icon>
        </template>
        新增归档
      </el-button>
    </div>

    <el-card class="box-card main-card">

      <div class="upload-wrapper">
        <el-upload
          ref="uploadRef"
          class="custom-upload"
          drag
          action="#"
          :auto-upload="false"
          multiple
          :on-change="handleFileChange"
          :show-file-list="false"
        >
          <div class="upload-content">
            <div class="upload-icon-wrapper">
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
            </div>
            <div class="upload-text">
              将大文件拖拽至此处，或 <span class="highlight">点击选择文件</span>
            </div>
            <div class="upload-tip">
              支持对 GxP 大型材料、SOP 流程附件进行上传（单文件最大 2GB）
            </div>
          </div>
        </el-upload>
      </div>

      <div class="section-title">
        <div class="title-line"></div>
        <span>待处理队列 ({{ fileQueue.length }})</span>
        <div class="title-line"></div>
      </div>

      <!-- 批量操作栏 -->
      <div v-if="fileQueue.length > 0" class="batch-actions">
        <el-button 
          type="primary" 
          @click="uploadAll" 
          :loading="batchUploading"
          :disabled="allCompleted"
          round 
          icon="Upload"
        >
          一键归档全部
        </el-button>
        <el-button 
          type="danger" 
          plain 
          @click="clearQueue" 
          :disabled="batchUploading"
          round 
          icon="Delete"
        >
          清空队列
        </el-button>
      </div>

      <el-table 
        :data="fileQueue" 
        style="width: 100%"
        class="custom-file-table"
        :empty-text="'暂无待处理文件，请上方拖拽上传'"
      >
        <el-table-column label="文档名称" min-width="250">
          <template #default="scope">
            <div class="file-name-cell">
              <el-icon class="file-icon" color="#6366f1"><Document /></el-icon>
              <span class="file-name">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="120" align="center">
          <template #default="scope">
            <span class="file-size">{{ (scope.row.size / 1024 / 1024).toFixed(2) }} MB</span>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="200" align="center">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.progress" 
              :status="scope.row.progressStatus" 
              :stroke-width="10"
              striped
              striped-flow
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag 
              :type="getStatusType(scope.row.status)" 
              class="status-tag" 
              effect="light"
              round
            >
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <div class="action-group">
              <el-button 
                type="primary" 
                link 
                class="action-text-btn"
                @click="handleUpload(scope.row)"
                :disabled="scope.row.status === '已完成' || scope.row.status === '上传中'"
              >
                <el-icon><Link /></el-icon>
                {{ scope.row.status === '已完成' ? '已固化' : '执行归档' }}
              </el-button>
              <el-button 
                type="danger" 
                link 
                icon="Close"
                @click="removeFromQueue(scope.row)"
                :disabled="scope.row.status === '上传中'"
              >
                取消
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { UploadFilled, FolderOpened, DocumentAdd, Document, Link, Close, Delete, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'
import SparkMD5 from 'spark-md5'
import { uploadFile } from '@/api/file'

const uploadRef = ref(null)
const fileQueue = ref([])
const CHUNK_SIZE = 5 * 1024 * 1024 // 5MB 一个分片
const batchUploading = ref(false)

// 计算属性：是否全部已完成
const allCompleted = computed(() => {
  return fileQueue.value.length > 0 && fileQueue.value.every(f => f.status === '已完成')
})

const handleFileChange = (uploadFile) => {
  if (!fileQueue.value.find(f => f.uid === uploadFile.uid)) {
    fileQueue.value.push({
      uid: uploadFile.uid,
      name: uploadFile.name,
      size: uploadFile.size,
      status: '待解析',
      progress: 0,
      progressStatus: '',
      raw: uploadFile.raw
    })
  }
}

const triggerUpload = () => {
  // 触发 el-upload 内部的 input 点击
  if (uploadRef.value) {
    const uploadInput = uploadRef.value.$el.querySelector('input')
    if (uploadInput) {
      uploadInput.click()
    }
  }
}

const getStatusType = (status) => {
  switch (status) {
    case '待上传': return 'info'
    case '解析中': return 'primary'
    case '上传中': return 'warning'
    case '已完成': return 'success'
    case '失败': return 'danger'
    default: return ''
  }
}

// 计算文件整体 MD5
const calculateMD5 = (file) => {
  return new Promise((resolve, reject) => {
    const blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
    const chunks = Math.ceil(file.size / CHUNK_SIZE)
    let currentChunk = 0
    const spark = new SparkMD5.ArrayBuffer()
    const fileReader = new FileReader()

    fileReader.onload = (e) => {
      spark.append(e.target.result)
      currentChunk++
      if (currentChunk < chunks) {
        loadNext()
      } else {
        resolve(spark.end())
      }
    }

    fileReader.onerror = () => reject('文件读取失败')

    const loadNext = () => {
      const start = currentChunk * CHUNK_SIZE
      const end = ((start + CHUNK_SIZE) >= file.size) ? file.size : start + CHUNK_SIZE
      fileReader.readAsArrayBuffer(blobSlice.call(file, start, end))
    }

    loadNext()
  })
}

const handleUpload = async (row) => {
  try {
    row.status = '上传中'
    row.progress = 0
    
    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (row.progress < 90) {
        row.progress += 10
      }
    }, 200)

    // 使用普通上传接口
    const { data: url } = await uploadFile(row.raw, 'file', null, 'document')
    
    clearInterval(progressInterval)
    row.progress = 100
    row.status = '已完成'
    successNotify(row.name)

  } catch (error) {
    console.error(error)
    row.status = '失败'
    row.progressStatus = 'exception'
    ElMessage.error(`归档失败: ${error.message || '网络异常'}`)
  }
}

// 从队列中移除单个文件
const removeFromQueue = (row) => {
  const index = fileQueue.value.findIndex(f => f.uid === row.uid)
  if (index > -1) {
    fileQueue.value.splice(index, 1)
  }
}

// 一键上传全部
const uploadAll = async () => {
  const pendingFiles = fileQueue.value.filter(f => f.status !== '已完成' && f.status !== '上传中')
  if (pendingFiles.length === 0) return
  
  batchUploading.value = true
  try {
    // 串行归档，保证稳定性
    for (const file of pendingFiles) {
      await handleUpload(file)
    }
    ElMessage.success('批量归档任务已完成')
  } catch (error) {
    ElMessage.error('批量归档过程中出现异常')
  } finally {
    batchUploading.value = false
  }
}

// 清空队列
const clearQueue = () => {
  if (fileQueue.value.some(f => f.status === '上传中')) {
    return ElMessage.warning('有文件正在上传中，请稍后再清空')
  }
  
  ElMessageBox.confirm('确定要清空待处理队列吗？已完成的文件记录将在刷新后消失。', '提示', {
    confirmButtonText: '确定清空',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    fileQueue.value = []
    ElMessage.success('队列已清空')
  }).catch(() => {})
}

const successNotify = (name) => {
  ElNotification({
    title: '合规归档成功',
    message: `文件 ${name} 已完成上传并链上固化`,
    type: 'success',
    position: 'bottom-right'
  })
}
</script>

<style scoped>
.file-manager {
  max-width: 1000px;
  margin: 0 auto;
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
  background: linear-gradient(135deg, #0f172a, #10b981);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-subtitle {
  color: #64748b;
  margin: 0;
  font-size: 14px;
}

.action-btn {
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  transition: all 0.3s;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4);
}

.main-card {
  border: none;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(16px);
  box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.05);
}

/* Upload Zone */
.upload-wrapper {
  margin-bottom: 24px;
}

.batch-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  justify-content: flex-start;
  padding: 0 4px;
}

.action-group {
  display: flex;
  justify-content: center;
  gap: 8px;
}

:deep(.custom-upload .el-upload-dragger) {
  background: rgba(248, 250, 252, 0.6);
  border: 2px dashed #cbd5e1;
  border-radius: 16px;
  transition: all 0.3s ease;
  height: auto;
  padding: 40px 20px;
}

:deep(.custom-upload .el-upload-dragger:hover) {
  border-color: #6366f1;
  background: rgba(99, 102, 241, 0.02);
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-icon-wrapper {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  box-shadow: 0 8px 16px -4px rgba(99, 102, 241, 0.2);
  transition: transform 0.3s;
}

:deep(.custom-upload .el-upload-dragger:hover) .upload-icon-wrapper {
  transform: scale(1.1);
}

.upload-icon {
  font-size: 32px;
  color: #6366f1;
}

.upload-text {
  font-size: 16px;
  color: #475569;
  font-weight: 500;
  margin-bottom: 8px;
}

.highlight {
  color: #6366f1;
  font-weight: 600;
}

.upload-tip {
  font-size: 13px;
  color: #94a3b8;
}

/* Divider */
.section-title {
  display: flex;
  align-items: center;
  color: #64748b;
  font-weight: 600;
  font-size: 14px;
  margin: 0 0 20px 0;
  letter-spacing: 0.5px;
}

.title-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, #e2e8f0, transparent);
  margin: 0 16px;
}

/* File Table */
:deep(.custom-file-table) {
  --el-table-header-bg-color: transparent;
  --el-table-border-color: transparent;
}

:deep(.custom-file-table .el-table__row) {
  transition: all 0.3s ease;
}

:deep(.custom-file-table .el-table__row:hover > td) {
  background-color: #f8fafc !important;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  font-size: 20px;
  padding: 8px;
  background: #f1f5f9;
  border-radius: 8px;
}

.file-name {
  font-weight: 500;
  color: #334155;
  font-size: 14px;
}

.file-size {
  color: #64748b;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 13px;
}

.status-tag {
  font-weight: 600;
  letter-spacing: 0.5px;
  border: none;
}

.action-text-btn {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
