import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录 - FastCRUD' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册 - FastCRUD' }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/data-center'
      },
      {
        path: '/data-center',
        name: 'DataCenter',
        component: () => import('@/views/data-center/DataCenter.vue'),
        meta: { title: '数据中心', requiresAuth: true }
      },
      {
        path: '/dashboard/workbench',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Workbench.vue'),
        meta: { title: '系统总览', requiresAuth: true }
      },
      {
        path: '/file/manager',
        name: 'FileManager',
        component: () => import('@/views/file/FileManager.vue'),
        meta: { title: '统一文件平台', requiresAuth: true }
      },
      {
        path: '/file/log',
        name: 'FileLog',
        component: () => import('@/views/file/FileLog.vue'),
        meta: { title: '归档审计日志', requiresAuth: true }
      },
      {
        path: '/system/audit',
        name: 'AuditLog',
        component: () => import('@/views/system/AuditLog.vue'),
        meta: { title: '操作审计日志', requiresAuth: true }
      },
      {
        path: '/system/login-log',
        name: 'LoginLog',
        component: () => import('@/views/system/LoginLog.vue'),
        meta: { title: '系统安全与拦截日志', requiresAuth: true }
      },
      // CRUD 管理相关路由
      {
        path: '/crud',
        name: 'CrudManager',
        component: () => import('@/views/crud/CrudManager.vue'),
        meta: { title: 'CRUD管理', requiresAuth: true }
      },
      {
        path: '/crud/:tableName',
        name: 'CrudTable',
        component: () => import('@/views/crud/CrudTable.vue'),
        meta: { title: 'CRUD表管理', requiresAuth: true }
      },
      // RBAC 权限管理相关路由
      {
        path: '/system/roles',
        name: 'RoleManager',
        component: () => import('@/views/system/RoleManager.vue'),
        meta: { title: '角色管理', requiresAuth: true }
      },
      {
        path: '/system/permissions',
        name: 'PermissionManager',
        component: () => import('@/views/system/PermissionManager.vue'),
        meta: { title: '权限管理', requiresAuth: true }
      },
      {
        path: '/system/modules',
        name: 'ModuleManager',
        component: () => import('@/views/system/ModuleManager.vue'),
        meta: { title: '模块管理', requiresAuth: true }
      },
      {
        path: '/tool/api-manager',
        name: 'ApiManager',
        component: () => import('@/views/api/ApiManager.vue'),
        meta: { title: 'API管理', requiresAuth: true }
      },
      {
        path: '/tool/api-generator',
        name: 'ApiGenerator',
        component: () => import('@/views/api/ApiGenerator.vue'),
        meta: { title: 'API生成器', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }

  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.name === 'Login' || to.name === 'Register') && token) {
    next('/')
  } else {
    next()
  }
})

export default router
