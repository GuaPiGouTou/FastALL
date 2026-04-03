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
          <el-step title="选择数据表" :icon="Edit" />
          <el-step title="选择操作类型" :icon="Operation" />
          <el-step title="配置参数" :icon="Setting" />
        </el-steps>
      </div>

      <div class="step-content-wrapper">
        <div v-show="currentStep === 0" class="step-content">
          <div class="step-title">
            <span class="step-num">01</span>
            <span class="step-text">选择数据表</span>
          </div>
          <div class="api-path-preview">
            <div class="path-label">API路径预览：</div>
            <div class="path-value">
              <span class="path-prefix">/api/v1/</span>
              <span class="path-table">{{ wizardForm.tableName || '{tableName}' }}</span>
              <span class="path-suffix">/{{ getOperationSuffix(wizardForm.operationType) || '{suffix}' }}</span>
            </div>
          </div>
          <el-form :model="wizardForm" label-width="120px" class="api-form">
            <el-form-item label="API 分组（筛选表）" required>
              <el-select 
                v-model="wizardForm.dataCenterGroupId" 
                style="width: 100%" 
                placeholder="与 API 管理中的分组一致" 
                @change="onDataCenterGroupChange"
              >
                <el-option v-for="g in flattenDataCenterGroups" :key="g.id" :label="g.groupName" :value="g.id">
                  <span :style="{ paddingLeft: (g.level || 0) * 20 + 'px' }">{{ g.groupName }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="数据表" required>
              <el-select 
                v-model="wizardForm.tableName" 
                style="width: 100%" 
                placeholder="请选择数据表" 
                :loading="loadingTables"
                @change="onTableChange"
              >
                <el-option 
                  v-for="t in dataCenterTables" 
                  :key="t.tableName" 
                  :label="t.tableName + (t.description ? ' - ' + t.description : '')" 
                  :value="t.tableName" 
                />
              </el-select>
            </el-form-item>
            <el-form-item label="表字段预览" v-if="tableColumns.length > 0">
              <div class="field-preview">
                <div class="field-preview-header">
                  <span>字段名</span>
                  <span>类型</span>
                  <span>可空</span>
                  <span>主键</span>
                </div>
                <div class="field-preview-list">
                  <div v-for="col in tableColumns" :key="col.columnName || col.fieldName" class="field-preview-item">
                    <span>{{ col.columnName || col.fieldName }}</span>
                    <span class="field-type">{{ col.dataType || col.fieldType }}</span>
                    <span>{{ col.isNullable ? '是' : '否' }}</span>
                    <span>{{ col.key === 'PRI' ? '是' : '否' }}</span>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-form>
          <div class="step-footer">
            <el-button
              type="primary"
              size="large"
              :disabled="!canNextFromStep0"
              @click="nextStep"
            >
              <span>下一步</span>
              <el-icon style="margin-left: 6px"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>

        <div v-show="currentStep === 1" class="step-content">
          <div class="step-title">
            <span class="step-num">02</span>
            <span class="step-text">选择操作类型</span>
          </div>
          <div class="api-path-preview">
            <div class="path-label">API路径预览：</div>
            <div class="path-value">
              <span class="path-prefix">/api/v1/</span>
              <span class="path-table">{{ wizardForm.tableName || '{tableName}' }}</span>
              <span class="path-suffix">/{{ getOperationSuffix(wizardForm.operationType) || '{suffix}' }}</span>
            </div>
            <div class="path-method">
              <span class="method-label">请求方法：</span>
              <span :class="['method-tag', `method-${getOperationMethod(wizardForm.operationType)?.toLowerCase() || 'get'}`]">
                {{ getOperationMethod(wizardForm.operationType) || 'GET' }}
              </span>
            </div>
          </div>
          <el-form :model="wizardForm" label-width="120px" class="api-form">
            <el-form-item label="操作类型" required>
              <el-radio-group v-model="wizardForm.operationType" class="operation-type-group" @change="onOperationTypeChange">
                <div class="operation-options">
                  <div class="operation-option" :class="{ active: wizardForm.operationType === 'list' }" @click="wizardForm.operationType = 'list'">
                    <div class="operation-icon">
                      <el-icon><List /></el-icon>
                    </div>
                    <div class="operation-content">
                      <div class="operation-title">列表查询</div>
                      <div class="operation-desc">获取分页列表数据</div>
                      <div class="operation-suffix">/list</div>
                    </div>
                  </div>
                  <div class="operation-option" :class="{ active: wizardForm.operationType === 'detail' }" @click="wizardForm.operationType = 'detail'">
                    <div class="operation-icon">
                      <el-icon><Document /></el-icon>
                    </div>
                    <div class="operation-content">
                      <div class="operation-title">详情查询</div>
                      <div class="operation-desc">根据主键获取单条记录</div>
                      <div class="operation-suffix">/detail</div>
                    </div>
                  </div>
                  <div class="operation-option" :class="{ active: wizardForm.operationType === 'add' }" @click="wizardForm.operationType = 'add'">
                    <div class="operation-icon">
                      <el-icon><Plus /></el-icon>
                    </div>
                    <div class="operation-content">
                      <div class="operation-title">新增记录</div>
                      <div class="operation-desc">创建新记录</div>
                      <div class="operation-suffix">/add</div>
                    </div>
                  </div>
                  <div class="operation-option" :class="{ active: wizardForm.operationType === 'update' }" @click="wizardForm.operationType = 'update'">
                    <div class="operation-icon">
                      <el-icon><Edit /></el-icon>
                    </div>
                    <div class="operation-content">
                      <div class="operation-title">编辑记录</div>
                      <div class="operation-desc">根据主键更新记录</div>
                      <div class="operation-suffix">/update</div>
                    </div>
                  </div>
                  <div class="operation-option" :class="{ active: wizardForm.operationType === 'delete' }" @click="wizardForm.operationType = 'delete'">
                    <div class="operation-icon">
                      <el-icon><Delete /></el-icon>
                    </div>
                    <div class="operation-content">
                      <div class="operation-title">删除记录</div>
                      <div class="operation-desc">根据主键删除记录</div>
                      <div class="operation-suffix">/delete</div>
                    </div>
                  </div>
                </div>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="API名称" required>
              <el-input v-model="wizardForm.apiName" placeholder="根据操作类型自动生成，可手动修改">
                <template #append>
                  <el-button @click="generateApiName" type="primary" link>自动生成</el-button>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="API描述">
              <el-input v-model="wizardForm.description" type="textarea" :rows="2" placeholder="请输入API描述" />
            </el-form-item>
            
            <el-form-item label="所属分组">
              <el-select v-model="wizardForm.groupId" style="width: 100%" placeholder="请选择分组">
                <el-option v-for="g in groups" :key="g.id" :label="g.groupName" :value="g.id" />
              </el-select>
            </el-form-item>
          </el-form>
          <div class="step-footer">
            <el-button @click="prevStep" size="large">
              <el-icon><ArrowLeft /></el-icon>
              上一步
            </el-button>
            <el-button type="primary" size="large" @click="nextStep">
              下一步
              <el-icon style="margin-left: 6px"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>

        <div v-show="currentStep === 2" class="step-content">
          <div class="step-title">
            <span class="step-num">03</span>
            <span class="step-text">配置参数</span>
          </div>
          <div class="api-path-preview">
            <div class="path-label">API路径：</div>
            <div class="path-value final">
              <span class="path-prefix">/api/v1/</span>
              <span class="path-table">{{ wizardForm.tableName || '{tableName}' }}</span>
              <span class="path-suffix">/{{ getOperationSuffix(wizardForm.operationType) || '{suffix}' }}</span>
            </div>
            <div class="path-method">
              <span class="method-label">请求方法：</span>
              <span :class="['method-tag', `method-${getOperationMethod(wizardForm.operationType)?.toLowerCase() || 'get'}`]">
                {{ getOperationMethod(wizardForm.operationType) || 'GET' }}
              </span>
            </div>
          </div>
          
          <div class="params-section">
            <div class="section-header">
              <span class="section-title">字段配置</span>
            </div>
            
            <!-- 返回字段配置 -->
            <div class="field-config-section">
              <div class="field-config-header">
                <div class="field-config-title">
                  <span>返回字段</span>
                  <span class="field-config-subtitle">（控制API响应包含哪些字段）</span>
                </div>
                <div class="field-config-actions">
                  <el-button size="small" @click="selectAllReturnFields">
                    <el-icon><Select /></el-icon>
                    全选
                  </el-button>
                  <el-button size="small" @click="clearAllReturnFields">
                    <el-icon><Close /></el-icon>
                    清空
                  </el-button>
                </div>
              </div>
              <div class="field-config-hint" v-if="tableColumns.length === 0">
                <el-empty description="请先在前面的步骤中选择数据表" :image-size="60" />
              </div>
              <div class="field-checkbox-group" v-else>
                <el-checkbox-group v-model="wizardForm.returnFields" class="field-checkbox-list">
                  <el-checkbox 
                    v-for="col in tableColumns" 
                    :key="col.columnName || col.fieldName" 
                    :label="col.columnName || col.fieldName"
                    class="field-checkbox-item"
                  >
                    <div class="field-checkbox-content">
                      <span class="field-name">{{ col.columnName || col.fieldName }}</span>
                      <span class="field-type">{{ col.dataType || col.fieldType }}</span>
                      <span class="field-key" v-if="col.key === 'PRI'">主键</span>
                      <span class="field-nullable" v-if="col.isNullable">可空</span>
                    </div>
                  </el-checkbox>
                </el-checkbox-group>
              </div>
              <div class="field-config-note">
                <el-icon><InfoFilled /></el-icon>
                默认全选；若一个都不选 → 返回全部字段
              </div>
            </div>
            
            <!-- 请求字段配置（根据操作类型不同） -->
            <div class="field-config-section" v-if="['add', 'update'].includes(wizardForm.operationType)">
              <div class="field-config-header">
                <div class="field-config-title">
                  <span>请求字段</span>
                  <span class="field-config-subtitle">（配置API接收哪些字段{{ wizardForm.operationType === 'add' ? '进行创建' : '进行更新' }}）</span>
                </div>
                <div class="field-config-actions">
                  <el-button size="small" @click="selectAllRequestFields">
                    <el-icon><Select /></el-icon>
                    全选
                  </el-button>
                  <el-button size="small" @click="clearAllRequestFields">
                    <el-icon><Close /></el-icon>
                    清空
                  </el-button>
                </div>
              </div>
              <div class="field-checkbox-group" v-if="tableColumns.length > 0">
                <el-checkbox-group v-model="wizardForm.requestFields" class="field-checkbox-list">
                  <el-checkbox 
                    v-for="col in filteredRequestColumns" 
                    :key="col.columnName || col.fieldName" 
                    :label="col.columnName || col.fieldName"
                    class="field-checkbox-item"
                    :disabled="col.key === 'PRI'"
                  >
                    <div class="field-checkbox-content">
                      <span class="field-name">{{ col.columnName || col.fieldName }}</span>
                      <span class="field-type">{{ col.dataType || col.fieldType }}</span>
                      <span class="field-key" v-if="col.key === 'PRI'">主键</span>
                      <span class="field-required" v-if="!col.isNullable && col.key !== 'PRI'">必填</span>
                      <span class="field-nullable" v-if="col.isNullable">可空</span>
                    </div>
                  </el-checkbox>
                </el-checkbox-group>
              </div>
              <div class="field-config-note">
                <el-icon><InfoFilled /></el-icon>
                <span v-if="wizardForm.operationType === 'add'">至少选择1个请求字段，否则创建按钮禁用</span>
                <span v-if="wizardForm.operationType === 'update'">主键字段自动包含，不可取消</span>
              </div>
            </div>
            
            <!-- 查询条件配置（列表查询时） -->
            <div class="field-config-section" v-if="wizardForm.operationType === 'list'">
              <div class="field-config-header">
                <div class="field-config-title">
                  <span>查询条件字段</span>
                  <span class="field-config-subtitle">（配置列表查询时可用的过滤条件）</span>
                </div>
              </div>
              <div class="field-checkbox-group" v-if="tableColumns.length > 0">
                <el-checkbox-group v-model="wizardForm.conditionFields" class="field-checkbox-list">
                  <el-checkbox 
                    v-for="col in tableColumns" 
                    :key="col.columnName || col.fieldName" 
                    :label="col.columnName || col.fieldName"
                    class="field-checkbox-item"
                  >
                    <div class="field-checkbox-content">
                      <span class="field-name">{{ col.columnName || col.fieldName }}</span>
                      <span class="field-type">{{ col.dataType || col.fieldType }}</span>
                      <span class="field-key" v-if="col.key === 'PRI'">主键</span>
                    </div>
                  </el-checkbox>
                </el-checkbox-group>
              </div>
              <div class="field-config-note">
                <el-icon><InfoFilled /></el-icon>
                列表查询自动包含分页参数：page（页码）、pageSize（每页条数）、orderBy（排序字段）、sortDirection（排序方向）
              </div>
            </div>
          </div>
          
          <!-- 安全配置 -->
          <div class="params-section">
            <div class="section-header">
              <span class="section-title">安全配置</span>
            </div>
            <el-form :model="wizardForm" label-width="120px">
              <el-form-item label="携带Token" required>
                <el-switch
                  v-model="wizardForm.needToken"
                  active-text="开启"
                  inactive-text="关闭"
                  :active-value="true"
                  :inactive-value="false"
                  inline-prompt
                  active-color="#13ce66"
                  inactive-color="#ff4949"
                />
                <div class="security-note">
                  <el-icon v-if="wizardForm.needToken" color="#13ce66"><SuccessFilled /></el-icon>
                  <el-icon v-else color="#ff4949"><WarningFilled /></el-icon>
                  <span v-if="wizardForm.needToken">开启：API需要验证token（推荐）</span>
                  <span v-else class="warning-text">关闭：API为公开接口（安全风险！）</span>
                </div>
              </el-form-item>
              <el-form-item label="二次确认" v-if="!wizardForm.needToken && ['add', 'update', 'delete'].includes(wizardForm.operationType)">
                <el-alert
                  title="安全警告"
                  type="warning"
                  description="对于新增/编辑/删除等写操作，强烈建议开启Token验证，否则可能被恶意调用！"
                  show-icon
                  :closable="false"
                />
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 创建按钮区域 -->
          <div class="create-section">
            <el-button @click="prevStep" size="large">
              <el-icon><ArrowLeft /></el-icon>
              上一步
            </el-button>
            <el-button 
              @click="handleCreate" 
              type="success" 
              size="large"
              :loading="creating"
              :disabled="!canCreate"
            >
              <el-icon><Check /></el-icon>
              创建API
            </el-button>
            <el-button 
              @click="handleCreateAndContinue" 
              size="large"
              :loading="creating"
              :disabled="!canCreate"
            >
              <el-icon><Plus /></el-icon>
              创建并继续
            </el-button>
          </div>
        </div>


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
  Refresh, MagicStick, Document, Back, List, Select, Close, 
  InfoFilled, SuccessFilled, WarningFilled
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
  // 步骤1：选择数据表
  dataCenterGroupId: null,
  tableName: '',
  
  // 步骤2：选择操作类型
  operationType: 'list',
  apiName: '',
  description: '',
  groupId: null,
  
  // 步骤3：配置参数
  needToken: true,
  returnFields: [],      // 返回字段列表
  requestFields: [],     // 请求字段列表（新增/编辑时）
  conditionFields: [],   // 查询条件字段列表（列表查询时）
  
  // 向后兼容字段
  apiMethod: 'GET',      // 根据操作类型自动设置
  apiPath: '',           // 根据表名和操作类型自动生成
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

// 计算属性：过滤请求字段（排除主键）
const filteredRequestColumns = computed(() => {
  return tableColumns.value.filter(col => col.key !== 'PRI')
})

const getPrimaryKeyName = () => {
  const pk = tableColumns.value.find(c => c.key === 'PRI')
  return pk ? (pk.columnName || pk.fieldName) : null
}

// 步骤控制：第 1 步（选择数据表）必须先选分组 + 选表
const canNextFromStep0 = computed(() => {
  return wizardForm.value.dataCenterGroupId != null &&
    wizardForm.value.tableName &&
    tableColumns.value &&
    tableColumns.value.length > 0
})

// 计算属性：是否可以创建API
const canCreate = computed(() => {
  // 基本验证
  if (!wizardForm.value.tableName || !wizardForm.value.operationType) {
    return false
  }
  
  // API名称必填
  if (!wizardForm.value.apiName?.trim()) {
    return false
  }
  
  // 对于新增操作，至少需要一个请求字段
  if (wizardForm.value.operationType === 'add' && wizardForm.value.requestFields.length === 0) {
    return false
  }
  
  // 对于编辑操作，至少需要一个请求字段（主键除外）
  if (wizardForm.value.operationType === 'update' && wizardForm.value.requestFields.length === 0) {
    return false
  }
  
  return true
})

// 操作类型映射方法
const getOperationSuffix = (operationType) => {
  const suffixMap = {
    list: 'list',
    detail: 'detail',
    add: 'add',
    update: 'update',
    delete: 'delete'
  }
  return suffixMap[operationType] || ''
}

const getOperationMethod = (operationType) => {
  const methodMap = {
    list: 'GET',
    detail: 'GET',
    add: 'POST',
    update: 'POST',
    delete: 'POST'
  }
  return methodMap[operationType] || 'GET'
}

// 操作类型变化处理
const onOperationTypeChange = (operationType) => {
  // 更新API方法
  wizardForm.value.apiMethod = getOperationMethod(operationType)
  
  // 自动生成API名称
  generateApiName()

  // 根据操作类型同步字段选择（避免切换后仍使用旧选择）
  if (!tableColumns.value || tableColumns.value.length === 0) return

  if (operationType === 'list') {
    wizardForm.value.requestFields = []
    wizardForm.value.conditionFields = tableColumns.value.map(col => col.columnName || col.fieldName)
  } else if (operationType === 'add') {
    wizardForm.value.conditionFields = []
    selectAllRequestFields()
  } else if (operationType === 'update') {
    wizardForm.value.conditionFields = []
    selectAllRequestFields()
  } else {
    wizardForm.value.requestFields = []
    wizardForm.value.conditionFields = []
  }
}

// 生成API名称
const generateApiName = () => {
  if (!wizardForm.value.tableName || !wizardForm.value.operationType) return
  
  const tableName = wizardForm.value.tableName
  const operationType = wizardForm.value.operationType
  
  const nameMap = {
    list: `获取${tableName}列表`,
    detail: `获取${tableName}详情`,
    add: `创建${tableName}`,
    update: `更新${tableName}`,
    delete: `删除${tableName}`
  }
  
  wizardForm.value.apiName = nameMap[operationType] || `API: ${tableName}`
}

// 字段选择辅助方法
const selectAllReturnFields = () => {
  wizardForm.value.returnFields = tableColumns.value.map(col => col.columnName || col.fieldName)
}

const clearAllReturnFields = () => {
  wizardForm.value.returnFields = []
}

const selectAllRequestFields = () => {
  const pkName = getPrimaryKeyName()
  const nonPk = filteredRequestColumns.value.map(col => col.columnName || col.fieldName)
  // update 操作：主键自动包含，且 UI 中主键项不可取消
  if (wizardForm.value.operationType === 'update' && pkName) {
    wizardForm.value.requestFields = Array.from(new Set([pkName, ...nonPk]))
  } else {
    wizardForm.value.requestFields = nonPk
  }
}

const clearAllRequestFields = () => {
  const pkName = getPrimaryKeyName()
  if (wizardForm.value.operationType === 'update' && pkName) {
    wizardForm.value.requestFields = [pkName]
  } else {
    wizardForm.value.requestFields = []
  }
}

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
  wizardForm.value.returnFields = []
  wizardForm.value.requestFields = []
  wizardForm.value.conditionFields = []
  // 对接：选择数据分组后，同步 API 所属分组为同一个分组
  if (groupId) wizardForm.value.groupId = groupId
  await loadDataCenterTables(groupId)
}

const onTableChange = async (tableName) => {
  if (tableName) {
    try {
      const res = await api.getTableColumnsDirect(tableName)
      if (res.code === 200 || res.code === 0) {
        // 统一字段结构：后端返回 columnKey/nullable，但本页面用 key/isNullable
        tableColumns.value = (res.data || []).map(col => ({
          ...col,
          key: col.key ?? col.columnKey ?? null,
          isNullable: col.isNullable ?? col.nullable ?? true,
          fieldName: col.fieldName ?? col.columnName ?? '',
          fieldType: col.fieldType ?? col.dataType ?? ''
        }))
        // 自动选择所有返回字段
        selectAllReturnFields()
        // 自动选择所有请求字段（新增/编辑操作）
        if (['add', 'update'].includes(wizardForm.value.operationType)) {
          selectAllRequestFields()
        }
        // 自动选择所有查询条件字段（列表查询）
        if (wizardForm.value.operationType === 'list') {
          wizardForm.value.conditionFields = tableColumns.value.map(col => col.columnName || col.fieldName)
        }
        // 更新API名称
        generateApiName()
      } else {
        tableColumns.value = []
        wizardForm.value.returnFields = []
        wizardForm.value.requestFields = []
        wizardForm.value.conditionFields = []
      }
    } catch (error) {
      tableColumns.value = []
      wizardForm.value.returnFields = []
      wizardForm.value.requestFields = []
      wizardForm.value.conditionFields = []
    }
  } else {
    tableColumns.value = []
    wizardForm.value.returnFields = []
    wizardForm.value.requestFields = []
    wizardForm.value.conditionFields = []
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const nextStep = () => {
  if (currentStep.value < 2) {
    // 步骤0 → 步骤1：自动生成API名称和路径
    if (currentStep.value === 0 && wizardForm.value.tableName) {
      generateApiName()
      // 生成API路径
      wizardForm.value.apiPath = `/api/v1/${wizardForm.value.tableName}/${getOperationSuffix(wizardForm.value.operationType)}`
    }
    currentStep.value++
  }
}

const handleCreate = async (skipRedirect = false) => {
  creating.value = true
  try {
    // 构建符合新标准的数据对象
    let apiData = {
      // 基础信息
      apiName: wizardForm.value.apiName,
      apiPath: wizardForm.value.apiPath || `/api/v1/${wizardForm.value.tableName}/${getOperationSuffix(wizardForm.value.operationType)}`,
      apiMethod: wizardForm.value.apiMethod || getOperationMethod(wizardForm.value.operationType),
      groupId: wizardForm.value.groupId,
      description: wizardForm.value.description,

      // 执行模式：后端动态执行逻辑以 sqlTemplate 是否为空为准，但为了和 generateCrudApi 统一，这里按 crud 记录
      execMode: 'crud',
      dataCenterGroupId: wizardForm.value.dataCenterGroupId,
      tableName: wizardForm.value.tableName,

      // 新标准字段
      operationType: wizardForm.value.operationType,
      tenantAppId: null, // 暂时为空，后续可扩展
      authType: wizardForm.value.needToken ? 'TOKEN' : 'NONE',
      sqlTemplate: '',

      // 注意：不要 JSON.stringify，直接传数组/对象让后端 JacksonTypeHandler 正确落库
      returnFields: wizardForm.value.returnFields.length > 0 ? wizardForm.value.returnFields : null,
      requestFields: wizardForm.value.requestFields.length > 0 ? wizardForm.value.requestFields : null,
      conditionFields: wizardForm.value.conditionFields.length > 0 ? wizardForm.value.conditionFields : null
    }

    const res = await api.createApi(apiData)
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('创建成功')
      if (!skipRedirect) {
        router.push('/tool/api-manager')
      }
      return true
    } else {
      ElMessage.error(res.msg || '创建失败')
      return false
    }
  } catch (error) {
    ElMessage.error(error.message || '创建失败')
    return false
  } finally {
    creating.value = false
  }
}

const handleCreateAndContinue = async () => {
  const success = await handleCreate(true)
  if (success) {
    // 重置表单，保留一些默认值
    wizardForm.value = {
      dataCenterGroupId: wizardForm.value.dataCenterGroupId, // 保持相同分组
      tableName: '',
      operationType: 'list',
      apiName: '',
      description: '',
      groupId: wizardForm.value.groupId, // 保持相同分组
      needToken: true,
      returnFields: [],
      requestFields: [],
      conditionFields: [],
      apiMethod: 'GET',
      apiPath: ''
    }
    // 重置步骤到第一步
    currentStep.value = 0
    // 清空表字段
    tableColumns.value = []
    ElMessage.success('API已创建，可以继续创建下一个')
  }
}



const resetForm = () => {
  currentStep.value = 0
  wizardForm.value = {
    // 步骤1：选择数据表
    dataCenterGroupId: null,
    tableName: '',
    
    // 步骤2：选择操作类型
    operationType: 'list',
    apiName: '',
    description: '',
    groupId: null,
    
    // 步骤3：配置参数
    needToken: true,
    returnFields: [],
    requestFields: [],
    conditionFields: [],
    
    // 向后兼容字段
    apiMethod: 'GET',
    apiPath: ''
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

/* 新标准样式 */
.api-path-preview {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.api-path-preview .path-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.api-path-preview .path-value {
  font-size: 18px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  color: #409eff;
  margin-bottom: 8px;
  padding: 8px 12px;
  background-color: white;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.api-path-preview .path-value.final {
  color: #67c23a;
  font-weight: 600;
}

.api-path-preview .path-prefix {
  color: #909399;
}

.api-path-preview .path-table {
  color: #409eff;
  font-weight: 600;
}

.api-path-preview .path-suffix {
  color: #e6a23c;
}

.api-path-preview .path-method {
  display: flex;
  align-items: center;
  gap: 8px;
}

.api-path-preview .method-label {
  font-size: 14px;
  color: #606266;
}

.api-path-preview .method-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.api-path-preview .method-get {
  background-color: #67c23a;
}

.api-path-preview .method-post {
  background-color: #409eff;
}

.api-path-preview .method-put {
  background-color: #e6a23c;
}

.api-path-preview .method-delete {
  background-color: #f56c6c;
}

/* 字段预览 */
.field-preview {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.field-preview-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  background-color: #f5f7fa;
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  border-bottom: 1px solid #e4e7ed;
}

.field-preview-list {
  max-height: 200px;
  overflow-y: auto;
}

.field-preview-item {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  padding: 8px 12px;
  font-size: 13px;
  border-bottom: 1px solid #f0f2f5;
}

.field-preview-item:last-child {
  border-bottom: none;
}

.field-preview-item .field-type {
  color: #e6a23c;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

/* 操作类型选择 */
.operation-type-group {
  width: 100%;
}

.operation-options {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.operation-option {
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.operation-option:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.operation-option.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.operation-icon {
  font-size: 32px;
  color: #409eff;
  margin-bottom: 12px;
}

.operation-option.active .operation-icon {
  color: #67c23a;
}

.operation-content {
  flex: 1;
}

.operation-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.operation-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.operation-suffix {
  font-size: 13px;
  color: #e6a23c;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

/* 字段配置 */
.field-config-section {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f9fafc;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.field-config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.field-config-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.field-config-subtitle {
  font-size: 13px;
  color: #909399;
  margin-left: 8px;
}

.field-config-actions {
  display: flex;
  gap: 8px;
}

.field-config-hint {
  text-align: center;
  padding: 20px;
}

.field-checkbox-group {
  max-height: 300px;
  overflow-y: auto;
  padding: 8px;
  background-color: white;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.field-checkbox-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 8px;
}

.field-checkbox-item {
  margin-right: 0 !important;
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.field-checkbox-item:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.field-checkbox-item.is-checked {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.field-checkbox-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.field-checkbox-content .field-name {
  font-weight: 600;
  color: #303133;
}

.field-checkbox-content .field-type {
  color: #e6a23c;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
}

.field-checkbox-content .field-key,
.field-checkbox-content .field-required,
.field-checkbox-content .field-nullable {
  font-size: 11px;
  padding: 1px 4px;
  border-radius: 2px;
  background-color: #f0f2f5;
  color: #606266;
}

.field-checkbox-content .field-key {
  background-color: #fef0f0;
  color: #f56c6c;
}

.field-checkbox-content .field-required {
  background-color: #f0f9eb;
  color: #67c23a;
}

.field-checkbox-content .field-nullable {
  background-color: #f4f4f5;
  color: #909399;
}

.field-config-note {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 安全配置 */
.security-note {
  margin-top: 8px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.security-note .warning-text {
  color: #f56c6c;
}
```
/* 创建按钮区域 */
.create-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-right: 20px;
}
</style>
```