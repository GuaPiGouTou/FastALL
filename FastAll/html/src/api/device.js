import request from '@/utils/request'

/**
 * 获取设备列表
 */
export function getDeviceList(params) {
  return request({
    url: '/api/device/list',
    method: 'get',
    params
  })
}

/**
 * 新增设备
 */
export function addDevice(data) {
  return request({
    url: '/api/device/add',
    method: 'post',
    data
  })
}

/**
 * 修改设备
 */
export function updateDevice(data) {
  return request({
    url: '/api/device/update',
    method: 'put',
    data
  })
}

/**
 * 删除设备
 */
export function deleteDevice(id) {
  return request({
    url: `/api/device/delete/${id}`,
    method: 'delete'
  })
}
