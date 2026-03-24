import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 导入页面组件
import Login from '../views/Login.vue'
// 各角色首页（如果还没创建对应文件，先创建空的vue文件）
import PatientDashboard from '../views/patient/PatientDashboard.vue'
import DoctorDashboard from '../views/doctor/DoctorDashboard.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'

// 路由配置：补全所有跳转路径
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  // 登录页
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  // 患者首页
  {
    path: '/patient/dashboard',
    name: 'PatientDashboard',
    component: PatientDashboard,
    meta: { requiresAuth: true, role: 3 }
  },
  // 医生首页
  {
    path: '/doctor/dashboard',
    name: 'DoctorDashboard',
    component: DoctorDashboard,
    meta: { requiresAuth: true, role: 2 }
  },
  // 管理员首页
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresAuth: true, role: 1 }
  },
  // 通用首页（兜底）
  {
    path: '/dashboard',
    redirect: '/patient/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：核心修复，统一token key为 followup_token
router.beforeEach((to, from, next) => {
  // 【关键】和登录页存储的token key完全一致
  const hasToken = localStorage.getItem('followup_token')
  const userType = localStorage.getItem('userType')

  console.log('路由跳转：', to.path, '是否有token：', !!hasToken, '用户角色：', userType)

  // 规则1：访问登录页，已经登录了 → 直接跳对应角色首页
  if (to.path === '/login' && hasToken) {
    switch (Number(userType)) {
      case 1:
        next('/admin/dashboard')
        break
      case 2:
        next('/doctor/dashboard')
        break
      case 3:
        next('/patient/dashboard')
        break
      default:
        next('/login')
    }
    return
  }

  // 规则2：访问需要登录的页面，没登录 → 强制跳登录页
  if (to.meta.requiresAuth && !hasToken) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  // 规则3：已登录，校验角色权限
  if (to.meta.requiresAuth && hasToken) {
    if (to.meta.role && to.meta.role !== Number(userType)) {
      ElMessage.error('无权限访问该页面')
      // 跳回自己的角色首页
      switch (Number(userType)) {
        case 1:
          next('/admin/dashboard')
          break
        case 2:
          next('/doctor/dashboard')
          break
        case 3:
          next('/patient/dashboard')
          break
        default:
          next('/login')
      }
      return
    }
  }

  // 所有校验通过，正常跳转
  next()
})

export default router