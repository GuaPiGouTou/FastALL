import request from '@/utils/request'

export function getApiList(params) {
  return request({
    url: '/api/api-manager/list',
    method: 'get',
    params
  })
}

export function getApiDetail(id) {
  return request({
    url: `/api/api-manager/${id}`,
    method: 'get'
  })
}

export function createApi(data) {
  return request({
    url: '/api/api-manager/create',
    method: 'post',
    data
  })
}

export function updateApi(id, data) {
  return request({
    url: `/api/api-manager/${id}`,
    method: 'put',
    data
  })
}

export function deleteApi(id) {
  return request({
    url: `/api/api-manager/${id}`,
    method: 'delete'
  })
}

export function testApi(id, params) {
  return request({
    url: `/api/api-manager/${id}/test`,
    method: 'post',
    data: params
  })
}

export function getApiOverview() {
  return request({
    url: '/api/api-manager/overview',
    method: 'get'
  })
}

export function getStatsByModule() {
  return request({
    url: '/api/api-manager/stats/module',
    method: 'get'
  })
}

export function getStatsByMethod() {
  return request({
    url: '/api/api-manager/stats/method',
    method: 'get'
  })
}

export function getCallLogs(params) {
  return request({
    url: '/api/api-manager/logs',
    method: 'get',
    params
  })
}

export function getTopCalledApis() {
  return request({
    url: '/api/api-manager/logs/top',
    method: 'get'
  })
}

export function getCallTrend() {
  return request({
    url: '/api/api-manager/logs/trend',
    method: 'get'
  })
}
