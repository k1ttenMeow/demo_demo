<template>
  <div class="patient-dashboard">
    <div class="header">
      <h1>患者中心</h1>
      <p class="welcome">欢迎回来，{{ userInfo.realName }}！</p>
    </div>

    <!-- 健康档案卡片 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📋 我的健康档案</span>
          <el-button type="primary" size="small" @click="handleUpdateInfo">更新档案</el-button>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ patientInfo.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ patientInfo.gender === 1 ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ patientInfo.age || '-' }}岁</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ maskIdCard(patientInfo.idCard) }}</el-descriptions-item>
        <el-descriptions-item label="慢性病种" :span="2">{{ patientInfo.chronicDisease || '-' }}</el-descriptions-item>
        <el-descriptions-item label="居住地址" :span="2">{{ patientInfo.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系人">{{ patientInfo.emergencyContact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系电话">{{ patientInfo.emergencyPhone || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 专属医生卡片 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <span>👨‍⚕️ 我的专属医生</span>
      </template>
      <div v-if="doctorInfo" class="doctor-info">
        <el-avatar :size="80" class="doctor-avatar">
          {{ doctorInfo.realName?.charAt(0) }}
        </el-avatar>
        <div class="doctor-detail">
          <h3>{{ doctorInfo.realName }}</h3>
          <p class="dept">{{ doctorInfo.deptName }} · {{ doctorInfo.title || '主治医师' }}</p>
          <p class="specialty">擅长：{{ doctorInfo.specialty || '-' }}</p>
          <p class="community">管辖社区：{{ doctorInfo.manageCommunity || '-' }}</p>
          <el-button type="primary" size="small" @click="handleAppointment">预约随访</el-button>
        </div>
      </div>
      <el-empty v-else description="暂无专属医生" />
    </el-card>

    <!-- 随访计划 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <span>📅 我的随访计划</span>
      </template>
      <el-table :data="followupPlans" stripe style="width: 100%">
        <el-table-column prop="planType" label="计划类型" width="120" />
        <el-table-column prop="cycleType" label="随访周期" width="120" />
        <el-table-column prop="nextFollowTime" label="下次随访时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.nextFollowTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 随访记录 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <span>📝 我的随访记录</span>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="record in followupRecords"
          :key="record.id"
          :timestamp="formatDate(record.followTime)"
          placement="top"
        >
          <el-card>
            <div class="record-item">
              <div class="record-header">
                <span class="doctor-name">随访医生：{{ record.doctorName }}</span>
              </div>
              <el-descriptions :column="2" size="small" border>
                <el-descriptions-item label="血压">{{ record.bloodPressure || '-' }}</el-descriptions-item>
                <el-descriptions-item label="血糖">{{ record.bloodSugar || '-' }} mmol/L</el-descriptions-item>
                <el-descriptions-item label="用药情况" :span="2">{{ record.medication || '-' }}</el-descriptions-item>
                <el-descriptions-item label="症状描述" :span="2">{{ record.symptoms || '-' }}</el-descriptions-item>
                <el-descriptions-item label="医生备注" :span="2">{{ record.doctorRemark || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
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

// 患者健康档案
const patientInfo = reactive({
  realName: '',
  gender: 1,
  age: null,
  idCard: '',
  chronicDisease: '',
  address: '',
  emergencyContact: '',
  emergencyPhone: ''
})

// 专属医生信息
const doctorInfo = ref(null)

// 随访计划
const followupPlans = ref([])

// 随访记录
const followupRecords = ref([])

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 身份证号脱敏
const maskIdCard = (idCard) => {
  if (!idCard) return '-'
  return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2')
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

// 更新档案
const handleUpdateInfo = () => {
  ElMessage.info('跳转到更新档案页面')
}

// 预约随访
const handleAppointment = () => {
  ElMessage.info('跳转到预约随访页面')
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
    const [patientRes, doctorRes, plansRes, recordsRes] = await Promise.all([
      request.get('/patient/info'),
      request.get('/patient/doctor'),
      request.get('/patient/followup/plans'),
      request.get('/patient/followup/records')
    ])

    if (patientRes.code === 200) {
      Object.assign(patientInfo, patientRes.data)
    }
    if (doctorRes.code === 200) {
      doctorInfo.value = doctorRes.data
    }
    if (plansRes.code === 200) {
      followupPlans.value = plansRes.data || []
    }
    if (recordsRes.code === 200) {
      followupRecords.value = recordsRes.data || []
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
.patient-dashboard {
  padding: 24px;
  max-width: 1200px;
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

.info-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.doctor-info {
  display: flex;
  gap: 24px;
  align-items: center;
}

.doctor-avatar {
  background: linear-gradient(135deg, #4299e1, #3182ce);
  color: #fff;
  font-size: 32px;
  font-weight: bold;
}

.doctor-detail h3 {
  margin: 0 0 8px 0;
  color: #1a1a1a;
}

.dept {
  margin: 0 0 4px 0;
  color: #4299e1;
  font-weight: 500;
}

.specialty, .community {
  margin: 4px 0;
  color: #666;
  font-size: 14px;
}

.record-item {
  width: 100%;
}

.record-header {
  margin-bottom: 12px;
}

.doctor-name {
  font-weight: 600;
  color: #1a1a1a;
}
</style>