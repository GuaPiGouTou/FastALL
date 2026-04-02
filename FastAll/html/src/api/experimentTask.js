import request from '@/utils/request'

/**
 * 获取实验任务列表
 */
export function getExperimentTaskList(params) {
  return request({
    url: '/experimentTask/list',
    method: 'get',
    params
  })
}

/**
 * 新增实验任务
 */
export function addExperimentTask(data) {
  return request({
    url: '/experimentTask/add',
    method: 'post',
    data
  })
}

/**
 * 修改实验任务
 */
export function updateExperimentTask(data) {
  return request({
    url: '/experimentTask/update',
    method: 'put',
    data
  })
}

/**
 * 删除实验任务
 */
export function deleteExperimentTask(id) {
  return request({
    url: `/experimentTask/delete/${id}`,
    method: 'delete'
  })
}
