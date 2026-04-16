<template>
  <div class="my-doctor-plans">
    <div class="header">
      <div class="header-left">
        <el-button type="info" @click="goBack" style="margin-right: 16px;">
          <el-icon><ArrowLeft /></el-icon>
          返回主页
        </el-button>
        <h1>📝 我的随访计划</h1>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增计划
      </el-button>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="患者姓名">
          <el-input v-model="searchForm.patientName" placeholder="请输入患者姓名" clearable />
        </el-form-item>
        <el-form-item label="计划类型">
          <el-select v-model="searchForm.planType" placeholder="请选择计划类型" clearable>
            <el-option label="月度随访" value="月度随访" />
            <el-option label="季度随访" value="季度随访" />
            <el-option label="年度随访" value="年度随访" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待执行" value="待执行" />
            <el-option label="执行中" value="执行中" />
            <el-option label="已完成" value="已完成" />
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
      <el-table :data="tableData" stripe style="width: 100%" max-height="600" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="patientName" label="患者姓名" width="120" />
        <el-table-column prop="planType" label="计划类型" width="120" />
        <el-table-column prop="cycle" label="周期" width="100" />
        <el-table-column prop="nextTime" label="下次随访时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleEditStatus(row)"
            >
              修改状态
            </el-button>
          </template>
        </el-table-column>
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

    <!-- 修改状态弹窗 -->
    <el-dialog
      v-model="statusDialogVisible"
      title="修改随访计划状态"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="statusForm" label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(statusForm.currentStatus)">
            {{ statusForm.currentStatus }}
          </el-tag>
        </el-form-item>
        <el-form-item label="修改为" required>
          <el-select v-model="statusForm.newStatus" placeholder="请选择新状态" style="width: 100%">
            <el-option label="待执行" value="待执行" />
            <el-option label="执行中" value="执行中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStatusChange" :loading="statusLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新增计划弹窗 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增随访计划"
      width="600px"
      :close-on-click-modal="false"
      @close="resetAddForm"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addRules"
        label-width="120px"
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
        <el-form-item label="计划类型" prop="planType">
          <el-select v-model="addForm.planType" placeholder="请选择计划类型" style="width: 100%" @change="handlePlanTypeChange">
            <el-option label="月度随访" value="月度随访" />
            <el-option label="季度随访" value="季度随访" />
            <el-option label="年度随访" value="年度随访" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期" prop="cycle">
          <el-input v-model="addForm.cycle" placeholder="例如：30 天、90 天" />
        </el-form-item>
        <el-form-item label="下次随访时间" prop="nextTime">
          <el-date-picker
            v-model="addForm.nextTime"
            type="date"
            placeholder="选择下次随访时间"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="addForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待执行" value="待执行" />
            <el-option label="执行中" value="执行中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="addForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddForm" :loading="addLoading">
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
  planType: '',
  status: ''
})

const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 当前医生 ID
const doctorId = ref(null)

// 状态修改相关
const statusDialogVisible = ref(false)
const statusLoading = ref(false)
const statusForm = reactive({
  planId: null,
  currentStatus: '',
  newStatus: ''
})

// 新增计划相关
const addDialogVisible = ref(false)
const addLoading = ref(false)
const addFormRef = ref(null)
const patientNameInput = ref('')
const addForm = reactive({
  patientId: null,
  planType: '月度随访',
  cycle: '30 天',
  nextTime: '',
  status: '待执行',
  remark: ''
})

const addRules = {
  patientId: [
    { required: true, message: '请选择患者', trigger: 'change' }
  ],
  planType: [
    { required: true, message: '请选择计划类型', trigger: 'change' }
  ],
  cycle: [
    { required: true, message: '请输入周期', trigger: 'blur' }
  ],
  nextTime: [
    { required: true, message: '请选择下次随访时间', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取状态对应的标签类型
const getStatusType = (status) => {
  const map = { 
    '待执行': 'warning', 
    '执行中': 'primary', 
    '已完成': 'success' 
  }
  return map[status] || 'info'
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
    const res = await request.get('/followup/plan/list', { params })
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
  searchForm.planType = ''
  searchForm.status = ''
  currentPage.value = 1
  loadData()
}

// 修改状态
const handleEditStatus = (row) => {
  statusForm.planId = row.id
  statusForm.currentStatus = row.status
  statusForm.newStatus = row.status
  statusDialogVisible.value = true
}

// 提交状态修改
const submitStatusChange = async () => {
  if (!statusForm.newStatus) {
    ElMessage.warning('请选择新状态')
    return
  }

  if (statusForm.newStatus === statusForm.currentStatus) {
    ElMessage.warning('新状态与当前状态相同')
    statusDialogVisible.value = false
    return
  }

  statusLoading.value = true
  try {
    const res = await request.put('/followup/plan/status', {
      id: statusForm.planId,
      status: statusForm.newStatus
    })

    if (res.code === 200) {
      ElMessage.success('状态修改成功')
      statusDialogVisible.value = false
      // 刷新列表
      loadData()
    } else {
      ElMessage.error(res.msg || '状态修改失败')
    }
  } catch (error) {
    console.error('修改状态失败:', error)
    ElMessage.error('修改状态失败')
  } finally {
    statusLoading.value = false
  }
}

// 分页处理
const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

// 新增计划
const handleAdd = () => {
  addDialogVisible.value = true
  patientNameInput.value = ''
}

// 搜索患者
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

      const formattedResults = results.map(p => ({
        value: p.realName,
        id: p.userId
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
const handlePatientSelect = (item) => {
  addForm.patientId = item.id
  patientNameInput.value = item.value
}

// 计划类型变化时自动设置周期
const handlePlanTypeChange = (value) => {
  switch (value) {
    case '月度随访':
      addForm.cycle = '30 天'
      break
    case '季度随访':
      addForm.cycle = '90 天'
      break
    case '年度随访':
      addForm.cycle = '365 天'
      break
  }
}

// 重置新增表单
const resetAddForm = () => {
  if (addFormRef.value) {
    addFormRef.value.resetFields()
  }
  addForm.patientId = null
  addForm.planType = '月度随访'
  addForm.cycle = '30 天'
  addForm.nextTime = ''
  addForm.status = '待执行'
  addForm.remark = ''
  patientNameInput.value = ''
}

// 提交新增表单
const submitAddForm = async () => {
  if (!addFormRef.value) return
  
  await addFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    addLoading.value = true
    try {
      if (!doctorId.value) {
        ElMessage.error('未找到医生信息')
        return
      }

      const data = {
        patientId: addForm.patientId,
        doctorId: doctorId.value,
        planType: addForm.planType,
        cycle: addForm.cycle,
        nextTime: addForm.nextTime,
        status: addForm.status,
        remark: addForm.remark
      }

      console.log('=== 提交新增计划数据 ===', data)
      const res = await request.post('/followup/plan', data)
      
      if (res.code === 200) {
        ElMessage.success('新增成功')
        addDialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.msg || '新增失败')
      }
    } catch (error) {
      console.error('新增失败', error)
      ElMessage.error('新增失败：' + (error.message || '未知错误'))
    } finally {
      addLoading.value = false
    }
  })
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
.my-doctor-plans {
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
