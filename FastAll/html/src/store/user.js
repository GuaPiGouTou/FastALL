import { defineStore } from 'pinia'
import { ref } from 'vue'
import { logout as apiLogout } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const setToken = (newToken, tokenName) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
    if (tokenName) {
      localStorage.setItem('tokenName', tokenName)
    }
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  const logout = async () => {
    try {
      await apiLogout()
    } catch (e) {
      console.error('Logout failed:', e)
    } finally {
      token.value = ''
      userInfo.value = {}
      localStorage.removeItem('token')
      localStorage.removeItem('tokenName')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('dataCenterConfig')
    }
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    logout
  }
})
