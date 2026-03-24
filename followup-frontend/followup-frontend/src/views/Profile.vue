<template>
  <div class="profile-container">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon size="28"><UserFilled /></el-icon>
        个人中心
      </h1>
      <p class="page-desc">管理您的个人账号信息</p>
    </div>

    <div class="content-grid">
      <el-card class="info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-button
              :type="isEdit ? 'success' : 'primary'"
              @click="handleEditInfo"
              :loading="isSubmitLoading"
            >
              {{ isEdit ? '保存修改' : '编辑信息' }}
            </el-button>
          </div>
        </template>

        <el-form
          :model="infoForm"
          ref="infoFormRef"
          :rules="infoRules"
          label-width="100px"
          class="info-form"
          :disabled="!isEdit"
        >
          <el-form-item label="账号" prop="username">
            <el-input v-model="infoForm.username" disabled></el-input>
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="infoForm.realName" placeholder="请输入真实姓名"></el-input>
          </el-form-item>
          <el-form-item label="角色" prop="userType">
            <el-input v-model="roleText" disabled></el-input>
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input
              v-model="infoForm.phone"
              placeholder="请输入11位手机号"
              maxlength="11"
            ></el-input>
          </el-form-item>
          <el-form-item label="身份证号" prop="idCard" v-if="userType === 1">
            <el-input
              v-model="infoForm.idCard"
              placeholder="请输入18位身份证号"
              maxlength="18"
            ></el-input>
          </el-form-item>
          <el-form-item label="家庭住址" prop="address" v-if="userType === 1">
            <el-input
              v-model="infoForm.address"
              type="textarea"
              :rows="2"
              placeholder="请输入家庭住址"
            ></el-input>
          </el-form-item>
          <el-form-item label="所属科室" prop="department" v-if="userType === 2">
            <el-input v-model="infoForm.department" placeholder="请输入所属科室"></el-input>
          </el-form-item>
          <el-form-item label="职称" prop="title" v-if="userType === 2">
            <el-input v-model="infoForm.title" placeholder="请输入职称"></el-input>
          </el-form-item>
          <el-form-item label="账号创建时间" prop="createTime">
            <el-input v-model="infoForm.createTime" disabled></el-input>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="password-card" shadow="hover">
        <template #header>
          <span>修改密码</span>
        </template>

        <el-form
          :model="passwordForm"
          ref="passwordFormRef"
          :rules="passwordRules"
          label-width="120px"
          class="password-form"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入原密码"
              show-password
            ></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入6-20位新密码"
              show-password
            ></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="handleChangePassword"
              :loading="isPasswordLoading"
            >
              确认修改密码
            </el-button>
            <el-button @click="handleResetPassword">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { UserFilled } from "@element-plus/icons-vue";
// 引入接口
import { getUserInfo, updateUserInfo, changePassword } from '../api/user.js'

const router = useRouter()
const isEdit = ref(false)
const isSubmitLoading = ref(false)
const isPasswordLoading = ref(false)
const infoFormRef = ref(null)
const passwordFormRef = ref(null)
const userInfo = ref({})
const userType = ref(0)

// 角色文本
const roleText = computed(() => {
  switch(userType.value) {
    case 1: return '患者';
    case 2: return '医生';
    case 3: return '管理员';
    default: return '未知角色';
  }
})

// 基本信息表单
const infoForm = reactive({
  username: '',
  realName: '',
  userType: 0,
  phone: '',
  idCard: '',
  address: '',
  department: '',
  title: '',
  createTime: ''
})
// 基本信息表单验证规则
const infoRules = reactive({
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  phone: [
    { required: true, message: "请输入联系电话", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的11位手机号", trigger: "blur" }
  ],
  idCard: [
    { required: true, message: "请输入身份证号", trigger: "blur" },
    { pattern: /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: "请输入正确的18位身份证号", trigger: "blur" }
  ],
  department: [{ required: true, message: "请输入所属科室", trigger: "blur" }],
  title: [{ required: true, message: "请输入职称", trigger: "blur" }]
})

// 密码修改表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
// 密码验证规则
const passwordRules = reactive({
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度为6-20位", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"))
        } else {
          callback()
        }
      },
      trigger: "blur"
    }
  ]
})

// 获取用户信息
const initUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
    userType.value = res.data.userType
    // 表单赋值
    Object.assign(infoForm, res.data)
    // 更新本地缓存
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    localStorage.setItem('userType', res.data.userType)
  } catch (err) {
    console.error('获取用户信息失败：', err)
  }
}

// 编辑/保存信息
const handleEditInfo = async () => {
  if (isEdit.value) {
    const valid = await infoFormRef.value.validate()
    if (!valid) return

    isSubmitLoading.value = true
    try {
      await updateUserInfo(infoForm)
      ElMessage.success('信息修改成功')
      // 重新获取用户信息
      await initUserInfo()
      isEdit.value = false
    } finally {
      isSubmitLoading.value = false
    }
  } else {
    isEdit.value = true
  }
}

// 修改密码
const handleChangePassword = async () => {
  const valid = await passwordFormRef.value.validate()
  if (!valid) return

  isPasswordLoading.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    handleResetPassword()
    // 清除登录态，跳回登录页
    localStorage.clear()
    router.push('/')
  } finally {
    isPasswordLoading.value = false
  }
}

// 重置密码表单
const handleResetPassword = () => {
  passwordFormRef.value?.resetFields()
}

// 页面挂载
onMounted(() => {
  if (!localStorage.getItem('token')) {
    router.push('/')
    return
  }
  initUserInfo()
})
</script>

<style scoped>
.profile-container {
  width: 100%;
  min-height: 100vh;
  background: #f7fafc;
  padding: 24px;
  box-sizing: border-box;
  overflow-x: hidden;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  color: #2d3748;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-desc {
  color: #718096;
  font-size: 16px;
  margin: 0;
}

.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  width: 100%;
}

.info-card, .password-card {
  border-radius: 16px !important;
  border: 1px solid #e2e8f0 !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #2d3748;
}

.info-form, .password-form {
  padding: 10px 0;
}

:deep(.info-form .el-input__wrapper),
:deep(.password-form .el-input__wrapper),
:deep(.info-form .el-textarea__inner) {
  border-radius: 12px !important;
}

@media (max-width: 1200px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>