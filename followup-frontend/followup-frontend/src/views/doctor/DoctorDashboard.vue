<template>
  <div class="doctor-dashboard">
    <!-- 头部信息 -->
    <div class="header">
      <h1>🏥 医生工作台</h1>
      <p class="welcome">欢迎回来，{{ doctorInfo.realName || '医生' }}</p>
    </div>

    <!-- 医生信息卡片 -->
    <el-card class="info-card">
      <template #header>
        <div class="info-card-header">
          <h3>👨‍⚕️ 个人信息</h3>
          <el-button 
            :type="doctorInfo.isOnline === 1 ? 'success' : 'info'" 
            @click="toggleOnlineStatus"
            :loading="toggleLoading"
          >
            {{ doctorInfo.isOnline === 1 ? '在线' : '离线' }}
          </el-button>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="姓名">{{ doctorInfo.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ getGenderText(doctorInfo.gender) }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ doctorInfo.department || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职称">{{ doctorInfo.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专长">{{ doctorInfo.skill || '-' }}</el-descriptions-item>
        <el-descriptions-item label="社区">{{ doctorInfo.community || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ doctorInfo.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="doctorInfo.status === 1 ? 'success' : 'danger'">
            {{ doctorInfo.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 统计数据 -->
    <div class="stats-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">📅</div>
        <div class="stat-content">
          <h3>{{ stats.appointCount }}</h3>
          <p>待接诊</p>
          <el-button type="primary" size="small" @click="handleMyAppoint">我的预约</el-button>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">📋</div>
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

    <!-- 我的患者 -->
    <div class="my-patients">
      <div class="patients-header">
        <h3>👥 我的患者</h3>
        <el-button type="primary" @click="openPatientSearch">
          <el-icon><Search /></el-icon>
          查找患者
        </el-button>
      </div>
      <el-card>
        <el-table :data="patientList" stripe style="width: 100%" v-loading="patientLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="患者姓名" width="120">
            <template #default="{ row }">
              <span>{{ row.realName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="手机号" width="130">
            <template #default="{ row }">
              <span>{{ row.phone || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="chronicType" label="慢病类型" width="120" />
          <el-table-column prop="age" label="年龄" width="80" />
          <el-table-column prop="gender" label="性别" width="80">
            <template #default="{ row }">
              <span>{{ getGenderText(row.gender) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="地址" width="200" show-overflow-tooltip />
          <el-table-column prop="emergencyContact" label="紧急联系人" width="120" />
          <el-table-column prop="emergencyPhone" label="紧急电话" width="130" />
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="patientPage"
            v-model:page-size="patientPageSize"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="patientTotal"
            @size-change="handlePatientSizeChange"
            @current-change="handlePatientCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const doctorInfo = reactive({
  id: null,
  realName: '',
  department: '',
  skill: '',
  community: '',
  phone: '',
  status: null,
  gender: null,
  title: '',
  isOnline: null
})

const stats = reactive({
  appointCount: 0,
  planCount: 0,
  recordCount: 0
})

// 患者列表相关
const patientList = ref([])
const patientPage = ref(1)
const patientPageSize = ref(10)
const patientTotal = ref(0)
const patientLoading = ref(false)

// 在线状态切换加载状态
const toggleLoading = ref(false)

// 获取性别文本
const getGenderText = (gender) => {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

// 加载医生信息
const loadDoctorInfo = async () => {
  try {
    console.log('=== 开始加载医生信息 ===')

    const userStr = sessionStorage.getItem('userInfo')
    console.log('1. sessionStorage 中的 userInfo:', userStr)

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
    const userRole = user.userType || user.role
    console.log('6. 用户角色:', userRole)

    if (userRole !== 2) {
      ElMessage.error('当前用户不是医生账户，当前角色：' + userRole)
      return
    }

    const doctorId = user.doctorId || user.id
    console.log('7. 使用 doctorId:', doctorId)

    if (!doctorId) {
      console.error('无法获取 doctorId')
      ElMessage.warning('未找到医生档案信息')
      return
    }

    console.log('8. 调用接口：/doctor/dashboard/' + doctorId)

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
      doctorInfo.gender = doctor.gender
      doctorInfo.title = doctor.title || ''
      doctorInfo.isOnline = doctor.isOnline

      console.log('11. 最终显示的 doctorInfo:', doctorInfo)
      ElMessage.success('医生工作台加载成功')

      // 加载统计数据
      await loadStats()
      
      // 加载患者列表
      await loadPatientList()
    } else {
      console.error('获取医生信息失败:', res.msg || res.message)
      ElMessage.warning('获取医生信息失败：' + (res.msg || res.message))
    }
  } catch (error) {
    console.error('处理用户信息失败', error)
    ElMessage.error('处理失败：' + error.message)
  }
}

// 加载统计数据（从数据库获取真实数据）
const loadStats = async () => {
  try {
    console.log('=== 开始加载统计数据 ===')
    
    // 使用正确的 doctorId（user.id）
    const userStr = sessionStorage.getItem('userInfo')
    if (!userStr) return
    
    const user = JSON.parse(userStr)
    const doctorId = user.doctorId || user.id
    
    console.log('使用的医生 ID:', doctorId)
    
    // 调用后端统计接口
    const statsRes = await request.get(`/doctor/${doctorId}/stats`)
    console.log('统计数据返回:', statsRes)
    
    if (statsRes.code === 200 && statsRes.data) {
      // 从 follow_appoint 表统计预约数量（排除已取消的）
      stats.appointCount = statsRes.data.appointCount || 0
      
      // 从 follow_plan 表统计随访计划数量
      stats.planCount = statsRes.data.planCount || 0
      
      // 从 follow_record 表统计随访记录数量
      stats.recordCount = statsRes.data.recordCount || 0
      
      console.log('最终统计数据:', stats)
      ElMessage.success('统计数据加载成功')
    } else {
      console.warn('统计数据获取失败:', statsRes.msg)
      // 如果失败，至少显示 0
      stats.appointCount = 0
      stats.planCount = 0
      stats.recordCount = 0
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
    // 如果异常，显示 0
    stats.appointCount = 0
    stats.planCount = 0
    stats.recordCount = 0
  }
}

// 加载患者列表
const loadPatientList = async () => {
  patientLoading.value = true
  try {
    // ✅ 从 sessionStorage 获取医生 ID（user.id）
    const userStr = sessionStorage.getItem('userInfo')
    if (!userStr) {
      console.error('未找到用户信息')
      return
    }
    
    const user = JSON.parse(userStr)
    const doctorId = user.doctorId || user.id
    
    console.log('=== 开始加载患者列表 ===')
    console.log('使用的医生 ID (user.id):', doctorId)
    
    if (!doctorId) {
      console.error('doctorId 为空')
      return
    }

    const res = await request.get(`/doctor/${doctorId}/patients`, {
      params: {
        page: patientPage.value,
        size: patientPageSize.value
      }
    })
    
    console.log('患者列表响应:', res)

    if (res.code === 200 && res.data) {
      patientList.value = res.data.records || []
      patientTotal.value = res.data.total || 0
      console.log('患者列表加载成功，数量:', patientList.value.length)
    } else {
      console.error('患者列表加载失败:', res.msg)
    }
  } catch (error) {
    console.error('加载患者列表失败', error)
    ElMessage.error('加载患者列表失败')
  } finally {
    patientLoading.value = false
  }
}

// 切换在线状态
const toggleOnlineStatus = async () => {
  toggleLoading.value = true
  try {
    const newStatus = doctorInfo.isOnline === 1 ? 0 : 1
    const statusText = newStatus === 1 ? '上线' : '下线'
    
    // 调用后端接口更新在线状态
    const res = await request.put(`/doctor/${doctorInfo.id}/online-status`, {
      isOnline: newStatus
    })
    
    if (res.code === 200) {
      doctorInfo.isOnline = newStatus
      ElMessage.success(`${statusText}成功`)
    } else {
      ElMessage.error(res.msg || `${statusText}失败`)
    }
  } catch (error) {
    console.error('切换在线状态失败', error)
    ElMessage.error('操作失败')
  } finally {
    toggleLoading.value = false
  }
}

// 处理我的预约
const handleMyAppoint = () => {
  router.push('/doctor/my-appointments')
}

// 查看计划
const handlePlan = () => {
  router.push('/doctor/my-plans')
}

// 查看记录
const handleRecord = () => {
  router.push('/doctor/my-records')
}

// 打开患者搜索页面
const openPatientSearch = () => {
  router.push('/doctor/my-patients')
}

// 患者分页处理
const handlePatientSizeChange = () => {
  loadPatientList()
}

const handlePatientCurrentChange = () => {
  loadPatientList()
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

.info-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  color: #303133;
}

.stat-content p {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #909399;
}

.my-patients {
  margin-top: 24px;
}

.patients-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.patients-header h3 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
