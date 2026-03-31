import request from '@/utils/request'

/**
 * 获取指定设备的维保记录列表
 */
export function getMaintenanceList(deviceId) {
  return request({
    url: `/api/device-maintenance/list/${deviceId}`,
    method: 'get'
  })
}

/**
 * 新增维保记录
 */
export function addMaintenance(data) {
  return request({
    url: '/api/device-maintenance/add',
    method: 'post',
    data
  })
}
