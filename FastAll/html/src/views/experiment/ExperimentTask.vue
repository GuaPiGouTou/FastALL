<template>
  <div class="task-container">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><Compass /></el-icon> 实验任务调度中心
        </h2>
        <p class="page-subtitle">任务全生命周期追踪 · 资源高效配位 · GxP 电子记录</p>
      </div>
      <div class="header-actions">
        <el-button @click="fetchData" :loading="loading" plain round icon="Refresh">刷新</el-button>
        <el-button type="primary" color="#6366f1" round icon="Plus" @click="handleAdd">创建实验任务</el-button>
      </div>
    </div>

    <!-- 任务看板栅格 -->
    <el-row :gutter="20" class="kanban-row">
      <el-col :span="6" v-for="col in kanbanColumns" :key="col.status">
        <div class="kanban-column">
          <div class="column-header">
            <span class="dot" :style="{ backgroundColor: col.color }"></span>
            <span class="title">{{ col.label }}</span>
            <span class="count">{{ getStatusCount(col.status) }}</span>
          </div>
          
          <div class="task-list" v-loading="loading">
            <el-card 
              v-for="task in getTasksByStatus(col.status)" 
              :key="task.id"
              shadow="hover"
              class="task-card"
              @click="handleEdit(task)"
            >
              <div class="task-priority" :class="'p-' + task.priority"></div>
              <div class="task-card-header">
                <span class="task-id">#{{ task.id }}</span>
                <!-- 使用 stop 阻止事件冒泡到卡片的 handleEdit -->
                <div class="card-actions" @click.stop>
                  <el-dropdown trigger="click">
                    <el-icon class="more-icon"><MoreFilled /></el-icon>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item icon="Edit" @click="handleEdit(task)">编辑详情</el-dropdown-item>
                        <el-dropdown-item icon="Delete" style="color: #ef4444" @click="handleDelete(task)">终止任务</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
              <h4 class="task-title">{{ task.taskName }}</h4>
              <p class="task-desc">{{ task.description || '暂无描述信息...' }}</p>
              
              <div class="task-tags">
                <el-tag size="small" effect="plain" round>{{ getPriorityLabel(task.priority) }}</el-tag>
              </div>

              <div class="task-footer">
                <div class="task-time">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(task.endTime) }}</span>
                </div>
                <div class="task-charge">
                  <el-avatar :size="24" :src="`https://ui-avatars.com/api/?name=${task.chargeUserName || '?'}&background=random`" />
                  <span class="charge-name" :title="task.chargeUserName">{{ task.chargeUserName || '未指派' }}</span>
                </div>
              </div>
            </el-card>

            <div v-if="getTasksByStatus(col.status).length === 0" class="empty-placeholder">
              暂无任务
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑任务详情' : '新建实验任务'" width="700px" class="premium-dialog">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="form.taskName" placeholder="例如：新化合物溶出度稳定性测试" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="负责人" prop="chargeUserName">
              <el-input v-model="form.chargeUserName" placeholder="姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任务优先级" prop="priority">
              <el-select v-model="form.priority" style="width: 100%">
                <el-option label="低 (Low)" :value="0" />
                <el-option label="中 (Medium)" :value="1" />
                <el-option label="高 (High)" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="草稿" :value="0" />
                <el-option label="待执行" :value="1" />
                <el-option label="执行中" :value="2" />
                <el-option label="后期复核" :value="3" />
                <el-option label="已结项" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开始" prop="startTime">
              <el-date-picker v-model="form.startTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束" prop="endTime">
              <el-date-picker v-model="form.endTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="任务描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="结果摘要" prop="resultSummary" v-if="form.status >= 3">
          <el-input v-model="form.resultSummary" type="textarea" :rows="3" placeholder="录入实验主要结论或数据摘要..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">保存任务</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Compass, Refresh, Plus, MoreFilled, Calendar, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExperimentTaskList, addExperimentTask, updateExperimentTask, deleteExperimentTask } from '@/api/experimentTask'

const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const tasks = ref([])

const kanbanColumns = [
  { label: '待处理/草稿', status: 0, color: '#94a3b8' },
  { label: '待执行', status: 1, color: '#6366f1' },
  { label: '执行中', status: 2, color: '#f59e0b' },
  { label: '已完成/复核', status: 4, color: '#10b981' }
]

const queryParams = reactive({
  pageNum: 1,
  pageSize: 100,
  taskName: ''
})

const form = reactive({
  id: undefined,
  taskName: '',
  priority: 1,
  status: 0,
  startTime: '',
  endTime: '',
  chargeUserId: 1, 
  chargeUserName: '',
  description: '',
  resultSummary: ''
})

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  chargeUserName: [{ required: true, message: '请输入负责人姓名', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getExperimentTaskList(queryParams)
    console.log('Task List Debug:', res.data.records)
    tasks.value = res.data.records || []
  } catch(e) {
    ElMessage.error('获取列表失败')
  } finally { loading.value = false }
}

const getTasksByStatus = (status) => {
  if (status === 4) return tasks.value.filter(t => t.status >= 3)
  return tasks.value.filter(t => t.status === status)
}

const getStatusCount = (status) => getTasksByStatus(status).length

const handleAdd = () => { resetForm(); dialogVisible.value = true }

const handleEdit = (task) => {
  resetForm()
  nextTick(() => {
    Object.assign(form, JSON.parse(JSON.stringify(task)))
    dialogVisible.value = true
  })
}

const handleDelete = (task) => {
  ElMessageBox.confirm(`确定终止并删除实验任务 [${task.taskName}] 吗？`, '警告', { type: 'error' }).then(async () => {
    try {
      await deleteExperimentTask(task.id)
      ElMessage.success('任务已终止')
      fetchData()
    } catch(e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const submitForm = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.id) {
          console.log('Updating task:', form)
          await updateExperimentTask(form)
        } else {
          await addExperimentTask(form)
        }
        ElMessage.success('保存成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {
        console.error('Submit task error:', e)
        ElMessage.error('保存失败：' + (e.message || '网络异常'))
      } finally { submitLoading.value = false }
    } else {
      ElMessage.warning('请补充完整任务信息')
    }
  })
}

const resetForm = () => {
  Object.assign(form, {
    id: undefined, taskName: '', priority: 1, status: 0,
    startTime: '', endTime: '', chargeUserId: 1, chargeUserName: '',
    description: '', resultSummary: ''
  })
  if (formRef.value) formRef.value.clearValidate()
}

const getPriorityLabel = (p) => ['低', '中', '高'][p] || '未知'
const formatDate = (date) => date ? new Date(date).toLocaleDateString() : '未定'

onMounted(fetchData)
</script>

<style scoped>
.task-container {
  padding: 24px;
  background-color: #f1f5f9;
  min-height: calc(100vh - 84px);
}
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #0f172a; display: flex; align-items: center; gap: 12px; margin: 0; }
.page-subtitle { color: #64748b; margin: 4px 0 0 36px; font-size: 14px; }

.kanban-row { height: calc(100vh - 200px); overflow-x: auto; flex-wrap: nowrap; padding-bottom: 12px; }
.kanban-column {
  background-color: #e2e8f0;
  border-radius: 12px;
  min-height: 500px;
  display: flex;
  flex-direction: column;
  padding: 12px;
  width: 100%;
}

.column-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}
.column-header .dot { width: 8px; height: 8px; border-radius: 50%; margin-right: 8px; }
.column-header .title { font-weight: 700; color: #475569; font-size: 14px; flex: 1; }
.column-header .count { background: #cbd5e1; color: #475569; padding: 2px 8px; border-radius: 10px; font-size: 12px; }

.task-list { flex: 1; overflow-y: auto; }
.task-card {
  margin-bottom: 12px;
  border: none;
  border-radius: 10px;
  position: relative;
  cursor: pointer;
  transition: all 0.2s;
}
.task-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }

.task-priority { position: absolute; left: 0; top: 12px; bottom: 12px; width: 4px; border-radius: 0 2px 2px 0; }
.task-priority.p-0 { background-color: #94a3b8; }
.task-priority.p-1 { background-color: #6366f1; }
.task-priority.p-2 { background-color: #ef4444; }

.task-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.task-id { font-family: 'JetBrains Mono'; font-size: 12px; color: #94a3b8; }
.more-icon { color: #94a3b8; cursor: pointer; }

.task-title { margin: 0 0 8px 0; font-size: 15px; color: #1e293b; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.task-desc { margin: 0 0 12px 0; font-size: 13px; color: #64748b; font-style: italic; }

.task-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 12px; border-top: 1px solid #f1f5f9; }
.task-time { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #64748b; }

.task-charge { display: flex; align-items: center; gap: 8px; }
.charge-name { font-size: 12px; color: #475569; font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 80px; }

.empty-placeholder { text-align: center; color: #94a3b8; font-size: 13px; padding-top: 40px; }
</style>
