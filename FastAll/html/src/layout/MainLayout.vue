<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="aside-container">
      <div class="logo-box">
        <img src="/logo.jpg" alt="Logo">
        <span v-show="!isCollapse">FastCRUD</span>
      </div>
      <el-menu
        active-text-color="#ffd04b"
        background-color="#001529"
        class="el-menu-vertical"
        :default-active="$route.path"
        text-color="#fff"
        :collapse="isCollapse"
        router
      >
        <el-menu-item index="/data-center">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据中心</span>
        </el-menu-item>
        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Management /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/file/manager">文件管理</el-menu-item>
          <el-menu-item index="/file/log">归档审计日志</el-menu-item>
          <el-menu-item index="/system/audit">操作审计日志</el-menu-item>
          <el-menu-item index="/system/login-log">登录日志</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/crud" v-if="modules.length > 0">
          <template #title>
            <el-icon><Management /></el-icon>
            <span>CRUD管理</span>
          </template>
          <el-menu-item 
            v-for="module in modules" 
            :key="module.tableName" 
            :index="`/crud/${module.tableName}`"
          >
            {{ module.moduleName }}
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/tool">
          <template #title>
            <el-icon><Connection /></el-icon>
            <span>接口工具</span>
          </template>
          <el-menu-item index="/tool/api-manager">API管理</el-menu-item>
          <el-menu-item index="/tool/api-generator">API生成器</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header-container">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-input
            v-model="searchQuery"
            placeholder="全局搜索..."
            :prefix-icon="Search"
            style="width: 200px; margin-right: 20px"
          />
          <el-badge :value="5" class="nav-item">
            <el-icon :size="20"><Bell /></el-icon>
          </el-badge>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info nav-item">
              <el-avatar :size="32" src="/logo.jpg" />
              <span class="username">{{ userStore.userInfo.username || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人设置</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  Management, Expand, Fold, Bell, ArrowDown, Search, DataAnalysis, Connection
} from '@element-plus/icons-vue'
import { getModuleList } from '@/api/crud'

const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const searchQuery = ref('')
const modules = ref([])

const loadModules = async () => {
  try {
    const res = await getModuleList()
    if (res.code === 200 || res.code === 0) {
      modules.value = res.data || []
    }
  } catch (error) {
    console.error('加载模块失败:', error)
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  } else {
    ElMessage.info(`执行操作: ${command}`)
  }
}

onMounted(() => {
  loadModules()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside-container {
  background: linear-gradient(180deg, #0f172a 0%, #1e1b4b 100%);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow-x: hidden;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  z-index: 100;
}

.logo-box {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  background-color: transparent;
  color: #fff;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.logo-box img {
  width: 32px;
  height: 32px;
}

.logo-box span {
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
  letter-spacing: 0.5px;
  background: linear-gradient(90deg, #fff, #a5b4fc);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.el-menu-vertical {
  border-right: none;
  background-color: transparent !important;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  color: #94a3b8 !important;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.05) !important;
  color: #fff !important;
  transform: translateX(4px);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.15), rgba(99, 102, 241, 0.05)) !important;
  color: #818cf8 !important;
  font-weight: 600;
  border-left: 3px solid #818cf8;
}

.header-container {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  z-index: 90;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.3s ease;
  padding: 8px;
  border-radius: 8px;
}

.collapse-btn:hover {
  color: #818cf8;
  background-color: #f1f5f9;
  transform: scale(1.05);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.nav-item {
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.nav-item:hover {
  background-color: #f1f5f9;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.main-content {
  background-color: #f8fafc;
  padding: 24px;
  height: calc(100vh - 64px);
  overflow-y: auto;
}

:deep(.el-breadcrumb__inner) {
  color: #64748b;
  font-weight: 500;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #0f172a;
  font-weight: 600;
}
</style>
