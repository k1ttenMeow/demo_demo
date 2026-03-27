<template>
  <div class="patient-manage">
    <div class="header">
      <div class="header-left">
        <el-button type="info" @click="goBack" style="margin-right: 16px;">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1>👤 患者管理</h1>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增患者
      </el-button>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="患者姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入患者姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="慢病类型">
          <el-select v-model="searchForm.chronicType" placeholder="请选择慢病类型" clearable>
            <el-option label="高血压" value="高血压" />
            <el-option label="糖尿病" value="糖尿病" />
            <el-option label="冠心病" value="冠心病" />
            <el-option label="脑卒中" value="脑卒中" />
            <el-option label="慢阻肺" value="慢阻肺" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="责任医生">
          <el-input v-model="searchForm.doctorName" placeholder="请输入医生姓名" clearable />
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
            <span>{{ row.realName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="手机号" width="130">
          <template #default="{ row }">
            <span>{{ row.phone }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="chronicType" label="慢病类型" width="120" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="address" label="地址" width="200" show-overflow-tooltip />
        <el-table-column prop="emergencyContact" label="紧急联系人" width="120" />
        <el-table-column prop="emergencyPhone" label="紧急电话" width="130" />
        <el-table-column label="责任医生" width="120">
          <template #default="{ row }">
            <span>{{ row.doctorName }}</span>
          </template>
        </el-table-column>
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
            <el-form-item label="登录账号" prop="username">
              <el-input v-model="form.username" placeholder="请输入登录账号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="登录密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="慢病类型" prop="chronicType">
              <el-select v-model="form.chronicType" placeholder="请选择慢病类型" style="width: 100%">
                <el-option label="高血压" value="高血压" />
                <el-option label="糖尿病" value="糖尿病" />
                <el-option label="冠心病" value="冠心病" />
                <el-option label="脑卒中" value="脑卒中" />
                <el-option label="慢阻肺" value="慢阻肺" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="1" :max="150" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细住址" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="紧急联系人" prop="emergencyContact">
              <el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急联系电话" prop="emergencyPhone">
              <el-input v-model="form.emergencyPhone" placeholder="请输入紧急联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="责任医生" prop="doctorId">
          <el-select v-model="form.doctorId" placeholder="请选择责任医生" style="width: 100%">
            <el-option
              v-for="item in doctorList"
              :key="item.id"
              :label="item.realName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
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
  realName: '',
  phone: '',
  chronicType: '',
  doctorName: ''
})

const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('新增患者')
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  userId: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  chronicType: '',
  age: 0,
  address: '',
  emergencyContact: '',
  emergencyPhone: '',
  doctorId: null,
  status: 1
})

const doctorList = ref([])

const rules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  chronicType: [
    { required: true, message: '请选择慢病类型', trigger: 'change' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入地址', trigger: 'blur' }
  ],
  emergencyContact: [
    { required: true, message: '请输入紧急联系人', trigger: 'blur' }
  ],
  emergencyPhone: [
    { required: true, message: '请输入紧急联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  doctorId: [
    { required: true, message: '请选择责任医生', trigger: 'change' }
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
      ...searchForm
    }
    const res = await request.get('/patient/list', { params })
    console.log('后端返回的原始数据:', res)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
      console.log('表格数据:', tableData.value)
      if (tableData.value.length > 0) {
        console.log('第一条记录的 realName:', tableData.value[0].realName)
        console.log('第一条记录的 phone:', tableData.value[0].phone)
        console.log('第一条记录的 doctorName:', tableData.value[0].doctorName)
      }
    }
  } catch (error) {
    console.error('加载数据异常:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载医生列表
const loadDoctorList = async () => {
  try {
    const res = await request.get('/doctor/list', { params: { page: 1, size: 100 } })
    if (res.code === 200) {
      doctorList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载医生列表失败', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const resetSearch = () => {
  searchForm.realName = ''
  searchForm.phone = ''
  searchForm.chronicType = ''
  searchForm.doctorName = ''
  currentPage.value = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增患者'
  dialogVisible.value = true
  // 加载下拉框数据
  loadDoctorList()
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑患者'
  form.id = row.id
  form.userId = row.userId
  form.username = row.username || ''
  form.password = '' // 编辑时不显示密码
  form.realName = row.realName || ''
  form.phone = row.phone || ''
  form.chronicType = row.chronicType || ''
  form.age = row.age || 0
  form.address = row.address || ''
  form.emergencyContact = row.emergencyContact || ''
  form.emergencyPhone = row.emergencyPhone || ''
  form.doctorId = row.doctorId || null
  form.status = row.status !== undefined ? row.status : 1
  dialogVisible.value = true
  
  // 加载下拉框数据
  loadDoctorList()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除患者「${row.realName}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await request.delete(`/patient/${row.id}`)
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
          res = await request.put('/patient', form)
        } else {
          // 新增
          res = await request.post('/patient', form)
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
  form.userId = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.phone = ''
  form.chronicType = ''
  form.age = 0
  form.address = ''
  form.emergencyContact = ''
  form.emergencyPhone = ''
  form.doctorId = null
  form.status = 1
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
.patient-manage {
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
