<template>
  <div class="api-generator">
    <div class="generator-header">
      <div class="header-left">
        <el-icon class="header-icon"><MagicStick /></el-icon>
        <h2>API 生成器</h2>
      </div>
      <div class="header-right">
        <el-button @click="goToManager">
          <el-icon><Back /></el-icon>
          返回API管理
        </el-button>
      </div>
    </div>

    <div class="generator-content">
      <div class="steps-container">
        <el-steps :active="currentStep" align-center>
          <el-step title="基础配置" :icon="Edit" />
          <el-step title="执行逻辑" :icon="Operation" />
          <el-step title="参数配置" :icon="Setting" />
          <el-step title="完成" :icon="Check" />
        </el-steps>
      </div>

      <div class="step-content-wrapper">
        <div v-show="currentStep === 0" class="step-content">
          <div class="step-title">
            <span class="step-num">01</span>
            <span class="step-text">基础信息配置</span>
          </div>
          <el-form :model="wizardForm" label-width="100px" class="api-form">
            <el-form-item label="API名称" required>
              <el-input v-model="wizardForm.apiName" placeholder="请输入API名称，如：用户列表查询" />
            </el-form-item>
            <el-form-item label="API路径" required>
              <el-input v-model="wizardForm.apiPath" placeholder="/user/list">
                <template #prepend>/api/dynamic</template>
              </el-input>
            </el-form-item>
            <el-form-item label="请求方法" required>
              <el-radio-group v-model="wizardForm.apiMethod" class="method-group">
                <el-radio-button label="GET">GET</el-radio-button>
                <el-radio-button label="POST">POST</el-radio-button>
                <el-radio-button label="PUT">PUT</el-radio-button>
                <el-radio-button label="DELETE">DELETE</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="所属分组">
              <el-select v-model="wizardForm.groupId" style="width: 100%" placeholder="请选择分组">
                <el-option v-for="g in groups" :key="g.id" :label="g.groupName" :value="g.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="描述">
              <el-input v-model="wizardForm.description" type="textarea" :rows="2" placeholder="请输入API描述" />
            </el-form-item>
          </el-form>
        </div>

        <div v-show="currentStep === 1" class="step-content">
          <div class="step-title">
            <span class="step-num">02</span>
            <span class="step-text">执行逻辑配置</span>
          </div>
          <el-form :model="wizardForm" label-width="100px" class="api-form">
            <el-form-item label="执行模式" required>
              <el-radio-group v-model="wizardForm.execMode" class="exec-mode-group">
                <el-radio-button label="AUTO">
                  <div class="mode-content">
                    <el-icon><Operation /></el-icon>
                    <span>自动CRUD</span>
                  </div>
                </el-radio-button>
                <el-radio-button label="SQL">
                  <div class="mode-content">
                    <el-icon><Document /></el-icon>
                    <span>SQL模式</span>
                  </div>
                </el-radio-button>
              </el-radio-group>
            </el-form-item>

            <template v-if="wizardForm.execMode === 'AUTO'">
              <el-form-item label="数据分组" required>
                <el-select v-model="wizardForm.dataCenterGroupId" style="width: 100%" placeholder="请选择数据中心分组" @change="onDataCenterGroupChange">
                  <el-option label="全部分组" :value="null" />
                  <el-option v-for="g in flattenDataCenterGroups" :key="g.id" :label="g.groupName" :value="g.id">
                    <span :style="{ paddingLeft: (g.level || 0) * 20 + 'px' }">{{ g.groupName }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="数据表" required>
                <el-select v-model="wizardForm.tableName" style="width: 100%" placeholder="请选择数据表" :loading="loadingTables" @change="onTableChange">
                  <el-option v-for="t in dataCenterTables" :key="t.tableName" :label="t.tableName + (t.description ? ' - ' + t.description : '')" :value="t.tableName" />
                </el-select>
              </el-form-item>
              <el-form-item label="操作类型" required>
                <el-radio-group v-model="wizardForm.autoOperation" class="operation-group">
                  <el-radio label="list">查询列表</el-radio>
                  <el-radio label="get">查询单条</el-radio>
                  <el-radio label="create">新增</el-radio>
                  <el-radio label="update">更新</el-radio>
                  <el-radio label="delete">删除</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="表字段" v-if="tableColumns.length > 0">
                <div class="field-list">
                  <el-tag v-for="col in tableColumns" :key="col.columnName || col.fieldName" class="field-tag" effect="plain">
                    <span class="field-name">{{ col.columnName || col.fieldName }}</span>
                    <span class="field-type">{{ col.dataType || col.fieldType }}</span>
                  </el-tag>
                </div>
              </el-form-item>
            </template>

            <template v-if="wizardForm.execMode === 'SQL'">
              <el-form-item label="数据分组" required>
                <el-select v-model="wizardForm.dataCenterGroupId" style="width: 100%" placeholder="请选择数据中心分组" @change="onDataCenterGroupChange">
                  <el-option label="全部分组" :value="null" />
                  <el-option v-for="g in flattenDataCenterGroups" :key="g.id" :label="g.groupName" :value="g.id">
                    <span :style="{ paddingLeft: (g.level || 0) * 20 + 'px' }">{{ g.groupName }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="SQL模板" required>
                <el-input
                  v-model="wizardForm.sqlTemplate"
                  type="textarea"
                  :rows="8"
                  placeholder="SELECT * FROM user WHERE id = {{id}}"
                  class="sql-input"
                />
              </el-form-item>
              <el-form-item label="可用表">
                <el-button size="small" @click="loadDataCenterTables(wizardForm.dataCenterGroupId)" :loading="loadingTables">
                  <el-icon><Refresh /></el-icon>
                  加载表列表
                </el-button>
                <div class="field-list" v-if="dataCenterTables.length > 0" style="margin-top: 10px;">
                  <el-tag v-for="t in dataCenterTables" :key="t.tableName" class="field-tag clickable" @click="insertTableName(t.tableName)">
                    {{ t.tableName }}
                  </el-tag>
                </div>
              </el-form-item>
            </template>
          </el-form>
        </div>

        <div v-show="currentStep === 2" class="step-content">
          <div class="step-title">
            <span class="step-num">03</span>
            <span class="step-text">参数配置</span>
          </div>
          <div class="params-section">
            <div class="section-header">
              <span class="section-title">请求参数</span>
              <el-button type="primary" size="small" @click="addRequestParam">
                <el-icon><Plus /></el-icon>
                添加参数
              </el-button>
            </div>
            <el-table :data="wizardForm.requestParams" size="small" v-if="wizardForm.requestParams.length > 0">
              <el-table-column prop="paramName" label="参数名" width="130">
                <template #default="scope">
                  <el-input v-model="scope.row.paramName" size="small" placeholder="参数名" />
                </template>
              </el-table-column>
              <el-table-column prop="paramType" label="类型" width="100">
                <template #default="scope">
                  <el-select v-model="scope.row.paramType" size="small">
                    <el-option label="String" value="String" />
                    <el-option label="Integer" value="Integer" />
                    <el-option label="Boolean" value="Boolean" />
                    <el-option label="Array" value="Array" />
                    <el-option label="Object" value="Object" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="required" label="必填" width="60" align="center">
                <template #default="scope">
                  <el-checkbox v-model="scope.row.required" :true-value="1" :false-value="0" />
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述">
                <template #default="scope">
                  <el-input v-model="scope.row.description" size="small" placeholder="描述" />
                </template>
              </el-table-column>
              <el-table-column width="50" align="center">
                <template #default="scope">
                  <el-button type="danger" size="small" link @click="removeRequestParam(scope.$index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无参数，点击上方按钮添加" :image-size="60" />
          </div>

          <div class="params-section">
            <div class="section-header">
              <span class="section-title">响应配置</span>
            </div>
            <el-form :model="wizardForm" label-width="80px">
              <el-form-item label="响应格式">
                <el-select v-model="wizardForm.responseFormat" style="width: 200px">
                  <el-option label="JSON对象" value="object" />
                  <el-option label="JSON数组" value="array" />
                  <el-option label="分页数据" value="page" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <div v-show="currentStep === 3" class="step-content">
          <div class="step-title">
            <span class="step-num">04</span>
            <span class="step-text">确认配置</span>
          </div>
          <el-result icon="success" title="配置完成" sub-title="请确认以下配置信息后点击创建按钮">
            <template #extra>
              <el-descriptions :column="1" border class="confirm-desc">
                <el-descriptions-item label="API名称">
                  <span class="confirm-value">{{ wizardForm.apiName }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="API路径">
                  <span class="confirm-value path">/api/dynamic{{ wizardForm.apiPath }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="请求方法">
                  <span :class="['method-tag', `method-${wizardForm.apiMethod?.toLowerCase()}`]">
                    {{ wizardForm.apiMethod }}
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="执行模式">
                  <el-tag effect="plain">{{ wizardForm.execMode === 'AUTO' ? '自动CRUD' : 'SQL模式' }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="数据分组">
                  <span class="confirm-value">{{ getDataCenterGroupName(wizardForm.dataCenterGroupId) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="数据表" v-if="wizardForm.execMode === 'AUTO'">
                  <span class="confirm-value">{{ wizardForm.tableName }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="操作类型" v-if="wizardForm.execMode === 'AUTO'">
                  <el-tag effect="plain">{{ getOperationText(wizardForm.autoOperation) }}</el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </template>
          </el-result>
        </div>
      </div>

      <div class="step-footer">
        <el-button @click="prevStep" v-if="currentStep > 0">
          <el-icon><ArrowLeft /></el-icon>
          上一步
        </el-button>
        <el-button @click="nextStep" v-if="currentStep < 3" type="primary">
          下一步
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button @click="handleCreate" v-if="currentStep === 3" type="success" :loading="creating">
          <el-icon><Check /></el-icon>
          创建API
        </el-button>
        <el-button @click="handleCreateAndContinue" v-if="currentStep === 3" :loading="creating">
          创建并继续
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Plus, Delete, Edit, Operation, Setting, Check, ArrowLeft, ArrowRight, 
  Refresh, MagicStick, Document, Back
} from '@element-plus/icons-vue'
import * as api from '@/api/apiManagerV2'

const router = useRouter()

const currentStep = ref(0)
const creating = ref(false)
const loadingTables = ref(false)

const groups = ref([])
const dataCenterGroups = ref([])
const dataCenterTables = ref([])
const tableColumns = ref([])

const wizardForm = ref({
  apiName: '',
  apiPath: '',
  apiMethod: 'GET',
  groupId: null,
  description: '',
  execMode: 'AUTO',
  dataCenterGroupId: null,
  tableName: '',
  sqlTemplate: '',
  autoOperation: 'list',
  responseFormat: 'object',
  requestParams: []
})

const flattenDataCenterGroups = computed(() => {
  const result = []
  const flatten = (groups, level = 0) => {
    for (const group of groups) {
      result.push({ ...group, level })
      if (group.children && group.children.length > 0) {
        flatten(group.children, level + 1)
      }
    }
  }
  flatten(dataCenterGroups.value)
  return result
})

const loadGroups = async () => {
  const res = await api.getApiGroups()
  if (res.code === 200 || res.code === 0) {
    groups.value = res.data || []
  }
}

const loadDataCenterGroups = async () => {
  const res = await api.getDataCenterGroupTree()
  if (res.code === 200 || res.code === 0) {
    dataCenterGroups.value = res.data || []
  }
}

const loadDataCenterTables = async (groupId) => {
  loadingTables.value = true
  try {
    if (groupId) {
      const res = await api.getDataCenterTables(groupId)
      if (res.code === 200 || res.code === 0) {
        dataCenterTables.value = res.data || []
      }
    } else {
      const res = await api.getAllDataCenterTables()
      if (res.code === 200 || res.code === 0) {
        dataCenterTables.value = res.data || []
      }
    }
  } finally {
    loadingTables.value = false
  }
}

const onDataCenterGroupChange = async (groupId) => {
  wizardForm.value.tableName = ''
  tableColumns.value = []
  dataCenterTables.value = []
  await loadDataCenterTables(groupId)
}

const onTableChange = async (tableName) => {
  if (tableName) {
    const res = await api.getTableColumnsDirect(tableName)
    if (res.code === 200 || res.code === 0) {
      tableColumns.value = res.data || []
      autoGenerateParams()
    }
  }
}

const autoGenerateParams = () => {
  if (wizardForm.value.execMode === 'AUTO') {
    wizardForm.value.requestParams = tableColumns.value.map(col => ({
      paramName: col.columnName || col.fieldName,
      paramType: mapFieldType(col.dataType || col.fieldType),
      required: col.isNullable === false || col.key === 'PRI' ? 1 : 0,
      description: col.remarks || ''
    }))
  }
}

const mapFieldType = (mysqlType) => {
  if (!mysqlType) return 'String'
  const type = mysqlType.toLowerCase()
  if (type.includes('int') || type.includes('decimal') || type.includes('float') || type.includes('double')) {
    return 'Integer'
  }
  if (type.includes('bool')) {
    return 'Boolean'
  }
  if (type.includes('json') || type.includes('text')) {
    return 'Object'
  }
  return 'String'
}

const insertTableName = (tableName) => {
  wizardForm.value.sqlTemplate += tableName
}

const getDataCenterGroupName = (groupId) => {
  if (!groupId) return '全部分组'
  const group = flattenDataCenterGroups.value.find(g => g.id === groupId)
  return group ? group.groupName : '-'
}

const getOperationText = (op) => {
  const texts = {
    list: '查询列表',
    get: '查询单条',
    create: '新增',
    update: '更新',
    delete: '删除'
  }
  return texts[op] || op
}

const addRequestParam = () => {
  wizardForm.value.requestParams.push({
    paramName: '',
    paramType: 'String',
    required: 0,
    description: ''
  })
}

const removeRequestParam = (index) => {
  wizardForm.value.requestParams.splice(index, 1)
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const nextStep = () => {
  if (currentStep.value === 0) {
    if (!wizardForm.value.apiName || !wizardForm.value.apiPath) {
      ElMessage.warning('请填写API名称和路径')
      return
    }
  }
  if (currentStep.value === 1) {
    if (wizardForm.value.execMode === 'AUTO' && !wizardForm.value.tableName) {
      ElMessage.warning('请选择数据表')
      return
    }
    if (wizardForm.value.execMode === 'SQL' && !wizardForm.value.sqlTemplate) {
      ElMessage.warning('请填写SQL模板')
      return
    }
  }
  if (currentStep.value < 3) {
    currentStep.value++
  }
}

const handleCreate = async () => {
  await createApi()
  router.push('/tool/api-manager')
}

const handleCreateAndContinue = async () => {
  await createApi()
  resetForm()
}

const createApi = async () => {
  creating.value = true
  try {
    const data = {
      apiName: wizardForm.value.apiName,
      apiPath: wizardForm.value.apiPath,
      apiMethod: wizardForm.value.apiMethod,
      groupId: wizardForm.value.groupId,
      description: wizardForm.value.description,
      execMode: wizardForm.value.execMode,
      dataCenterGroupId: wizardForm.value.dataCenterGroupId,
      tableName: wizardForm.value.tableName,
      sqlTemplate: wizardForm.value.sqlTemplate,
      authType: 'TOKEN',
      rateLimit: 0,
      corsEnabled: 1,
      mockEnabled: 0,
      requestParams: wizardForm.value.requestParams,
      responseParams: []
    }
    
    const res = await api.createApi(data)
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('创建成功')
      return true
    } else {
      ElMessage.error(res.msg || '创建失败')
      return false
    }
  } catch (e) {
    ElMessage.error('创建失败: ' + e.message)
    return false
  } finally {
    creating.value = false
  }
}

const resetForm = () => {
  currentStep.value = 0
  wizardForm.value = {
    apiName: '',
    apiPath: '',
    apiMethod: 'GET',
    groupId: null,
    description: '',
    execMode: 'AUTO',
    dataCenterGroupId: null,
    tableName: '',
    sqlTemplate: '',
    autoOperation: 'list',
    responseFormat: 'object',
    requestParams: []
  }
  tableColumns.value = []
  dataCenterTables.value = []
}

const goToManager = () => {
  router.push('/tool/api-manager')
}

onMounted(() => {
  loadGroups()
  loadDataCenterGroups()
})
</script>

<style scoped>
.api-generator {
  height: calc(100vh - 112px);
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  margin: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.generator-header {
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-left h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.header-icon {
  font-size: 20px;
  color: #6366f1;
}

.generator-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.steps-container {
  padding: 20px 40px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.step-content-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.step-content {
  max-width: 750px;
  margin: 0 auto;
}

.step-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.step-num {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border-radius: 50%;
  font-size: 13px;
  font-weight: 600;
}

.step-text {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.step-footer {
  padding: 14px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-shrink: 0;
}

.api-form {
  padding: 12px 0;
}

.method-group :deep(.el-radio-button__inner) {
  font-family: 'Monaco', 'Menlo', monospace;
  font-weight: 600;
}

.exec-mode-group :deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
}

.mode-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.operation-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.operation-group :deep(.el-radio) {
  margin-right: 0;
}

.sql-input {
  font-family: 'Monaco', 'Menlo', monospace;
}

.sql-input :deep(textarea) {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
}

.field-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.field-tag {
  display: flex;
  align-items: center;
  gap: 6px;
}

.field-tag.clickable {
  cursor: pointer;
  transition: all 0.2s;
}

.field-tag.clickable:hover {
  background-color: #6366f1;
  color: #fff;
  border-color: #6366f1;
}

.field-name {
  font-weight: 500;
}

.field-type {
  font-size: 11px;
  color: #9ca3af;
}

.params-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.confirm-desc {
  width: 100%;
}

.confirm-value {
  color: #374151;
}

.confirm-value.path {
  font-family: 'Monaco', 'Menlo', monospace;
  color: #6366f1;
}

.method-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  font-family: 'Monaco', 'Menlo', monospace;
}

.method-get {
  background: #dcfce7;
  color: #16a34a;
}

.method-post {
  background: #dbeafe;
  color: #2563eb;
}

.method-put {
  background: #fef3c7;
  color: #d97706;
}

.method-delete {
  background: #fee2e2;
  color: #dc2626;
}

:deep(.el-form-item__label) {
  font-size: 14px;
  color: #6b7280;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-button) {
  border-radius: 6px;
}

:deep(.el-tag) {
  border-radius: 4px;
}
</style>
