<template>
  <div class="device-container">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><Monitor /></el-icon> 精密设备运行看板
        </h2>
        <p class="page-subtitle">设备台账管理 · 状态实时监测 · 自动校准提醒</p>
      </div>
      <div class="header-actions">
        <el-button @click="fetchData" :loading="loading" plain round :icon="Refresh">刷新</el-button>
        <el-button type="primary" color="#6366f1" round :icon="Plus" @click="handleAdd">新增设备</el-button>
      </div>
    </div>

    <!-- 统计区块 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="item in dynamicStats" :key="item.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" :style="{ backgroundColor: item.color + '20', color: item.color }">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主表 -->
    <el-card class="main-card">
      <div class="filter-bar">
        <el-input
          v-model="queryParams.deviceCode"
          placeholder="搜索设备编号..."
          :prefix-icon="Search"
          style="width: 260px"
          clearable
          @keyup.enter="handleQuery"
        />
        <el-button type="primary" plain :icon="Search" @click="handleQuery">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" style="width: 100%" class="premium-table">
        <el-table-column prop="deviceCode" label="设备编号" width="150">
          <template #default="scope">
            <span class="code-text">{{ scope.row.deviceCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="型号/规格" min-width="150" />
        <el-table-column prop="department" label="所属科室" width="150" />
        <el-table-column label="运行状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light" round>
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="校准周期" width="300" align="center">
          <template #default="scope">
            <div class="calibration-cell">
              <div class="date-info">
                <span class="label">上次:</span> {{ formatDate(scope.row.lastCalibrationTime) }}
              </div>
              <div class="date-info highlight">
                <span class="label">下次:</span> {{ formatDate(scope.row.nextCalibrationTime) }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" :icon="Calendar" @click="handleMaintenance(scope.row)">维保</el-button>
              <el-button size="small" :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" plain :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 设备信息弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑设备' : '新增设备'" width="600px" class="premium-dialog">
      <el-form :model="form" label-width="110px" :rules="rules" ref="formRef">
        <el-form-item label="设备编号" prop="deviceCode">
          <el-input v-model="form.deviceCode" placeholder="如: ECU-2024-001" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备型号" prop="model">
              <el-input v-model="form.model" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="序列号" prop="serialNumber">
              <el-input v-model="form.serialNumber" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责科室" prop="department">
              <el-input v-model="form.department" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="运行状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="闲置 (Idle)" :value="0" />
                <el-option label="在用 (Running)" :value="1" />
                <el-option label="维保 (Maintenance)" :value="2" />
                <el-option label="故障 (Error)" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上次校准" prop="lastCalibrationTime">
              <el-date-picker v-model="form.lastCalibrationTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下次校准" prop="nextCalibrationTime">
              <el-date-picker v-model="form.nextCalibrationTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">保 存</el-button>
      </template>
    </el-dialog>

    <!-- 维保登记弹窗 (已加宽) -->
    <el-dialog v-model="maintenanceVisible" title="预防性维护 (PM) 与校准登记" width="960px" class="premium-dialog">
      <div class="maintenance-layout">
        <div class="maintenance-main">
          <div class="section-title">维保信息录入</div>
          <el-form :model="maintenanceForm" label-width="100px" :rules="maintenanceRules" ref="maintenanceFormRef">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="维保人" prop="maintenancePerson">
                  <el-input v-model="maintenanceForm.maintenancePerson" placeholder="姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="维保日期" prop="maintenanceDate">
                  <el-date-picker v-model="maintenanceForm.maintenanceDate" type="datetime" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="维保内容" prop="content">
              <el-input v-model="maintenanceForm.content" type="textarea" :rows="4" placeholder="录入维保工作要点、更换配件、校准参数等..." />
            </el-form-item>
            <el-form-item label="下次建议" prop="nextCalibrationTime">
              <el-date-picker v-model="maintenanceForm.nextCalibrationTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" placeholder="设置下次校准/维保提醒日期" />
            </el-form-item>
          </el-form>
          <div class="form-actions">
            <el-button @click="maintenanceVisible = false">取消</el-button>
            <el-button type="primary" color="#6366f1" @click="submitMaintenance" :loading="maintenanceLoading">提交维保记录</el-button>
          </div>
        </div>

        <div class="maintenance-aside">
          <div class="section-title">设备维保足迹</div>
          <el-skeleton :loading="historyLoading" animated>
            <template #template>
              <el-skeleton-item variant="p" style="width: 100%; margin-bottom: 10px;" />
              <el-skeleton-item variant="p" style="width: 80%;" />
            </template>
            <div class="history-scroll" v-if="maintenanceHistory.length > 0">
              <el-timeline>
                <el-timeline-item
                  v-for="h in maintenanceHistory"
                  :key="h.id"
                  :timestamp="formatDate(h.maintenanceDate)"
                  placement="top"
                  type="primary"
                >
                  <el-card shadow="never" class="history-card">
                    <div class="h-user"><el-icon><User /></el-icon> {{ h.maintenancePerson }}</div>
                    <div class="h-content">{{ h.content }}</div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
            <div v-else class="h-empty">
              <el-icon size="40"><Document /></el-icon>
              <p>尚无历史记录</p>
            </div>
          </el-skeleton>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { Monitor, Refresh, Plus, Search, Cpu, Connection, Tools, Warning, Calendar, Edit, Delete, User, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeviceList, addDevice, updateDevice, deleteDevice } from '@/api/device'
import { getMaintenanceList, addMaintenance } from '@/api/deviceMaintenance'

const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const total = ref(0)

// 维保相关
const maintenanceVisible = ref(false)
const maintenanceLoading = ref(false)
const historyLoading = ref(false)
const maintenanceFormRef = ref(null)
const maintenanceHistory = ref([])
const activeDevice = ref(null)

const maintenanceForm = reactive({
  deviceId: undefined,
  maintenanceDate: '',
  maintenancePerson: '',
  content: '',
  nextCalibrationTime: ''
})

const maintenanceRules = {
  maintenancePerson: [{ required: true, message: '请输入维保人', trigger: 'blur' }],
  maintenanceDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  content: [{ required: true, message: '请填写维保内容', trigger: 'blur' }]
}

const dynamicStats = computed(() => [
  { label: '总台账设备', value: total.value, icon: Cpu, color: '#6366f1' },
  { label: '正在运行中', value: tableData.value.filter(i => i.status === 1).length, icon: Connection, color: '#10b981' },
  { label: '需校准提醒', value: tableData.value.filter(i => i.nextCalibrationTime && new Date(i.nextCalibrationTime) < new Date()).length, icon: Warning, color: '#f59e0b' },
  { label: '维保/故障', value: tableData.value.filter(i => i.status >= 2).length, icon: Tools, color: '#ef4444' }
])

const queryParams = reactive({
  pageNum: 1, 
  pageSize: 10,
  deviceCode: ''
})

const form = reactive({
  id: undefined,
  deviceCode: '',
  model: '',
  serialNumber: '',
  department: '',
  status: 0,
  lastCalibrationTime: '',
  nextCalibrationTime: ''
})

const rules = {
  deviceCode: [{ required: true, message: '必填', trigger: 'blur' }],
  status: [{ required: true, message: '必填', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDeviceList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取列表失败')
  } finally { loading.value = false }
}

const handleQuery = () => { queryParams.pageNum = 1; fetchData() }

const handleAdd = () => { resetForm(); dialogVisible.value = true }

const handleEdit = (row) => {
  resetForm()
  nextTick(() => {
    Object.assign(form, JSON.parse(JSON.stringify(row)))
    dialogVisible.value = true
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`物理删除设备记录 [${row.deviceCode}] 将无法找回，继续？`, '严重警告', { 
    type: 'error',
    confirmButtonText: '强制删除'
  }).then(async () => {
    try {
      await deleteDevice(row.id)
      ElMessage.success('已删除')
      fetchData()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 获取当前系统时间并格式化
const getNowTime = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

const handleMaintenance = async (row) => {
  activeDevice.value = row
  maintenanceForm.deviceId = row.id
  maintenanceForm.maintenanceDate = getNowTime()
  maintenanceForm.maintenancePerson = ''
  maintenanceForm.content = ''
  maintenanceForm.nextCalibrationTime = ''
  
  maintenanceVisible.value = true
  
  // 加载历史记录
  historyLoading.value = true
  try {
    const res = await getMaintenanceList(row.id)
    maintenanceHistory.value = res.data || []
  } catch (e) {
    ElMessage.error('获取历史记录失败')
  } finally { historyLoading.value = false }
}

const submitMaintenance = () => {
  maintenanceFormRef.value.validate(async (valid) => {
    if (valid) {
      maintenanceLoading.value = true
      try {
        console.log('Submit Maintenance Data:', maintenanceForm)
        const res = await addMaintenance(maintenanceForm)
        if(res.code === 200) {
          ElMessage.success('维保记录已同步，设备状态已恢复')
          maintenanceVisible.value = false
          fetchData()
        }
      } catch (e) {
        console.error('Submit Maintenance Error:', e)
        ElMessage.error('维保保存失败：' + (e.message || '网络异常'))
      } finally { maintenanceLoading.value = false }
    } else {
      ElMessage.warning('请补充完整维保项')
    }
  })
}

const submitForm = () => {
  if (!formRef.value) return
  formRef.value.validate(async (v) => {
    if (v) {
      submitLoading.value = true
      try {
        if (form.id) await updateDevice(form)
        else await addDevice(form)
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {
        ElMessage.error('保存失败')
      } finally { submitLoading.value = false }
    }
  })
}

const resetForm = () => {
  Object.assign(form, {
    id: undefined, deviceCode: '', model: '', serialNumber: '',
    department: '', status: 0, lastCalibrationTime: '', nextCalibrationTime: ''
  })
  if (formRef.value) formRef.value.clearValidate()
}

const getStatusType = (s) => {
  const m = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return m[s] || 'info'
}

const getStatusLabel = (s) => {
  const m = { 0: '闲置', 1: '在用', 2: '维保', 3: '故障' }
  return m[s] || '未知'
}

const formatDate = (d) => d ? new Date(d).toLocaleDateString() : '-'

onMounted(fetchData)
</script>

<style scoped>
.device-container {
  padding: 24px;
  background-color: #f8fafc;
  min-height: calc(100vh - 84px);
}
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e293b; display: flex; align-items: center; gap: 12px; margin: 0; }
.page-subtitle { color: #64748b; margin: 4px 0 0 36px; font-size: 14px; }
.stat-row { margin-bottom: 24px; }
.stat-card { border: none; border-radius: 16px; transition: all 0.3s; }
.stat-card:hover { transform: translateY(-5px); box-shadow: 0 12px 24px -10px rgba(0,0,0,0.1); }
.stat-item { display: flex; gap: 16px; align-items: center; }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.stat-label { font-size: 13px; color: #64748b; }
.stat-value { font-size: 20px; font-weight: 700; color: #1e293b; }
.main-card { border: none; border-radius: 20px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
.filter-bar { display: flex; gap: 16px; margin-bottom: 20px; }
.code-text { font-family: 'JetBrains Mono', monospace; font-weight: 600; color: #6366f1; }
.calibration-cell { display: flex; flex-direction: column; gap: 4px; font-size: 12px; }
.date-info .label { color: #64748b; margin-right: 4px; }
.date-info.highlight { color: #475569; font-weight: 600; }
.pagination-container { margin-top: 24px; display: flex; justify-content: flex-end; }

.maintenance-layout { display: flex; gap: 40px; }
.maintenance-main { flex: 1; border-right: 1px solid #f1f5f9; padding-right: 40px; }
.maintenance-aside { flex: 0.8; }
.section-title { font-size: 16px; font-weight: 700; color: #1e293b; margin-bottom: 24px; padding-left: 10px; border-left: 4px solid #6366f1; }
.form-actions { margin-top: 32px; display: flex; justify-content: flex-end; gap: 12px; }

.history-scroll { height: 400px; overflow-y: auto; padding-right: 10px; }
.history-card { border: 1px solid #f1f5f9; border-radius: 8px; margin-bottom: 4px; }
.h-user { display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 700; color: #475569; margin-bottom: 8px; }
.h-content { font-size: 13px; color: #64748b; line-height: 1.6; }
.h-empty { text-align: center; color: #94a3b8; padding-top: 80px; }
.h-empty p { font-size: 14px; margin-top: 12px; }

.premium-dialog :deep(.el-dialog) { border-radius: 20px; overflow: hidden; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25); }
.premium-dialog :deep(.el-dialog__header) { margin: 0; padding: 24px; background: #fff; border-bottom: 1px solid #f1f5f9; }
.premium-dialog :deep(.el-dialog__body) { padding: 30px 40px; }
</style>
