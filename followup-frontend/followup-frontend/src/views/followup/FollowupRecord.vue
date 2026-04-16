<template>
  <div class="followup-record">
    <div class="header">
      <div class="header-left">
        <el-button type="info" @click="goBack" style="margin-right: 16px;">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1>📓 随访记录管理</h1>
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
        <el-form-item label="医生姓名">
          <el-input v-model="searchForm.doctorName" placeholder="请输入医生姓名" clearable />
        </el-form-item>
        <el-form-item label="随访时间">
          <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px"
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
        <el-table-column prop="doctorName" label="医生姓名" width="120" />
        <el-table-column prop="bloodPressure" label="血压" width="100" />
        <el-table-column prop="bloodSugar" label="血糖" width="100" />
        <el-table-column prop="drug" label="用药情况" width="200" show-overflow-tooltip />
        <el-table-column prop="symptom" label="症状" width="200" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" width="200" show-overflow-tooltip />
        <el-table-column prop="followTime" label="随访时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="700px"
        :close-on-click-modal="false"
        @close="resetForm"
    >
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
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
          </el-col>
          <el-col :span="12">
            <el-form-item label="医生" prop="doctorId">
              <el-autocomplete
                  v-model="doctorNameInput"
                  :fetch-suggestions="searchDoctor"
                  placeholder="请输入医生姓名"
                  style="width: 100%"
                  clearable
                  @select="handleDoctorSelect"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="血压" prop="bloodPressure">
              <el-input v-model="form.bloodPressure" placeholder="例如：135/85" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="血糖" prop="bloodSugar">
              <el-input v-model="form.bloodSugar" placeholder="例如：6.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="用药情况" prop="drug">
          <el-input v-model="form.drug" type="textarea" :rows="2" placeholder="请输入用药情况" />
        </el-form-item>

        <el-form-item label="症状" prop="symptom">
          <el-input v-model="form.symptom" type="textarea" :rows="3" placeholder="请输入症状描述" />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const searchForm = reactive({
  patientName: '',
  doctorName: '',
  dateRange: []
})

const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('新增记录')
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  planId: null,
  patientId: null,
  doctorId: null,
  bloodPressure: '',
  bloodSugar: '',
  drug: '',
  symptom: '',
  remark: '',
  followTime: ''
})

const patientNameInput = ref('')
const doctorNameInput = ref('')

const rules = {
  patientId: [
    { required: true, message: '请选择患者', trigger: 'change' }
  ],
  doctorId: [
    { required: true, message: '请选择医生', trigger: 'change' }
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

// 返回管理员首页
const goBack = () => {
  router.push('/admin/dashboard')
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      patientName: searchForm.patientName,
      doctorName: searchForm.doctorName,
      startDate: searchForm.dateRange?.[0] || null,
      endDate: searchForm.dateRange?.[1] || null
    }
    const res = await request.get('/followup/record/admin/list', { params })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0

      // 处理数据，添加患者和医生姓名（后端已返回，只需处理空值）
      enrichRecordData()
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 丰富记录数据（添加姓名）
const enrichRecordData = () => {
  // 后端已经返回了 patientName 和 doctorName，无需额外查询
  // 只需要处理可能的空值
  tableData.value.forEach(item => {
    if (!item.patientName) {
      item.patientName = '未知患者'
    }
    if (!item.doctorName) {
      item.doctorName = '未知医生'
    }
  })
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
        id: p.id
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

// 搜索医生（自动补全）
const searchDoctor = async (queryString, cb) => {
  try {
    const res = await request.get('/doctor/list', { 
      params: { page: 1, size: 100 } 
    })
    if (res.code === 200) {
      const doctors = res.data.records || []
      console.log('获取到的医生列表:', doctors) // 添加调试日志
      const results = queryString
        ? doctors.filter(d => d.realName && d.realName.includes(queryString))
        : doctors
      
      // 转换为 el-autocomplete 需要的格式
      const formattedResults = results.map(d => ({
        value: d.realName,
        id: d.id
      }))
      
      console.log('格式化后的医生建议:', formattedResults) // 添加调试日志
      cb(formattedResults)
    } else {
      cb([])
    }
  } catch (error) {
    console.error('搜索医生失败', error)
    cb([])
  }
}

// 患者选择处理
const handlePatientSelect = (item) => {
  form.patientId = item.id
  patientNameInput.value = item.value
}

// 医生选择处理
const handleDoctorSelect = (item) => {
  form.doctorId = item.id
  doctorNameInput.value = item.value
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const resetSearch = () => {
  searchForm.patientName = ''
  searchForm.doctorName = ''
  searchForm.dateRange = []
  currentPage.value = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增随访记录'
  dialogVisible.value = true
  // 设置默认随访时间为当前时间
  form.followTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
  patientNameInput.value = ''
  doctorNameInput.value = ''
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑随访记录'
  form.id = row.id
  form.planId = row.planId
  form.patientId = row.patientId
  form.doctorId = row.doctorId
  form.bloodPressure = row.bloodPressure || ''
  form.bloodSugar = row.bloodSugar || ''
  form.drug = row.drug || ''
  form.symptom = row.symptom || ''
  form.remark = row.remark || ''
  form.followTime = row.followTime

  // 设置输入框的值
  patientNameInput.value = row.patientName || ''
  doctorNameInput.value = row.doctorName || ''

  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除这条随访记录吗？`,
        '删除确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const res = await request.delete(`/followup/record/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (form.id) {
          // 编辑
          res = await request.put('/followup/record', form)
        } else {
          // 新增
          res = await request.post('/followup/record', form)
        }

        if (res.code === 200) {
          ElMessage.success(form.id ? '更新成功' : '新增成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.id = null
  form.planId = null
  form.patientId = null
  form.doctorId = null
  form.bloodPressure = ''
  form.bloodSugar = ''
  form.drug = ''
  form.symptom = ''
  form.remark = ''
  form.followTime = ''
  patientNameInput.value = ''
  doctorNameInput.value = ''
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.followup-record {
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
