<template>
  <div class="my-patient-records">
    <div class="header">
      <div class="header-left">
        <el-button type="info" @click="goBack" style="margin-right: 16px;">
          <el-icon><ArrowLeft /></el-icon>
          返回主页
        </el-button>
        <h1>📊 我的随访记录</h1>
      </div>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="随访时间">
          <el-date-picker
            v-model="searchForm.followTime"
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
      <el-table :data="recordList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="医生姓名" width="120">
          <template #default="{ row }">
            {{ row.doctorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="followTime" label="随访时间" width="120">
          <template #default="{ row }">
            {{ formatDate(row.followTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="bloodPressure" label="血压" width="120" />
        <el-table-column prop="bloodSugar" label="血糖" width="120" />
        <el-table-column prop="drug" label="用药情况" width="150" show-overflow-tooltip />
        <el-table-column prop="symptom" label="症状" width="150" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showDetail(row)">
              详情
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="📋 随访记录详情" width="600px">
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="随访时间">{{ formatDate(currentRecord.followTime) }}</el-descriptions-item>
        <el-descriptions-item label="医生姓名">{{ currentRecord.doctorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="血压">{{ currentRecord.bloodPressure || '-' }}</el-descriptions-item>
        <el-descriptions-item label="血糖">{{ currentRecord.bloodSugar || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用药情况" :span="2">{{ currentRecord.drug || '-' }}</el-descriptions-item>
        <el-descriptions-item label="症状" :span="2">{{ currentRecord.symptom || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRecord.remark || '-' }}</el-descriptions-item>
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
  followTime: ''
})

const recordList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 当前患者 ID（user.id）
const patientId = ref(null)

// 详情弹窗相关
const detailVisible = ref(false)
const currentRecord = ref(null)

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  // 如果是完整的日期时间格式，只取日期部分
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 返回主页
const goBack = () => {
  router.push('/patient/dashboard')
}

// 加载记录列表
const loadRecordList = async () => {
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
    const res = await request.get(`/followup/record/my/${patientId.value}`, { params })
    console.log('=== 响应数据 ===', res)
    
    if (res.code === 200 && res.data) {
      recordList.value = res.data.records || []
      total.value = res.data.total || 0
      console.log('=== 记录列表 ===', recordList.value)
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
  loadRecordList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.followTime = ''
  currentPage.value = 1
  loadRecordList()
}

// 显示详情
const showDetail = (row) => {
  currentRecord.value = row
  detailVisible.value = true
}

// 分页处理
const handleSizeChange = () => {
  loadRecordList()
}

const handleCurrentChange = () => {
  loadRecordList()
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
      
      await loadRecordList()
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
.my-patient-records {
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
