<template>
  <div class="my-appointments">
    <div class="header">
      <div class="header-left">
        <el-button type="info" @click="goBack" style="margin-right: 16px;">
          <el-icon><ArrowLeft /></el-icon>
          返回主页
        </el-button>
        <h1>📅 我的预约</h1>
      </div>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="患者姓名">
          <el-input v-model="searchForm.patientName" placeholder="请输入患者姓名" clearable />
        </el-form-item>
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
        <el-table-column label="患者姓名" width="120">
          <template #default="{ row }">
            <span>{{ row.patientName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="手机号" width="130">
          <template #default="{ row }">
            <span>{{ row.patientPhone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="appointTime" label="预约时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === '待确认'" 
              type="success" 
              size="small" 
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
            <el-button 
              v-if="row.status === '待确认' || row.status === '已确认'" 
              type="warning" 
              size="small" 
              @click="handleComplete(row)"
            >
              完成
            </el-button>
            <el-button 
              v-if="row.status !== '已完成' && row.status !== '已取消'" 
              type="danger" 
              size="small" 
              @click="handleCancel(row)"
            >
              取消
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const searchForm = reactive({
  patientName: '',
  status: '',
  appointTime: ''
})

const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const doctorId = ref(null)

// 获取状态对应的标签类型
const getStatusType = (status) => {
  const map = { 
    '待确认': 'warning', 
    '已确认': 'primary', 
    '已完成': 'success', 
    '已取消': 'info' 
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
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm,
      doctorId: doctorId.value
    }
    
    console.log('=== 请求参数 ===', params)
    const res = await request.get('/doctor/appointments/my', { params })
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
  searchForm.status = ''
  searchForm.appointTime = ''
  currentPage.value = 1
  loadData()
}

// 确认预约
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要确认这条预约吗？`,
      '确认预约',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await request.put(`/doctor/appointment/${row.id}/confirm`)
    if (res.code === 200) {
      ElMessage.success('确认成功')
      loadData()
    } else {
      ElMessage.error(res.msg || '确认失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认失败')
    }
  }
}

// 完成预约
const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要标记这条预约为已完成吗？`,
      '完成预约',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await request.put(`/doctor/appointment/${row.id}/complete`)
    if (res.code === 200) {
      ElMessage.success('操作成功')
      loadData()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 取消预约
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消这条预约吗？`,
      '取消预约',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await request.put(`/doctor/appointment/${row.id}/cancel`)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      loadData()
    } else {
      ElMessage.error(res.msg || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
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
      console.log('=== user.id ===', user.id)
      console.log('=== user.userType ===', user.userType)
      console.log('=== user.doctorId ===', user.doctorId)
      
      // ✅ 现在 Login.vue 已经确保 doctorId 存储的是 user.id
      // 直接使用 doctorId 或 id 字段即可
      doctorId.value = user.doctorId || user.id
      
      console.log('=== 最终使用的医生 ID (应该是 user.id，例如 2) ===', doctorId.value)
      
      if (!doctorId.value) {
        ElMessage.error('未找到医生信息')
        return
      }
      
      // 验证一下是否是正确的 ID（应该是 user.id 格式）
      if (doctorId.value === 1) {
        console.warn('⚠️ 警告：医生 ID 是 1，这可能是 doctor 表的主键，应该是 user.id（例如 2）')
        console.warn('⚠️ 请重新登录以确保获取正确的用户信息')
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
.my-appointments {
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
