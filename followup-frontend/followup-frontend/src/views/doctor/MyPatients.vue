<template>
  <div class="my-patients-page">
    <div class="header">
      <div class="header-left">
        <el-button type="info" @click="goBack" style="margin-right: 16px;">
          <el-icon><ArrowLeft /></el-icon>
          返回主页
        </el-button>
        <h1>👥 我的患者</h1>
      </div>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="患者姓名">
          <el-input v-model="searchForm.patientName" placeholder="请输入患者姓名" clearable />
        </el-form-item>
        <el-form-item label="慢病类型">
          <el-select v-model="searchForm.chronicType" placeholder="请选择类型" clearable>
            <el-option label="高血压" value="高血压" />
            <el-option label="糖尿病" value="糖尿病" />
            <el-option label="冠心病" value="冠心病" />
            <el-option label="脑卒中" value="脑卒中" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="patientList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="chronicType" label="慢病类型" width="120" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ getGenderText(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="showDetail(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 患者详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="📋 患者详细信息"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="currentPatient">
        <el-descriptions-item label="姓名">{{ currentPatient.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ getGenderText(currentPatient.gender) }}
        </el-descriptions-item>
        <el-descriptions-item label="年龄">{{ currentPatient.age || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentPatient.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="慢病类型">{{ currentPatient.chronicType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ currentPatient.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职业">{{ currentPatient.occupation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文化程度">{{ currentPatient.education || '-' }}</el-descriptions-item>
        <el-descriptions-item label="婚姻状况">
          {{ getMaritalText(currentPatient.maritalStatus) }}
        </el-descriptions-item>
        <el-descriptions-item label="医保类型">{{ currentPatient.insuranceType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="过敏史">{{ currentPatient.allergyHistory || '无' }}</el-descriptions-item>
        <el-descriptions-item label="家族病史">{{ currentPatient.familyHistory || '无' }}</el-descriptions-item>
        <el-descriptions-item label="既往病史">{{ currentPatient.medicalHistory || '无' }}</el-descriptions-item>
        <el-descriptions-item label="手术史">{{ currentPatient.surgeryHistory || '无' }}</el-descriptions-item>
        <el-descriptions-item label="吸烟史">{{ getSmokingText(currentPatient.smokingHistory) }}</el-descriptions-item>
        <el-descriptions-item label="饮酒史">{{ getDrinkingText(currentPatient.drinkingHistory) }}</el-descriptions-item>
        <el-descriptions-item label="运动频率">{{ currentPatient.exerciseFrequency || '-' }}</el-descriptions-item>
        <el-descriptions-item label="饮食习惯">{{ currentPatient.dietaryHabits || '-' }}</el-descriptions-item>
        <el-descriptions-item label="心理健康">{{ currentPatient.mentalHealth || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生活质量">{{ currentPatient.qualityOfLife || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用药情况">{{ currentPatient.medicationStatus || '-' }}</el-descriptions-item>
        <el-descriptions-item label="随访医生">{{ currentPatient.doctorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系人">{{ currentPatient.emergencyContact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="紧急电话">{{ currentPatient.emergencyPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家庭住址" :span="2">{{ currentPatient.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentPatient.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const searchForm = reactive({
  patientName: '',
  chronicType: ''
})

const patientList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 当前医生 ID
const doctorId = ref(null)

// 详情弹窗相关
const detailVisible = ref(false)
const currentPatient = ref(null)

// 获取性别文本
const getGenderText = (gender) => {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

// 获取婚姻状况文本
const getMaritalText = (status) => {
  const map = { 1: '未婚', 2: '已婚', 3: '离异', 4: '丧偶' }
  return map[status] || '未知'
}

// 获取吸烟史文本
const getSmokingText = (history) => {
  const map = { 1: '从不吸烟', 2: '已戒烟', 3: '偶尔吸烟', 4: '经常吸烟' }
  return map[history] || '未知'
}

// 获取饮酒史文本
const getDrinkingText = (history) => {
  const map = { 1: '从不饮酒', 2: '已戒酒', 3: '偶尔饮酒', 4: '经常饮酒' }
  return map[history] || '未知'
}

// 返回医生工作台
const goBack = () => {
  router.push('/doctor/dashboard')
}

// 加载患者列表
const loadPatientList = async () => {
  loading.value = true
  try {
    if (!doctorId.value) {
      ElMessage.error('未找到医生信息')
      return
    }

    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm,
      doctorId: doctorId.value
    }
    
    console.log('=== 请求参数 ===', params)
    const res = await request.get(`/doctor/${doctorId.value}/patients`, { params })
    console.log('=== 响应数据 ===', res)
    
    if (res.code === 200 && res.data) {
      patientList.value = res.data.records || []
      total.value = res.data.total || 0
      console.log('=== 患者列表 ===', patientList.value)
    } else {
      ElMessage.error(res.msg || '加载数据失败')
    }
  } catch (error) {
    console.error('加载数据异常:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadPatientList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.patientName = ''
  searchForm.chronicType = ''
  currentPage.value = 1
  loadPatientList()
}

// 显示详情
const showDetail = (row) => {
  currentPatient.value = row
  detailVisible.value = true
}

// 分页处理
const handleSizeChange = () => {
  loadPatientList()
}

const handleCurrentChange = () => {
  loadPatientList()
}

// 初始化
onMounted(async () => {
  // 从 sessionStorage 获取用户信息
  const userStr = sessionStorage.getItem('userInfo')
  console.log('=== sessionStorage userInfo ===', userStr)
  
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      console.log('=== 解析后的用户对象 ===', user)
      console.log('=== user.doctorId ===', user.doctorId)
      console.log('=== user.id ===', user.id)
      
      // 使用 doctorId（存储的是 user.id）
      doctorId.value = user.doctorId || user.id
      
      console.log('=== 最终使用的医生 ID ===', doctorId.value)
      
      if (!doctorId.value) {
        ElMessage.error('未找到医生信息')
        return
      }
      
      await loadPatientList()
    } catch (error) {
      console.error('获取医生 ID 失败', error)
      ElMessage.error('获取医生信息失败')
    }
  } else {
    ElMessage.warning('未找到用户信息，请重新登录')
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  }
})
</script>

<style scoped>
.my-patients-page {
  padding: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
