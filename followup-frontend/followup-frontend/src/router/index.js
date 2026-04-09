import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 页面导入
import Login from '../views/Login.vue'
import PatientDashboard from '../views/patient/PatientDashboard.vue'
import DoctorDashboard from '../views/doctor/DoctorDashboard.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import FollowupAppoint from '../views/followup/FollowupAppoint.vue'
import FollowupPlan from '../views/followup/FollowupPlan.vue'
import FollowupRecord from '../views/followup/FollowupRecord.vue'
import PatientManage from '../views/patient/PatientManage.vue'
import DoctorManage from '../views/doctor/DoctorManage.vue'
import MyDoctorAppointments from '../views/doctor/MyDoctorAppointments.vue'
import MyDoctorPlans from '../views/doctor/MyDoctorPlans.vue' 
import MyDoctorRecords from '../views/doctor/MyDoctorRecords.vue'
import MyPatients from '../views/doctor/MyPatients.vue' 
import MyPatientAppoints from '../views/patient/MyPatientAppoints.vue'
import MyPatientPlans from '../views/patient/MyPatientPlans.vue'
import MyPatientRecords from '../views/patient/MyPatientRecords.vue'

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
  // 随访管理路由
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
  
  // 患者和医生管理路由
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
  
  // 医生预约管理路由
  { 
    path: '/doctor/my-appointments', 
    name: 'MyDoctorAppointments', 
    component: MyDoctorAppointments, 
    meta: { requiresAuth: true, role: 2 } 
  },
  
  // 医生随访计划路由
  { 
    path: '/doctor/my-plans', 
    name: 'MyDoctorPlans', 
    component: MyDoctorPlans, 
    meta: { requiresAuth: true, role: 2 } 
  },

  // 医生随访记录路由
  { 
    path: '/doctor/my-records', 
    name: 'MyDoctorRecords', 
    component: MyDoctorRecords, 
    meta: { requiresAuth: true, role: 2 } 
  },

  // 我的患者路由
  { 
    path: '/doctor/my-patients', 
    name: 'MyPatients', 
    component: MyPatients, 
    meta: { requiresAuth: true, role: 2 } 
  },

  // ========== 患者端路由 ==========
  // 患者预约管理
  { 
    path: '/patient/my-patient-appoints', 
    name: 'MyPatientAppoints', 
    component: MyPatientAppoints, 
    meta: { requiresAuth: true, role: 3 } 
  },

  // 患者随访计划
  { 
    path: '/patient/my-patient-plans', 
    name: 'MyPatientPlans', 
    component: MyPatientPlans, 
    meta: { requiresAuth: true, role: 3 } 
  },

  // 患者随访记录
  { 
    path: '/patient/my-patient-records', 
    name: 'MyPatientRecords', 
    component: MyPatientRecords, 
    meta: { requiresAuth: true, role: 3 } 
  },

  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
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
