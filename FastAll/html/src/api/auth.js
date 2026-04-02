import request from '@/utils/request'

/**
 * 获取验证码
 */
export function getCaptcha(data = { captchaType: 'blockPuzzle' }) {
  return request({
    url: '/captcha/get',
    method: 'post',
    data
  })
}

/**
 * 校验验证码 (Check)
 */
export function checkCaptcha(data) {
  return request({
    url: '/captcha/check',
    method: 'post',
    data
  })
}

/**
 * 登录 (注入验证凭证)
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 注册 (注入验证凭证)
 */
export function register(data) {
  return request({
    url: '/auth/regit',
    method: 'post',
    data
  })
}
/**
 * 退出登录
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
