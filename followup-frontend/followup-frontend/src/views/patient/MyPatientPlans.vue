<template>
  <div class="my-patient-plans">
    <div class="header">
      <div class="header-left">
        <el-button type="info" @click="goBack" style="margin-right: 16px;">
          <el-icon><ArrowLeft /></el-icon>
          返回主页
        </el-button>
        <h1>📝 我的随访计划</h1>
      </div>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="计划状态">
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
      <el-table :data="planList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="planType" label="计划类型" width="120" />
        <el-table-column label="医生姓名" width="120">
          <template #default="{ row }">
            {{ row.doctorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="cycle" label="周期" width="120" />
        <el-table-column prop="nextTime" label="下次随访" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
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
  status: ''
})

const planList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 当前患者 ID（user.id）
const patientId = ref(null)

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    '待执行': 'warning',
    '执行中': 'primary',
    '已完成': 'success'
  }
  return typeMap[status] || ''
}

// 返回主页
const goBack = () => {
  router.push('/patient/dashboard')
}

// 加载计划列表
const loadPlanList = async () => {
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
    const res = await request.get(`/followup/plan/my/${patientId.value}`, { params })
    console.log('=== 响应数据 ===', res)
    
    if (res.code === 200 && res.data) {
      planList.value = res.data.records || []
      total.value = res.data.total || 0
      console.log('=== 计划列表 ===', planList.value)
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
  loadPlanList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.status = ''
  currentPage.value = 1
  loadPlanList()
}

// 分页处理
const handleSizeChange = () => {
  loadPlanList()
}

const handleCurrentChange = () => {
  loadPlanList()
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
      
      await loadPlanList()
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
.my-patient-plans {
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
