<template>
  <div class="my-doctor-records">
    <div class="header">
      <div class="header-left">
        <el-button type="info" @click="goBack" style="margin-right: 16px;">
          <el-icon><ArrowLeft /></el-icon>
          返回主页
        </el-button>
        <h1>📊 我的随访记录</h1>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增记录
      </el-button>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="患者姓名">
          <el-input v-model="searchForm.patientName" placeholder="请输入患者姓名" clearable />
        </el-form-item>
        <el-form-item label="随访时间">
          <el-date-picker
            v-model="searchForm.followTime"
            type="date"
            placeholder="选择日期"
            style="width: 180px"
            value-format="YYYY-MM-DD"
          />
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
      <el-table :data="tableData" stripe style="width: 100%" max-height="600" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="patientName" label="患者姓名" width="120" />
        <el-table-column prop="bloodPressure" label="血压" width="100" />
        <el-table-column prop="bloodSugar" label="血糖" width="100" />
        <el-table-column prop="symptom" label="症状" width="150" show-overflow-tooltip />
        <el-table-column prop="drug" label="用药情况" width="150" show-overflow-tooltip />
        <el-table-column prop="followTime" label="随访时间" width="180" />
        <el-table-column prop="remark" label="备注" width="200" show-overflow-tooltip />
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="患者" prop="patientId">
          <el-autocomplete
            v-model="patientNameInput"
            :fetch-suggestions="searchPatient"
            placeholder="请输入患者姓名"
            style="width: 100%"
            clearable
            @select="handlePatientSelect"
          />
        </el-form-item>
        <el-form-item label="随访计划" prop="planId">
          <el-select 
            v-model="form.planId" 
            placeholder="请选择随访计划" 
            style="width: 100%" 
            :disabled="!form.patientId"
          >
            <el-option 
              v-for="plan in planOptions" 
              :key="plan.id" 
              :label="plan.planType + ' - ' + plan.cycle" 
              :value="plan.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="血压" prop="bloodPressure">
          <el-input v-model="form.bloodPressure" placeholder="例如：120/80 mmHg" style="width: 100%" />
        </el-form-item>
        <el-form-item label="血糖" prop="bloodSugar">
          <el-input v-model="form.bloodSugar" placeholder="例如：6.1 mmol/L" style="width: 100%" />
        </el-form-item>
        <el-form-item label="用药情况" prop="drug">
          <el-input 
            v-model="form.drug" 
            type="textarea" 
            :rows="2" 
            placeholder="请输入用药情况" 
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="症状" prop="symptom">
          <el-input 
            v-model="form.symptom" 
            type="textarea" 
            :rows="2" 
            placeholder="请输入症状描述" 
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="随访时间" prop="followTime">
          <el-date-picker
            v-model="form.followTime"
            type="datetime"
            placeholder="选择随访时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="form.remark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入备注" 
            style="width: 100%" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ArrowLeft, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const searchForm = reactive({
  patientName: '',
  followTime: ''
})

const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 当前医生 ID
const doctorId = ref(null)

// 新增/编辑相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增记录')
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  patientId: null,
  planId: null,
  bloodPressure: '',
  bloodSugar: '',
  drug: '',
  symptom: '',
  followTime: '',
  remark: ''
})

const patientNameInput = ref('')

// 随访计划选项
const planOptions = ref([])

const rules = {
  patientId: [
    { required: true, message: '请选择患者', trigger: 'change' }
  ],
  planId: [
    { required: true, message: '请选择随访计划', trigger: 'change' }
  ],
  bloodPressure: [
    { required: true, message: '请输入血压', trigger: 'blur' }
  ],
  bloodSugar: [
    { required: true, message: '请输入血糖', trigger: 'blur' }
  ],
  followTime: [
    { required: true, message: '请选择随访时间', trigger: 'change' }
  ]
}

// 返回医生工作台
const goBack = () => {
  router.push('/doctor/dashboard')
}

// 加载数据
const loadData = async () => {
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
    const res = await request.get('/followup/record/list', { params })
    console.log('=== 响应数据 ===', res)
    
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
      console.log('=== 表格数据 ===', tableData.value)
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
  loadData()
}

// 重置搜索
const resetSearch = () => {
  searchForm.patientName = ''
  searchForm.followTime = ''
  currentPage.value = 1
  loadData()
}

// 搜索患者（自动补全）
const searchPatient = async (queryString, cb) => {
  try {
    const res = await request.get('/patient/list', {
      params: { page: 1, size: 100 }
    })
    if (res.code === 200) {
      const patients = res.data.records || []
      const results = queryString
        ? patients.filter(p => p.realName && p.realName.includes(queryString))
        : patients

      // 转换为 el-autocomplete 需要的格式
      const formattedResults = results.map(p => ({
        value: p.realName,
        id: p.userId  // 使用 userId（user.id）
      }))

      cb(formattedResults)
    } else {
      cb([])
    }
  } catch (error) {
    console.error('搜索患者失败', error)
    cb([])
  }
}

// 患者选择处理
const handlePatientSelect = async (item) => {
  form.patientId = item.id
  patientNameInput.value = item.value
  
  // 清空计划选择
  form.planId = null
  planOptions.value = []
  
  // 加载该患者在该医生名下的随访计划
  await loadPatientPlans(item.id)
}

// 加载患者的随访计划（只加载当前医生的计划）
const loadPatientPlans = async (patientId) => {
  try {
    const res = await request.get('/followup/plan/list', {
      params: {
        page: 1,
        size: 100,
        doctorId: doctorId.value  // 只查询当前医生的计划
      }
    })
    if (res.code === 200) {
      const plans = res.data.records || []
      // 过滤出该患者的计划
      planOptions.value = plans.filter(plan => plan.patientId === patientId)
      
      console.log('=== 患者 ID:', patientId)
      console.log('=== 找到的随访计划数量:', planOptions.value.length)
      console.log('=== 随访计划列表:', planOptions.value)
      
      if (planOptions.value.length === 0) {
        ElMessage.warning('该患者没有可用的随访计划')
      }
    }
  } catch (error) {
    console.error('加载随访计划失败', error)
    ElMessage.error('加载随访计划失败')
  }
}

// 新增记录
const handleAdd = () => {
  dialogTitle.value = '新增记录'
  dialogVisible.value = true
  patientNameInput.value = ''
  form.patientId = null
  form.planId = null
  planOptions.value = []
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 设置医生 ID
        const submitData = {
          ...form,
          doctorId: doctorId.value
        }
        
        console.log('=== 提交数据 ===', submitData)
        const res = await request.post('/followup/record', submitData)
        
        if (res.code === 200) {
          ElMessage.success('新增成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.msg || '新增失败')
        }
      } catch (error) {
        console.error('新增失败:', error)
        ElMessage.error('新增失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.id = null
  form.patientId = null
  form.planId = null
  form.bloodPressure = ''
  form.bloodSugar = ''
  form.drug = ''
  form.symptom = ''
  form.followTime = ''
  form.remark = ''
  patientNameInput.value = ''
  planOptions.value = []
}

// 分页处理
const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
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
      
      await loadData()
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
.my-doctor-records {
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
