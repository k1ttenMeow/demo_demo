import request from '../utils/request.js'

// 获取管理员仪表盘统计数据
export const getAdminDashboardData = () => {
  return request.get('/dashboard/admin')
}

// 获取医生仪表盘统计数据
export const getDoctorDashboardData = () => {
  return request.get('/dashboard/doctor')
}

// 获取患者仪表盘统计数据
export const getPatientDashboardData = () => {
  return request.get('/dashboard/patient')
}

// 获取近6个月随访完成趋势
export const getFollowTrendData = () => {
  return request.get('/dashboard/follow-trend')
}

// 获取患者慢病类型分布
export const getDiseaseDistribution = () => {
  return request.get('/dashboard/disease-distribution')
}

// 获取系统用户角色分布
export const getUserRoleDistribution = () => {
  return request.get('/dashboard/user-role-distribution')
}

// 获取月度新增用户统计
export const getMonthlyUserAddData = () => {
  return request.get('/dashboard/monthly-user-add')
}

// 获取患者个人健康指标趋势
export const getHealthTrendData = () => {
  return request.get('/dashboard/health-trend')
}

// 获取患者个人随访完成统计
export const getMyFollowupStats = () => {
  return request.get('/dashboard/my-followup-stats')
}