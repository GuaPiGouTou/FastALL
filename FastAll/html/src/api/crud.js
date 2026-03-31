import request from '@/utils/request'

export function getTableSchema(tableName) {
  return request({
    url: `/api/crud/${tableName}/schema`,
    method: 'get'
  })
}

export function getTableList(tableName, params) {
  return request({
    url: `/api/crud/${tableName}/list`,
    method: 'get',
    params
  })
}

export function saveTableData(tableName, data) {
  return request({
    url: `/api/crud/${tableName}/save`,
    method: 'post',
    data
  })
}

export function deleteTableData(tableName, id) {
  return request({
    url: `/api/crud/${tableName}/${id}`,
    method: 'delete'
  })
}

export function addFieldConfig(tableName, data) {
  return request({
    url: `/api/crud/${tableName}/column`,
    method: 'post',
    data
  })
}

export function updateFieldConfig(tableName, data) {
  return request({
    url: `/api/crud/${tableName}/column`,
    method: 'put',
    data
  })
}

export function exportTableData(tableName, data) {
  return request({
    url: `/api/crud/${tableName}/export`,
    method: 'post',
    data,
    responseType: 'blob'
  })
}

export function getModuleList() {
  return request({
    url: '/api/crud/modules',
    method: 'get'
  })
}

export function deleteModule(moduleName) {
  return request({
    url: `/api/crud/module/${moduleName}`,
    method: 'delete'
  })
}
