<template>
  <div class="doctor-dashboard">
    <div class="header">
      <h1>医生工作台</h1>
      <p class="welcome">欢迎回来，{{ userInfo.realName }}医生！</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <h3>{{ stats.patientCount }}</h3>
          <p>我的患者</p>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">⏰</div>
        <div class="stat-content">
          <h3>{{ stats.pendingCount }}</h3>
          <p>待办随访</p>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <h3>{{ stats.completedCount }}</h3>
          <p>本月完成</p>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">📋</div>
        <div class="stat-content">
          <h3>{{ stats.appointmentCount }}</h3>
          <p>待确认预约</p>
        </div>
      </el-card>
    </div>

    <!-- 个人信息 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>👨‍⚕️ 我的信息</span>
          <el-button type="primary" size="small" @click="handleUpdateInfo">更新信息</el-button>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ doctorInfo.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属科室">{{ doctorInfo.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职称">{{ doctorInfo.title || '主治医师' }}</el-descriptions-item>
        <el-descriptions-item label="管辖社区">{{ doctorInfo.manageCommunity || '-' }}</el-descriptions-item>
        <el-descriptions-item label="擅长病种" :span="2">{{ doctorInfo.specialty || '-' }}</el-descriptions-item>
        <el-descriptions-item label="可预约时段" :span="2">{{ doctorInfo.appointmentTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 待办随访任务 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>⏰ 待办随访任务</span>
          <el-button type="primary" size="small" @click="handleAddFollowup">新增随访</el-button>
        </div>
      </template>
      <el-table :data="pendingFollowups" stripe style="width: 100%">
        <el-table-column prop="patientName" label="患者姓名" width="120" />
        <el-table-column prop="planType" label="计划类型" width="120" />
        <el-table-column prop="nextFollowTime" label="计划随访时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.nextFollowTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleStartFollowup(row)">开始随访</el-button>
            <el-button size="small" @click="handleViewPatient(row)">查看患者</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 我的患者列表 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <span>👥 我的患者</span>
      </template>
      <el-table :data="myPatients" stripe style="width: 100%">
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="chronicDisease" label="慢性病种" />
        <el-table-column prop="address" label="居住地址" show-overflow-tooltip />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">查看详情</el-button>
            <el-button size="small" @click="handleAddRecord(row)">添加记录</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// 用户信息
const userInfo = reactive({
  realName: ''
})

// 医生信息
const doctorInfo = reactive({
  realName: '',
  deptName: '',
  title: '',
  specialty: '',
  manageCommunity: '',
  appointmentTime: ''
})

// 统计数据
const stats = reactive({
  patientCount: 0,
  pendingCount: 0,
  completedCount: 0,
  appointmentCount: 0
})

// 待办随访
const pendingFollowups = ref([])

// 我的患者
const myPatients = ref([])

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 获取状态类型
const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = { 0: '未完成', 1: '已完成', 2: '逾期' }
  return map[status] || '未知'
}

// 更新信息
const handleUpdateInfo = () => {
  ElMessage.info('跳转到更新信息页面')
}

// 新增随访
const handleAddFollowup = () => {
  ElMessage.info('跳转到新增随访页面')
}

// 开始随访
const handleStartFollowup = (row) => {
  ElMessage.info(`开始随访患者：${row.patientName}`)
}

// 查看患者
const handleViewPatient = (row) => {
  ElMessage.info(`查看患者详情：${row.patientName}`)
}

// 查看详情
const handleViewDetail = (row) => {
  ElMessage.info(`查看患者详情：${row.realName}`)
}

// 添加记录
const handleAddRecord = (row) => {
  ElMessage.info(`为患者添加随访记录：${row.realName}`)
}

// 加载数据
const loadData = async () => {
  try {
    // 从本地存储获取用户基础信息
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      Object.assign(userInfo, JSON.parse(userInfoStr))
    }

    // 并行加载所有数据
    const [doctorRes, statsRes, pendingRes, patientsRes] = await Promise.all([
      request.get('/doctor/info'),
      request.get('/doctor/stats'),
      request.get('/doctor/followup/pending'),
      request.get('/doctor/patients')
    ])

    if (doctorRes.code === 200) {
      Object.assign(doctorInfo, doctorRes.data)
    }
    if (statsRes.code === 200) {
      Object.assign(stats, statsRes.data)
    }
    if (pendingRes.code === 200) {
      pendingFollowups.value = pendingRes.data || []
    }
    if (patientsRes.code === 200) {
      myPatients.value = patientsRes.data || []
    }
  } catch (err) {
    console.error('加载数据失败：', err)
    ElMessage.error('加载数据失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.doctor-dashboard {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  margin-bottom: 24px;
}

.header h1 {
  margin: 0 0 8px 0;
  color: #1a1a1a;
}

.welcome {
  margin: 0;
  color: #666;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 20px;
}

.stat-content h3 {
  margin: 0 0 4px 0;
  color: #1a1a1a;
  font-size: 28px;
}

.stat-content p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.info-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>