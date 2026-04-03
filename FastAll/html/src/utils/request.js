import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 兼容：线上环境 VITE_APP_BASE_API='/api' 时，部分接口 url 写成了 '/api/xxx'
    // axios 会拼成 '/api/api/xxx' 导致 404，这里做一次前缀去重
    const baseApi = import.meta.env.VITE_APP_BASE_API
    if (baseApi === '/api' && typeof config.url === 'string' && config.url.startsWith('/api/')) {
      config.url = config.url.replace(/^\/api\/?/, '/')
    }

    const token = localStorage.getItem('token')
    const tokenName = localStorage.getItem('tokenName') || 'satoken'
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
      config.headers[tokenName] = token
    }
    
    if (userInfo.username) {
      config.headers['X-User-Name'] = userInfo.username
    }
    
    // 开发环境打印日志
    if (import.meta.env.DEV) {
      console.log(`[Request] ${config.method.toUpperCase()} ${config.url}`, {
        headers: config.headers,
        params: config.params,
        data: config.data
      })
    }
    
    return config
  },
  error => {
    console.error('[Request Error]', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    const configUrl = response.config.url
    
    // 开发环境打印响应
    if (import.meta.env.DEV) {
      console.log(`[Response] ${configUrl}:`, res)
    }
    
    // 1. 优先处理 AJ-Captcha 验证码接口（特殊结构）
    if (res.repCode !== undefined) {
      if (res.repCode === '0000') {
        return res
      }
      const errorMsg = res.repMsg || '验证码服务异常'
      ElMessage.error(errorMsg)
      return Promise.reject(new Error(errorMsg))
    }
    
    // 2. 处理业务状态码（统一成功条件：code 为 200 或 0）
    const code = res.code
    const isSuccess = code === 200 || code === 0
    
    // 如果没有 code 字段且没有 success 标志，记录警告但仍尝试返回
    if (code === undefined && res.success === undefined) {
      if (import.meta.env.DEV) {
        console.warn(`[Response Warning] ${configUrl} 缺少 code 和 success 字段`, res)
      }
      return res
    }
    
    // 3. 判断是否成功
    if (!isSuccess) {
      // 401 特殊处理（Token 失效）
      if (code === 401) {
        ElMessage.warning(res.msg || '登录已过期，请重新登录')
        setTimeout(() => {
          window.location.href = '/login'
        }, 1000)
        const error = new Error(res.msg || 'Unauthorized')
        error.handled = true
        return Promise.reject(error)
      }
      
      // 其他错误码
      const errorMsg = res.msg || res.message || '服务器错误'
      ElMessage.error(errorMsg)
      const error = new Error(errorMsg)
      error.handled = true
      error.code = code
      return Promise.reject(error)
    }
    
    // 4. 处理 success 标志（兼容部分接口）
    if (res.success === false) {
      const errorMsg = res.msg || res.repMsg || '内部错误'
      ElMessage.error(errorMsg)
      const error = new Error(errorMsg)
      error.handled = true
      return Promise.reject(error)
    }
    
    // 5. 成功返回数据
    return res
  },
  error => {
    // HTTP 错误处理
    console.error('[HTTP Error]', error)
    
    // 处理网络错误
    if (!error.response) {
      ElMessage.error('网络连接失败，请检查网络')
      error.handled = true
      return Promise.reject(error)
    }
    
    const { status, data } = error.response
    
    // 根据 HTTP 状态码处理
    switch (status) {
      case 401:
        ElMessage.warning('会话已超时，请重新登录')
        if (!window.location.pathname.includes('/login')) {
          setTimeout(() => {
            window.location.href = '/login'
          }, 1000)
        }
        error.handled = true
        break
      case 403:
        ElMessage.error(data?.msg || '没有权限访问')
        error.handled = true
        break
      case 404:
        ElMessage.error(data?.msg || '请求的资源不存在')
        error.handled = true
        break
      case 500:
        ElMessage.error(data?.msg || '服务器内部错误')
        error.handled = true
        break
      default:
        ElMessage.error(data?.msg || error.message || '请求失败')
        error.handled = true
    }
    
    return Promise.reject(error)
  }
)

export default service