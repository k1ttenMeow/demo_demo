<template>
  <div class="my-patient-appoints">
    <div class="header">
      <div class="header-left">
        <el-button type="info" @click="goBack" style="margin-right: 16px;">
          <el-icon><ArrowLeft /></el-icon>
          返回主页
        </el-button>
        <h1>📅 我的随访预约</h1>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增预约
      </el-button>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="预约状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待确认" value="待确认" />
            <el-option label="已确认" value="已确认" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已取消" value="已取消" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约时间">
          <el-date-picker
            v-model="searchForm.appointTime"
            type="date"
            placeholder="选择日期"
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
      <el-table :data="appointList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="appointTime" label="预约时间" width="120" />
        <el-table-column label="医生姓名" width="120">
          <template #default="{ row }">
            {{ row.doctorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === '待确认'"
              type="danger" 
              size="small" 
              @click="handleCancel(row.id)"
            >
              取消预约
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

    <!-- 新增预约弹窗 -->
    <el-dialog v-model="dialogVisible" title="📅 新增随访预约" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="预约时间" prop="appointTime">
          <el-date-picker
            v-model="form.appointTime"
            type="date"
            placeholder="选择预约日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowLeft, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const searchForm = reactive({
  status: '',
  appointTime: ''
})

const appointList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 当前患者 ID（user.id）
const patientId = ref(null)

// 新增预约相关
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const form = reactive({
  appointTime: '',
  remark: ''
})

const rules = {
  appointTime: [
    { required: true, message: '请选择预约时间', trigger: 'change' }
  ]
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    '待确认': 'warning',
    '已确认': 'primary',
    '已完成': 'success',
    '已取消': 'info'
  }
  return typeMap[status] || ''
}

// 返回主页
const goBack = () => {
  router.push('/patient/dashboard')
}

// 加载预约列表
const loadAppointList = async () => {
  loading.value = true
  try {
    if (!patientId.value) {
      ElMessage.error('未找到患者信息')
      return
    }

    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    
    console.log('=== 请求参数 ===', params)
    // ✅ 调用患者专属接口
    const res = await request.get(`/followup/appoint/my/${patientId.value}`, { params })
    console.log('=== 响应数据 ===', res)
    
    if (res.code === 200 && res.data) {
      appointList.value = res.data.records || []
      total.value = res.data.total || 0
      console.log('=== 预约列表 ===', appointList.value)
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
  loadAppointList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.status = ''
  searchForm.appointTime = ''
  currentPage.value = 1
  loadAppointList()
}

// 新增预约
const handleAdd = async () => {
  // ✅ 先获取患者信息，确认有责任医生
  const userStr = sessionStorage.getItem('userInfo')
  if (!userStr) {
    ElMessage.error('未找到用户信息')
    return
  }
  
  const user = JSON.parse(userStr)
  
  try {
    // 查询患者档案，获取责任医生 ID
    const patientRes = await request.get('/patient/list', {
      params: {
        page: 1,
        size: 100
      }
    })
    
    if (patientRes.code === 200 && patientRes.data && patientRes.data.records) {
      const patientRecord = patientRes.data.records.find(p => p.userId === user.id)
      
      if (!patientRecord || !patientRecord.doctorId) {
        ElMessage.warning('您还没有分配责任医生，无法预约')
        return
      }
      
      console.log('✅ 责任医生 ID:', patientRecord.doctorId)
    }
  } catch (error) {
    console.error('查询患者信息失败', error)
  }
  
  // 打开弹窗
  form.appointTime = ''
  form.remark = ''
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
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
      
      // ✅ 先查询患者档案，获取责任医生 ID
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
      
      const patientRecord = patientRes.data.records.find(p => p.userId === user.id)
      
      if (!patientRecord) {
        ElMessage.error('未找到患者档案')
        return
      }
      
      if (!patientRecord.doctorId) {
        ElMessage.warning('您还没有分配责任医生，无法预约')
        return
      }
      
      // ✅ 构建预约数据
      const data = {
        patientId: user.id,           // 患者 ID（user.id）
        doctorId: patientRecord.doctorId,  // 医生 ID（user.id）
        appointTime: form.appointTime,
        status: '待确认',
        remark: form.remark || ''
      }
      
      console.log('=== 提交预约数据 ===', data)
      const res = await request.post('/followup/appoint', data)
      console.log('=== 响应 ===', res)
      
      if (res.code === 200) {
        ElMessage.success('预约成功')
        dialogVisible.value = false
        loadAppointList()
      } else {
        ElMessage.error(res.msg || '预约失败')
      }
    } catch (error) {
      console.error('预约失败', error)
      ElMessage.error('预约失败：' + (error.message || '未知错误'))
    } finally {
      submitLoading.value = false
    }
  })
}

// 取消预约
const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await request.put(`/followup/appoint/${id}/cancel`)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      loadAppointList()
    } else {
      ElMessage.error(res.msg || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败', error)
      ElMessage.error('取消失败')
    }
  }
}

// 分页处理
const handleSizeChange = () => {
  loadAppointList()
}

const handleCurrentChange = () => {
  loadAppointList()
}

// 初始化
onMounted(async () => {
  const userStr = sessionStorage.getItem('userInfo')
  console.log('=== sessionStorage userInfo ===', userStr)
  
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      console.log('=== 解析后的用户对象 ===', user)
      
      // ✅ 直接使用 user.id 作为 patientId
      patientId.value = user.id
      
      console.log('=== 最终使用的患者 ID ===', patientId.value)
      
      if (!patientId.value) {
        ElMessage.error('未找到患者信息')
        return
      }
      
      await loadAppointList()
    } catch (error) {
      console.error('获取患者 ID 失败', error)
      ElMessage.error('获取患者信息失败')
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
.my-patient-appoints {
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
