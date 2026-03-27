<template>
  <div class="doctor-dashboard">
    <div class="header">
      <h1>👨‍⚕️ 医生工作台</h1>
      <p class="welcome">欢迎回来，{{ doctorInfo.realName || '医生' }}！</p>
    </div>

    <!-- 个人信息卡片 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <span>📋 医生信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ doctorInfo.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ doctorInfo.department || '-' }}</el-descriptions-item>
        <el-descriptions-item label="技能">{{ doctorInfo.skill || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属社区">{{ doctorInfo.community || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ doctorInfo.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="doctorInfo.status === 1" type="success">正常</el-tag>
          <el-tag v-else-if="doctorInfo.status === 0" type="danger">禁用</el-tag>
          <el-tag v-else>未知</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">📅</div>
        <div class="stat-content">
          <h3>{{ stats.appointCount }}</h3>
          <p>随访预约</p>
          <el-button type="primary" size="small" @click="handleAppoint">处理预约</el-button>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">📝</div>
        <div class="stat-content">
          <h3>{{ stats.planCount }}</h3>
          <p>随访计划</p>
          <el-button type="success" size="small" @click="handlePlan">查看计划</el-button>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <h3>{{ stats.recordCount }}</h3>
          <p>随访记录</p>
          <el-button type="warning" size="small" @click="handleRecord">查看记录</el-button>
        </div>
      </el-card>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3>⚡ 快捷操作</h3>
      <div class="action-buttons">
        <el-button type="primary" @click="goToPatientManage">患者管理</el-button>
        <el-button type="success" @click="goToFollowupAppoint">随访预约</el-button>
        <el-button type="warning" @click="goToFollowupPlan">随访计划</el-button>
        <el-button type="info" @click="goToFollowupRecord">随访记录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()

const doctorInfo = reactive({
  id: null,
  realName: '',
  department: '',
  skill: '',
  community: '',
  phone: '',
  status: null
})

const stats = reactive({
  appointCount: 0,
  planCount: 0,
  recordCount: 0
})

// 加载医生信息
const loadDoctorInfo = async () => {
  try {
    console.log('=== 开始加载医生信息 ===')

    // 从 sessionStorage 获取用户信息
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
    console.error('加载医生信息失败', error)
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
  }
}

// 处理用户信息
const processUserInfo = async (user) => {
  try {
    // 检查用户角色是否为医生（role=2 或 userType=2）
    const userRole = user.userType || user.role
    console.log('6. 用户角色:', userRole)

    if (userRole !== 2) {
      ElMessage.error('当前用户不是医生账户，当前角色：' + userRole)
      return
    }

    // 获取 doctorId
    const doctorId = user.doctorId || user.id
    console.log('7. 使用 doctorId:', doctorId)

    if (!doctorId) {
      console.error('无法获取 doctorId')
      ElMessage.warning('未找到医生档案信息')
      return
    }

    console.log('8. 调用接口：/doctor/dashboard/' + doctorId)

    // 查询医生详细信息
    const res = await request.get(`/doctor/dashboard/${doctorId}`)
    console.log('9. 后端返回的数据:', res)

    if (res.code === 200 && res.data) {
      const doctor = res.data
      console.log('10. 医生详细信息:', doctor)

      doctorInfo.id = doctor.doctorId
      doctorInfo.realName = doctor.realName || ''
      doctorInfo.department = doctor.department || ''
      doctorInfo.skill = doctor.skill || ''
      doctorInfo.community = doctor.community || ''
      doctorInfo.phone = doctor.phone || ''
      doctorInfo.status = doctor.status

      console.log('11. 最终显示的 doctorInfo:', doctorInfo)
      ElMessage.success('医生工作台加载成功')

      // 加载统计数据
      await loadStats()
    } else {
      console.error('获取医生信息失败:', res.msg || res.message)
      ElMessage.warning('获取医生信息失败：' + (res.msg || res.message))
    }
  } catch (error) {
    console.error('处理用户信息失败', error)
    ElMessage.error('处理失败：' + error.message)
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    // TODO: 后续需要从后端接口获取真实的统计数据
    // 这里先使用模拟数据
    stats.appointCount = Math.floor(Math.random() * 20)
    stats.planCount = Math.floor(Math.random() * 50)
    stats.recordCount = Math.floor(Math.random() * 100)

    console.log('统计数据:', stats)
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 处理预约
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

// 跳转到患者管理
const goToPatientManage = () => {
  router.push('/patient/manage')
}

// 跳转到随访预约
const goToFollowupAppoint = () => {
  router.push('/followup/appoint')
}

// 跳转到随访计划
const goToFollowupPlan = () => {
  router.push('/followup/plan')
}

// 跳转到随访记录
const goToFollowupRecord = () => {
  router.push('/followup/record')
}

onMounted(() => {
  loadDoctorInfo()
})
</script>

<style scoped>
.doctor-dashboard {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  font-size: 48px;
  margin-right: 20px;
}

.stat-content {
  flex: 1;
}

.stat-content h3 {
  margin: 0 0 8px 0;
  font-size: 32px;
  color: #409EFF;
}

.stat-content p {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #909399;
}

.quick-actions {
  background: #f5f7fa;
  padding: 24px;
  border-radius: 8px;
}

.quick-actions h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
