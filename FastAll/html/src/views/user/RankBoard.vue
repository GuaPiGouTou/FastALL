<template>
  <div class="rank-board">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><Trophy /></el-icon> 员工绩效龙虎榜
        </h2>
        <p class="page-subtitle">实时统计当月各科室及代表的邀请奖励积分</p>
      </div>
      <el-button color="#6366f1" class="refresh-btn" :loading="loading" @click="fetchRank" round>
        <template #icon>
          <el-icon><Refresh /></el-icon>
        </template>
        刷新榜单
      </el-button>
    </div>

    <el-card class="box-card rank-card">

      <el-table 
        :data="rankList" 
        style="width: 100%" 
        v-loading="loading"
        class="custom-table"
        :row-class-name="tableRowClassName"
      >
        <el-table-column label="排名" width="100" align="center">
          <template #default="scope">
            <div :class="['rank-badge-wrapper', `rank-wrapper-${scope.$index + 1}`]">
              <span :class="['rank-badge', `rank-${scope.$index + 1}`]">
                {{ scope.$index + 1 }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="业务精英">
          <template #default="scope">
            <div class="user-info">
              <el-avatar 
                :size="40" 
                :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${scope.row.username}`" 
                class="user-avatar"
              />
              <span class="user-name">{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="总积分" align="right" width="200">
          <template #default="scope">
            <div class="score-display">
              <el-icon class="score-icon"><Coin /></el-icon>
              <span class="score-value">{{ scope.row.score.toLocaleString() }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const rankList = ref([])

// 模拟获取数据的接口
const fetchRank = async () => {
  loading.value = true
  try {
    // 模拟数据加载延迟以展示动画
    setTimeout(() => {
      rankList.value = [
        { username: '李医生 (心内科)', score: 12500 },
        { username: '王主任 (外科)', score: 9800 },
        { username: '张药师 (临床药房)', score: 8600 },
        { username: '陈代表 (华东大区)', score: 4500 },
        { username: '林代表 (华南大区)', score: 3200 },
        { username: '刘护士长 (急诊科)', score: 2800 },
        { username: '赵主任 (呼吸科)', score: 2100 }
      ]
      loading.value = false
      ElMessage({
        message: '🏆 榜单数据已更新',
        type: 'success',
        customClass: 'premium-message'
      })
    }, 800)
  } catch (error) {
    console.error('获取榜单失败', error)
    loading.value = false
  }
}

const tableRowClassName = ({ rowIndex }) => {
  if (rowIndex === 0) return 'top-1-row'
  if (rowIndex === 1) return 'top-2-row'
  if (rowIndex === 2) return 'top-3-row'
  return ''
}

onMounted(() => {
  fetchRank()
})
</script>

<style scoped>
.rank-board {
  max-width: 900px;
  margin: 0 auto;
  animation: fadeIn 0.5s ease;
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

.refresh-btn {
  font-weight: 600;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  transition: all 0.3s;
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
}

.rank-card {
  border: none;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(16px);
  box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

/* Custom Table Styles */
:deep(.custom-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: rgba(248, 250, 252, 0.8);
  --el-table-header-text-color: #64748b;
}

:deep(.custom-table .el-table__row) {
  transition: all 0.3s ease;
  cursor: pointer;
}

:deep(.custom-table .el-table__row:hover > td) {
  background-color: #f8fafc !important;
}

:deep(.custom-table .el-table__cell) {
  padding: 16px 0;
}

/* Rank Badges */
.rank-badge-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.rank-badge {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  font-weight: 800;
  font-size: 16px;
  color: #64748b;
  background: #f1f5f9;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.02);
  transition: all 0.3s;
}

.rank-wrapper-1 .rank-badge {
  background: linear-gradient(135deg, #fef08a, #f59e0b);
  color: #78350f;
  box-shadow: 0 8px 16px -4px rgba(245, 158, 11, 0.4);
  transform: scale(1.15);
}

.rank-wrapper-2 .rank-badge {
  background: linear-gradient(135deg, #e2e8f0, #94a3b8);
  color: #1e293b;
  box-shadow: 0 8px 16px -4px rgba(148, 163, 184, 0.4);
  transform: scale(1.05);
}

.rank-wrapper-3 .rank-badge {
  background: linear-gradient(135deg, #fed7aa, #ea580c);
  color: #431407;
  box-shadow: 0 8px 16px -4px rgba(234, 88, 12, 0.4);
}

/* Top 3 Row Highlights */
:deep(.top-1-row) {
  background: linear-gradient(90deg, rgba(245, 158, 11, 0.05), transparent) !important;
}
:deep(.top-2-row) {
  background: linear-gradient(90deg, rgba(148, 163, 184, 0.05), transparent) !important;
}
:deep(.top-3-row) {
  background: linear-gradient(90deg, rgba(234, 88, 12, 0.05), transparent) !important;
}

/* User Info */
.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-avatar {
  background: #f1f5f9;
  border: 2px solid white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.user-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 15px;
}

.custom-table .el-table__row:hover .user-name {
  color: #6366f1;
}

/* Score Display */
.score-display {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: rgba(16, 185, 129, 0.1);
  border-radius: 20px;
  color: #059669;
}

.score-icon {
  font-size: 16px;
}

.score-value {
  font-weight: 700;
  font-size: 16px;
  font-family: 'Monaco', 'Consolas', monospace;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
