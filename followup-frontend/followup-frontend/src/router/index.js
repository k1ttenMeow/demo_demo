import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 页面导入（保持你原来的路径）
import Login from '../views/Login.vue'
import PatientDashboard from '../views/patient/PatientDashboard.vue'
import DoctorDashboard from '../views/doctor/DoctorDashboard.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import FollowupAppoint from '../views/followup/FollowupAppoint.vue'
import FollowupPlan from '../views/followup/FollowupPlan.vue'
import FollowupRecord from '../views/followup/FollowupRecord.vue'
import PatientManage from '../views/patient/PatientManage.vue'
import DoctorManage from '../views/doctor/DoctorManage.vue'


const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login, meta: { requiresAuth: false } },
  { path: '/patient/dashboard', name: 'PatientDashboard', component: PatientDashboard, meta: { requiresAuth: true, role: 3 } },
  { path: '/doctor/dashboard', name: 'DoctorDashboard', component: DoctorDashboard, meta: { requiresAuth: true, role: 2 } },
  { path: '/admin/dashboard', name: 'AdminDashboard', component: AdminDashboard, meta: { requiresAuth: true, role: 1 } },
  {
    path: '/dashboard',
    redirect: () => {
      const userTypeStr = localStorage.getItem('userType') || '0'
      const userType = Number(userTypeStr) || 0
      switch (userType) {
        case 1: return '/admin/dashboard'
        case 2: return '/doctor/dashboard'
        case 3: return '/patient/dashboard'
        default: return '/login'
      }
    }
  },
    // 新增随访管理路由
  { 
    path: '/followup/appoint', 
    name: 'FollowupAppoint', 
    component: FollowupAppoint, 
    meta: { requiresAuth: true, role: 1 } 
  },
  { 
    path: '/followup/plan', 
    name: 'FollowupPlan', 
    component: FollowupPlan, 
    meta: { requiresAuth: true, role: 1 } 
  },
  { 
    path: '/followup/record', 
    name: 'FollowupRecord', 
    component: FollowupRecord, 
    meta: { requiresAuth: true, role: 1 } 
  },
  
  // 新增患者和医生管理路由
  { 
    path: '/patient/manage', 
    name: 'PatientManage', 
    component: PatientManage, 
    meta: { requiresAuth: true, role: 1 } 
  },
  { 
    path: '/doctor/manage', 
    name: 'DoctorManage', 
    component: DoctorManage, 
    meta: { requiresAuth: true, role: 1 } 
  },
  
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ✅ 修复后的路由守卫（解决无限重定向 + NaN 问题）
router.beforeEach((to, from, next) => {
  // ============= 修复：换成 sessionStorage =============
  const token = sessionStorage.getItem('followup_token')
  const hasToken = !!token
  const userTypeStr = sessionStorage.getItem('userType') || '0'
  let userType = Number(userTypeStr)
  
  if (isNaN(userType)) userType = 0

  if (hasToken && userType === 0) {
    sessionStorage.removeItem('followup_token')
    sessionStorage.removeItem('userType')
    ElMessage.warning('用户信息异常，请重新登录')
    next('/login')
    return
  }

  if (to.path === '/login' && hasToken) {
    switch (userType) {
      case 1: next('/admin/dashboard'); break
      case 2: next('/doctor/dashboard'); break
      case 3: next('/patient/dashboard'); break
      default: next('/login'); break
    }
    return
  }

  if (to.meta.requiresAuth && !hasToken) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  if (to.meta.requiresAuth && hasToken) {
    if (to.meta.role && to.meta.role !== userType) {
      ElMessage.error('无权限访问该页面')
      switch (userType) {
        case 1: next('/admin/dashboard'); break
        case 2: next('/doctor/dashboard'); break
        case 3: next('/patient/dashboard'); break
        default: next('/login'); break
      }
      return
    }
  }

  next()
})

export default router