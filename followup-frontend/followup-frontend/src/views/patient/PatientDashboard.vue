<template>
  <div class="patient-dashboard">
    <!-- 未登录提示 -->
    <el-result v-if="!isLoggedIn" icon="warning" title="请先登录" sub-title="您尚未登录或登录已过期，请重新登录">
      <template #extra>
        <el-button type="primary" @click="goToLogin">去登录</el-button>
      </template>
    </el-result>

    <!-- 已登录显示正常内容 -->
    <template v-else>
      <div class="header">
        <h1>🧑 患者健康家园</h1>
        <p class="welcome">欢迎回来，{{ userInfo.realName || '患者' }}！</p>
      </div>

      <!-- 个人信息卡片 -->
      <el-card class="info-card" shadow="hover">
        <template #header>
          <span>📋 个人健康档案</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ userInfo.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ userInfo.age || '-' }}</el-descriptions-item>
          <el-descriptions-item label="慢病类型">{{ userInfo.chronicType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="责任医生">{{ userInfo.doctorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ userInfo.address || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 主要功能模块 -->
      <div class="main-grid">
        <el-card class="main-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📅 随访预约</span>
            </div>
          </template>
          <div class="main-content">
            <p class="description">在线预约随访时间，方便快捷</p>
            <el-button type="primary" size="large" @click="handleAppoint">
              <el-icon><Calendar /></el-icon>
              进行预约
            </el-button>
          </div>
        </el-card>

        <el-card class="main-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📝 随访计划</span>
            </div>
          </template>
          <div class="main-content">
            <p class="description">查看您的个性化随访计划</p>
            <el-button type="success" size="large" @click="handlePlan">
              <el-icon><Document /></el-icon>
              查看计划
            </el-button>
          </div>
        </el-card>

        <el-card class="main-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📊 随访记录</span>
            </div>
          </template>
          <div class="main-content">
            <p class="description">回顾历史随访记录和健康数据</p>
            <el-button type="warning" size="large" @click="handleRecord">
              <el-icon><Notebook /></el-icon>
              查看记录
            </el-button>
          </div>
        </el-card>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Document, Notebook } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const isLoggedIn = ref(false)

const userInfo = reactive({
  id: null,
  realName: '',
  age: 0,
  chronicType: '',
  doctorName: '',
  phone: '',
  address: ''
})

// 加载患者信息
const loadPatientInfo = async () => {
  try {
    console.log('=== 开始加载患者信息 ===')

    // ============= 修改：从 sessionStorage 获取（与 Login.vue 保持一致） =============
    const userStr = sessionStorage.getItem('userInfo')
    console.log('1. sessionStorage 中的 userInfo:', userStr)

    // 如果 sessionStorage 中没有，再尝试 localStorage
    if (!userStr || userStr === 'undefined' || userStr === 'null') {
      const localUserStr = localStorage.getItem('userInfo')
      console.log('2. localStorage 中的 userInfo:', localUserStr)

      if (localUserStr && localUserStr !== 'undefined' && localUserStr !== 'null') {
        const localUser = JSON.parse(localUserStr)
        console.log('3. 从 localStorage 获取到用户:', localUser)
        await processUserInfo(localUser)
        return
      }

      console.error('4. sessionStorage 和 localStorage 中都没有有效的 userInfo')
      ElMessage.warning('未找到用户信息，请重新登录')
      isLoggedIn.value = false

      // 3 秒后跳转到登录页
      setTimeout(() => {
        router.push('/login')
      }, 3000)
      return
    }

    const user = JSON.parse(userStr)
    console.log('5. 解析后的用户对象:', user)
    await processUserInfo(user)

  } catch (error) {
    console.error('加载患者信息失败', error)
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
    isLoggedIn.value = false
  }
}

// 处理用户信息
const processUserInfo = async (user) => {
  try {
    // 检查用户角色是否为患者（role=3 或 userType=3）
    const userRole = user.userType || user.role
    console.log('6. 用户角色:', userRole)

    if (userRole !== 3) {
      ElMessage.error('当前用户不是患者账户，当前角色：' + userRole)
      isLoggedIn.value = false
      return
    }

    isLoggedIn.value = true
    userInfo.id = user.id
    userInfo.realName = user.realName || ''
    userInfo.phone = user.phone || ''

    // 获取 patientId（可能在不同字段名中）
    const patientId = user.patientId || user.id
    console.log('7. 使用 patientId:', patientId)

    if (!patientId) {
      console.error('无法获取 patientId')
      ElMessage.warning('未找到患者档案信息')
      return
    }

    console.log('8. 调用接口：/patient/dashboard/' + patientId)

    // 查询患者详细信息
    const res = await request.get(`/patient/dashboard/${patientId}`)
    console.log('9. 后端返回的数据:', res)

    if (res.code === 200 && res.data) {
      const patient = res.data
      console.log('10. 患者详细信息:', patient)

      userInfo.realName = patient.realName || userInfo.realName
      userInfo.age = patient.age || 0
      userInfo.chronicType = patient.chronicType || ''
      userInfo.address = patient.address || ''
      userInfo.doctorName = patient.doctorName || '未分配'
      userInfo.phone = patient.phone || userInfo.phone

      console.log('11. 最终显示的 userInfo:', userInfo)
      ElMessage.success('健康档案加载成功')
    } else {
      console.error('获取患者信息失败:', res.msg || res.message)
      ElMessage.warning('获取健康档案失败：' + (res.msg || res.message))
    }
  } catch (error) {
    console.error('处理用户信息失败', error)
    ElMessage.error('处理失败：' + error.message)
    isLoggedIn.value = false
  }
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}

// 进行预约
const handleAppoint = () => {
  router.push('/followup/appoint')
}

// 查看计划
const handlePlan = () => {
  router.push('/followup/plan')
}

// 查看记录
const handleRecord = () => {
  router.push('/followup/record')
}

onMounted(() => {
  loadPatientInfo()
})
</script>

<style scoped>
.patient-dashboard {
  padding: 24px;
}

.header {
  margin-bottom: 24px;
}

.header h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #303133;
}

.welcome {
  margin: 0;
  font-size: 16px;
  color: #909399;
}

.info-card {
  margin-bottom: 24px;
}

.main-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.main-card {
  text-align: center;
  transition: transform 0.3s;
}

.main-card:hover {
  transform: translateY(-5px);
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.main-content {
  padding: 20px 0;
}

.description {
  margin: 0 0 24px 0;
  font-size: 14px;
  color: #606266;
}
</style>
