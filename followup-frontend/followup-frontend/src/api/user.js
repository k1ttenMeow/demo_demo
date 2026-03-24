import request from '../utils/request.js'

// 登录接口
export const userLogin = (data) => {
  return request.post('/user/login', data)
}

// 获取当前登录用户信息
export const getUserInfo = () => {
  return request.get('/user/info')
}

// 更新用户个人信息
export const updateUserInfo = (data) => {
  return request.put('/user/update', data)
}

// 修改密码
export const changePassword = (data) => {
  return request.post('/user/change-password', data)
}