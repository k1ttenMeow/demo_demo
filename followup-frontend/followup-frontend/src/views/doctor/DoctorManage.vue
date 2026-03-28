<template>
  <div class="doctor-manage">
    <div class="header">
      <div class="header-left">
        <el-button type="info" @click="goBack" style="margin-right: 16px;">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1>👨‍⚕️ 医生管理</h1>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增医生
      </el-button>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="医生姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入医生姓名" clearable />
        </el-form-item>
        <el-form-item label="所属科室">
          <el-input v-model="searchForm.department" placeholder="请输入科室" clearable />
        </el-form-item>
        <el-form-item label="所属社区">
          <el-input v-model="searchForm.community" placeholder="请输入社区" clearable />
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
        <el-table-column prop="realName" label="医生姓名" width="120" />
        <el-table-column prop="username" label="登录账号" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="department" label="所属科室" width="150" />
        <el-table-column prop="skill" label="擅长技能" width="200" show-overflow-tooltip />
        <el-table-column prop="community" label="所属社区" width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
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
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="登录账号" prop="username">
              <el-input v-model="formData.username" placeholder="请输入登录账号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="登录密码" prop="password">
              <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="医生姓名" prop="realName">
              <el-input v-model="formData.realName" placeholder="请输入医生姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属科室" prop="department">
              <el-input v-model="formData.department" placeholder="请输入科室" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称">
              <el-select v-model="formData.title" placeholder="请选择职称" style="width: 100%">
                <el-option label="主治医师" value="主治医师" />
                <el-option label="副主任医师" value="副主任医师" />
                <el-option label="主任医师" value="主任医师" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="擅长技能" prop="skill">
          <el-input
            v-model="formData.skill"
            type="textarea"
            :rows="3"
            placeholder="请输入擅长技能，如：高血压、糖尿病等"
          />
        </el-form-item>

        <el-form-item label="所属社区" prop="community">
          <el-input v-model="formData.community" placeholder="请输入所属社区" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">
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
const formRef = ref(null)

const searchForm = reactive({
  realName: '',
  department: '',
  community: ''
})

const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增医生')
const submitLoading = ref(false)

const formData = reactive({
  id: null,
  userId: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  department: '',
  skill: '',
  community: '',
  title: '',
  status: 1
})

const formRules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入医生姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请输入科室', trigger: 'blur' }
  ],
  community: [
    { required: true, message: '请输入社区', trigger: 'blur' }
  ]
}

// 加载医生列表
const loadDoctorList = async () => {
  loading.value = true
  try {
    const res = await request.get('/doctor/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        realName: searchForm.realName,
        department: searchForm.department,
        community: searchForm.community
      }
    })
    
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载医生列表失败:', error)
    ElMessage.error('加载医生列表失败')
  } finally {
    loading.value = false
  }
}

// 返回管理员首页
const goBack = () => {
  router.push('/admin/dashboard')
}

const handleAdd = () => {
  dialogTitle.value = '新增医生'
  dialogVisible.value = true
}

const handleSearch = () => {
  currentPage.value = 1
  loadDoctorList()
}

const resetSearch = () => {
  searchForm.realName = ''
  searchForm.department = ''
  searchForm.community = ''
  currentPage.value = 1
  loadDoctorList()
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑医生'
  formData.id = row.id
  formData.userId = row.userId
  formData.username = row.username || ''
  formData.password = '' // 编辑时不显示密码
  formData.realName = row.realName || ''
  formData.phone = row.phone || ''
  formData.department = row.department || ''
  formData.skill = row.skill || ''
  formData.community = row.community || ''
  formData.title = row.title || ''
  formData.status = row.status !== undefined ? row.status : 1
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除医生「${row.realName}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await request.delete(`/doctor/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadDoctorList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSizeChange = () => {
  loadDoctorList()
}

const handleCurrentChange = () => {
  loadDoctorList()
}

const resetForm = () => {
  formData.id = null
  formData.userId = null
  formData.username = ''
  formData.password = ''
  formData.realName = ''
  formData.phone = ''
  formData.department = ''
  formData.skill = ''
  formData.community = ''
  formData.title = ''
  formData.status = 1
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      let res
      if (formData.id) {
        // 更新
        res = await request.put('/doctor', formData)
      } else {
        // 新增
        res = await request.post('/doctor', formData)
      }
      
      if (res.code === 200) {
        ElMessage.success(formData.id ? '更新成功' : '新增成功')
        dialogVisible.value = false
        loadDoctorList()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

onMounted(() => {
  loadDoctorList()
})
</script>

<style scoped>
.doctor-manage {
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
