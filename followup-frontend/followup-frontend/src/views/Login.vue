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
              prefix-icon="user"
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
              prefix-icon="lock"
              class="login-input"
              autocomplete="off"
              show-password
          />
        </el-form-item>
        <el-form-item prop="userType" class="role-form-item">
          <label class="form-label">登录角色</label>
          <el-radio-group
              v-model="loginForm.userType"
              size="large"
              direction="vertical"
          >
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
              prefix-icon="key"
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
              :disabled="isLoading"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { userLogin } from "../api/user.js";
import request from "@/utils/request";

const router = useRouter();
const loginRef = ref(null);
const isLoading = ref(false);
const verifyCode = ref("");

const loginForm = reactive({
  username: "",
  password: "",
  userType: 3,
  code: "",
});

const loginRules = reactive({
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { pattern: /^\S+$/, message: "用户名不能包含空格", trigger: "blur" },
    { min: 3, max: 20, message: "用户名长度为 3-20 位", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { pattern: /^\S+$/, message: "密码不能包含空格", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度为 6-20 位", trigger: "blur" },
  ],
  userType: [{ required: true, message: "请选择登录角色", trigger: "change" }],
  code: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { len: 4, message: "验证码为 4 位数字", trigger: "blur" },
    { pattern: /^\d{4}$/, message: "验证码只能是 4 位数字", trigger: "blur" },
  ],
});

const generateCode = () => {
  let code = "";
  for (let i = 0; i < 4; i++) {
    code += Math.floor(Math.random() * 10);
  }
  verifyCode.value = code;
};

const refreshCode = () => {
  generateCode();
};

const handleLogin = async () => {
  try {
    const valid = await loginRef.value.validate()
    if (!valid) return

    if (loginForm.code.trim() !== verifyCode.value) {
      ElMessage.error('验证码输入错误')
      refreshCode()
      loginForm.code = ''
      return
    }

    isLoading.value = true
    console.log('=== 开始登录 ===')
    console.log('登录参数:', {
      username: loginForm.username.trim(),
      userType: loginForm.userType,
      code: loginForm.code.trim()
    })

    const res = await userLogin({
      username: loginForm.username.trim(),
      password: loginForm.password.trim(),
      userType: loginForm.userType,
      code: loginForm.code.trim()
    })

    console.log('登录接口返回:', res)

    if (res.code !== 200) {
      ElMessage.error(res?.message || '登录失败')
      refreshCode()
      loginForm.code = ''
      isLoading.value = false
      return
    }

    // ✅ 新的返回结构：res.data 包含 token 和用户信息
    if (!res.data || !res.data.token) {
      ElMessage.error('登录成功但未获取到令牌')
      refreshCode()
      loginForm.code = ''
      isLoading.value = false
      return
    }

    // ============= 存储 token 和角色 =============
    sessionStorage.setItem('followup_token', res.data.token)
    const role = loginForm.userType
    sessionStorage.setItem('userType', role)

    // ============= 构建并存储用户信息 =============
    // ✅ 直接使用后端返回的用户信息
    const userInfo = {
      id: res.data.id,
      userId: res.data.id,
      patientId: res.data.id,  // 患者使用 user.id 作为 patientId
      username: res.data.username,
      realName: res.data.realName || '',
      phone: res.data.phone || '',
      userType: role,
      role: role
    }

    console.log('✅ 用户信息已从后端获取:', userInfo)

    // 存储用户信息到 sessionStorage
    sessionStorage.setItem('userInfo', JSON.stringify(userInfo))
    console.log('✅ 用户信息已保存到 sessionStorage')

    ElMessage.success('登录成功')

    setTimeout(() => {
      let targetPath = '/login'
      if (role === 1) {
        targetPath = '/admin/dashboard'
      } else if (role === 2) {
        targetPath = '/doctor/dashboard'
      } else if (role === 3) {
        targetPath = '/patient/dashboard'
      }
      router.push(targetPath).catch(() => {
        window.location.href = targetPath
      })
    }, 300)

  } catch (err) {
    console.error('登录异常：', err)
    ElMessage.error('登录失败，请检查账号密码')
    refreshCode()
    loginForm.code = ''
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  generateCode()
  // 检查是否已登录
  const token = sessionStorage.getItem('followup_token')
  const userTypeStr = sessionStorage.getItem('userType') || '0'
  const userType = Number(userTypeStr)

  if (token && !isNaN(userType) && [1,2,3].includes(userType)) {
    let targetPath = '/login'
    if (userType === 1) {
      targetPath = '/admin/dashboard'
    } else if (userType === 2) {
      targetPath = '/doctor/dashboard'
    } else if (userType === 3) {
      targetPath = '/patient/dashboard'
    }
    router.push(targetPath).catch(err => {
      console.error('已登录跳转失败：', err)
      window.location.href = targetPath
    })
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
