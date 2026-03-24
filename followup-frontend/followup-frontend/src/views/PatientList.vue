<template>
  <div class="patient-list-container">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon size="28"><UserFilled /></el-icon>
        患者管理
      </h1>
      <p class="page-desc">社区慢病随访患者信息列表</p>
    </div>

    <el-card class="filter-card" shadow="hover">
      <el-form :model="filterForm" inline @submit.prevent>
        <el-form-item label="患者姓名">
          <el-input
            v-model="filterForm.name"
            placeholder="请输入姓名"
            clearable
            size="large"
            class="filter-input"
          />
        </el-form-item>
        <el-form-item label="慢病类型">
          <el-select
            v-model="filterForm.diseaseType"
            placeholder="请选择慢病类型"
            clearable
            size="large"
            class="filter-select"
          >
            <el-option label="高血压" value="hypertension" />
            <el-option label="糖尿病" value="diabetes" />
            <el-option label="冠心病" value="chd" />
            <el-option label="脑卒中" value="stroke" />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-btn-group">
          <el-button
            type="primary"
            @click="handleSearch"
            size="large"
            class="search-btn"
            :loading="isSearchLoading"
          >
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button
            @click="handleResetFilter"
            size="large"
            class="reset-btn"
          >
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="operate-btn-group">
      <el-button
        type="primary"
        size="large"
        class="add-btn"
        @click="handleAddPatient"
      >
        <el-icon><Plus /></el-icon>
        新增患者
      </el-button>
      <el-button
        type="success"
        size="large"
        class="export-btn"
        @click="handleExport"
        :loading="isExportLoading"
      >
        <el-icon><Download /></el-icon>
        导出数据
      </el-button>
    </div>

    <el-card class="list-card" shadow="hover">
      <div class="list-content">
        <div v-if="isTableLoading" class="loading-container">
          <el-skeleton :rows="8" animated class="skeleton-loading" />
        </div>

        <div v-else-if="patientList.length === 0" class="empty-container">
          <el-empty description="暂无患者数据" :image-size="120">
            <el-button type="primary" @click="handleSearch">重新加载</el-button>
          </el-empty>
        </div>

        <el-table
          v-else
          :data="patientList"
          border
          stripe
          size="large"
          class="patient-table"
          :header-cell-style="{ background: '#f7fafc', borderRadius: '8px' }"
          empty-text="暂无数据"
        >
          <el-table-column type="selection" width="60" align="center" />
          <el-table-column prop="name" label="患者姓名" min-width="100" align="center" />
          <el-table-column prop="gender" label="性别" width="80" align="center">
            <template #default="scope">{{ scope.row.gender === 'male' ? '男' : '女' }}</template>
          </el-table-column>
          <el-table-column prop="age" label="年龄" width="80" align="center" />
          <el-table-column prop="phone" label="联系电话" min-width="120" align="center" />
          <el-table-column prop="diseaseType" label="慢病类型" min-width="100" align="center">
            <template #default="scope">
              <el-tag size="small" :type="getDiseaseTagType(scope.row.diseaseType)">
                {{ scope.row.diseaseType === 'hypertension' ? '高血压' : scope.row.diseaseType === 'diabetes' ? '糖尿病' : scope.row.diseaseType === 'chd' ? '冠心病' : '脑卒中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="followStatus" label="随访状态" min-width="100" align="center">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.followStatus === 'pending' ? 'warning' : scope.row.followStatus === 'completed' ? 'success' : 'danger'">
                {{ scope.row.followStatus === 'pending' ? '待随访' : scope.row.followStatus === 'completed' ? '已随访' : '逾期' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="240" align="center" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" class="operate-btn edit-btn" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="success" size="small" class="operate-btn follow-btn" @click="handleFollow(scope.row)">随访</el-button>
              <el-button type="danger" size="small" class="operate-btn delete-btn" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10,20,50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          size="large"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
          class="patient-pagination"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑患者' : '新增患者'"
      width="600px"
      :close-on-click-modal="false"
      class="patient-dialog"
    >
      <el-form
        :model="patientForm"
        ref="patientFormRef"
        :rules="patientRules"
        label-width="100px"
        class="patient-form"
      >
        <el-form-item label="患者姓名" prop="name">
          <el-input v-model="patientForm.name" placeholder="请输入患者姓名" size="large" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="patientForm.gender" size="large">
            <el-radio value="male">男</el-radio>
            <el-radio value="female">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="patientForm.age" :min="1" :max="120" size="large" style="width: 100%" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="patientForm.phone" placeholder="请输入11位手机号" size="large" maxlength="11" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="patientForm.idCard" placeholder="请输入18位身份证号" size="large" maxlength="18" />
        </el-form-item>
        <el-form-item label="慢病类型" prop="diseaseType">
          <el-select v-model="patientForm.diseaseType" placeholder="请选择慢病类型" size="large" style="width: 100%">
            <el-option label="高血压" value="hypertension" />
            <el-option label="糖尿病" value="diabetes" />
            <el-option label="冠心病" value="chd" />
            <el-option label="脑卒中" value="stroke" />
          </el-select>
        </el-form-item>
        <el-form-item label="家庭住址" prop="address">
          <el-input v-model="patientForm.address" type="textarea" :rows="3" placeholder="请输入家庭住址" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" size="large">取消</el-button>
        <el-button type="primary" @click="handleSubmitPatient" size="large" :loading="isSubmitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="followDialogVisible"
      title="创建随访记录"
      width="600px"
      :close-on-click-modal="false"
      class="follow-dialog"
    >
      <el-form
        :model="followForm"
        ref="followFormRef"
        :rules="followRules"
        label-width="100px"
        class="follow-form"
      >
        <el-form-item label="患者姓名" prop="patientName">
          <el-input v-model="followForm.patientName" disabled size="large" />
        </el-form-item>
        <el-form-item label="随访日期" prop="followDate">
          <el-date-picker
            v-model="followForm.followDate"
            type="date"
            placeholder="请选择随访日期"
            size="large"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="随访方式" prop="followType">
          <el-select v-model="followForm.followType" placeholder="请选择随访方式" size="large" style="width: 100%">
            <el-option label="电话随访" value="phone" />
            <el-option label="上门随访" value="home" />
            <el-option label="门诊随访" value="clinic" />
          </el-select>
        </el-form-item>
        <el-form-item label="血压(mmHg)" prop="bloodPressure">
          <el-input v-model="followForm.bloodPressure" placeholder="如：120/80" size="large" />
        </el-form-item>
        <el-form-item label="血糖(mmol/L)" prop="bloodSugar">
          <el-input v-model="followForm.bloodSugar" placeholder="如：6.5" size="large" />
        </el-form-item>
        <el-form-item label="心率(次/分)" prop="heartRate">
          <el-input-number v-model="followForm.heartRate" :min="30" :max="200" size="large" style="width: 100%" />
        </el-form-item>
        <el-form-item label="随访备注" prop="remark">
          <el-input v-model="followForm.remark" type="textarea" :rows="4" placeholder="请输入随访备注" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="followDialogVisible = false" size="large">取消</el-button>
        <el-button type="primary" @click="handleSubmitFollow" size="large" :loading="isFollowSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { UserFilled, Search, Refresh, Plus, Download } from "@element-plus/icons-vue";
// 引入接口
import { getPatientList, addPatient, updatePatient, deletePatient, exportPatientList } from '../api/patient.js'
import { addFollowup } from '../api/followup.js'

const router = useRouter()
// 基础加载状态
const isTableLoading = ref(false)
const isSearchLoading = ref(false)
const isExportLoading = ref(false)
const isSubmitLoading = ref(false)
const isFollowSubmitLoading = ref(false)
// 筛选表单
const filterForm = reactive({ name: "", diseaseType: "" })
// 分页参数
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
// 患者列表数据
const patientList = ref([])

// 新增/编辑患者弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const patientFormRef = ref(null)
const patientForm = reactive({
  id: null,
  name: "",
  gender: "male",
  age: 50,
  phone: "",
  idCard: "",
  diseaseType: "",
  address: ""
})
const patientRules = reactive({
  name: [{ required: true, message: "请输入患者姓名", trigger: "blur" }],
  gender: [{ required: true, message: "请选择性别", trigger: "change" }],
  age: [{ required: true, message: "请输入年龄", trigger: "blur" }],
  phone: [
    { required: true, message: "请输入联系电话", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的11位手机号", trigger: "blur" }
  ],
  idCard: [
    { required: true, message: "请输入身份证号", trigger: "blur" },
    { pattern: /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: "请输入正确的18位身份证号", trigger: "blur" }
  ],
  diseaseType: [{ required: true, message: "请选择慢病类型", trigger: "change" }]
})

// 随访记录弹窗相关
const followDialogVisible = ref(false)
const followFormRef = ref(null)
const followForm = reactive({
  patientId: null,
  patientName: "",
  followDate: "",
  followType: "",
  bloodPressure: "",
  bloodSugar: "",
  heartRate: 75,
  remark: ""
})
const followRules = reactive({
  followDate: [{ required: true, message: "请选择随访日期", trigger: "change" }],
  followType: [{ required: true, message: "请选择随访方式", trigger: "change" }],
  bloodPressure: [{ required: true, message: "请输入血压", trigger: "blur" }],
  remark: [{ required: true, message: "请输入随访备注", trigger: "blur" }]
})

// 慢病类型标签样式映射
const getDiseaseTagType = (type) => {
  const typeMap = { hypertension: "primary", diabetes: "warning", chd: "info", stroke: "danger" };
  return typeMap[type] || "default";
};

// 获取患者列表数据
const getPatientData = async () => {
  if (isTableLoading.value) return;
  isTableLoading.value = true;
  try {
    // 调用接口
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...(filterForm.name && { name: filterForm.name }),
      ...(filterForm.diseaseType && { diseaseType: filterForm.diseaseType }),
    };
    const res = await getPatientList(params)
    patientList.value = res.data.list || [];
    pagination.total = res.data.total || 0;
  } catch (error) {
    patientList.value = [];
    pagination.total = 0;
  } finally {
    isTableLoading.value = false;
  }
};

// 搜索功能
const handleSearch = async () => {
  if (isSearchLoading.value) return;
  isSearchLoading.value = true;
  pagination.pageNum = 1;
  setTimeout(async () => {
    await getPatientData();
    isSearchLoading.value = false;
  }, 300);
};

// 重置筛选
const handleResetFilter = () => {
  Object.keys(filterForm).forEach(key => filterForm[key] = "");
  pagination.pageNum = 1;
  getPatientData();
};

// 分页切换
const handleSizeChange = (val) => {
  pagination.pageSize = val;
  pagination.pageNum = 1;
  getPatientData();
};
const handleCurrentChange = (val) => {
  pagination.pageNum = val;
  getPatientData();
};

// 新增患者
const handleAddPatient = () => {
  isEdit.value = false;
  Object.assign(patientForm, {
    id: null,
    name: "",
    gender: "male",
    age: 50,
    phone: "",
    idCard: "",
    diseaseType: "",
    address: ""
  });
  dialogVisible.value = true;
};

// 编辑患者
const handleEdit = (row) => {
  isEdit.value = true;
  Object.assign(patientForm, row);
  dialogVisible.value = true;
};

// 提交患者表单
const handleSubmitPatient = async () => {
  const valid = await patientFormRef.value.validate()
  if (!valid) return

  isSubmitLoading.value = true
  try {
    if (isEdit.value) {
      await updatePatient(patientForm)
      ElMessage.success('编辑成功')
    } else {
      await addPatient(patientForm)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getPatientData()
  } finally {
    isSubmitLoading.value = false
  }
};

// 打开随访弹窗
const handleFollow = (row) => {
  Object.assign(followForm, {
    patientId: row.id,
    patientName: row.name,
    followDate: "",
    followType: "",
    bloodPressure: "",
    bloodSugar: "",
    heartRate: 75,
    remark: ""
  })
  followDialogVisible.value = true
};

// 提交随访记录
const handleSubmitFollow = async () => {
  const valid = await followFormRef.value.validate()
  if (!valid) return

  isFollowSubmitLoading.value = true
  try {
    await addFollowup(followForm)
    ElMessage.success('随访记录创建成功')
    followDialogVisible.value = false
    getPatientData()
  } finally {
    isFollowSubmitLoading.value = false
  }
};

// 删除患者
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除患者【${row.name}】？此操作不可撤销！`, "删除确认", { type: "warning" });
    await deletePatient(row.id)
    ElMessage.success("删除成功");
    getPatientData();
  } catch (err) {
    if (err !== "cancel") ElMessage.error("删除失败");
  }
};

// 导出数据
const handleExport = async () => {
  isExportLoading.value = true;
  try {
    const params = {
      ...(filterForm.name && { name: filterForm.name }),
      ...(filterForm.diseaseType && { diseaseType: filterForm.diseaseType }),
    };
    const res = await exportPatientList(params)
    const blob = new Blob([res.data]);
    const a = document.createElement("a");
    a.href = URL.createObjectURL(blob);
    a.download = `患者列表_${new Date().getTime()}.xlsx`;
    a.click();
    URL.revokeObjectURL(a.href);
    ElMessage.success('导出成功');
  } catch (err) {
    ElMessage.error("导出失败");
  } finally {
    isExportLoading.value = false;
  }
};

// 页面挂载时加载数据
onMounted(() => {
  if (!localStorage.getItem('token')) {
    router.push('/')
    return
  }
  getPatientData();
});
</script>

<style scoped>
.patient-list-container {
  width: 100%;
  min-height: 100vh;
  background: #f7fafc;
  padding: 24px;
  box-sizing: border-box;
  overflow-x: hidden;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  color: #2d3748;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-desc {
  color: #718096;
  font-size: 16px;
  margin: 0;
}

.filter-card {
  border-radius: 16px !important;
  background: #fff !important;
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid #e2e8f0 !important;
}

.filter-input, .filter-select {
  border-radius: 12px !important;
  width: 200px;
  margin-right: 16px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px !important;
}

.filter-btn-group {
  display: flex;
  gap: 12px;
}

.search-btn, .reset-btn {
  border-radius: 12px !important;
  padding: 12px 24px;
  font-weight: 600;
}

.search-btn {
  background: #4299e1 !important;
  border: none !important;
}

.operate-btn-group {
  margin-bottom: 24px;
  display: flex;
  gap: 12px;
}

.add-btn, .export-btn {
  border-radius: 12px !important;
  padding: 12px 24px;
  font-weight: 600;
}

.add-btn {
  background: #4299e1 !important;
  border: none !important;
}

.export-btn {
  background: #68d391 !important;
  border: none !important;
}

.list-card {
  border-radius: 16px !important;
  background: #fff !important;
  border: 1px solid #e2e8f0 !important;
}

.list-content {
  padding: 0 24px 24px;
}

.loading-container {
  padding: 24px;
}

.empty-container {
  padding: 40px 0;
  text-align: center;
}

.patient-table {
  --el-table-border-color: #e2e8f0;
  --el-table-row-hover-bg-color: #f7fafc;
  border-radius: 12px !important;
  overflow: hidden;
}

:deep(.el-table__cell) {
  padding: 16px 0;
  color: #2d3748;
}

.operate-btn {
  border-radius: 8px !important;
  margin: 0 4px;
}

.edit-btn {
  background: #4299e1 !important;
  border: none !important;
}

.follow-btn {
  background: #68d391 !important;
  border: none !important;
}

.delete-btn {
  background: #e53e3e !important;
  border: none !important;
}

.pagination-container {
  padding: 24px;
  text-align: right;
}

:deep(.el-pagination__item) {
  border-radius: 8px !important;
}

:deep(.el-pagination__item.is-active) {
  background: #4299e1 !important;
  color: #fff;
}

:deep(.patient-dialog .el-dialog),
:deep(.follow-dialog .el-dialog) {
  border-radius: 16px !important;
}

.patient-form, .follow-form {
  padding: 10px 0;
}

:deep(.patient-form .el-input__wrapper),
:deep(.follow-form .el-input__wrapper),
:deep(.patient-form .el-textarea__inner),
:deep(.follow-form .el-textarea__inner) {
  border-radius: 12px !important;
}
</style>