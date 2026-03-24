<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <div class="login-header">
        <h2 class="login-title">慢病随访管理系统</h2>
        <p class="login-desc">请输入账号密码完成登录</p>
      </div>
      <el-form
        :model="loginForm"
        ref="loginRef"
        :rules="loginRules"
        label-width="0px"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
            class="login-input"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            class="login-input"
            autocomplete="off"
            show-password
          />
        </el-form-item>
        <el-form-item prop="userType" class="role-form-item">
          <label class="form-label">登录角色</label>
          <el-radio-group v-model="loginForm.userType" size="large" direction="vertical">
            <el-radio :value="3" border>患者</el-radio>
            <el-radio :value="2" border>医生</el-radio>
            <el-radio :value="1" border>管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="code" class="code-form-item">
          <el-input
            v-model="loginForm.code"
            placeholder="请输入验证码"
            size="large"
            prefix-icon="Key"
            class="code-input"
            autocomplete="off"
            maxlength="4"
          />
          <div class="code-box" @click="refreshCode">
            {{ verifyCode }}
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            @click="handleLogin"
            :loading="isLoading"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
// 引入登录接口
import { userLogin } from '../api/user.js'

const router = useRouter()
const loginRef = ref(null)
const isLoading = ref(false)
const verifyCode = ref('')

// 登录表单：默认选中患者(3)
const loginForm = reactive({
  username: '',
  password: '',
  userType: 3,
  code: ''
})

// 表单验证规则
const loginRules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择登录角色', trigger: 'change' }],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位数字', trigger: 'blur' }
  ]
})

// 生成4位随机验证码
const generateCode = () => {
  let code = ''
  for (let i = 0; i < 4; i++) {
    code += Math.floor(Math.random() * 10)
  }
  verifyCode.value = code
}

// 刷新验证码
const refreshCode = () => {
  generateCode()
}

// 登录核心逻辑
const handleLogin = async () => {
  const valid = await loginRef.value.validate()
  if (!valid) return

  // 验证码校验
  if (loginForm.code.trim() !== verifyCode.value) {
    ElMessage.error('验证码输入错误')
    refreshCode()
    loginForm.code = ''
    return
  }

  isLoading.value = true
  try {
    console.log('开始调用登录接口，参数：', loginForm)
    // 调用登录接口
    const res = await userLogin({
      username: loginForm.username.trim(),
      password: loginForm.password.trim(),
      userType: loginForm.userType,
      code: loginForm.code.trim()
    })

    console.log('登录接口返回结果：', res)

    // 【关键1】登录成功，统一存储key为 followup_token，和路由守卫完全一致
    localStorage.setItem('followup_token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
    localStorage.setItem('userType', res.data.userInfo.userType)
    
    ElMessage.success('登录成功')
    
    // 【关键2】按角色跳转，和路由配置的路径完全一致
    const userType = res.data.userInfo.userType
    console.log('登录成功，准备跳转，用户角色：', userType)

    switch (userType) {
      case 1: // 管理员
        await router.push('/admin/dashboard')
        break
      case 2: // 医生
        await router.push('/doctor/dashboard')
        break
      case 3: // 患者
        await router.push('/patient/dashboard')
        break
      default: // 默认跳转
        await router.push('/patient/dashboard')
    }

    console.log('路由跳转执行完成')
  } catch (err) {
    console.error('登录失败：', err)
    ElMessage.error('登录失败，请检查账号密码或角色')
    refreshCode()
    loginForm.code = ''
  } finally {
    isLoading.value = false
  }
}

// 【关键3】修复onMounted自动跳转逻辑
onMounted(() => {
  generateCode()
  // 已登录则直接跳转到对应角色首页
  const token = localStorage.getItem('followup_token')
  const userType = localStorage.getItem('userType')
  if (token && userType) {
    console.log('已登录，自动跳转')
    switch (Number(userType)) {
      case 1:
        router.push('/admin/dashboard')
        break
      case 2:
        router.push('/doctor/dashboard')
        break
      case 3:
        router.push('/patient/dashboard')
        break
    }
  }
})
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #f0f4ff 0%, #e6efff 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;
  margin: 0;
}
.login-card {
  width: 100%;
  max-width: 460px;
  border-radius: 20px !important;
  padding: 45px 35px;
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(10px);
  border: 1px solid #e8eefb !important;
}
.login-header {
  text-align: center;
  margin-bottom: 35px;
}
.login-title {
  font-size: 28px;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 10px 0;
}
.login-desc {
  font-size: 14px;
  color: #718096;
  margin: 0;
}
.login-form {
  width: 100%;
}
.login-input {
  border-radius: 12px !important;
  margin-bottom: 10px;
}
.role-form-item {
  margin-bottom: 20px;
}
.form-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}
:deep(.el-radio-group) {
  width: 100%;
  gap: 10px;
}
:deep(.el-radio) {
  width: 100%;
  margin: 0;
}
:deep(.el-radio__label) {
  width: 100%;
  padding: 10px 0;
}
.code-form-item {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}
.code-input {
  flex: 1;
  border-radius: 12px !important;
}
.code-box {
  width: 120px;
  height: 40px;
  background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 4px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s ease;
}
.code-box:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(66, 153, 225, 0.4);
}
.login-btn {
  width: 100%;
  border-radius: 12px !important;
  padding: 12px 0;
  font-size: 16px;
  font-weight: 600;
  background: #4299e1 !important;
  border: none !important;
  transition: all 0.3s ease;
}
.login-btn:hover {
  background: #3182ce !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(66, 153, 225, 0.4);
}
</style>