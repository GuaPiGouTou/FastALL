<template>
  <div class="api-manager">
    <div class="stats-overview">
      <div class="stat-card" v-for="(stat, index) in statsCards" :key="index">
        <div class="stat-icon" :class="stat.iconClass">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
        <div class="stat-trend" v-if="stat.trend">
          <span :class="['trend-value', stat.trend > 0 ? 'up' : 'down']">
            {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%
          </span>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="left-panel">
        <div class="panel-header">
          <div class="header-title">
            <el-icon class="header-icon"><FolderOpened /></el-icon>
            <span>API 分组</span>
          </div>
          <el-tooltip content="新建分组" placement="top">
            <el-button type="primary" size="small" circle @click="showAddGroupDialog" class="add-group-btn">
              <el-icon><Plus /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
        <div class="tree-container">
          <el-tree
            :data="groupTree"
            :props="{ label: 'groupName', children: 'children' }"
            node-key="id"
            :current-node-key="selectedGroupId"
            @node-click="handleGroupClick"
            highlight-current
            :expand-on-click-node="false"
            default-expand-all
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <div class="node-info">
                  <el-icon class="tree-icon" :class="{ 'has-children': data.children && data.children.length > 0 }">
                    <Folder v-if="!data.children || data.children.length === 0" />
                    <FolderOpened v-else />
                  </el-icon>
                  <span class="tree-label">{{ data.groupName }}</span>
                  <el-badge :value="getGroupApiCount(data.id)" :max="99" class="tree-badge" type="primary" />
                </div>
                <div class="node-actions" @click.stop>
                  <el-dropdown trigger="click">
                    <el-button type="primary" link size="small" class="action-btn">
                      <el-icon><MoreFilled /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item @click="showEditGroupDialog(data)">
                          <el-icon><Edit /></el-icon>编辑分组
                        </el-dropdown-item>
                        <el-dropdown-item divided @click="handleDeleteGroup(data)" :disabled="getGroupApiCount(data.id) > 0">
                          <el-icon><Delete /></el-icon>删除分组
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </span>
            </template>
          </el-tree>
        </div>
      </div>

      <div class="center-panel">
        <div class="panel-header">
          <div class="header-left">
            <div class="header-title">
              <el-icon class="header-icon"><Document /></el-icon>
              <span>API 列表</span>
            </div>
            <el-tag v-if="selectedGroup" size="small" effect="plain" class="group-tag">
              {{ selectedGroup.groupName }}
            </el-tag>
          </div>
          <div class="header-right">
            <el-input v-model="searchKeyword" placeholder="搜索 API 名称/路径" class="search-input" clearable @keyup.enter="loadApiList">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="filterStatus" placeholder="状态" clearable class="status-select" @change="loadApiList">
              <el-option label="草稿" value="draft" />
              <el-option label="已发布" value="published" />
              <el-option label="已下线" value="offline" />
            </el-select>
            <el-button type="primary" @click="goToGenerator" class="create-btn">
              <el-icon><Plus /></el-icon>
              新建 API
            </el-button>
          </div>
        </div>

        <div class="table-container">
          <el-table :data="apiList" style="width: 100%" @row-click="handleRowClick" highlight-current-row row-class-name="api-row">
            <el-table-column prop="apiName" label="API 名称" min-width="200">
              <template #default="scope">
                <div class="api-name-cell">
                  <span class="api-name">{{ scope.row.apiName }}</span>
                  <span class="api-desc" v-if="scope.row.description">{{ scope.row.description }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="apiPath" label="路径" min-width="220">
              <template #default="scope">
                <div class="path-cell">
                  <span :class="['method-badge', `method-${scope.row.apiMethod.toLowerCase()}`]">
                    {{ scope.row.apiMethod }}
                  </span>
                  <el-tooltip content="点击复制完整路径" placement="top">
                    <span class="api-path" @click.stop="copyPath(scope.row.apiPath)">
                      /api/dynamic{{ scope.row.apiPath }}
                    </span>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="scope">
                <span :class="['status-tag', `status-${scope.row.status}`]">
                  <span class="status-dot"></span>
                  {{ getStatusText(scope.row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="callCount" label="调用" width="80" align="center">
              <template #default="scope">
                <span class="call-count">{{ formatNumber(scope.row.callCount || 0) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="150">
              <template #default="scope">
                <span class="update-time">{{ scope.row.updateTime }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center" fixed="right">
              <template #default="scope">
                <el-dropdown trigger="click" @click.stop>
                  <el-button type="primary" size="small" link class="more-btn">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="scope.row.status === 'draft' || scope.row.status === 'offline'" @click.stop="handleOnline(scope.row)">
                        <el-icon><CircleCheck /></el-icon>上线
                      </el-dropdown-item>
                      <el-dropdown-item v-if="scope.row.status === 'published'" @click.stop="handleOffline(scope.row)">
                        <el-icon><CircleClose /></el-icon>禁用
                      </el-dropdown-item>
                      <el-dropdown-item @click.stop="handleEdit(scope.row)">
                        <el-icon><Edit /></el-icon>编辑
                      </el-dropdown-item>
                      <el-dropdown-item @click.stop="handleDeleteApi(scope.row)" divided>
                        <el-icon><Delete /></el-icon>删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadApiList"
            @current-change="loadApiList"
            background
          />
        </div>
      </div>

      <div class="right-panel" v-if="selectedApi">
        <div class="detail-header">
          <div class="detail-title">
            <span class="api-title-name">{{ apiForm.apiName || 'API 详情' }}</span>
            <span :class="['method-tag', `method-${apiForm.apiMethod?.toLowerCase()}`]">
              {{ apiForm.apiMethod }}
            </span>
            <span :class="['status-tag', `status-${apiForm.status}`]">
              <span class="status-dot"></span>
              {{ getStatusText(apiForm.status) }}
            </span>
          </div>
          <el-button text @click="selectedApi = null" class="close-btn">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        
        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane name="basic">
            <template #label>
              <span class="tab-label">基础配置</span>
            </template>
            <div class="tab-content">
              <el-form :model="apiForm" label-width="90px" class="api-form">
                <el-form-item label="API 名称">
                  <el-input v-model="apiForm.apiName" placeholder="请输入 API 名称" />
                </el-form-item>
                <el-form-item label="API 路径">
                  <el-input v-model="apiForm.apiPath" placeholder="/user/list">
                    <template #prepend>/api/dynamic</template>
                  </el-input>
                </el-form-item>
                <el-form-item label="请求方法">
                  <el-select v-model="apiForm.apiMethod" style="width: 100%">
                    <el-option label="GET" value="GET" />
                    <el-option label="POST" value="POST" />
                    <el-option label="PUT" value="PUT" />
                    <el-option label="DELETE" value="DELETE" />
                  </el-select>
                </el-form-item>
                <el-form-item label="所属分组">
                  <el-select v-model="apiForm.groupId" style="width: 100%">
                    <el-option v-for="g in groups" :key="g.id" :label="g.groupName" :value="g.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="状态">
                  <el-select v-model="apiForm.status" style="width: 100%">
                    <el-option label="草稿" value="draft" />
                    <el-option label="已发布" value="published" />
                    <el-option label="已下线" value="offline" />
                  </el-select>
                </el-form-item>
                <el-form-item label="描述">
                  <el-input v-model="apiForm.description" type="textarea" :rows="2" placeholder="请输入 API 描述" />
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane name="logic">
            <template #label>
              <span class="tab-label">执行逻辑</span>
            </template>
            <div class="tab-content">
              <el-form :model="apiForm" label-width="90px" class="api-form">
                <el-form-item label="执行模式">
                  <el-radio-group v-model="apiForm.execMode" class="exec-mode-group">
                    <el-radio-button label="AUTO">
                      <el-icon><Operation /></el-icon>
                      自动 CRUD
                    </el-radio-button>
                    <el-radio-button label="SQL">
                      <el-icon><Document /></el-icon>
                      SQL 模式
                    </el-radio-button>
                  </el-radio-group>
                </el-form-item>

                <template v-if="apiForm.execMode === 'AUTO'">
                  <el-form-item label="数据分组">
                    <el-select v-model="apiForm.dataCenterGroupId" style="width: 100%" @change="onApiDataCenterGroupChange" placeholder="请选择数据中心分组">
                      <el-option label="全部分组" :value="null" />
                      <el-option v-for="g in flattenDataCenterGroups" :key="g.id" :label="g.groupName" :value="g.id">
                        <span :style="{ paddingLeft: (g.level || 0) * 20 + 'px' }">{{ g.groupName }}</span>
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="关联表">
                    <el-select v-model="apiForm.tableName" style="width: 100%" placeholder="请选择数据表">
                      <el-option v-for="t in dataCenterTables" :key="t.tableName" :label="t.tableName + (t.description ? ' - ' + t.description : '')" :value="t.tableName" />
                    </el-select>
                  </el-form-item>
                </template>

                <template v-if="apiForm.execMode === 'SQL'">
                  <el-form-item label="数据分组">
                    <el-select v-model="apiForm.dataCenterGroupId" style="width: 100%" placeholder="请选择数据中心分组">
                      <el-option label="全部分组" :value="null" />
                      <el-option v-for="g in flattenDataCenterGroups" :key="g.id" :label="g.groupName" :value="g.id">
                        <span :style="{ paddingLeft: (g.level || 0) * 20 + 'px' }">{{ g.groupName }}</span>
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="SQL 模板">
                    <el-input
                      v-model="apiForm.sqlTemplate"
                      type="textarea"
                      :rows="8"
                      placeholder="SELECT * FROM user WHERE id = {{id}}"
                      class="sql-input"
                    />
                  </el-form-item>
                </template>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane name="params">
            <template #label>
              <span class="tab-label">参数配置</span>
            </template>
            <div class="tab-content params-tab">
              <div class="params-section">
                <div class="section-header">
                  <span class="section-title">请求参数</span>
                  <el-button type="primary" size="small" @click="addRequestParam">
                    <el-icon><Plus /></el-icon>
                    添加
                  </el-button>
                </div>
                <el-table :data="apiForm.requestParams" size="small" v-if="apiForm.requestParams.length > 0">
                  <el-table-column prop="paramName" label="参数名" width="120">
                    <template #default="scope">
                      <el-input v-model="scope.row.paramName" size="small" />
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
                      <el-input v-model="scope.row.description" size="small" />
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
                <el-empty v-else description="暂无参数" :image-size="60" />
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane name="security">
            <template #label>
              <span class="tab-label">安全配置</span>
            </template>
            <div class="tab-content">
              <el-form :model="apiForm" label-width="90px" class="api-form">
                <el-form-item label="鉴权类型">
                  <el-select v-model="apiForm.authType" style="width: 100%">
                    <el-option label="公开访问" value="PUBLIC" />
                    <el-option label="Token 验证" value="TOKEN" />
                    <el-option label="API Key" value="API_KEY" />
                  </el-select>
                </el-form-item>
                <el-form-item label="限流配置">
                  <div class="rate-limit">
                    <el-input-number v-model="apiForm.rateLimit" :min="0" :max="10000" />
                    <span class="rate-unit">次/分钟 (0 表示不限)</span>
                  </div>
                </el-form-item>
                <el-form-item label="IP 白名单">
                  <el-input v-model="apiForm.ipWhitelist" placeholder="多个 IP 用逗号分隔" />
                </el-form-item>
                <el-form-item label="IP 黑名单">
                  <el-input v-model="apiForm.ipBlacklist" placeholder="多个 IP 用逗号分隔" />
                </el-form-item>
                <el-form-item label="允许跨域">
                  <el-switch v-model="apiForm.corsEnabled" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane name="debug">
            <template #label>
              <span class="tab-label">在线调试</span>
            </template>
            <div class="tab-content">
              <div class="debug-section">
                <div class="debug-info">
                  <span class="debug-path">/api/dynamic{{ apiForm.apiPath }}</span>
                  <span :class="['method-tag', `method-${apiForm.apiMethod?.toLowerCase()}`]">
                    {{ apiForm.apiMethod }}
                  </span>
                </div>
                <el-input v-model="debugParams" type="textarea" :rows="4" placeholder="JSON 格式的请求参数" class="debug-input" />
                <div class="debug-actions">
                  <el-button type="primary" @click="executeDebug">
                    <el-icon><VideoPlay /></el-icon>
                    执行测试
                  </el-button>
                  <el-button @click="debugParams = ''">清空</el-button>
                </div>
                <div class="debug-result" v-if="debugResult">
                  <div class="result-header">
                    <span>响应结果</span>
                    <el-button size="small" text @click="copyDebugResult">
                      <el-icon><CopyDocument /></el-icon>
                      复制
                    </el-button>
                  </div>
                  <pre class="result-content">{{ debugResult }}</pre>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>

        <div class="panel-footer">
          <el-button @click="handleSave" type="primary">保存</el-button>
          <el-button @click="handleCopy">复制</el-button>
          <el-button @click="handleDelete" type="danger">删除</el-button>
        </div>
      </div>

      <div class="right-panel empty" v-if="!selectedApi">
        <div class="empty-content">
          <el-icon class="empty-icon"><Document /></el-icon>
          <p class="empty-text">请选择一个 API 查看详情</p>
          <p class="empty-hint">或点击"新建 API"创建新的接口</p>
          <el-button type="primary" @click="goToGenerator">
            <el-icon><Plus /></el-icon>
            新建 API
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="showGroupDialog" :title="isEditGroup ? '编辑分组' : '新建分组'" width="450px" class="group-dialog">
      <el-form :model="groupForm" label-width="80px">
        <el-form-item label="分组名称" required>
          <el-input v-model="groupForm.groupName" placeholder="请输入分组名称" />
        </el-form-item>
        <el-form-item label="分组编码">
          <el-input v-model="groupForm.groupCode" placeholder="请输入分组编码（可选）" />
        </el-form-item>
        <el-form-item label="父级分组">
          <el-cascader
            v-model="groupForm.parentPath"
            :options="groupOptions"
            :props="{ checkStrictly: true, value: 'id', label: 'groupName', emitPath: false }"
            clearable
            placeholder="选择父级分组（可选）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="groupForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="groupForm.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveGroup" :loading="savingGroup">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Delete, Close, FolderOpened, Folder, Document, Search, Edit, 
  Operation, VideoPlay, CopyDocument, MoreFilled, Connection, CircleCheck, 
  TrendCharts, EditPen, CircleClose
} from '@element-plus/icons-vue'
import * as api from '@/api/apiManagerV2'

const router = useRouter()

const stats = reactive({
  total: 0,
  published: 0,
  draft: 0,
  calls: 0
})

const statsCards = computed(() => [
  { icon: Connection, iconClass: 'total', value: stats.total, label: 'API 总数', trend: 12 },
  { icon: CircleCheck, iconClass: 'published', value: stats.published, label: '已发布', trend: 8 },
  { icon: EditPen, iconClass: 'draft', value: stats.draft, label: '草稿', trend: -5 },
  { icon: TrendCharts, iconClass: 'calls', value: stats.calls, label: '总调用', trend: 25 }
])

const groupTree = ref([])
const groups = ref([])
const selectedGroupId = ref(null)
const selectedGroup = ref(null)

const apiList = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchKeyword = ref('')
const filterStatus = ref('')

const selectedApi = ref(null)
const activeTab = ref('basic')
const apiForm = ref({
  apiName: '',
  apiPath: '',
  apiMethod: 'GET',
  groupId: null,
  tags: [],
  description: '',
  execMode: 'AUTO',
  dataCenterGroupId: null,
  tableName: '',
  sqlTemplate: '',
  authType: 'TOKEN',
  rateLimit: 0,
  ipWhitelist: '',
  ipBlacklist: '',
  corsEnabled: 1,
  mockEnabled: 0,
  mockData: '',
  requestParams: [],
  responseParams: []
})

const dataCenterGroups = ref([])
const dataCenterTables = ref([])

const debugParams = ref('')
const debugResult = ref('')

const showGroupDialog = ref(false)
const isEditGroup = ref(false)
const savingGroup = ref(false)
const groupForm = ref({
  id: null,
  groupName: '',
  groupCode: '',
  parentId: null,
  parentPath: [],
  sortOrder: 0,
  description: ''
})

const groupOptions = computed(() => {
  return groupTree.value
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

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num
}

const loadStats = async () => {
  const res = await api.getApiOverview()
  if (res.code === 200 || res.code === 0) {
    stats.total = res.data.totalApis || 0
    stats.published = res.data.publishedApis || 0
    stats.draft = res.data.draftApis || 0
    stats.calls = res.data.todayCalls || 0
  }
}

const loadGroups = async () => {
  const res = await api.getApiGroups()
  if (res.code === 200 || res.code === 0) {
    groups.value = res.data || []
    groupTree.value = buildTree(res.data || [])
  }
}

const buildTree = (list) => {
  const map = {}
  const roots = []
  list.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  list.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })
  return roots
}

const getGroupApiCount = (groupId) => {
  return apiList.value.filter(a => a.groupId === groupId).length
}

const handleGroupClick = (data) => {
  selectedGroupId.value = data.id
  selectedGroup.value = data
  loadApiList()
}

const loadApiList = async () => {
  const res = await api.getApiList({
    page: currentPage.value,
    size: pageSize.value,
    keyword: searchKeyword.value,
    status: filterStatus.value,
    groupId: selectedGroupId.value
  })
  if (res.code === 200 || res.code === 0) {
    apiList.value = res.data.list || []
    total.value = res.data.total || 0
  }
}

const loadDataCenterGroups = async () => {
  const res = await api.getDataCenterGroupTree()
  if (res.code === 200 || res.code === 0) {
    dataCenterGroups.value = res.data || []
  }
}

const loadDataCenterTables = async (groupId) => {
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
}

const onApiDataCenterGroupChange = async (groupId) => {
  apiForm.value.tableName = ''
  dataCenterTables.value = []
  await loadDataCenterTables(groupId)
}

const handleRowClick = async (row) => {
  selectedApi.value = row
  const res = await api.getApiDetail(row.id)
  if (res.code === 200 || res.code === 0) {
    apiForm.value = {
      ...res.data,
      tags: res.data.tags ? res.data.tags.split(',') : [],
      requestParams: res.data.requestParams || [],
      responseParams: res.data.responseParams || []
    }
    loadDataCenterGroups()
    if (apiForm.value.dataCenterGroupId) {
      loadDataCenterTables(apiForm.value.dataCenterGroupId)
    } else {
      loadDataCenterTables(null)
    }
  }
}

const handleOnline = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要上线 API "${row.apiName}" 吗？`, '上线确认', { type: 'warning' })
    const res = await api.publishApi(row.id, 'admin')
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('上线成功')
      row.status = 'published'
      loadStats()
    } else {
      ElMessage.error(res.msg || '上线失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('上线失败：' + e.message)
    }
  }
}

const handleOffline = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要禁用 API "${row.apiName}" 吗？`, '禁用确认', { type: 'warning' })
    const res = await api.offlineApi(row.id, 'admin')
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('禁用成功')
      row.status = 'offline'
      loadStats()
    } else {
      ElMessage.error(res.msg || '禁用失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('禁用失败：' + e.message)
    }
  }
}

const handleEdit = async (row) => {
  selectedApi.value = row
  const res = await api.getApiDetail(row.id)
  if (res.code === 200 || res.code === 0) {
    apiForm.value = {
      ...res.data,
      tags: res.data.tags ? res.data.tags.split(',') : [],
      requestParams: res.data.requestParams || [],
      responseParams: res.data.responseParams || []
    }
    loadDataCenterGroups()
    if (apiForm.value.dataCenterGroupId) {
      loadDataCenterTables(apiForm.value.dataCenterGroupId)
    } else {
      loadDataCenterTables(null)
    }
  }
}

const handleDeleteApi = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除 API "${row.apiName}" 吗？`, '删除确认', { type: 'warning' })
    const res = await api.deleteApi(row.id)
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('删除成功')
      if (selectedApi.value && selectedApi.value.id === row.id) {
        selectedApi.value = null
      }
      loadApiList()
      loadStats()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const goToGenerator = () => {
  router.push('/tool/api-generator')
}

const showAddGroupDialog = () => {
  isEditGroup.value = false
  groupForm.value = {
    id: null,
    groupName: '',
    groupCode: '',
    parentId: null,
    parentPath: [],
    sortOrder: 0,
    description: ''
  }
  showGroupDialog.value = true
}

const showEditGroupDialog = (row) => {
  isEditGroup.value = true
  groupForm.value = {
    id: row.id,
    groupName: row.groupName,
    groupCode: row.groupCode,
    parentId: row.parentId,
    parentPath: row.parentId ? [row.parentId] : [],
    sortOrder: row.sortOrder || 0,
    description: row.description
  }
  showGroupDialog.value = true
}

const handleSaveGroup = async () => {
  if (!groupForm.value.groupName || !groupForm.value.groupName.trim()) {
    ElMessage.warning('请输入分组名称')
    return
  }
  
  savingGroup.value = true
  try {
    const data = {
      groupName: groupForm.value.groupName,
      groupCode: groupForm.value.groupCode,
      parentId: groupForm.value.parentPath && groupForm.value.parentPath.length > 0 
        ? groupForm.value.parentPath[groupForm.value.parentPath.length - 1] 
        : null,
      sortOrder: groupForm.value.sortOrder,
      description: groupForm.value.description
    }
    
    let res
    if (isEditGroup.value) {
      res = await api.updateApiGroup(groupForm.value.id, data)
    } else {
      res = await api.createApiGroup(data)
    }
    
    if (res.code === 200 || res.code === 0) {
      ElMessage.success(isEditGroup.value ? '更新成功' : '创建成功')
      showGroupDialog.value = false
      await loadGroups()
    }
  } catch (error) {
    console.error('保存分组失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingGroup.value = false
  }
}

const handleDeleteGroup = async (row) => {
  const apiCount = getGroupApiCount(row.id)
  if (apiCount > 0) {
    ElMessage.warning(`该分组下有 ${apiCount} 个 API，请先移动或删除这些 API`)
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除分组 "${row.groupName}" 吗？`,
      '删除确认',
      { type: 'warning' }
    )
    
    const res = await api.deleteApiGroup(row.id)
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('删除成功')
      if (selectedGroupId.value === row.id) {
        selectedGroupId.value = null
        selectedGroup.value = null
      }
      await loadGroups()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分组失败:', error)
    }
  }
}

const handleSave = async () => {
  try {
    const data = {
      ...apiForm.value,
      tags: apiForm.value.tags ? apiForm.value.tags.join(',') : ''
    }
    
    const res = await api.updateApi(selectedApi.value.id, data)
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('保存成功')
      loadApiList()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('操作失败：' + e.message)
  }
}

const handlePublish = async () => {
  try {
    const res = await api.publishApi(selectedApi.value.id, 'admin')
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('发布成功')
      apiForm.value.status = 'published'
      loadApiList()
      loadStats()
    } else {
      ElMessage.error(res.msg || '发布失败')
    }
  } catch (e) {
    ElMessage.error('发布失败：' + e.message)
  }
}

const handleCopy = async () => {
  const { value } = await ElMessageBox.prompt('请输入新 API 名称', '复制 API', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  if (value) {
    const res = await api.copyApi(selectedApi.value.id, value, 'admin')
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('复制成功')
      loadApiList()
      loadStats()
    }
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该 API 吗？', '警告', { type: 'warning' })
    const res = await api.deleteApi(selectedApi.value.id)
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('删除成功')
      selectedApi.value = null
      loadApiList()
      loadStats()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const addRequestParam = () => {
  apiForm.value.requestParams.push({
    paramName: '',
    paramType: 'String',
    required: 0,
    description: ''
  })
}

const removeRequestParam = (index) => {
  apiForm.value.requestParams.splice(index, 1)
}

const executeDebug = async () => {
  try {
    let params = {}
    if (debugParams.value) {
      params = JSON.parse(debugParams.value)
    }
    const res = await api.testApi(selectedApi.value.id, params)
    debugResult.value = JSON.stringify(res, null, 2)
  } catch (e) {
    debugResult.value = 'Error: ' + e.message
  }
}

const copyPath = (path) => {
  navigator.clipboard.writeText('/api/dynamic' + path)
  ElMessage.success('已复制')
}

const copyDebugResult = () => {
  navigator.clipboard.writeText(debugResult.value)
  ElMessage.success('已复制')
}

const getStatusText = (status) => {
  const texts = { draft: '草稿', published: '已发布', offline: '已下线' }
  return texts[status] || status
}

onMounted(() => {
  loadStats()
  loadGroups()
  loadApiList()
  loadDataCenterGroups()
})
</script>

<style scoped>
.api-manager {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 112px);
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 16px;
  gap: 16px;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04), 0 1px 3px rgba(0, 0, 0, 0.02);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--card-color) 0%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08), 0 4px 8px rgba(0, 0, 0, 0.04);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  position: relative;
}

.stat-icon::after {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 18px;
  background: inherit;
  opacity: 0.2;
  filter: blur(8px);
}

.stat-icon.total {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff;
  --card-color: #6366f1;
}

.stat-icon.published {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  color: #fff;
  --card-color: #10b981;
}

.stat-icon.draft {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  color: #fff;
  --card-color: #f59e0b;
}

.stat-icon.calls {
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  color: #fff;
  --card-color: #3b82f6;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
  font-weight: 500;
}

.stat-trend {
  position: absolute;
  top: 16px;
  right: 16px;
}

.trend-value {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
}

.trend-value.up {
  background: #dcfce7;
  color: #16a34a;
}

.trend-value.down {
  background: #fee2e2;
  color: #dc2626;
}

.main-content {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

.left-panel {
  width: 260px;
  background: #fff;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.center-panel {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  min-width: 0;
  overflow: hidden;
}

.right-panel {
  width: 520px;
  background: #fff;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  max-height: 100%;
  overflow: hidden;
}

.right-panel.empty {
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-content {
  text-align: center;
  padding: 40px;
}

.empty-icon {
  font-size: 64px;
  color: #d1d5db;
  margin-bottom: 16px;
  opacity: 0.8;
}

.empty-text {
  font-size: 16px;
  color: #374151;
  margin: 0 0 8px;
  font-weight: 500;
}

.empty-hint {
  font-size: 13px;
  color: #9ca3af;
  margin: 0 0 24px;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  background: linear-gradient(180deg, #fff 0%, #fafbfc 100%);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.header-icon {
  font-size: 18px;
  color: #6366f1;
}

.add-group-btn {
  width: 32px;
  height: 32px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-input {
  width: 220px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: #f5f7fa;
  border: 1px solid transparent;
  transition: all 0.3s;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: #e5e7eb;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.status-select {
  width: 110px;
}

.create-btn {
  font-weight: 500;
  border-radius: 10px;
  padding: 0 16px;
  height: 36px;
}

.group-tag {
  border-radius: 8px;
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  color: #6366f1;
  border-color: transparent;
  font-weight: 500;
}

.tree-container {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 8px;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tree-icon {
  font-size: 16px;
  color: #f59e0b;
  transition: all 0.2s;
}

.tree-icon.has-children {
  color: #6366f1;
}

.tree-label {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.tree-badge {
  transform: scale(0.85);
}

.node-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

.action-btn {
  padding: 4px;
}

.table-container {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px;
}

.api-row {
  cursor: pointer;
  transition: all 0.2s;
}

.api-row:hover {
  background-color: #fafbfc !important;
}

.api-name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.api-name {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
}

.api-desc {
  font-size: 12px;
  color: #9ca3af;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 300px;
}

.path-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.method-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 700;
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  flex-shrink: 0;
  letter-spacing: 0.5px;
}

.method-badge.method-get {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #16a34a;
}

.method-badge.method-post {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
}

.method-badge.method-put {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.method-badge.method-delete {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
}

.api-path {
  color: #6366f1;
  cursor: pointer;
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 6px;
  transition: all 0.2s;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.api-path:hover {
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
}

.method-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  letter-spacing: 0.5px;
}

.method-get {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #16a34a;
}

.method-post {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
}

.method-put {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.method-delete {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
}

.more-btn {
  padding: 6px;
  font-size: 16px;
  color: #9ca3af;
  transition: all 0.2s;
}

.more-btn:hover {
  color: #6366f1;
  background: #f3f4f6;
  border-radius: 6px;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-draft {
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  color: #6b7280;
}

.status-draft .status-dot {
  background: #9ca3af;
}

.status-published {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #16a34a;
}

.status-published .status-dot {
  background: #22c55e;
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.6);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(0.9);
  }
}

.status-offline {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
}

.status-offline .status-dot {
  background: #ef4444;
}

.call-count {
  color: #6b7280;
  font-size: 13px;
  font-weight: 600;
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
}

.update-time {
  color: #9ca3af;
  font-size: 12px;
}

.pagination-container {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  background: #fafbfc;
}

.detail-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  background: linear-gradient(180deg, #fff 0%, #fafbfc 100%);
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.api-title-name {
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
}

.close-btn {
  color: #9ca3af;
  padding: 8px;
}

.close-btn:hover {
  color: #6b7280;
  background: #f3f4f6;
  border-radius: 8px;
}

.detail-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  flex-shrink: 0;
}

.detail-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

.tab-label {
  font-size: 13px;
  font-weight: 600;
  padding: 14px 0;
  display: inline-block;
}

.tab-content {
  height: calc(100vh - 300px);
  overflow-y: auto;
  padding: 20px;
}

.params-tab {
  padding: 0;
}

.params-tab .tab-content {
  height: auto;
  overflow: visible;
}

.api-form {
  padding: 20px;
}

.api-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.exec-mode-group :deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  font-weight: 500;
}

.sql-input {
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
}

.sql-input :deep(textarea) {
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  border-radius: 10px;
  background: #f8fafc;
}

.params-section {
  padding: 0;
}

.params-section :deep(.el-form-item) {
  margin-bottom: 16px;
}

.params-section :deep(.el-table) {
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.params-section :deep(.el-table__header th) {
  background: linear-gradient(180deg, #f9fafb 0%, #f3f4f6 100%);
  padding: 12px 0;
}

.params-section :deep(.el-table__body td) {
  padding: 10px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 20px;
  padding-top: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.rate-limit {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rate-unit {
  color: #6b7280;
  font-size: 12px;
}

.debug-section {
  padding: 20px;
}

.debug-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.debug-path {
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 13px;
  color: #374151;
}

.debug-input {
  margin-bottom: 12px;
}

.debug-input :deep(textarea) {
  border-radius: 10px;
  background: #f8fafc;
}

.debug-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.debug-result {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: linear-gradient(180deg, #f1f5f9 0%, #e5e7eb 100%);
  font-size: 12px;
  font-weight: 600;
  color: #475569;
}

.result-content {
  padding: 16px;
  margin: 0;
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  overflow-x: auto;
  max-height: 320px;
  color: #1f2937;
  line-height: 1.6;
}

.panel-footer {
  padding: 14px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 10px;
  flex-shrink: 0;
  background: #fafbfc;
}

.group-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

.group-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
  margin-right: 0;
  background: linear-gradient(180deg, #fff 0%, #fafbfc 100%);
}

.group-dialog :deep(.el-dialog__title) {
  font-weight: 600;
}

.group-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.group-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid #f0f0f0;
  padding: 14px 20px;
  background: #fafbfc;
}

:deep(.el-tree-node__content) {
  height: 40px;
  border-radius: 10px;
  margin: 3px 0;
  transition: all 0.2s;
  padding: 0 12px;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f3f4f6;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.15);
}

:deep(.el-tabs__item) {
  font-size: 13px;
  padding: 0 20px;
  height: 48px;
  line-height: 48px;
}

:deep(.el-tabs__item.is-active) {
  font-weight: 600;
  color: #6366f1;
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #6366f1 0%, #8b5cf6 100%);
  height: 3px;
  border-radius: 2px;
}

:deep(.el-form-item__label) {
  font-size: 13px;
  color: #6b7280;
  font-weight: 600;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c7d2fe;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2), 0 0 0 1px #6366f1;
}

:deep(.el-button) {
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
  border: none;
}

:deep(.el-tag) {
  border-radius: 8px;
  font-weight: 500;
}

:deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table th.el-table__cell) {
  background: linear-gradient(180deg, #f9fafb 0%, #f3f4f6 100%);
  font-weight: 600;
  color: #374151;
  font-size: 12px;
  padding: 16px 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

:deep(.el-table td.el-table__cell) {
  padding: 18px 0;
}

:deep(.el-table .el-table__body-wrapper .el-table__body tbody tr:hover > td) {
  background-color: #fafbfc !important;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

:deep(.el-select .el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2), 0 0 0 1px #6366f1;
}

:deep(.el-select-dropdown__item.is-selected) {
  color: #6366f1;
  font-weight: 600;
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
}

:deep(.el-dropdown-menu__item) {
  padding: 10px 16px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-dropdown-menu__item:hover) {
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  color: #6366f1;
}

:deep(.el-empty__description) {
  color: #9ca3af;
}

:deep(.el-textarea__inner) {
  border-radius: 10px;
  transition: all 0.3s;
}

:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2), 0 0 0 1px #6366f1;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-color: #6366f1;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-color: #6366f1;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  border-radius: 10px;
}
</style>
