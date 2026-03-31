import request from '@/utils/request'

/**
 * 获取登录日志列表
 */
export function getLoginLogList(params) {
  return request({
    url: '/api/syslog/login/pagelist',
    method: 'get',
    params
  })
}

/**
 * 获取操作审计日志列表
 */
export function getOperLogList(params) {
  return request({
    url: '/api/syslog/oper/pagelist',
    method: 'get',
    params
  })
}
