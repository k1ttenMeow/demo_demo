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
      <!-- 头部信息 -->
      <div class="header">
        <h1>🧑 患者健康家园</h1>
        <p class="welcome">欢迎回来，{{ userInfo.realName || '患者' }}！</p>
      </div>

      <!-- 个人信息卡片 -->
      <el-card class="info-card" shadow="hover">
        <template #header>
          <div class="info-card-header">
            <span>📋 个人健康档案</span>
            <el-button type="primary" @click="openEditDialog">
              <el-icon><Edit /></el-icon>
              修改个人信息
            </el-button>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="姓名">{{ userInfo.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ getGenderText(userInfo.gender) }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ userInfo.age || '-' }}</el-descriptions-item>
          <el-descriptions-item label="慢病类型">{{ userInfo.chronicType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="责任医生">{{ userInfo.doctorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="家庭住址" :span="2">{{ userInfo.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系人">{{ userInfo.emergencyContact || '-' }}</el-descriptions-item>
          <el-descriptions-item label="紧急电话">{{ userInfo.emergencyPhone || '-' }}</el-descriptions-item>
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
              开始预约
            </el-button>
          </div>
        </el-card>

        <el-card class="main-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📝 我的随访计划</span>
            </div>
          </template>
          <div class="main-content">
            <p class="description">查看您的个性化随访计划</p>
            <el-button type="success" size="large" @click="handlePlan">
              <el-icon><Document /></el-icon>
              我的计划
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
              随访记录
            </el-button>
          </div>
        </el-card>
      </div>
    </template>

    <!-- 修改个人信息弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="✏️ 修改个人信息"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="editForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="editForm.age" :min="1" :max="150" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="慢病类型" prop="chronicType">
          <el-select v-model="editForm.chronicType" placeholder="请选择慢病类型" style="width: 100%">
            <el-option label="高血压" value="高血压" />
            <el-option label="糖尿病" value="糖尿病" />
            <el-option label="冠心病" value="冠心病" />
            <el-option label="脑卒中" value="脑卒中" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="家庭住址" prop="address">
          <el-input v-model="editForm.address" type="textarea" :rows="2" placeholder="请输入家庭住址" />
        </el-form-item>
        
        <el-form-item label="紧急联系人" prop="emergencyContact">
          <el-input v-model="editForm.emergencyContact" placeholder="请输入紧急联系人" />
        </el-form-item>
        
        <el-form-item label="紧急电话" prop="emergencyPhone">
          <el-input v-model="editForm.emergencyPhone" placeholder="请输入紧急联系电话" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Document, Notebook, Edit } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const isLoggedIn = ref(false)

const userInfo = reactive({
  id: null,
  userId: null,
  realName: '',
  gender: 0,
  age: 0,
  chronicType: '',
  doctorName: '',
  phone: '',
  address: '',
  emergencyContact: '',
  emergencyPhone: ''
})

// 编辑弹窗相关
const editDialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const editForm = reactive({
  realName: '',
  gender: 0,
  age: 0,
  chronicType: '',
  phone: '',
  address: '',
  emergencyContact: '',
  emergencyPhone: ''
})

// 表单验证规则
const rules = {
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  emergencyPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 获取性别文本
const getGenderText = (gender) => {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

// 加载患者信息
const loadPatientInfo = async () => {
  try {
    console.log('=== 开始加载患者信息 ===')

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
      isLoggedIn.value = false

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
    const userRole = user.userType || user.role
    console.log('6. 用户角色:', userRole)

    if (userRole !== 3) {
      ElMessage.error('当前用户不是患者账户，当前角色：' + userRole)
      isLoggedIn.value = false
      return
    }

    isLoggedIn.value = true
    
    // ✅ 关键修改：直接使用 user.id
    // 如果 user.id 不存在，尝试其他可能的字段
    const patientId = user.id || user.patientId || user.userId
    
    console.log('=== 患者 ID 信息 ===')
    console.log('user 对象:', user)
    console.log('user.id:', user.id)
    console.log('user.patientId:', user.patientId)
    console.log('user.userId:', user.userId)
    console.log('最终使用的 patientId:', patientId)
    
    if (!patientId) {
      console.error('❌ 无法获取患者 ID，user 对象:', user)
      ElMessage.warning('未找到患者档案信息，请重新登录')
      
      // 清除错误的数据
      sessionStorage.removeItem('userInfo')
      setTimeout(() => {
        router.push('/login')
      }, 2000)
      return
    }
    
    userInfo.id = patientId
    userInfo.userId = patientId

    console.log('8. 调用接口：/patient/dashboard/' + patientId)

    const res = await request.get(`/patient/dashboard/${patientId}`)
    console.log('9. 后端返回的数据:', res)

    if (res.code === 200 && res.data) {
      const patient = res.data
      console.log('10. 患者详细信息:', patient)

      userInfo.realName = patient.realName || user.realName || ''
      userInfo.gender = patient.gender || 0
      userInfo.age = patient.age || 0
      userInfo.chronicType = patient.chronicType || ''
      userInfo.address = patient.address || ''
      userInfo.doctorName = patient.doctorName || '未分配'
      userInfo.phone = patient.phone || user.phone || ''
      userInfo.emergencyContact = patient.emergencyContact || ''
      userInfo.emergencyPhone = patient.emergencyPhone || ''

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

// 打开编辑弹窗
const openEditDialog = () => {
  // 填充表单数据
  editForm.realName = userInfo.realName
  editForm.gender = userInfo.gender
  editForm.age = userInfo.age
  editForm.chronicType = userInfo.chronicType
  editForm.phone = userInfo.phone
  editForm.address = userInfo.address
  editForm.emergencyContact = userInfo.emergencyContact
  editForm.emergencyPhone = userInfo.emergencyPhone
  
  editDialogVisible.value = true
}

// 提交修改
const submitEdit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const userStr = sessionStorage.getItem('userInfo')
      if (!userStr) {
        ElMessage.error('未找到用户信息')
        return
      }
      
      const user = JSON.parse(userStr)
      const userId = user.id
      
      console.log('=== 准备更新患者信息 ===')
      console.log('userId:', userId)
      
      // ✅ 先查询 patient 表获取 patient.id
      const patientRes = await request.get('/patient/list', {
        params: {
          page: 1,
          size: 100
        }
      })
      
      if (patientRes.code !== 200 || !patientRes.data || !patientRes.data.records) {
        ElMessage.error('查询患者档案失败')
        return
      }
      
      // 找到当前用户的 patient 记录
      const patientRecord = patientRes.data.records.find(p => p.userId === userId)
      
      if (!patientRecord) {
        ElMessage.error('未找到患者档案')
        return
      }
      
      console.log('找到 patient 记录:', patientRecord)
      
      // 准备更新数据
      const updateData = {
        id: patientRecord.id,  // ✅ 使用 patient 表的主键 id
        userId: userId,
        gender: editForm.gender,
        doctorId: patientRecord.doctorId,  // 保持原医生 ID
        chronicType: editForm.chronicType,
        age: editForm.age,
        address: editForm.address,
        emergencyContact: editForm.emergencyContact,
        emergencyPhone: editForm.emergencyPhone,
        realName: editForm.realName,
        phone: editForm.phone
      }
      
      console.log('=== 提交更新数据 ===', updateData)
      
      // 调用后端接口更新
      const res = await request.put('/patient', updateData)
      console.log('更新响应:', res)
      
      if (res.code === 200) {
        ElMessage.success('修改成功')
        editDialogVisible.value = false
        
        // 重新加载患者信息
        await loadPatientInfo()
      } else {
        ElMessage.error(res.msg || '修改失败')
      }
    } catch (error) {
      console.error('修改失败', error)
      ElMessage.error('修改失败：' + (error.message || '未知错误'))
    } finally {
      submitLoading.value = false
    }
  })
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}

// 进行预约
const handleAppoint = () => {
  router.push('/patient/my-patient-appoints')
}

// 查看计划
const handlePlan = () => {
  router.push('/patient/my-patient-plans')
}

// 查看记录
const handleRecord = () => {
  router.push('/patient/my-patient-records')
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

.info-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
