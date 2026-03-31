<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-wrapper">
          <img src="/logo.jpg" alt="Logo" class="logo">
        </div>
        <h2>FastCRUD</h2>
        <p class="subtitle">快速 · 简洁 · 高效 · 企业级</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        label-position="top"
        @keyup.enter="handlePreLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名 / 手机号 / 邮箱"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>

        <div class="form-options">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-link type="primary" :underline="false">忘记密码？</el-link>
        </div>

        <el-button
          type="primary"
          class="submit-btn"
          size="large"
          :loading="loading"
          @click="handlePreLogin"
        >
          登 录
        </el-button>

        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
    
    <!-- 滑块验证码弹窗 -->
    <el-dialog
      v-model="captchaVisible"
      title="安全验证"
      width="340px"
      append-to-body
      destroy-on-close
      class="captcha-dialog"
    >
      <div class="captcha-dialog-content">
        <SliderCaptcha @success="onCaptchaSuccess" />
      </div>
    </el-dialog>

    <div class="login-footer">
      <p>© 2026 FastCRUD | 企业级快速开发平台</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SliderCaptcha from '@/components/common/SliderCaptcha.vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'
import { aesEncrypt } from '@/utils/aes'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)
const captchaVisible = ref(false)
const captchaVerification = ref('')

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 校验规则
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/**
 * 预登录：先校验表单，通后显示验证码
 */
const handlePreLogin = () => {
  loginFormRef.value.validate((valid) => {
    if (valid) {
      captchaVisible.value = true
    }
  })
}

/**
 * 验证码通过后的回调
 */
const onCaptchaSuccess = (verification) => {
  console.log('Login received verification:', verification)
  // 延迟一秒关闭，让用户看到成功状态
  setTimeout(() => {
    captchaVisible.value = false
    doLogin(verification)
  }, 800)
}

/**
 * 执行最终登录请求
 */
const doLogin = async (verification) => {
  loading.value = true
  try {
    const payload = {
      username: loginForm.username,
      password: aesEncrypt(loginForm.password, 'ecmo_aes_key_123'),
      captchaVerification: verification
    }
    console.log('Sending Login Payload:', payload)
    
    const res = await login(payload)
    console.log('Login Response:', res)
    console.log('Response code:', res.code)
    console.log('Response data:', res.data)
    
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('登录成功')
      console.log('Token Value:', res.data.tokenValue)
      console.log('Token Name:', res.data.tokenName)
      userStore.setToken(res.data.tokenValue, res.data.tokenName)
      userStore.setUserInfo({ username: loginForm.username })
      console.log('Token saved, redirecting to /')
      router.push('/')
    } else {
      ElMessage.error(res.msg || '登录失败，请检查账号密码')
    }
  } catch (error) {
    console.error('Login Error:', error)
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: -20%;
  left: -10%;
  width: 50%;
  height: 50%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.15) 0%, rgba(99, 102, 241, 0) 70%);
  z-index: 0;
}

.login-container::after {
  content: '';
  position: absolute;
  bottom: -20%;
  right: -10%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0) 70%);
  z-index: 0;
}

.login-box {
  width: 440px;
  padding: 48px;
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  z-index: 1;
  animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-wrapper {
  background: rgba(255, 255, 255, 0.1);
  width: 80px;
  height: 80px;
  border-radius: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 20px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.login-header .logo {
  width: 50px;
  height: 50px;
}

.login-header h2 {
  font-size: 26px;
  font-weight: 800;
  margin: 10px 0;
  background: linear-gradient(135deg, #fff, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 0.5px;
}

.login-header .subtitle {
  font-size: 14px;
  color: #94a3b8;
  letter-spacing: 1px;
}

:deep(.login-form .el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-input__wrapper) {
  background-color: rgba(15, 23, 42, 0.6) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset !important;
  border-radius: 12px;
  padding: 8px 16px;
}

:deep(.el-input__inner) {
  color: #f8fafc;
  font-size: 15px;
  height: 32px;
}

:deep(.el-input__inner::placeholder) {
  color: #64748b;
}

:deep(.el-input__prefix-inner) {
  color: #818cf8;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #818cf8 inset !important;
  background-color: rgba(15, 23, 42, 0.8) !important;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

:deep(.el-checkbox__label) {
  color: #cbd5e1;
}

:deep(.el-checkbox__inner) {
  border-color: rgba(255, 255, 255, 0.2);
  background: rgba(15, 23, 42, 0.6);
}

.form-options .el-link {
  color: #94a3b8;
}

.form-options .el-link:hover {
  color: #f8fafc;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.6);
}

.register-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #94a3b8;
}

.register-link a {
  color: #818cf8;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: color 0.3s;
}

.register-link a:hover {
  color: #a5b4fc;
}

.login-footer {
  position: absolute;
  bottom: 30px;
  color: #64748b;
  font-size: 13px;
  letter-spacing: 1px;
}

.captcha-dialog-content {
  display: flex;
  justify-content: center;
  padding-bottom: 10px;
}

::v-deep(.captcha-dialog .el-dialog__body) {
  padding-top: 10px;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
