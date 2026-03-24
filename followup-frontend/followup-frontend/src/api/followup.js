import request from '../utils/request.js'

// 分页获取随访记录列表
export const getFollowupList = (params) => {
  return request.get('/followup/list', { params })
}

// 新增随访记录
export const addFollowup = (data) => {
  return request.post('/followup/add', data)
}

// 删除随访记录
export const deleteFollowup = (id) => {
  return request.delete(`/followup/delete/${id}`)
}

// 获取随访记录详情
export const getFollowupDetail = (id) => {
  return request.get(`/followup/detail/${id}`)
}