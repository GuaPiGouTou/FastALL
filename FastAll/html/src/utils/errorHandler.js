import { ElMessage, ElNotification } from 'element-plus'

export const handleError = (error, customMessage = null) => {
  console.error('Error:', error)
  
  let message = customMessage || '操作失败'
  
  if (error.response) {
    const { status, data } = error.response
    
    switch (status) {
      case 400:
        message = data.msg || '请求参数错误'
        break
      case 401:
        message = '登录已过期，请重新登录'
        break
      case 403:
        message = '没有权限执行此操作'
        break
      case 404:
        message = '请求的资源不存在'
        break
      case 500:
        message = data.msg || '服务器内部错误'
        break
      default:
        message = data.msg || `请求失败 (${status})`
    }
  } else if (error.message) {
    if (error.message.includes('timeout')) {
      message = '请求超时，请检查网络连接'
    } else if (error.message.includes('Network Error')) {
      message = '网络错误，请检查网络连接'
    } else {
      message = error.message
    }
  }
  
  ElMessage.error(message)
  return message
}

export const handleSuccess = (message = '操作成功') => {
  ElMessage.success(message)
}

export const handleWarning = (message) => {
  ElMessage.warning(message)
}

export const handleInfo = (message) => {
  ElMessage.info(message)
}

export const showNotification = (title, message, type = 'info') => {
  ElNotification({
    title,
    message,
    type,
    duration: 3000
  })
}

export const handleApiError = (error, operation = '操作') => {
  const errorMessage = error.response?.data?.msg || error.message || '未知错误'
  console.error(`${operation}失败:`, error)
  ElMessage.error(`${operation}失败: ${errorMessage}`)
  return errorMessage
}

export const handleApiSuccess = (res, operation = '操作') => {
  if (res.code === 200 || res.code === 0) {
    ElMessage.success(`${operation}成功`)
    return true
  } else {
    ElMessage.error(res.msg || `${operation}失败`)
    return false
  }
}

export const validateForm = (formData, rules) => {
  const errors = []
  
  for (const [field, rule] of Object.entries(rules)) {
    const value = formData[field]
    
    if (rule.required && (!value || value.trim() === '')) {
      errors.push(`${rule.label || field}不能为空`)
    }
    
    if (rule.minLength && value && value.length < rule.minLength) {
      errors.push(`${rule.label || field}长度不能少于${rule.minLength}个字符`)
    }
    
    if (rule.maxLength && value && value.length > rule.maxLength) {
      errors.push(`${rule.label || field}长度不能超过${rule.maxLength}个字符`)
    }
    
    if (rule.pattern && value && !rule.pattern.test(value)) {
      errors.push(`${rule.label || field}格式不正确`)
    }
  }
  
  if (errors.length > 0) {
    ElMessage.error(errors[0])
    return false
  }
  
  return true
}

export const confirmOperation = async (message, title = '确认操作') => {
  try {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    return true
  } catch {
    return false
  }
}

export const retryOperation = async (operation, maxRetries = 3, delay = 1000) => {
  let lastError
  
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await operation()
    } catch (error) {
      lastError = error
      if (i < maxRetries - 1) {
        await new Promise(resolve => setTimeout(resolve, delay))
      }
    }
  }
  
  throw lastError
}
