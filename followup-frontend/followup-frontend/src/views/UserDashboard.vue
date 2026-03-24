<template>
  <div class="dashboard-container">
    <!-- 顶部欢迎栏 -->
    <div class="header">
      <h1>慢性病随访系统 - 用户中心</h1>
      <p class="welcome-text">欢迎回来，{{ userInfo.realName }}！</p>
    </div>

    <!-- 统计卡片区域 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <h3>{{ stats.patientCount }}</h3>
          <p>在管患者数</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📋</div>
        <div class="stat-content">
          <h3>{{ stats.followupCount }}</h3>
          <p>本月随访记录</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⏰</div>
        <div class="stat-content">
          <h3>{{ stats.pendingCount }}</h3>
          <p>待办随访任务</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <h3>{{ stats.completedRate }}%</h3>
          <p>随访完成率</p>
        </div>
      </div>
    </div>

    <!-- 用户信息展示 -->
    <div class="info-section">
      <h2>个人信息</h2>
      <div class="info-grid">
        <div class="info-item">
          <span class="label">登录账号：</span>
          <span class="value">{{ userInfo.username }}</span>
        </div>
        <div class="info-item">
          <span class="label">真实姓名：</span>
          <span class="value">{{ userInfo.realName }}</span>
        </div>
        <div class="info-item">
          <span class="label">联系电话：</span>
          <span class="value">{{ userInfo.phone }}</span>
        </div>
        <div class="info-item">
          <span class="label">用户类型：</span>
          <span class="value">{{ userTypeText }}</span>
        </div>
        <div v-if="userInfo.department" class="info-item">
          <span class="label">所属科室：</span>
          <span class="value">{{ userInfo.department }}</span>
        </div>
        <div v-if="userInfo.title" class="info-item">
          <span class="label">职称：</span>
          <span class="value">{{ userInfo.title }}</span>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-section">
      <button class="btn btn-primary" @click="handleUpdateInfo">修改个人信息</button>
      <button class="btn btn-secondary" @click="handleRefresh">刷新数据</button>
    </div>
  </div>
</template>

<!-- 核心修正：显式导入所有用到的 Vue 组合式 API -->
<script setup>
import { reactive, ref, computed, onMounted } from 'vue'

// --- 1. 响应式数据定义 ---
// 用户信息（使用 reactive）
const userInfo = reactive({
  id: null,
  username: '',
  realName: '',
  phone: '',
  userType: null,
  department: '',
  title: '',
  createTime: ''
})

// 统计数据（使用 reactive）
const stats = reactive({
  patientCount: 0,
  followupCount: 0,
  pendingCount: 0,
  completedRate: 0
})

// 加载状态（使用 ref）
const loading = ref(false)

// --- 2. 计算属性 ---
const userTypeText = computed(() => {
  const typeMap = {
    1: '患者',
    2: '医生',
    3: '管理员'
  }
  return typeMap[userInfo.userType] || '未知'
})

// --- 3. 方法定义 ---
// 模拟获取用户信息
const fetchUserInfo = () => {
  loading.value = true
  // 模拟 API 请求延迟
  setTimeout(() => {
    // 模拟从后端获取的数据
    userInfo.id = 1
    userInfo.username = 'doctor001'
    userInfo.realName = '张医生'
    userInfo.phone = '13800138000'
    userInfo.userType = 2
    userInfo.department = '内科'
    userInfo.title = '主治医师'
    userInfo.createTime = '2025-01-15 10:30:00'
    
    loading.value = false
    console.log('用户信息加载完成')
  }, 500)
}

// 模拟获取统计数据
const fetchStats = () => {
  setTimeout(() => {
    stats.patientCount = 128
    stats.followupCount = 45
    stats.pendingCount = 12
    stats.completedRate = 85
  }, 800)
}

// 修改个人信息
const handleUpdateInfo = () => {
  alert('跳转到修改个人信息页面（示例）')
}

// 刷新数据
const handleRefresh = () => {
  fetchUserInfo()
  fetchStats()
  alert('数据已刷新')
}

// --- 4. 生命周期钩子 ---
onMounted(() => {
  console.log('UserDashboard 组件已挂载')
  fetchUserInfo()
  fetchStats()
})
</script>

<!-- 简单的样式，让页面更美观 -->
<style scoped>
.dashboard-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.header {
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.header h1 {
  margin: 0 0 8px 0;
  color: #1a1a1a;
  font-size: 28px;
}

.welcome-text {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
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

.info-section {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

.info-section h2 {
  margin: 0 0 20px 0;
  color: #1a1a1a;
  font-size: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.info-item {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  color: #666;
  font-weight: 500;
}

.value {
  color: #1a1a1a;
  margin-left: 8px;
}

.action-section {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-primary {
  background-color: #1890ff;
  color: #fff;
}

.btn-primary:hover {
  background-color: #40a9ff;
}

.btn-secondary {
  background-color: #f0f0f0;
  color: #1a1a1a;
}

.btn-secondary:hover {
  background-color: #d9d9d9;
}
</style>