<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <div class="logo-wrapper">
          <el-icon class="register-icon"><UserFilled /></el-icon>
        </div>
        <h2>创建新账户</h2>
        <p class="subtitle">加入 FastCRUD 企业级开发平台</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        label-position="top"
        @keyup.enter="handlePreRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="建议使用中英文组合" prefix-icon="User" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入" prefix-icon="Lock" show-password />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入 11 位手机号" prefix-icon="Phone" />
        </el-form-item>

        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="example@biomed.com" prefix-icon="Message" />
        </el-form-item>
        
        <el-form-item label="邀请码（可选）" prop="inviteCode">
          <el-input v-model="registerForm.inviteCode" placeholder="如有邀请码请填写" prefix-icon="Ticket" />
        </el-form-item>

        <el-button
          type="primary"
          class="submit-btn"
          size="large"
          :loading="loading"
          @click="handlePreRegister"
        >
          注 册
        </el-button>

        <div class="login-link">
          已有账号？<router-link to="/login">返回登录</router-link>
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
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SliderCaptcha from '@/components/common/SliderCaptcha.vue'
import { register } from '@/api/auth'
import { aesEncrypt } from '@/utils/aes'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)
const captchaVisible = ref(false)
const captchaVerification = ref('')

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  inviteCode: ''
})

// 自定义密码校验
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 手机号正则
const validatePhone = (rule, value, callback) => {
  const reg = /^1[3-9]\d{9}$/
  if (value && !reg.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { validator: validatePhone, trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const handlePreRegister = () => {
  registerFormRef.value.validate((valid) => {
    if (valid) {
      captchaVisible.value = true
    }
  })
}

const onCaptchaSuccess = (verification) => {
  console.log('Register received verification:', verification)
  setTimeout(() => {
    captchaVisible.value = false
    doRegister(verification)
  }, 800)
}

const doRegister = async (verification) => {
  loading.value = true
  try {
    const payload = {
      username: registerForm.username,
      password: registerForm.password,
      realName: '新用户',
      phone: registerForm.phone,
      email: registerForm.email,
      userType: 1,
      captchaVerification: verification
    }
    console.log('Sending Register Payload:', payload)
    
    const res = await register(payload)
    
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.msg || '注册失败，请稍后重试')
    }
  } catch (error) {
    console.error('Register Error:', error)
    ElMessage.error('注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%);
  position: relative;
  padding: 40px 0;
  overflow: hidden;
}

.register-container::before {
  content: '';
  position: absolute;
  top: -10%;
  left: -20%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, rgba(79, 70, 229, 0.15) 0%, rgba(79, 70, 229, 0) 70%);
  z-index: 0;
}

.register-container::after {
  content: '';
  position: absolute;
  bottom: -10%;
  right: -20%;
  width: 70%;
  height: 70%;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0) 70%);
  z-index: 0;
}

.register-box {
  width: 540px;
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

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-wrapper {
  background: rgba(255, 255, 255, 0.1);
  width: 72px;
  height: 72px;
  border-radius: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 20px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.register-icon {
  font-size: 36px;
  color: #818cf8;
}

.register-header h2 {
  font-size: 26px;
  font-weight: 800;
  margin: 10px 0;
  background: linear-gradient(135deg, #fff, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 0.5px;
}

.register-header .subtitle {
  font-size: 14px;
  color: #94a3b8;
  letter-spacing: 1px;
}

:deep(.register-form .el-form-item) {
  margin-bottom: 24px;
}

:deep(.register-form .el-form-item__label) {
  color: #e2e8f0;
  font-weight: 500;
  padding-bottom: 8px;
  font-size: 14px;
}

:deep(.el-input__wrapper) {
  background-color: rgba(15, 23, 42, 0.6) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset !important;
  border-radius: 12px;
  padding: 8px 16px;
}

:deep(.el-input__inner) {
  color: #f8fafc;
  font-size: 14px;
  height: 30px;
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

.submit-btn {
  width: 100%;
  height: 48px;
  margin-top: 16px;
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

.login-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #94a3b8;
}

.login-link a {
  color: #818cf8;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: color 0.3s;
}

.login-link a:hover {
  color: #a5b4fc;
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
