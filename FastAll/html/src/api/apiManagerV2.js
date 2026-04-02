import request from '@/utils/request'

export function getApiList(params) {
  return request({
    url: '/v2/api-manager/list',
    method: 'get',
    params
  })
}

export function getApiDetail(id) {
  return request({
    url: `/v2/api-manager/${id}`,
    method: 'get'
  })
}

export function createApi(data) {
  return request({
    url: '/v2/api-manager/create',
    method: 'post',
    data
  })
}

export function updateApi(id, data) {
  return request({
    url: `/v2/api-manager/${id}`,
    method: 'put',
    data
  })
}

export function deleteApi(id) {
  return request({
    url: `/v2/api-manager/${id}`,
    method: 'delete'
  })
}

export function publishApi(id, user) {
  return request({
    url: `/v2/api-manager/${id}/publish`,
    method: 'post',
    params: { user }
  })
}

export function offlineApi(id, user) {
  return request({
    url: `/v2/api-manager/${id}/offline`,
    method: 'post',
    params: { user }
  })
}

export function copyApi(id, newName, user) {
  return request({
    url: `/v2/api-manager/${id}/copy`,
    method: 'post',
    params: { newName, user }
  })
}

export function getApiVersions(id) {
  return request({
    url: `/v2/api-manager/${id}/versions`,
    method: 'get'
  })
}

export function rollbackApi(id, versionId) {
  return request({
    url: `/v2/api-manager/${id}/rollback/${versionId}`,
    method: 'post'
  })
}

export function getApiOverview() {
  return request({
    url: '/v2/api-manager/overview',
    method: 'get'
  })
}

export function getApiGroups() {
  return request({
    url: '/v2/api-manager/groups',
    method: 'get'
  })
}

export function getApiGroupTree() {
  return request({
    url: '/v2/api-manager/groups/tree',
    method: 'get'
  })
}

export function createApiGroup(data) {
  return request({
    url: '/v2/api-manager/groups',
    method: 'post',
    data
  })
}

export function updateApiGroup(id, data) {
  return request({
    url: `/v2/api-manager/groups/${id}`,
    method: 'put',
    data
  })
}

export function deleteApiGroup(id) {
  return request({
    url: `/v2/api-manager/groups/${id}`,
    method: 'delete'
  })
}

export function getDatasources(environment) {
  return request({
    url: '/v2/api-manager/datasources',
    method: 'get',
    params: { environment }
  })
}

export function createDatasource(data) {
  return request({
    url: '/v2/api-manager/datasources',
    method: 'post',
    data
  })
}

export function testDatasource(id) {
  return request({
    url: `/v2/api-manager/datasources/${id}/test`,
    method: 'post'
  })
}

export function getDatasourceTables(id) {
  return request({
    url: `/v2/api-manager/datasources/${id}/tables`,
    method: 'get'
  })
}

export function getTableColumns(id, tableName) {
  return request({
    url: `/v2/api-manager/datasources/${id}/tables/${tableName}/columns`,
    method: 'get'
  })
}

export function getStatsByGroup() {
  return request({
    url: '/v2/api-manager/stats/group',
    method: 'get'
  })
}

export function getStatsByMethod() {
  return request({
    url: '/v2/api-manager/stats/method',
    method: 'get'
  })
}

export function getStatsByEnvironment() {
  return request({
    url: '/v2/api-manager/stats/environment',
    method: 'get'
  })
}

export function testApi(id, params) {
  return request({
    url: `/v2/api-manager/${id}/test`,
    method: 'post',
    data: params
  })
}

export function getDataCenterGroupTree() {
  return request({
    url: '/data-center/groups/tree',
    method: 'get'
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

export function getTableColumnsDirect(tableName) {
  return request({
    url: `/data-center/tables/${tableName}/columns`,
    method: 'get'
  })
}
