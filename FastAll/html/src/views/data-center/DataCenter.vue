<template>
  <div class="smart-grid-container">
    <div class="view-header">
      <div class="header-left">
        <div class="table-info">
          <div class="icon-wrapper" style="background-color: #3b82f620">
            <el-icon style="color: #3b82f6"><DataAnalysis /></el-icon>
          </div>
          <div class="info-text">
            <span class="table-name">数据中心</span>
            <span class="db-name">Data Center</span>
          </div>
        </div>
      </div>
      <div class="view-tabs">
        <div class="tab-item" :class="{ active: currentView === 'tables' }" @click="currentView = 'tables'">
          <el-icon><Menu /></el-icon> 数据表
        </div>
        <div class="tab-item" :class="{ active: currentView === 'data' }" @click="currentView = 'data'">
          <el-icon><DataLine /></el-icon> 数据管理
        </div>
      </div>
      <div class="header-right">
        <el-button link @click="openRecycleBinDialog">
          <el-icon><Delete /></el-icon> 回收站
        </el-button>
        <el-button link @click="refreshData">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <template v-if="currentView === 'tables'">
      <div class="tables-view">
        <div class="section-label">
          <span>快速构建 / BUILD</span>
          <div class="divider"></div>
        </div>
        <div class="creation-grid">
          <div class="create-card" @click="openImportDialog('excel')">
            <div class="c-icon-bg green"><el-icon><DocumentAdd /></el-icon></div>
            <div class="c-info"><span class="c-title">Import Excel</span><span class="c-desc">解析 .xlsx 自动建表</span></div>
          </div>
          <div class="create-card" @click="openImportDialog('csv')">
            <div class="c-icon-bg orange"><el-icon><Document /></el-icon></div>
            <div class="c-info"><span class="c-title">Import CSV</span><span class="c-desc">解析 .csv 自动建表</span></div>
          </div>
          <div class="create-card" @click="openImportDialog('json')">
            <div class="c-icon-bg purple"><el-icon><Tickets /></el-icon></div>
            <div class="c-info"><span class="c-title">Import JSON</span><span class="c-desc">解析 .json 自动建表</span></div>
          </div>
          <div class="create-card" @click="openDbImportDialog">
            <div class="c-icon-bg cyan"><el-icon><Coin /></el-icon></div>
            <div class="c-info"><span class="c-title">Import Database</span><span class="c-desc">从现有数据库导入</span></div>
          </div>
          <div class="create-card" @click="openCreateTableDialog">
            <div class="c-icon-bg blue"><el-icon><Plus /></el-icon></div>
            <div class="c-info"><span class="c-title">Create Blank</span><span class="c-desc">手动设计字段结构</span></div>
          </div>
        </div>

        <div class="section-label mt-30">
          <span>数据表 / TABLES ({{ tableList.length }})</span>
          <div class="divider"></div>
        </div>

        <div class="table-toolbar">
          <el-input v-model="tableSearchText" placeholder="搜索表名..." :prefix-icon="Search" clearable style="width: 300px" />
          <div class="view-toggle">
            <el-tooltip content="网格视图">
              <div class="toggle-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
                <el-icon><Menu /></el-icon>
              </div>
            </el-tooltip>
            <el-tooltip content="列表视图">
              <div class="toggle-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
                <el-icon><Tickets /></el-icon>
              </div>
            </el-tooltip>
          </div>
        </div>

        <div v-if="viewMode === 'grid'" class="modules-grid">
          <div v-for="table in filteredTableList" :key="table.tableName" class="module-card" @click="openTableData(table)">
            <div class="m-card-header">
              <div class="m-icon" style="background-color: #3b82f620; color: #3b82f6">
                <el-icon><Grid /></el-icon>
              </div>
              <div class="m-action-btn" @click.stop>
                <el-dropdown trigger="click" @command="(cmd) => handleTableCommand(cmd, table)">
                  <el-icon class="m-more-icon"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit"><el-icon><EditPen /></el-icon> 编辑结构</el-dropdown-item>
                      <el-dropdown-item command="api"><el-icon><Connection /></el-icon> 生成API</el-dropdown-item>
                      <el-dropdown-item command="delete" style="color:#f56c6c" divided><el-icon><Delete /></el-icon> 移入回收站</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            <div class="m-card-body">
              <div class="m-name">{{ table.tableName }}</div>
              <div class="m-meta">{{ table.description || '暂无描述' }}</div>
            </div>
            <div class="m-card-footer">
              <span><el-icon style="vertical-align: -1px"><DataLine /></el-icon> {{ table.recordCount || 0 }}</span>
              <span>{{ table.columnCount || 0 }} 字段</span>
            </div>
          </div>
        </div>

        <div v-else class="modules-list">
          <div class="list-header">
            <div class="col-name">表名</div><div class="col-desc">描述</div><div class="col-recs">记录数</div><div class="col-cols">字段数</div><div class="col-act"></div>
          </div>
          <div v-for="table in filteredTableList" :key="table.tableName" class="list-item" @click="openTableData(table)">
            <div class="col-name">
              <div class="list-icon" style="background: #3b82f620; color: #3b82f6"><el-icon><Grid /></el-icon></div>
              <span>{{ table.tableName }}</span>
            </div>
            <div class="col-desc">{{ table.description || '-' }}</div>
            <div class="col-recs">{{ table.recordCount || 0 }}</div>
            <div class="col-cols">{{ table.columnCount || 0 }}</div>
            <div class="col-act" @click.stop>
              <el-dropdown trigger="click" @command="(cmd) => handleTableCommand(cmd, table)">
                <el-button link size="small"><el-icon><MoreFilled /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="delete" style="color:#f56c6c">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>

        <el-empty v-if="filteredTableList.length === 0" description="暂无数据表" />
      </div>
    </template>

    <template v-else>
      <div class="grid-toolbar">
        <div v-if="selectedRows.length > 0" class="batch-actions">
          <span class="selected-count">已选中 {{ selectedRows.length }} 项</span>
          <div class="divider-vertical"></div>
          <el-button type="danger" bg size="small" @click="batchDeleteSelectedRows">
            <el-icon><Delete /></el-icon> 批量删除
          </el-button>
          <el-button type="success" bg size="small" @click="openExportDrawer">
            <el-icon><Download /></el-icon> 导出选中
          </el-button>
          <el-button type="default" link size="small" @click="clearSelection">取消选择</el-button>
        </div>

        <div v-else class="tool-group">
          <el-popover placement="bottom-start" :width="460" trigger="click">
            <template #reference>
              <div class="tool-btn" :class="{ active: filterList.length > 0 }">
                <el-icon><Filter /></el-icon> 筛选
                <span v-if="filterList.length" class="badge">{{ filterList.length }}</span>
              </div>
            </template>
            <div class="filter-panel">
              <div v-if="filterList.length === 0" class="empty-filter">暂无筛选条件</div>
              <div v-for="(item, index) in filterList" :key="index" class="filter-row">
                <div class="logic">{{ index === 0 ? 'Where' : 'And' }}</div>
                <el-select v-model="item.col" size="small" placeholder="字段" style="width:140px">
                  <el-option v-for="c in viewTableColumns" :key="c.prop" :label="c.label" :value="c.prop"/>
                </el-select>
                <el-select v-model="item.op" size="small" placeholder="条件" style="width:100px">
                  <el-option label="包含" value="like" />
                  <el-option label="等于" value="eq" />
                </el-select>
                <el-input v-model="item.val" size="small" placeholder="值" style="flex:1" />
                <el-button link type="danger" :icon="Close" @click="filterList.splice(index, 1)" />
              </div>
              <div class="filter-footer">
                <el-button link type="primary" size="small" :icon="Plus" @click="addFilter">添加条件</el-button>
                <el-button type="primary" size="small" @click="loadViewTableData">应用</el-button>
              </div>
            </div>
          </el-popover>

          <el-popover placement="bottom-start" :width="320" trigger="click">
            <template #reference>
              <div class="tool-btn" :class="{ active: sortState.field }">
                <el-icon><Sort /></el-icon> 排序
              </div>
            </template>
            <div class="sort-panel">
              <div class="sort-row">
                <span>字段</span>
                <el-select v-model="sortState.field" size="small" clearable style="flex:1">
                  <el-option v-for="c in viewTableColumns" :key="c.prop" :label="c.label" :value="c.prop"/>
                </el-select>
              </div>
              <div class="sort-row">
                <span>方向</span>
                <el-radio-group v-model="sortState.order" size="small">
                  <el-radio-button label="ASC">升序</el-radio-button>
                  <el-radio-button label="DESC">降序</el-radio-button>
                </el-radio-group>
              </div>
              <div style="text-align:right; margin-top:10px;">
                <el-button type="primary" size="small" @click="loadViewTableData">应用排序</el-button>
              </div>
            </div>
          </el-popover>

          <el-popover placement="bottom-start" :width="200" trigger="click">
            <template #reference>
              <div class="tool-btn"><el-icon><Operation /></el-icon> 显示列</div>
            </template>
            <el-scrollbar max-height="300px">
              <div v-for="col in viewTableColumns" :key="col.prop" style="padding: 5px 10px;">
                <el-checkbox v-model="col.visible">{{ col.label }}</el-checkbox>
              </div>
            </el-scrollbar>
          </el-popover>

          <div class="divider"></div>
          <el-input v-model="viewSearchText" placeholder="搜索..." prefix-icon="Search" size="small" class="search-input" clearable @keyup.enter="loadViewTableData" />
        </div>

        <div class="tool-group">
          <el-button type="primary" size="small" @click="showAddDataDialog">
            <el-icon class="mr-1"><Plus /></el-icon> 添加记录
          </el-button>
        </div>
      </div>

      <div class="grid-body" v-loading="viewLoading">
        <el-table ref="tableRef" :data="viewTableData" border height="100%" style="width: 100%" class="nocodb-table" :header-cell-style="headerCellStyle" :cell-style="cellStyle" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" fixed="left" />

          <template v-for="(col, index) in visibleColumns" :key="col.prop">
            <el-table-column :prop="col.prop" :width="col.width || 180" resizable show-overflow-tooltip>
              <template #header>
                <div class="custom-header">
                  <el-icon class="header-icon" :class="col.uiType">
                    <component :is="getIconByUiType(col.uiType)" />
                  </el-icon>
                  <span class="header-title" :title="col.label">{{ col.label }}</span>
                  <el-dropdown trigger="click" class="header-menu" @command="(cmd) => handleColumnCommand(cmd, col)">
                    <span class="header-trigger"><el-icon><ArrowDown /></el-icon></span>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit"><el-icon><EditPen /></el-icon> 编辑字段</el-dropdown-item>
                        <el-dropdown-item v-if="col.prop !== 'id'" command="delete" style="color:#f56c6c" divided><el-icon><Delete /></el-icon> 删除字段</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>

              <template #default="{ row }">
                <div class="cell-wrapper" :class="{ 'primary-cell': index === 0 }">
                  <div v-if="col.uiType === 'Attachment'" class="cell-attachment" @click.stop>
                    <div v-if="row[col.prop]" class="file-wrapper">
                      <el-image v-if="isImage(row[col.prop])" :src="getFileUrl(row[col.prop])" :preview-src-list="[getFileUrl(row[col.prop])]" class="table-thumb" fit="cover" preview-teleported @click.stop />
                      <div v-else class="file-chip" @click.stop="downloadFile(row[col.prop])">
                        <el-icon><Document /></el-icon> <span>文件</span>
                      </div>
                    </div>
                    <span v-else class="no-data">-</span>
                  </div>

                  <div v-else-if="col.uiType === 'Select'" class="cell-tag">
                    <span v-if="row[col.prop]" class="noco-tag" :style="getColorHash(row[col.prop])">
                      {{ row[col.prop] }}
                    </span>
                  </div>

                  <div v-else-if="col.uiType === 'Switch'" class="cell-center">
                    <el-switch v-model="row[col.prop]" :active-value="1" :inactive-value="0" size="small" disabled />
                  </div>

                  <div v-else-if="col.uiType === 'Rating'" class="cell-center">
                    <el-rate v-model="row[col.prop]" disabled size="small" />
                  </div>

                  <div v-else-if="col.uiType === 'URL'" class="cell-link">
                    <a :href="row[col.prop]" target="_blank" v-if="row[col.prop]">{{ row[col.prop] }}</a>
                  </div>

                  <span v-else-if="col.uiType === 'Currency'" style="font-family: monospace; color:#16a34a;">
                    {{ row[col.prop] ? '¥ ' + row[col.prop] : '-' }}
                  </span>

                  <span v-else class="cell-text">{{ row[col.prop] }}</span>
                </div>
              </template>
            </el-table-column>
          </template>

          <el-table-column label="操作" width="100" fixed="right" align="center" header-align="center">
            <template #default="{ row }">
              <div class="row-ops-fixed">
                <el-tooltip content="编辑" placement="top" :show-after="500">
                  <el-button link type="primary" size="small" @click.stop="showEditDataDialog(row)">
                    <el-icon size="16"><EditPen /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-popconfirm title="确定删除?" @confirm="confirmDeleteData(row)">
                  <template #reference>
                    <el-button link type="danger" size="small" @click.stop>
                      <el-icon size="16"><Delete /></el-icon>
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>

          <el-table-column width="50" fixed="right" align="center" header-align="center">
            <template #header>
              <div class="add-col-trigger" @click="openColDrawer(false)">
                <el-icon><Plus /></el-icon>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="grid-pagination">
        <el-pagination v-model:current-page="viewPagination.page" v-model:page-size="viewPagination.pageSize" :page-sizes="[20, 50, 100]" :total="viewPagination.total" layout="total, sizes, prev, pager, next" @size-change="loadViewTableData" @current-change="loadViewTableData" small background />
      </div>
    </template>

    <el-drawer v-model="createTableDrawerVisible" title="创建新表" size="600px" destroy-on-close>
      <div class="drawer-body">
        <el-form :model="createTableForm" label-position="top">
          <el-form-item label="表名 (英文)">
            <el-input v-model="createTableForm.tableName" placeholder="例如：employee">
              <template #prepend>tb_</template>
            </el-input>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="createTableForm.description" placeholder="例如：员工信息表" />
          </el-form-item>
          <el-form-item label="字段设计">
            <div class="field-designer">
              <div v-for="(field, index) in createTableForm.fields" :key="index" class="field-row">
                <el-input v-model="field.label" placeholder="显示名" style="width: 120px" />
                <el-input v-model="field.name" placeholder="字段名" style="width: 120px">
                  <template #prepend v-if="index > 0">col_</template>
                </el-input>
                <el-select v-model="field.uiType" placeholder="类型" style="width: 140px">
                  <el-option v-for="t in fieldTypes" :key="t.value" :label="t.label" :value="t.value">
                    <div style="display: flex; align-items: center; gap: 8px;">
                      <el-icon><component :is="t.icon"/></el-icon>
                      <span>{{ t.label }}</span>
                    </div>
                  </el-option>
                </el-select>
                <el-input v-if="field.uiType === 'Select'" v-model="field.options" placeholder="选项(逗号分隔)" style="width: 120px" />
                <el-button type="danger" :icon="Delete" circle @click="createTableForm.fields.splice(index, 1)" />
              </div>
              <el-button type="primary" link @click="addFieldToCreate">
                <el-icon><Plus /></el-icon> 添加字段
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="createTableDrawerVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateTable" :loading="createTableLoading">创建表</el-button>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="rowDrawerVisible" :title="currentRow ? '编辑记录' : '新增记录'" size="500px" destroy-on-close>
      <div class="drawer-body">
        <el-form ref="dataFormRef" :model="rowData" :rules="dataFormRules" label-position="top">
          <div v-for="col in editableColumns" :key="col.prop" class="form-field-wrapper">
            <div class="field-label">
              <el-icon><component :is="getIconByUiType(col.uiType)"/></el-icon>{{ col.label }}
              <span v-if="!isNullableField(col)" class="required-mark">*</span>
              <span class="field-type-tag">{{ getFieldTypeName(col.uiType) }}</span>
            </div>

            <el-form-item :prop="col.prop" :rules="getFieldRules(col)">
              <div v-if="col.uiType === 'Attachment'" class="attachment-wrapper">
                <el-upload 
                  class="upload-box-wrapper" 
                  drag
                  action="/api/file/upload"
                  name="file"
                  :show-file-list="false" 
                  :on-success="(res) => handleUploadSuccess(res, col.prop)" 
                  :on-error="handleUploadError"
                  :before-upload="(file) => beforeUploadWithType(file, col)"
                  :accept="getAcceptTypes(col)"
                  :headers="uploadHeaders"
                  :with-credentials="true"
                >
                  <div v-if="rowData[col.prop]" class="uploaded-card">
                    <div class="card-left" @click.stop>
                      <img v-if="isImage(rowData[col.prop])" :src="getFileUrl(rowData[col.prop])" class="mini-preview"/>
                      <el-icon v-else size="24" color="#909399"><Document /></el-icon>
                    </div>
                    <div class="card-center">
                      <div class="file-title" :title="rowData[col.prop]">{{ getFileName(rowData[col.prop]) }}</div>
                      <div class="file-tip">点击此处更换文件</div>
                    </div>
                    <div class="card-right" @click.stop="rowData[col.prop] = null">
                      <el-icon class="delete-icon"><CircleCloseFilled /></el-icon>
                    </div>
                  </div>
                  <div v-else class="upload-empty-state">
                    <el-icon class="cloud-icon"><UploadFilled /></el-icon>
                    <div class="upload-text">点击或拖拽文件到这里</div>
                    <div class="upload-hint" v-if="col.options">{{ col.options }}</div>
                  </div>
                </el-upload>
              </div>

              <el-input 
                v-else-if="col.uiType === 'Input'" 
                v-model="rowData[col.prop]" 
                :maxlength="getFieldMaxLength(col)"
                show-word-limit
                clearable
                :placeholder="'请输入' + col.label"
              />

              <el-input-number 
                v-else-if="col.uiType === 'InputNumber'" 
                v-model="rowData[col.prop]" 
                style="width:100%" 
                controls-position="right"
                :precision="0"
                :min="getMinValue(col)"
                :max="getMaxValue(col)"
                :placeholder="'请输入' + col.label"
              />

              <el-input-number 
                v-else-if="col.uiType === 'Currency'" 
                v-model="rowData[col.prop]" 
                style="width:100%" 
                controls-position="right"
                :precision="2"
                :min="0"
                :placeholder="'请输入' + col.label"
              >
                <template #prefix>¥</template>
              </el-input-number>

              <el-date-picker 
                v-else-if="col.uiType === 'DatePicker'" 
                v-model="rowData[col.prop]" 
                type="datetime" 
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                style="width:100%"
                :placeholder="'请选择' + col.label"
                clearable
              />

              <div v-else-if="col.uiType === 'Switch'" class="switch-wrapper">
                <el-switch 
                  v-model="rowData[col.prop]" 
                  :active-value="1" 
                  :inactive-value="0"
                  active-text="是"
                  inactive-text="否"
                />
              </div>

              <el-select 
                v-else-if="col.uiType === 'Select'" 
                v-model="rowData[col.prop]" 
                style="width:100%" 
                clearable 
                filterable 
                allow-create
                :placeholder="'请选择' + col.label"
              >
                <el-option v-for="opt in parseOptions(col.options)" :key="opt" :label="opt" :value="opt" />
              </el-select>

              <el-rate 
                v-else-if="col.uiType === 'Rating'" 
                v-model="rowData[col.prop]" 
                :max="5"
                clearable 
                show-score
              />

              <el-input 
                v-else-if="col.uiType === 'Textarea'" 
                type="textarea" 
                :rows="4" 
                v-model="rowData[col.prop]"
                :maxlength="getFieldMaxLength(col)"
                show-word-limit
                :placeholder="'请输入' + col.label"
              />

              <el-input 
                v-else 
                v-model="rowData[col.prop]"
                :placeholder="'请输入' + col.label"
                clearable
              />
            </el-form-item>
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="rowDrawerVisible=false">取消</el-button>
          <el-button type="primary" @click="handleSaveData" :loading="dataFormLoading">保存</el-button>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="colDrawerVisible" :title="isEditColumn ? '编辑字段' : '添加新字段'" size="420px" append-to-body>
      <el-form :model="newColData" label-position="top">
        <el-form-item label="显示名称">
          <el-input v-model="newColData.label" placeholder="例如：状态" />
        </el-form-item>
        <el-form-item label="字段名 (英文)">
          <el-input v-model="newColData.prop" placeholder="status" :disabled="isEditColumn">
            <template #prepend v-if="!isEditColumn">col_</template>
          </el-input>
          <div v-if="isEditColumn" class="form-tip warning">
            <el-icon><InfoFilled /></el-icon> 注意：修改字段名或类型可能导致数据丢失
          </div>
        </el-form-item>
        <el-form-item label="字段类型">
          <div class="type-grid">
            <div v-for="t in fieldTypes" :key="t.value" class="type-item" :class="{active: newColData.uiType === t.value}" @click="newColData.uiType = t.value">
              <el-icon><component :is="t.icon"/></el-icon><span>{{ t.label }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="newColData.uiType === 'Select'" label="预设选项">
          <el-input v-model="newColData.options" placeholder="选项1,选项2,选项3" />
        </el-form-item>
        <el-form-item v-if="newColData.uiType === 'Attachment'" label="文件后缀限制">
          <el-input v-model="newColData.options" placeholder=".jpg,.png,.pdf" />
          <div class="form-tip">留空则不限制类型</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" style="width: 100%" @click="submitColumn" :loading="submitting">
          {{ isEditColumn ? '保存修改' : '确认创建' }}
        </el-button>
      </template>
    </el-drawer>

    <el-drawer v-model="exportDrawerVisible" title="导出到 Excel" size="400px" append-to-body>
      <div class="export-config">
        <div class="config-section">
          <div class="section-title">1. 选择数据范围</div>
          <el-radio-group v-model="exportConfig.scope" class="scope-radio">
            <el-radio label="all" border>所有结果 ({{ viewPagination.total }})</el-radio>
            <el-radio label="selected" border :disabled="selectedRows.length === 0">
              仅选中行 ({{ selectedRows.length }})
            </el-radio>
          </el-radio-group>
        </div>
        <div class="config-section">
          <div class="section-title">
            2. 选择导出字段
            <el-checkbox v-model="exportConfig.checkAll" :indeterminate="isIndeterminate" @change="handleCheckAllChange" size="small" style="float:right">全选</el-checkbox>
          </div>
          <el-scrollbar max-height="300px" class="col-list">
            <el-checkbox-group v-model="exportConfig.checkedColumns" @change="handleCheckedColumnsChange">
              <div v-for="col in viewTableColumns" :key="col.prop" class="col-item">
                <el-checkbox :label="col.prop">{{ col.label }}</el-checkbox>
              </div>
            </el-checkbox-group>
          </el-scrollbar>
        </div>
      </div>
      <template #footer>
        <el-button @click="exportDrawerVisible = false">取消</el-button>
        <el-button type="primary" @click="handleExport" :loading="exportLoading">
          <el-icon class="mr-1"><Download /></el-icon> 确认导出
        </el-button>
      </template>
    </el-drawer>

    <el-dialog v-model="importDialogVisible" :title="importDialogTitle" width="600px">
      <el-form :model="importForm" label-width="100px">
        <el-form-item label="目标表名">
          <el-input v-model="importForm.tableName" placeholder="请输入目标表名" />
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload ref="importUploadRef" :auto-upload="false" :limit="1" :on-change="handleImportFileChange" accept=".xlsx,.xls,.csv,.json">
            <el-button type="primary">选择文件</el-button>
            <template #tip><div class="el-upload__tip">支持 Excel (.xlsx, .xls)、CSV (.csv)、JSON (.json) 文件</div></template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importLoading">导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dbImportDialogVisible" title="从数据库导入" width="700px">
      <el-form :model="dbImportForm" label-width="120px">
        <el-form-item label="数据源类型">
          <el-select v-model="dbImportForm.dbType" style="width: 100%">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址"><el-input v-model="dbImportForm.host" placeholder="localhost" /></el-form-item>
        <el-form-item label="端口"><el-input v-model="dbImportForm.port" placeholder="3306" /></el-form-item>
        <el-form-item label="数据库名"><el-input v-model="dbImportForm.database" placeholder="database_name" /></el-form-item>
        <el-form-item label="用户名"><el-input v-model="dbImportForm.username" placeholder="root" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="dbImportForm.password" type="password" show-password /></el-form-item>
        <el-form-item label="选择表">
          <el-select v-model="dbImportForm.selectedTables" multiple style="width: 100%" placeholder="选择要导入的表">
            <el-option v-for="t in dbImportForm.availableTables" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dbImportDialogVisible = false">取消</el-button>
        <el-button @click="testDbConnection">测试连接</el-button>
        <el-button type="primary" @click="handleDbImport" :loading="dbImportLoading">导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recycleBinDialogVisible" title="回收站" width="800px">
      <el-table :data="recycleBinTables" v-loading="recycleBinLoading">
        <el-table-column prop="tableName" label="表名" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="150" />
        <el-table-column prop="recordCount" label="记录数" width="100" align="right" />
        <el-table-column prop="deleteTime" label="删除时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleRestoreTable(row)">恢复</el-button>
            <el-button link type="danger" @click="handlePermanentDelete(row)">彻底删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="recycleBinTables.length === 0" description="回收站为空" />
      <template #footer>
        <el-button @click="recycleBinDialogVisible = false">关闭</el-button>
        <el-button type="danger" @click="handleEmptyRecycleBin" :disabled="recycleBinTables.length === 0">清空回收站</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataAnalysis, DataLine, Grid, Plus, Delete, Search, Refresh, Download, Document, Upload,
  DocumentAdd, Tickets, Menu, MoreFilled, EditPen, Connection, Coin, List, Filter, Sort,
  Operation, ArrowDown, Close, CircleCloseFilled, UploadFilled, InfoFilled, Star, Money,
  Paperclip, Calendar, Select, Link
} from '@element-plus/icons-vue'
import {
  getOverview, getDataRanking, getSystemQuota, getTableList, createTable, getTableInfo,
  updateTable, deleteTable, getTableData, insertData, updateData, deleteData, batchDeleteData,
  exportTableData, importExcel, importCsv, importJson, getSourceTables, importTableFromDatabase,
  getTableColumns, addColumn, modifyColumn, dropColumn, generateCrudApi,
  getRecycleBinTables, softDeleteTable, restoreTable, permanentDeleteTable
} from '@/api/dataCenter'

const UPLOAD_ACTION_URL = '/api/file/upload'

const uploadHeaders = ref({
  'satoken': localStorage.getItem('token') || '',
  'Authorization': 'Bearer ' + (localStorage.getItem('token') || '')
})

const currentView = ref('tables')
const viewMode = ref('grid')
const loading = ref(false)
const tableSearchText = ref('')
const tableList = ref([])

const filteredTableList = computed(() => {
  if (!tableSearchText.value) return tableList.value
  const search = tableSearchText.value.toLowerCase()
  return tableList.value.filter(t => t.tableName.toLowerCase().includes(search) || (t.description && t.description.toLowerCase().includes(search)))
})

const createTableDrawerVisible = ref(false)
const createTableLoading = ref(false)
const createTableForm = reactive({
  tableName: '',
  description: '',
  fields: [{ label: '', name: 'id', uiType: 'Input', options: '' }]
})

const fieldTypes = [
  { label: '单行文本', value: 'Input', icon: Document },
  { label: '数字', value: 'InputNumber', icon: Sort },
  { label: '下拉单选', value: 'Select', icon: ArrowDown },
  { label: '附件/文件', value: 'Attachment', icon: Paperclip },
  { label: '开关', value: 'Switch', icon: Select },
  { label: '日期', value: 'DatePicker', icon: Calendar },
  { label: '评分', value: 'Rating', icon: Star },
  { label: '货币', value: 'Currency', icon: Money },
  { label: '多行文本', value: 'Textarea', icon: Document },
]

const viewTableInfo = reactive({ tableName: '', description: '', recordCount: 0 })
const viewTableColumns = ref([])
const viewTableData = ref([])
const viewLoading = ref(false)
const viewSearchText = ref('')
const viewPagination = reactive({ page: 1, pageSize: 20, total: 0 })
const selectedRows = ref([])
const tableRef = ref()

const filterList = ref([])
const sortState = reactive({ field: '', order: 'DESC' })

const visibleColumns = computed(() => viewTableColumns.value.filter(c => c.visible))

const rowDrawerVisible = ref(false)
const currentRow = ref(null)
const rowData = ref({})
const dataFormLoading = ref(false)
const dataFormRef = ref(null)
const dataFormRules = ref({})

const editableColumns = computed(() => {
  const systemFields = ['id', 'created_at', 'updated_at', 'created_by', 'updated_by', 'create_time', 'update_time', 'create_user', 'update_user', 'deleted']
  return viewTableColumns.value.filter(col => !systemFields.includes(col.prop.toLowerCase()))
})

const colDrawerVisible = ref(false)
const isEditColumn = ref(false)
const newColData = ref({ id: null, label: '', prop: '', uiType: 'Input', options: '', oldProp: '' })
const submitting = ref(false)

const exportDrawerVisible = ref(false)
const exportLoading = ref(false)
const exportConfig = reactive({
  scope: 'all',
  checkAll: true,
  checkedColumns: []
})
const isIndeterminate = computed(() => exportConfig.checkedColumns.length > 0 && exportConfig.checkedColumns.length < viewTableColumns.value.length)

const importDialogVisible = ref(false)
const importDialogTitle = ref('')
const importForm = reactive({ tableName: '', file: null })
const importLoading = ref(false)
const importUploadRef = ref(null)

const dbImportDialogVisible = ref(false)
const dbImportForm = reactive({ dbType: 'mysql', host: 'localhost', port: '3306', database: '', username: '', password: '', selectedTables: [], availableTables: [] })
const dbImportLoading = ref(false)

const recycleBinDialogVisible = ref(false)
const recycleBinTables = ref([])
const recycleBinLoading = ref(false)

const getColorHash = (str) => {
  if (!str) return {}
  let hash = 0
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash)
  const c = (hash & 0x00ffffff).toString(16).toUpperCase()
  const hex = "#" + "00000".substring(0, 6 - c.length) + c
  return { backgroundColor: hex + '20', color: hex, border: `1px solid ${hex}40` }
}

const getFileUrl = (fileName) => {
  if (!fileName) return ''
  if (fileName.startsWith('http')) return fileName
  return fileName
}

const getFileName = (fileName) => {
  if (!fileName) return ''
  return fileName.split('/').pop()
}

const downloadFile = (fileName) => {
  if (!fileName) return
  window.open(fileName, '_blank')
}

const isImage = (fileName) => /\.(jpg|jpeg|png|gif|webp|bmp)$/i.test(fileName)

const parseOptions = (optStr) => {
  if (!optStr) return []
  return optStr.split(/[,，]/).filter(s => s)
}

const getIconByUiType = (type) => {
  const found = fieldTypes.find(t => t.value === type)
  return found ? found.icon : Document
}

const getFieldTypeName = (uiType) => {
  const found = fieldTypes.find(t => t.value === uiType)
  return found ? found.label : '文本'
}

const isNullableField = (col) => {
  return col.nullable === true || col.nullable === 'YES' || col.nullable === 1
}

const getFieldMaxLength = (col) => {
  if (col.uiType === 'Textarea') return 65535
  if (col.uiType === 'Input') return 255
  return null
}

const getMinValue = (col) => {
  return -2147483648
}

const getMaxValue = (col) => {
  return 2147483647
}

const getAcceptTypes = (col) => {
  if (!col.options) return ''
  const exts = col.options.split(',').map(e => e.trim()).filter(e => e)
  if (exts.length === 0) return ''
  const imageExts = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp']
  const isImageOnly = exts.every(e => imageExts.includes(e.toLowerCase()))
  if (isImageOnly) return 'image/*'
  return exts.join(',')
}

const getFieldRules = (col) => {
  const rules = []
  
  const isNullable = col.nullable === true || col.nullable === 'YES' || col.nullable === 1
  if (!isNullable) {
    rules.push({ required: true, message: `${col.label}不能为空`, trigger: 'blur' })
  }
  
  if (col.uiType === 'Input') {
    rules.push({ max: 255, message: '长度不能超过255个字符', trigger: 'blur' })
  }
  
  if (col.uiType === 'InputNumber') {
    rules.push({ type: 'number', message: '请输入有效的数字', trigger: 'blur' })
  }
  
  if (col.uiType === 'Currency') {
    rules.push({ type: 'number', message: '请输入有效的金额', trigger: 'blur' })
  }
  
  if (col.uiType === 'DatePicker') {
    rules.push({ type: 'string', message: '请选择有效的日期', trigger: 'change' })
  }
  
  if (col.uiType === 'Rating') {
    rules.push({ type: 'number', min: 1, max: 5, message: '评分范围为1-5', trigger: 'change' })
  }
  
  return rules
}

const beforeUploadWithType = (file, col) => {
  console.log('Before upload - file:', file.name, 'size:', file.size, 'type:', file.type)
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  
  if (col.options) {
    const allowedExts = col.options.split(',').map(e => e.trim().toLowerCase()).filter(e => e)
    if (allowedExts.length > 0) {
      const fileName = file.name.toLowerCase()
      const isValid = allowedExts.some(ext => fileName.endsWith(ext))
      if (!isValid) {
        ElMessage.error(`只允许上传 ${col.options} 格式的文件`)
        return false
      }
    }
  }
  
  console.log('Upload headers:', uploadHeaders.value)
  return true
}

const getUiTypeFromDataType = (dataType, remarks) => {
  if (!dataType) return 'Input'
  const t = dataType.toUpperCase()
  const r = (remarks || '').toLowerCase()
  
  if (r.includes('[image]') || r.includes('[file]')) return 'Attachment'
  if (t.includes('INT') && !t.includes('BIGINT')) return 'InputNumber'
  if (t.includes('BIGINT')) return 'InputNumber'
  if (t.includes('DECIMAL') || t.includes('FLOAT') || t.includes('DOUBLE')) return 'Currency'
  if (t.includes('DATE') || t.includes('TIME')) return 'DatePicker'
  if (t.includes('TINYINT(1)')) return 'Switch'
  if (t.includes('TEXT')) return 'Textarea'
  if (t.includes('VARCHAR(500)')) {
    if (r.includes('image') || r.includes('file')) return 'Attachment'
  }
  return 'Input'
}

const loadTableList = async () => {
  loading.value = true
  try {
    const res = await getTableList()
    if (res.code === 200 || res.code === 0) tableList.value = res.data || []
  } catch (error) { console.error('获取表列表失败:', error) }
  finally { loading.value = false }
}

const refreshData = async () => {
  await loadTableList()
}

const openCreateTableDialog = () => {
  createTableForm.tableName = ''
  createTableForm.description = ''
  createTableForm.fields = [
    { label: 'ID', name: 'id', uiType: 'Input', options: '' },
    { label: '', name: '', uiType: 'Input', options: '' }
  ]
  createTableDrawerVisible.value = true
}

const addFieldToCreate = () => {
  createTableForm.fields.push({ label: '', name: '', uiType: 'Input', options: '' })
}

const handleCreateTable = async () => {
  if (!createTableForm.tableName) { ElMessage.warning('请输入表名'); return }
  const validFields = createTableForm.fields.filter(f => f.name && f.name.trim())
  if (validFields.length === 0) { ElMessage.warning('请至少添加一个字段'); return }
  
  createTableLoading.value = true
  try {
    const tableConfig = {
      tableName: 'tb_' + createTableForm.tableName,
      description: createTableForm.description,
      columns: validFields.map(f => {
        let dataType = 'VARCHAR(255)'
        if (f.uiType === 'InputNumber') dataType = 'INT'
        else if (f.uiType === 'Currency') dataType = 'DECIMAL(10,2)'
        else if (f.uiType === 'DatePicker') dataType = 'DATETIME'
        else if (f.uiType === 'Switch') dataType = 'TINYINT(1)'
        else if (f.uiType === 'Rating') dataType = 'INT'
        else if (f.uiType === 'Textarea') dataType = 'TEXT'
        else if (f.uiType === 'Attachment') dataType = 'VARCHAR(500)'
        
        let comment = f.label
        if (f.uiType === 'Select' && f.options) {
          comment = f.label + ' [' + f.options + ']'
        } else if (f.uiType === 'Attachment') {
          comment = f.label + ' [file]'
          if (f.options) {
            comment = f.label + ' [' + f.options + ']'
          }
        }
        
        return {
          name: f.name.startsWith('col_') ? f.name : (f.name === 'id' ? 'id' : 'col_' + f.name),
          type: dataType,
          comment,
          extra: ''
        }
      })
    }
    const res = await createTable(tableConfig)
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('表创建成功')
      createTableDrawerVisible.value = false
      await loadTableList()
    }
  } catch (error) { if (!error.handled) ElMessage.error('创建失败: ' + (error.message || '未知错误')) }
  finally { createTableLoading.value = false }
}

const handleTableCommand = async (cmd, table) => {
  if (cmd === 'edit') {
    currentView.value = 'data'
    await openTableData(table)
  } else if (cmd === 'delete') {
    try {
      await ElMessageBox.confirm(`确定将 "${table.tableName}" 移入回收站吗？`, '提示', { type: 'warning', confirmButtonText: '移入回收站' })
      const res = await softDeleteTable(table.tableName)
      if (res.code === 200 || res.code === 0) { ElMessage.success('已移入回收站'); await loadTableList() }
    } catch (error) { if (error !== 'cancel') console.error('删除表失败:', error) }
  } else if (cmd === 'api') {
    const res = await generateCrudApi(table.tableName, { generateList: true, generateDetail: true, generateCreate: true, generateUpdate: true, generateDelete: true })
    if (res.code === 200 || res.code === 0) ElMessage.success('API生成成功')
  }
}

const openTableData = async (table) => {
  currentView.value = 'data'
  viewTableInfo.tableName = table.tableName
  viewTableInfo.description = table.description
  viewTableInfo.recordCount = table.recordCount
  viewSearchText.value = ''
  viewPagination.page = 1
  filterList.value = []
  sortState.field = ''
  selectedRows.value = []
  await loadViewTableData()
}

const loadViewTableData = async () => {
  viewLoading.value = true
  try {
    const res = await getTableData(viewTableInfo.tableName, viewPagination.page, viewPagination.pageSize, viewSearchText.value, sortState.field, sortState.order)
    if (res.code === 200 || res.code === 0) {
      const data = res.data
      viewTableColumns.value = []
      if (data.columns && data.columns.length > 0) {
        if (typeof data.columns[0] === 'object') {
          data.columns.forEach(col => {
            const remarks = col.remarks || col.columnComment || col.COLUMN_COMMENT || ''
            const uiType = getUiTypeFromDataType(col.dataType || col.COLUMN_TYPE, remarks)
            viewTableColumns.value.push({
              prop: col.columnName || col.COLUMN_NAME,
              label: remarks.replace(/\[image\]|\[file\]/g, '').trim() || col.columnName || col.COLUMN_NAME,
              dataType: col.dataType || col.COLUMN_TYPE,
              nullable: col.nullable,
              remarks,
              uiType,
              options: extractOptions(remarks),
              visible: true,
              columnKey: col.columnKey || col.COLUMN_KEY
            })
          })
        } else {
          data.columns.forEach(col => viewTableColumns.value.push({ prop: col, label: col, uiType: 'Input', visible: true }))
        }
      } else if (data.data && data.data.length > 0) {
        Object.keys(data.data[0]).forEach(key => viewTableColumns.value.push({ prop: key, label: key, uiType: 'Input', visible: true }))
      }
      viewTableData.value = data.data || []
      viewPagination.total = data.total || 0
    }
  } catch (error) { console.error('获取表数据失败:', error); ElMessage.error('获取表数据失败') }
  finally { viewLoading.value = false }
}

const extractOptions = (remarks) => {
  if (!remarks) return ''
  const match = remarks.match(/\[([^\]]+)\]$/)
  if (match) {
    const content = match[1]
    if (content === 'image' || content === 'file') return ''
    if (content.includes('.')) return content
    return content
  }
  return ''
}

const handleSelectionChange = (val) => selectedRows.value = val
const clearSelection = () => tableRef.value?.clearSelection()
const addFilter = () => filterList.value.push({ col: '', op: 'like', val: '' })

const showAddDataDialog = () => {
  currentRow.value = null
  rowData.value = {}
  editableColumns.value.forEach(col => {
    rowData.value[col.prop] = col.uiType === 'Switch' ? 0 : null
  })
  rowDrawerVisible.value = true
}

const showEditDataDialog = (row) => {
  currentRow.value = row
  rowData.value = { ...row }
  rowDrawerVisible.value = true
}

const handleSaveData = async () => {
  if (!dataFormRef.value) return
  
  try {
    await dataFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请检查表单填写是否正确')
    return
  }
  
  dataFormLoading.value = true
  try {
    const submitData = {}
    
    editableColumns.value.forEach(col => {
      const value = rowData.value[col.prop]
      if (value !== undefined && value !== null && value !== '') {
        submitData[col.prop] = value
      }
    })
    
    console.log('Submitting data:', submitData)
    
    if (Object.keys(submitData).length === 0) {
      ElMessage.warning('请至少填写一个字段')
      dataFormLoading.value = false
      return
    }
    
    let res
    if (currentRow.value) {
      const pkCol = viewTableColumns.value.find(c => c.columnKey === 'PRI')
      const primaryKeyValue = pkCol ? rowData.value[pkCol.prop] : rowData.value.id || rowData.value.ID
      if (primaryKeyValue === null || primaryKeyValue === undefined) { 
        ElMessage.error('无法获取主键值')
        dataFormLoading.value = false
        return
      }
      res = await updateData(viewTableInfo.tableName, primaryKeyValue, submitData)
    } else {
      res = await insertData(viewTableInfo.tableName, submitData)
    }
    
    console.log('Save response:', res)
    
    if (res.code === 200 || res.code === 0) {
      ElMessage.success(currentRow.value ? '数据更新成功' : '数据添加成功')
      rowDrawerVisible.value = false
      await loadViewTableData()
    } else {
      ElMessage.error(res.message || res.msg || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(error.message || '操作失败，请重试')
  } finally {
    dataFormLoading.value = false
  }
}

const confirmDeleteData = async (row) => {
  try {
    const pkCol = viewTableColumns.value.find(c => c.columnKey === 'PRI')
    const primaryKeyValue = pkCol ? row[pkCol.prop] : row.id || row.ID
    const res = await deleteData(viewTableInfo.tableName, primaryKeyValue)
    if (res.code === 200 || res.code === 0) { ElMessage.success('删除成功'); await loadViewTableData() }
  } catch (error) { console.error('删除失败:', error) }
}

const batchDeleteSelectedRows = async () => {
  if (selectedRows.value.length === 0) { ElMessage.warning('请选择要删除的记录'); return }
  try {
    await ElMessageBox.confirm(`确定删除 ${selectedRows.value.length} 条记录？`, '批量删除', { type: 'warning' })
    const pkCol = viewTableColumns.value.find(c => c.columnKey === 'PRI')
    const ids = selectedRows.value.map(row => pkCol ? row[pkCol.prop] : row.id || row.ID)
    const res = await batchDeleteData(viewTableInfo.tableName, ids)
    if (res.code === 200 || res.code === 0) { ElMessage.success('批量删除成功'); selectedRows.value = []; await loadViewTableData() }
  } catch (error) { if (error !== 'cancel') console.error('批量删除失败:', error) }
}

const handleColumnCommand = (cmd, col) => {
  if (cmd === 'edit') openColDrawer(true, col)
  else if (cmd === 'delete') confirmDropColumn(col)
}

const openColDrawer = (isEdit, colData) => {
  isEditColumn.value = isEdit
  colDrawerVisible.value = true
  if (isEdit && colData) {
    newColData.value = { ...colData, oldProp: colData.prop }
  } else {
    newColData.value = { id: null, label: '', prop: '', uiType: 'Input', options: '', oldProp: '' }
  }
}

const submitColumn = async () => {
  if (!newColData.value.label || !newColData.value.prop) return ElMessage.warning('请填写完整')
  submitting.value = true
  try {
    let dataType = 'VARCHAR(255)'
    if (newColData.value.uiType === 'InputNumber') dataType = 'INT'
    else if (newColData.value.uiType === 'Currency') dataType = 'DECIMAL(10,2)'
    else if (newColData.value.uiType === 'DatePicker') dataType = 'DATETIME'
    else if (newColData.value.uiType === 'Switch') dataType = 'TINYINT(1)'
    else if (newColData.value.uiType === 'Rating') dataType = 'INT'
    else if (newColData.value.uiType === 'Textarea') dataType = 'TEXT'
    else if (newColData.value.uiType === 'Attachment') dataType = 'VARCHAR(500)'

    let comment = newColData.value.label
    if (newColData.value.uiType === 'Select' && newColData.value.options) {
      comment = newColData.value.label + ' [' + newColData.value.options + ']'
    } else if (newColData.value.uiType === 'Attachment') {
      comment = newColData.value.label + ' [file]'
      if (newColData.value.options) {
        comment = newColData.value.label + ' [' + newColData.value.options + ']'
      }
    }

    const columnConfig = {
      columnName: newColData.value.prop.startsWith('col_') ? newColData.value.prop : 'col_' + newColData.value.prop,
      dataType,
      comment,
      extra: ''
    }

    let res
    if (isEditColumn.value) {
      res = await modifyColumn(viewTableInfo.tableName, newColData.value.oldProp, columnConfig)
    } else {
      res = await addColumn(viewTableInfo.tableName, columnConfig)
    }
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('操作成功')
      colDrawerVisible.value = false
      await loadViewTableData()
    }
  } catch (error) { ElMessage.error('操作失败') }
  finally { submitting.value = false }
}

const confirmDropColumn = async (col) => {
  try {
    await ElMessageBox.confirm(`确定要删除列 "${col.prop}" 吗？此操作不可恢复！`, '删除确认', { type: 'warning' })
    const res = await dropColumn(viewTableInfo.tableName, col.prop)
    if (res.code === 200 || res.code === 0) { ElMessage.success('删除成功'); await loadViewTableData() }
  } catch (error) { if (error !== 'cancel') console.error('删除列失败:', error) }
}

const openExportDrawer = () => {
  exportConfig.checkedColumns = viewTableColumns.value.map(c => c.prop)
  exportConfig.checkAll = true
  exportConfig.scope = selectedRows.value.length > 0 ? 'selected' : 'all'
  exportDrawerVisible.value = true
}

const handleCheckAllChange = (val) => {
  exportConfig.checkedColumns = val ? viewTableColumns.value.map(c => c.prop) : []
}

const handleCheckedColumnsChange = (value) => {
  exportConfig.checkAll = value.length === viewTableColumns.value.length
}

const handleExport = async () => {
  if (exportConfig.checkedColumns.length === 0) return ElMessage.warning('请至少选择一列')
  exportLoading.value = true
  try {
    const res = await exportTableData(viewTableInfo.tableName, 'xlsx', viewSearchText.value)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `${viewTableInfo.tableName}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
    exportDrawerVisible.value = false
  } catch (error) { ElMessage.error('导出失败') }
  finally { exportLoading.value = false }
}

const handleUploadSuccess = (res, prop) => {
  console.log('Upload response:', res)
  if (res.code === 200 || res.code === 0) {
    rowData.value[prop] = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.message || res.msg || '上传失败')
  }
}

const handleUploadError = (error) => {
  console.error('Upload error:', error)
  ElMessage.error('文件上传失败，请检查网络连接')
}

const beforeUpload = (file) => {
  if (file.size / 1024 / 1024 > 50) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  return true
}

const openImportDialog = (type) => {
  importDialogTitle.value = type === 'excel' ? '导入 Excel' : type === 'csv' ? '导入 CSV' : '导入 JSON'
  importForm.tableName = ''
  importForm.file = null
  importDialogVisible.value = true
}

const handleImportFileChange = (file) => importForm.file = file.raw

const handleImport = async () => {
  if (!importForm.tableName || !importForm.file) { ElMessage.warning('请填写表名并选择文件'); return }
  importLoading.value = true
  try {
    let res
    if (importForm.file.name.endsWith('.xlsx') || importForm.file.name.endsWith('.xls')) res = await importExcel(importForm.file, importForm.tableName)
    else if (importForm.file.name.endsWith('.csv')) res = await importCsv(importForm.file, importForm.tableName)
    else if (importForm.file.name.endsWith('.json')) res = await importJson(importForm.file, importForm.tableName)
    if (res && (res.code === 200 || res.code === 0)) { ElMessage.success('导入成功'); importDialogVisible.value = false; await loadTableList() }
  } catch (error) { console.error('导入失败:', error) }
  finally { importLoading.value = false }
}

const openDbImportDialog = () => {
  dbImportForm.dbType = 'mysql'
  dbImportForm.host = 'localhost'
  dbImportForm.port = '3306'
  dbImportForm.database = ''
  dbImportForm.username = ''
  dbImportForm.password = ''
  dbImportForm.selectedTables = []
  dbImportForm.availableTables = []
  dbImportDialogVisible.value = true
}

const testDbConnection = async () => {
  try {
    const res = await getSourceTables({ type: dbImportForm.dbType, host: dbImportForm.host, port: dbImportForm.port, database: dbImportForm.database, username: dbImportForm.username, password: dbImportForm.password })
    if (res.code === 200 || res.code === 0) { dbImportForm.availableTables = res.data || []; ElMessage.success('连接成功') }
  } catch (error) { ElMessage.error('连接失败') }
}

const handleDbImport = async () => {
  if (dbImportForm.selectedTables.length === 0) { ElMessage.warning('请选择要导入的表'); return }
  dbImportLoading.value = true
  try {
    for (const tableName of dbImportForm.selectedTables) {
      await importTableFromDatabase({ type: dbImportForm.dbType, host: dbImportForm.host, port: dbImportForm.port, database: dbImportForm.database, username: dbImportForm.username, password: dbImportForm.password }, tableName, tableName)
    }
    ElMessage.success('导入成功')
    dbImportDialogVisible.value = false
    await loadTableList()
  } catch (error) { console.error('导入失败:', error) }
  finally { dbImportLoading.value = false }
}

const openRecycleBinDialog = async () => {
  recycleBinDialogVisible.value = true
  recycleBinLoading.value = true
  try {
    const res = await getRecycleBinTables()
    if (res.code === 200 || res.code === 0) recycleBinTables.value = res.data || []
  } catch (error) { console.error('获取回收站数据失败:', error) }
  finally { recycleBinLoading.value = false }
}

const handleRestoreTable = async (row) => {
  try {
    const res = await restoreTable(row.tableName)
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('恢复成功')
      await openRecycleBinDialog()
      await loadTableList()
    }
  } catch (error) { console.error('恢复失败:', error) }
}

const handlePermanentDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要彻底删除表 "${row.tableName}" 吗？此操作不可恢复！`, '彻底删除确认', { type: 'warning' })
    const res = await permanentDeleteTable(row.tableName)
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('彻底删除成功')
      await openRecycleBinDialog()
    }
  } catch (error) { if (error !== 'cancel') console.error('彻底删除失败:', error) }
}

const handleEmptyRecycleBin = async () => {
  try {
    await ElMessageBox.confirm('确定要清空回收站吗？所有表将被永久删除，此操作不可恢复！', '清空回收站', { type: 'warning' })
    for (const table of recycleBinTables.value) {
      await permanentDeleteTable(table.tableName)
    }
    ElMessage.success('回收站已清空')
    recycleBinTables.value = []
  } catch (error) { if (error !== 'cancel') console.error('清空回收站失败:', error) }
}

const headerCellStyle = { background: '#f7f9fd', color: '#666', fontWeight: '600', height: '44px', padding: '0', fontSize: '13px', borderRight: '1px solid #e0e0e0', borderBottom: '1px solid #e0e0e0' }
const cellStyle = { padding: '0', height: '44px', borderRight: '1px solid #eee', fontSize: '13px', color: '#333' }

onMounted(() => refreshData())
</script>

<style scoped>
.smart-grid-container { height: 100%; display: flex; flex-direction: column; background: #f5f7fa; font-family: 'Inter', -apple-system, sans-serif; }
.view-header { height: 50px; border-bottom: 1px solid #e0e0e0; display: flex; justify-content: space-between; align-items: center; padding: 0 16px; background: #fff; user-select: none; }
.header-left { display: flex; align-items: center; gap: 12px; }
.table-info { display: flex; align-items: center; gap: 10px; }
.icon-wrapper { width: 32px; height: 32px; border-radius: 6px; display: flex; align-items: center; justify-content: center; }
.info-text { display: flex; flex-direction: column; line-height: 1.2; }
.table-name { font-weight: 700; font-size: 14px; color: #333; }
.db-name { font-family: monospace; font-size: 11px; color: #999; }
.view-tabs { display: flex; height: 100%; align-items: flex-end; gap: 20px; margin-left: 20px; }
.tab-item { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #666; padding-bottom: 14px; cursor: pointer; border-bottom: 2px solid transparent; }
.tab-item:hover { color: #1890ff; }
.tab-item.active { color: #1890ff; border-bottom-color: #1890ff; font-weight: 500; }
.header-right { display: flex; align-items: center; gap: 10px; }

.tables-view { flex: 1; overflow: auto; padding: 20px; }
.section-label { display: flex; align-items: center; gap: 12px; margin-bottom: 15px; font-size: 12px; font-weight: bold; color: #909399; text-transform: uppercase; letter-spacing: 0.5px; }
.mt-30 { margin-top: 30px; }
.divider { flex: 1; height: 1px; background: #ebeef5; }

.creation-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 15px; margin-bottom: 20px; }
.create-card { background: #fff; border: 1px dashed #dcdfe6; border-radius: 8px; padding: 16px; display: flex; align-items: center; gap: 12px; cursor: pointer; transition: all 0.2s; }
.create-card:hover { border-color: #409EFF; background: #ecf5ff; transform: translateY(-2px); }
.c-icon-bg { width: 40px; height: 40px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 20px; }
.c-icon-bg.green { background: #f0f9eb; color: #67C23A; }
.c-icon-bg.orange { background: #fdf6ec; color: #E6A23C; }
.c-icon-bg.purple { background: #f4f4f5; color: #909399; }
.c-icon-bg.cyan { background: #ecf5ff; color: #409EFF; }
.c-icon-bg.blue { background: #ecf5ff; color: #409EFF; }
.c-info { display: flex; flex-direction: column; }
.c-title { font-weight: bold; font-size: 14px; color: #303133; }
.c-desc { font-size: 12px; color: #909399; margin-top: 2px; }

.table-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.view-toggle { display: flex; background: #f2f6fc; padding: 3px; border-radius: 6px; }
.toggle-btn { padding: 5px 10px; cursor: pointer; color: #909399; display: flex; transition: all 0.2s; border-radius: 4px; }
.toggle-btn:hover { color: #303133; }
.toggle-btn.active { background: #fff; color: #409EFF; box-shadow: 0 1px 2px rgba(0,0,0,0.1); }

.modules-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; }
.module-card { background: #fff; border: 1px solid #ebeef5; border-radius: 8px; padding: 18px; height: 140px; display: flex; flex-direction: column; cursor: pointer; transition: all 0.2s; }
.module-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); border-color: #dcdfe6; transform: translateY(-3px); }
.m-card-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.m-icon { width: 36px; height: 36px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 18px; }
.m-action-btn { padding: 4px; border-radius: 4px; transition: background 0.2s; }
.m-action-btn:hover { background: #f2f6fc; }
.m-more-icon { color: #909399; transform: rotate(90deg); cursor: pointer; }
.m-card-body { flex: 1; }
.m-name { font-weight: bold; font-size: 15px; color: #303133; margin-bottom: 6px; }
.m-meta { font-size: 12px; color: #909399; background: #f4f4f5; padding: 2px 6px; border-radius: 4px; display: inline-block; font-family: monospace; }
.m-card-footer { display: flex; justify-content: space-between; font-size: 12px; color: #C0C4CC; border-top: 1px solid #f9f9f9; padding-top: 10px; }

.modules-list { background: #fff; border: 1px solid #ebeef5; border-radius: 8px; font-size: 14px; }
.list-header { background: #fafafa; color: #909399; font-weight: bold; font-size: 13px; border-bottom: 1px solid #ebeef5; padding: 12px 20px; display: grid; grid-template-columns: 2fr 1.5fr 1fr 1fr 50px; }
.list-item { padding: 12px 20px; border-bottom: 1px solid #f9f9f9; display: grid; grid-template-columns: 2fr 1.5fr 1fr 1fr 50px; align-items: center; cursor: pointer; transition: background 0.2s; }
.list-item:hover { background: #f5f7fa; }
.list-icon { width: 28px; height: 28px; border-radius: 4px; display: flex; align-items: center; justify-content: center; margin-right: 10px; font-size: 14px; }
.col-name { display: flex; align-items: center; font-weight: 500; }
.col-desc { color: #606266; }

.grid-toolbar { height: 44px; border-bottom: 1px solid #e0e0e0; display: flex; justify-content: space-between; align-items: center; padding: 0 16px; background: #fff; }
.tool-group { display: flex; align-items: center; gap: 8px; }
.tool-btn { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #555; cursor: pointer; padding: 4px 8px; border-radius: 4px; position: relative; }
.tool-btn:hover { background: #f0f0f0; }
.tool-btn.active { background: #e6f7ff; color: #1890ff; font-weight: 500; }
.badge { background: #1890ff; color: #fff; font-size: 10px; padding: 0 4px; border-radius: 4px; margin-left: 4px; height: 16px; line-height: 16px; }
.divider { width: 1px; height: 16px; background: #ddd; margin: 0 4px; }
.divider-vertical { width: 1px; height: 16px; background: #ddd; margin: 0 10px; }
.search-input { width: 220px; }
.batch-actions { display: flex; align-items: center; background: #fff1f0; padding: 4px 12px; border-radius: 4px; border: 1px solid #ffa39e; }
.selected-count { font-size: 13px; color: #f56c6c; font-weight: 500; margin-right: 5px; }

.grid-body { flex: 1; overflow: hidden; display: flex; flex-direction: column; }
.grid-pagination { padding: 10px 16px; border-top: 1px solid #e0e0e0; display: flex; justify-content: flex-end; background: #fff; }

.custom-header { display: flex; align-items: center; width: 100%; height: 100%; cursor: pointer; padding-left: 8px; }
.custom-header:hover { background: #eceff5; }
.custom-header:hover .header-menu { opacity: 1; }
.header-menu { margin-left: auto; margin-right: 8px; opacity: 0; transition: opacity 0.2s; color: #999; }
.header-trigger { padding: 2px; border-radius: 4px; display: flex; opacity: 0; transition: opacity 0.2s; }
.custom-header:hover .header-trigger { opacity: 1; }
.header-trigger:hover { background: #dcdfe6; }
.header-icon { margin-right: 6px; font-size: 14px; color: #999; }
.header-title { font-size: 13px; color: #333; flex: 1; font-weight: 600; }

.cell-wrapper { display: flex; align-items: center; width: 100%; height: 100%; padding: 0 8px; }
.primary-cell { font-weight: 600; color: #303133; }
.cell-text { display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cell-center { display: flex; justify-content: center; width: 100%; }
.cell-link a { color: #1890ff; text-decoration: none; }
.noco-tag { padding: 2px 8px; border-radius: 12px; font-size: 12px; font-weight: 500; display: inline-block; white-space: nowrap; }
.row-ops-fixed { display: flex; justify-content: center; gap: 4px; }
.add-col-trigger { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; cursor: pointer; color: #999; }
.add-col-trigger:hover { background: #f0f0f0; color: #1890ff; }

.cell-attachment { display: flex; align-items: center; padding: 0 10px; height: 100%; width: 100%; }
.file-wrapper { display: flex; align-items: center; }
.table-thumb { width: 30px; height: 30px; border-radius: 4px; border: 1px solid #eee; cursor: zoom-in; }
.file-chip { display: flex; align-items: center; gap: 4px; background: #f5f7fa; padding: 2px 8px; border-radius: 12px; border: 1px solid #eee; cursor: pointer; font-size: 12px; color: #606266; transition: all 0.2s; }
.file-chip:hover { background: #ecf5ff; color: #1890ff; border-color: #b3d8ff; }
.no-data { color: #eee; }

.drawer-body { padding: 20px; }
.form-field-wrapper { margin-bottom: 20px; }
.field-label { display: flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 600; color: #666; margin-bottom: 6px; }
.required-mark { color: #f56c6c; margin-left: 2px; }
.field-type-tag { margin-left: auto; font-size: 10px; color: #909399; background: #f4f4f5; padding: 2px 6px; border-radius: 4px; font-weight: normal; }
.drawer-footer { display: flex; justify-content: flex-end; gap: 10px; }
.type-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.type-item { display: flex; align-items: center; gap: 8px; padding: 10px; border: 1px solid #eee; border-radius: 6px; cursor: pointer; transition: all 0.2s; font-size: 13px; color: #555; }
.type-item:hover { border-color: #1890ff; color: #1890ff; }
.type-item.active { background: #e6f7ff; border-color: #1890ff; color: #1890ff; font-weight: bold; }
.form-tip { font-size: 12px; color: #999; margin-top: 4px; }
.form-tip.warning { color: #e6a23c; }

.field-designer { display: flex; flex-direction: column; gap: 10px; max-height: 400px; overflow-y: auto; padding: 12px; background: #f8fafc; border-radius: 8px; }
.field-row { display: flex; align-items: center; gap: 8px; padding: 8px; background: white; border-radius: 6px; border: 1px solid #e2e8f0; }

.upload-box-wrapper { width: 100%; }
.upload-box-wrapper :deep(.el-upload) { width: 100%; display: block; }
.upload-box-wrapper :deep(.el-upload-dragger) { width: 100%; height: auto; min-height: 120px; padding: 0; border: 1px dashed #dcdfe6; background-color: #fafafa; display: flex; justify-content: center; align-items: center; transition: border-color 0.3s; }
.upload-box-wrapper :deep(.el-upload-dragger:hover) { border-color: #409EFF; background-color: #ecf5ff; }
.upload-empty-state { padding: 20px; display: flex; flex-direction: column; align-items: center; color: #606266; }
.cloud-icon { font-size: 32px; color: #909399; margin-bottom: 8px; }
.upload-text { font-size: 14px; }
.upload-hint { font-size: 12px; color: #909399; margin-top: 4px; }
.attachment-wrapper { width: 100%; }
.switch-wrapper { display: flex; align-items: center; padding: 8px 0; }
.uploaded-card { width: 100%; height: 100%; min-height: 120px; display: flex; align-items: center; padding: 15px; box-sizing: border-box; background: #fff; gap: 15px; }
.card-left { width: 50px; height: 50px; border-radius: 4px; border: 1px solid #eee; display: flex; align-items: center; justify-content: center; background: #f9f9f9; overflow: hidden; }
.mini-preview { width: 100%; height: 100%; object-fit: cover; }
.card-center { flex: 1; overflow: hidden; text-align: left; }
.file-title { font-size: 14px; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-weight: 500; }
.file-tip { font-size: 12px; color: #909399; margin-top: 4px; }
.card-right { cursor: pointer; padding: 5px; }
.delete-icon { font-size: 20px; color: #F56C6C; transition: transform 0.2s; }
.delete-icon:hover { transform: scale(1.2); }

.export-config { padding: 10px; }
.config-section { margin-bottom: 25px; }
.section-title { font-weight: 600; font-size: 14px; margin-bottom: 10px; color: #303133; }
.scope-radio { display: flex; flex-direction: column; gap: 10px; align-items: stretch; }
.col-list { border: 1px solid #ebeef5; border-radius: 4px; padding: 10px; }
.col-item { padding: 5px 0; }

.filter-panel, .sort-panel { padding: 12px; }
.empty-filter { text-align: center; color: #999; font-size: 12px; padding: 10px 0; }
.filter-row { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.logic { font-size: 12px; color: #999; width: 40px; text-align: center; font-weight: bold; }
.filter-footer { margin-top: 10px; padding-top: 10px; border-top: 1px dashed #eee; display: flex; justify-content: space-between; }
.sort-row { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; font-size: 13px; color: #666; }
</style>
