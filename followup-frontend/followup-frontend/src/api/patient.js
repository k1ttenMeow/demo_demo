import request from '../utils/request.js'

// 分页获取患者列表
export const getPatientList = (params) => {
  return request.get('/patient/list', { params })
}

// 新增患者
export const addPatient = (data) => {
  return request.post('/patient/add', data)
}

// 编辑患者
export const updatePatient = (data) => {
  return request.put('/patient/update', data)
}

// 删除患者
export const deletePatient = (id) => {
  return request.delete(`/patient/delete/${id}`)
}

// 导出患者列表
export const exportPatientList = (params) => {
  return request.get('/patient/export', { params, responseType: 'blob' })
}