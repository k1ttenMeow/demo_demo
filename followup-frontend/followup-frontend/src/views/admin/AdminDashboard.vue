<template>
  <div class="admin-dashboard">
    <div class="header">
      <h1>系统管理后台</h1>
      <p class="welcome">欢迎回来，{{ userInfo.realName }}管理员！</p>
    </div>

    <!-- 系统概览统计 -->
    <div class="stats-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <h3>{{ stats.totalUsers }}</h3>
          <p>系统总用户</p>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">👨‍⚕️</div>
        <div class="stat-content">
          <h3>{{ stats.doctorCount }}</h3>
          <p>注册医生</p>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">🧑‍🦽</div>
        <div class="stat-content">
          <h3>{{ stats.patientCount }}</h3>
          <p>在管患者</p>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">📝</div>
        <div class="stat-content">
          <h3>{{ stats.followupCount }}</h3>
          <p>随访记录</p>
        </div>
      </el-card>
    </div>

    <!-- 快捷操作 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <span>⚡ 快捷操作</span>
      </template>
      <div class="quick-actions">
        <el-button type="primary" size="large" @click="handleUserManage">
          <el-icon><User /></el-icon>
          用户管理
        </el-button>
        <el-button type="success" size="large" @click="handleDataStats">
          <el-icon><DataLine /></el-icon>
          数据统计
        </el-button>
        <el-button type="warning" size="large" @click="handleSystemSettings">
          <el-icon><Setting /></el-icon>
          系统设置
        </el-button>
        <el-button type="info" size="large" @click="handleLogView">
          <el-icon><Document /></el-icon>
          日志查看
        </el-button>
      </div>
    </el-card>

    <!-- 最近注册用户 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>👥 最近注册用户</span>
          <el-button type="primary" size="small" @click="handleViewAllUsers">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentUsers" stripe style="width: 100%">
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="登录账号" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="userType" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.userType)">{{ getRoleText(row.userType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditUser(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, DataLine, Setting, Document } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 用户信息
const userInfo = reactive({
  realName: ''
})

// 统计数据
const stats = reactive({
  totalUsers: 0,
  doctorCount: 0,
  patientCount: 0,
  followupCount: 0
})

// 最近注册用户
const recentUsers = ref([])

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 获取角色类型
const getRoleType = (userType) => {
  const map = { 1: 'danger', 2: 'primary', 3: 'success' }
  return map[userType] || 'info'
}

// 获取角色文本
const getRoleText = (userType) => {
  const map = { 1: '管理员', 2: '医生', 3: '患者' }
  return map[userType] || '未知'
}

// 快捷操作
const handleUserManage = () => {
  ElMessage.info('跳转到用户管理页面')
}
const handleDataStats = () => {
  ElMessage.info('跳转到数据统计页面')
}
const handleSystemSettings = () => {
  ElMessage.info('跳转到系统设置页面')
}
const handleLogView = () => {
  ElMessage.info('跳转到日志查看页面')
}
const handleViewAllUsers = () => {
  ElMessage.info('跳转到全部用户列表')
}

// 用户操作
const handleEditUser = (row) => {
  ElMessage.info(`编辑用户：${row.realName}`)
}
const handleDeleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户「${row.realName}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('删除成功')
  } catch {
    ElMessage.info('已取消删除')
  }
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
    const [statsRes, usersRes] = await Promise.all([
      request.get('/admin/stats'),
      request.get('/admin/user/recent')
    ])

    if (statsRes.code === 200) {
      Object.assign(stats, statsRes.data)
    }
    if (usersRes.code === 200) {
      recentUsers.value = usersRes.data || []
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
.admin-dashboard {
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

.quick-actions {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 32px;
  font-size: 16px;
}
</style>