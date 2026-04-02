import request from '@/utils/request'

export function getOverview() {
  return request({
    url: '/data-center/overview',
    method: 'get'
  })
}

export function getDataRanking() {
  return request({
    url: '/data-center/ranking',
    method: 'get'
  })
}

export function getSystemQuota() {
  return request({
    url: '/data-center/quota',
    method: 'get'
  })
}

export function saveDbConfig(data) {
  return request({
    url: '/data-center/db-config',
    method: 'post',
    data
  })
}

export function testConnection(data) {
  return request({
    url: '/data-center/test-connection',
    method: 'post',
    data
  })
}

export function getConnectionStatus() {
  return request({
    url: '/data-center/connection-status',
    method: 'get'
  })
}

export function getTableList() {
  return request({
    url: '/data-center/tables',
    method: 'get'
  })
}

export function createTable(data) {
  return request({
    url: '/data-center/tables',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

export function getTableInfo(tableName) {
  return request({
    url: `/data-center/tables/${tableName}`,
    method: 'get'
  })
}

export function updateTable(tableName, data) {
  return request({
    url: `/data-center/tables/${tableName}`,
    method: 'put',
    data
  })
}

export function deleteTable(tableName) {
  return request({
    url: `/data-center/tables/${tableName}`,
    method: 'delete'
  })
}

export function getTableData(tableName, page = 1, pageSize = 20, search = '', sortField = '', sortOrder = '') {
  return request({
    url: `/data-center/tables/${tableName}/data`,
    method: 'get',
    params: {
      page,
      pageSize,
      search,
      sortField,
      sortOrder
    }
  })
}

export function insertData(tableName, data) {
  return request({
    url: `/data-center/tables/${tableName}/data`,
    method: 'post',
    data
  })
}

export function updateData(tableName, id, data) {
  return request({
    url: `/data-center/tables/${tableName}/data/${id}`,
    method: 'put',
    data
  })
}

export function deleteData(tableName, id) {
  return request({
    url: `/data-center/tables/${tableName}/data/${id}`,
    method: 'delete'
  })
}

export function batchDeleteData(tableName, ids) {
  return request({
    url: `/data-center/tables/${tableName}/data/batch`,
    method: 'delete',
    data: ids
  })
}

export function exportTableData(tableName, format = 'csv', search = '') {
  return request({
    url: `/data-center/tables/${tableName}/export`,
    method: 'get',
    params: {
      format,
      search
    },
    responseType: 'blob'
  })
}

export function importFile(file, tableName) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('tableName', tableName)
  
  return request({
    url: '/data-center/import/file',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function importExcel(file, tableName) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('tableName', tableName)
  
  return request({
    url: '/data-center/import/excel',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function importCsv(file, tableName) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('tableName', tableName)
  
  return request({
    url: '/data-center/import/csv',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function importJson(file, tableName) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('tableName', tableName)
  
  return request({
    url: '/data-center/import/json',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getSourceTables(sourceConfig) {
  return request({
    url: '/data-center/import/database/tables',
    method: 'post',
    data: sourceConfig
  })
}

export function getSourceTableStructure(sourceConfig, tableName) {
  return request({
    url: '/data-center/import/database/table/structure',
    method: 'post',
    data: {
      sourceConfig,
      tableName
    }
  })
}

export function importTableFromDatabase(sourceConfig, sourceTableName, targetTableName) {
  return request({
    url: '/data-center/import/database/table',
    method: 'post',
    data: {
      sourceConfig,
      sourceTableName,
      targetTableName
    }
  })
}

export function importMultipleTables(sourceConfig, sourceTableNames) {
  return request({
    url: '/data-center/import/database/tables/batch',
    method: 'post',
    data: {
      sourceConfig,
      sourceTableNames
    }
  })
}

export function getTableColumns(tableName) {
  return request({
    url: `/data-center/tables/${tableName}/columns`,
    method: 'get'
  })
}

export function addColumn(tableName, columnConfig) {
  return request({
    url: `/data-center/tables/${tableName}/columns`,
    method: 'post',
    data: columnConfig
  })
}

export function modifyColumn(tableName, columnName, columnConfig) {
  return request({
    url: `/data-center/tables/${tableName}/columns/${columnName}`,
    method: 'put',
    data: columnConfig
  })
}

export function dropColumn(tableName, columnName) {
  return request({
    url: `/data-center/tables/${tableName}/columns/${columnName}`,
    method: 'delete'
  })
}

export function generateCrudApi(tableName, options) {
  return request({
    url: `/data-center/tables/${tableName}/generate-api`,
    method: 'post',
    data: options
  })
}

export function dynamicQuery(queryConfig) {
  return request({
    url: '/data-center/dynamic-query',
    method: 'post',
    data: queryConfig
  })
}

export function getTableGroups() {
  return request({
    url: '/data-center/groups/all',
    method: 'get'
  })
}

export function getTableGroupList(page = 1, size = 20, keyword = '') {
  return request({
    url: '/data-center/groups/list',
    method: 'get',
    params: { page, size, keyword }
  })
}

export function getTableGroupTree() {
  return request({
    url: '/data-center/groups/tree',
    method: 'get'
  })
}

export function createTableGroup(data) {
  return request({
    url: '/data-center/groups',
    method: 'post',
    data
  })
}

export function updateTableGroup(id, data) {
  return request({
    url: `/data-center/groups/${id}`,
    method: 'put',
    data
  })
}

export function deleteTableGroup(id) {
  return request({
    url: `/data-center/groups/${id}`,
    method: 'delete'
  })
}

export function updateTableGroupForTable(tableName, data) {
  return request({
    url: `/data-center/tables/${tableName}/group`,
    method: 'put',
    data
  })
}

export function getDataCenterTables(groupId) {
  return request({
    url: `/data-center/table-groups/${groupId}/tables`,
    method: 'get'
  })
}

export function getAllDataCenterTables() {
  return request({
    url: '/data-center/table-groups/all/tables',
    method: 'get'
  })
}

export function getRecycleBinTables() {
  return request({
    url: '/data-center/recycle-bin',
    method: 'get'
  })
}

export function softDeleteTable(tableName) {
  return request({
    url: `/data-center/tables/${tableName}/soft-delete`,
    method: 'put'
  })
}

export function restoreTable(tableName) {
  return request({
    url: `/data-center/tables/${tableName}/restore`,
    method: 'put'
  })
}

export function permanentDeleteTable(tableName) {
  return request({
    url: `/data-center/tables/${tableName}/permanent`,
    method: 'delete'
  })
}