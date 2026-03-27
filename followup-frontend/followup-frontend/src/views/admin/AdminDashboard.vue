<template>
  <div class="sysUser-dashboard">
    <div class="header">
      <h1>系统管理后台</h1>
      <p class="welcome">欢迎回来，{{ userInfo.realName }}管理员！</p>
    </div>

    <!-- 系统概览统计 -->
    <div class="stats-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <h3>{{ stats.totalUser }}</h3>
          <p>系统总用户</p>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">👨‍⚕️</div>
        <div class="stat-content">
          <h3>{{ stats.doctorCount }}</h3>
          <p>注册医生</p>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon">🧑‍</div>
        <div class="stat-content">
          <h3>{{ stats.patientCount }}</h3>
          <p>在管患者</p>
        </div>
      </el-card>
    </div>

    <!-- 随访管理快捷入口 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <span>📋 管理控制台</span>
      </template>
      <div class="quick-actions">
        <el-button type="primary" size="large" @click="goTo('/followup/appoint')">
          <el-icon><Calendar /></el-icon>
          随访预约
        </el-button>
        <el-button type="success" size="large" @click="goTo('/followup/plan')">
          <el-icon><Document /></el-icon>
          随访计划
        </el-button>
        <el-button type="warning" size="large" @click="goTo('/followup/record')">
          <el-icon><Notebook /></el-icon>
          随访记录
        </el-button>
        <el-button type="info" size="large" @click="goTo('/patient/manage')">
          <el-icon><User /></el-icon>
          患者管理
        </el-button>
        <el-button type="success" size="large" @click="goTo('/doctor/manage')">
          <el-icon><UserFilled /></el-icon>
          医生管理
        </el-button>
      </div>
    </el-card>

    <!-- 最近注册用户 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>👥 最近注册用户</span>
          <div>
            <el-button type="success" size="small" @click="showRegisterDialog">
              <el-icon><UserFilled /></el-icon>
              注册用户
            </el-button>
            <el-button type="primary" size="small" @click="showAllUsersDialog">查看全部</el-button>
          </div>
        </div>
      </template>
      <el-table :data="recentUsers" stripe style="width: 100%">
        <el-table-column prop="id" label="用户 ID" width="80" />
        <el-table-column prop="username" label="登录账号" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="userType" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.userType)">{{ getRoleText(row.userType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditUser(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 所有用户弹窗 -->
    <el-dialog
      v-model="allUsersDialogVisible"
      title="👥 用户管理"
      width="90%"
      :close-on-click-modal="false"
    >
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.userType" placeholder="请选择角色" clearable style="width: 120px">
            <el-option label="管理员" :value="1" />
            <el-option label="医生" :value="2" />
            <el-option label="患者" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 用户列表 -->
      <el-table :data="allUsers" stripe style="width: 100%" max-height="500">
        <el-table-column prop="id" label="用户 ID" width="80" />
        <el-table-column prop="username" label="登录账号" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="userType" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.userType)">{{ getRoleText(row.userType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditUser(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteUser(row)">删除</el-button>
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
    </el-dialog>

    <!-- 注册用户弹窗 -->
    <el-dialog
      v-model="registerDialogVisible"
      title="👤 注册用户"
      width="600px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="120px"
      >
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入登录密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="userType">
          <el-select v-model="registerForm.userType" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" :value="1" />
            <el-option label="医生" :value="2" />
            <el-option label="患者" :value="3" />
          </el-select>
        </el-form-item>
        
        <!-- 医生专属字段 -->
        <template v-if="registerForm.userType === 2">
          <el-form-item label="所属科室" prop="department">
            <el-input v-model="registerForm.department" placeholder="例如：心血管内科、神经内科等" />
          </el-form-item>
          <el-form-item label="擅长技能" prop="skill">
            <el-input v-model="registerForm.skill" placeholder="例如：高血压、糖尿病等" />
          </el-form-item>
          <el-form-item label="所属社区" prop="community">
            <el-input v-model="registerForm.community" placeholder="例如：阳光社区、幸福社区等" />
          </el-form-item>
        </template>
        
        <!-- 患者专属字段 -->
        <template v-if="registerForm.userType === 3">
          <el-form-item label="慢病类型" prop="chronicType">
            <el-select v-model="registerForm.chronicType" placeholder="请选择慢病类型" style="width: 100%">
              <el-option label="高血压" value="高血压" />
              <el-option label="糖尿病" value="糖尿病" />
              <el-option label="冠心病" value="冠心病" />
              <el-option label="脑卒中" value="脑卒中" />
              <el-option label="慢阻肺" value="慢阻肺" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="年龄" prop="age">
            <el-input-number v-model="registerForm.age" :min="1" :max="150" style="width: 100%" />
          </el-form-item>
          <el-form-item label="地址" prop="address">
            <el-input v-model="registerForm.address" placeholder="请输入详细住址" />
          </el-form-item>
          <el-form-item label="紧急联系人" prop="emergencyContact">
            <el-input v-model="registerForm.emergencyContact" placeholder="请输入紧急联系人姓名" />
          </el-form-item>
          <el-form-item label="紧急联系电话" prop="emergencyPhone">
            <el-input v-model="registerForm.emergencyPhone" placeholder="请输入紧急联系电话" />
          </el-form-item>
        </template>
        
        <el-form-item label="状态" prop="status">
          <el-switch v-model="registerForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="registerDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleRegister" :loading="registerLoading">
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
import { User, DataLine, Setting, Document, UserFilled, Search, Calendar, Notebook } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 新增加载状态
const loading = ref(true);

// 用户信息
const userInfo = reactive({
  realName: ''
})

// 统计数据
const stats = reactive({
  totalUser: 0,
  doctorCount: 0,
  patientCount: 0
})

// 最近注册用户
const recentUsers = ref([])

// 所有用户弹窗
const allUsersDialogVisible = ref(false)
const allUsers = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  username: '',
  realName: '',
  userType: null,
  phone: ''
})

// 注册用户弹窗
const registerDialogVisible = ref(false)
const registerFormRef = ref(null)
const registerLoading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  userType: 2,
  status: 1,
  // 医生专属字段
  department: '',
  skill: '',
  community: '',
  // 患者专属字段
  chronicType: '',
  age: 0,
  address: '',
  emergencyContact: '',
  emergencyPhone: ''
})

const registerRules = {
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
  userType: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  department: [
    { required: true, message: '请输入所属科室', trigger: 'blur', validator: (rule, value, callback) => {
      if (registerForm.userType === 2 && !value) {
        callback(new Error('医生必须填写科室'))
      } else {
        callback()
      }
    }}
  ],
  skill: [
    { required: true, message: '请输入擅长技能', trigger: 'blur', validator: (rule, value, callback) => {
      if (registerForm.userType === 2 && !value) {
        callback(new Error('医生必须填写擅长技能'))
      } else {
        callback()
      }
    }}
  ],
  community: [
    { required: true, message: '请输入所属社区', trigger: 'blur', validator: (rule, value, callback) => {
      if (registerForm.userType === 2 && !value) {
        callback(new Error('医生必须填写所属社区'))
      } else {
        callback()
      }
    }}
  ],
  chronicType: [
    { required: true, message: '请输入慢病类型', trigger: 'blur', validator: (rule, value, callback) => {
      if (registerForm.userType === 3 && !value) {
        callback(new Error('患者必须填写慢病类型'))
      } else {
        callback()
      }
    }}
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur', validator: (rule, value, callback) => {
      if (registerForm.userType === 3 && (!value || value < 1 || value > 150)) {
        callback(new Error('患者年龄必须在 1-150 之间'))
      } else {
        callback()
      }
    }}
  ],
  address: [
    { required: true, message: '请输入地址', trigger: 'blur', validator: (rule, value, callback) => {
      if (registerForm.userType === 3 && !value) {
        callback(new Error('患者必须填写地址'))
      } else {
        callback()
      }
    }}
  ],
  emergencyContact: [
    { required: true, message: '请输入紧急联系人', trigger: 'blur', validator: (rule, value, callback) => {
      if (registerForm.userType === 3 && !value) {
        callback(new Error('患者必须填写紧急联系人'))
      } else {
        callback()
      }
    }}
  ],
  emergencyPhone: [
    { required: true, message: '请输入紧急联系电话', trigger: 'blur', validator: (rule, value, callback) => {
      if (registerForm.userType === 3 && !value) {
        callback(new Error('患者必须填写紧急联系电话'))
      } else {
        callback()
      }
    }}
  ]
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 获取角色类型
const getRoleType = (userType) => {
  const map = { 1: 'danger', 2: 'primary', 3: 'success' }
  return map[userType] || 'info'
}

// 获取角色文本
const getRoleText = (userType) => {
  const map = { 1: '管理员', 2: '医生', 3: '患者' }
  return map[userType] || '未知'
}

// 路由跳转
const goTo = (path) => {
  router.push(path)
}

// 用户操作
const handleEditUser = (row) => {
  ElMessage.info(`编辑用户：${row.realName}`)
}
const handleDeleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户「${row.realName}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用后端删除接口
    const res = await request.delete(`/admin/user/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData() // 刷新列表
      loadAllUsers() // 刷新全部用户列表
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败，请重试')
    } else {
      ElMessage.info('已取消删除')
    }
  }
}

// 显示注册弹窗
const showRegisterDialog = () => {
  registerDialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (registerFormRef.value) {
    registerFormRef.value.resetFields()
  }
  registerForm.username = ''
  registerForm.password = ''
  registerForm.realName = ''
  registerForm.phone = ''
  registerForm.userType = 2
  registerForm.status = 1
  registerForm.department = ''
  registerForm.skill = ''
  registerForm.community = ''
  registerForm.chronicType = ''
  registerForm.age = 0
  registerForm.address = ''
  registerForm.emergencyContact = ''
  registerForm.emergencyPhone = ''
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      registerLoading.value = true
      try {
        const res = await request.post('/admin/user', registerForm)
        if (res.code === 200) {
          ElMessage.success('注册成功')
          registerDialogVisible.value = false
          resetForm()
          loadData() // 刷新列表
          loadAllUsers() // 刷新全部用户列表
        } else {
          ElMessage.error(res.msg || '注册失败')
        }
      } catch (error) {
        ElMessage.error('注册失败，请重试')
      } finally {
        registerLoading.value = false
      }
    }
  })
}

// 显示所有用户弹窗
const showAllUsersDialog = async () => {
  allUsersDialogVisible.value = true
  await loadAllUsers()
}

// 加载所有用户
const loadAllUsers = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    const res = await request.get('/admin/user/list', { params })
    if (res.code === 200) {
      allUsers.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadAllUsers()
}

// 重置搜索
const resetSearch = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.userType = null
  searchForm.phone = ''
  currentPage.value = 1
  loadAllUsers()
}

// 分页处理
const handleSizeChange = () => {
  loadAllUsers()
}

const handleCurrentChange = () => {
  loadAllUsers()
}

// 加载数据
const loadData = async () => {
  try {
    console.log("=== 请求管理员页面数据 ===");
    // 1. 请求统计数据
    const statsRes = await request.get("/admin/stats");
    console.log("统计数据响应:", statsRes);
    if (statsRes.code === 200 && statsRes.data) {
      // 正确映射后端返回的字段
      stats.totalUser = statsRes.data.totalUser || 0;
      stats.doctorCount = statsRes.data.doctorCount || 0;
      stats.patientCount = statsRes.data.patientCount || 0;
    } else {
      throw new Error(statsRes.msg || "获取统计数据失败");
    }

    // 2. 请求最近用户数据
    const usersRes = await request.get("/admin/recentUser");
    console.log("最近用户响应:", usersRes);
    if (usersRes.code === 200) {
      recentUsers.value = usersRes.data || [];
    } else {
      throw new Error(usersRes.msg || "获取最近用户失败");
    }
  } catch (err) {
    console.error("加载数据失败:", err);
    ElMessage.error("加载数据失败，请检查后端服务");
  }
};

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.sysUser-dashboard {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  margin-bottom: 24px;
}

.header h1 {
  margin: 0 0 8px 0;
  color: #1a1a1a;
}

.welcome {
  margin: 0;
  color: #666;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 20px;
}

.stat-content h3 {
  margin: 0 0 4px 0;
  color: #1a1a1a;
  font-size: 28px;
}

.stat-content p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.info-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px 40px;
  font-size: 16px;
  height: auto;
}

.search-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
