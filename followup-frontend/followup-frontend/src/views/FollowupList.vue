<template>
  <div class="followup-list-container">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon size="28"><Document /></el-icon>
        随访记录管理
      </h1>
      <p class="page-desc">社区慢病患者随访记录列表</p>
    </div>

    <el-card class="filter-card" shadow="hover">
      <el-form :model="filterForm" inline @submit.prevent>
        <el-form-item label="患者姓名">
          <el-input
            v-model="filterForm.patientName"
            placeholder="请输入患者姓名"
            clearable
            size="large"
            class="filter-input"
          />
        </el-form-item>
        <el-form-item label="随访日期">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            size="large"
            value-format="YYYY-MM-DD"
          />
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

    <el-card class="list-card" shadow="hover">
      <div class="list-content">
        <div v-if="isTableLoading" class="loading-container">
          <el-skeleton :rows="8" animated class="skeleton-loading" />
        </div>

        <div v-else-if="followupList.length === 0" class="empty-container">
          <el-empty description="暂无随访记录" :image-size="120">
            <el-button type="primary" @click="handleSearch">重新加载</el-button>
          </el-empty>
        </div>

        <el-table
          v-else
          :data="followupList"
          border
          stripe
          size="large"
          class="followup-table"
          :header-cell-style="{ background: '#f7fafc', borderRadius: '8px' }"
          empty-text="暂无数据"
        >
          <el-table-column prop="patientName" label="患者姓名" min-width="100" align="center" />
          <el-table-column prop="followDate" label="随访日期" min-width="120" align="center" />
          <el-table-column prop="followType" label="随访方式" min-width="100" align="center">
            <template #default="scope">
              {{ scope.row.followType === 'phone' ? '电话随访' : scope.row.followType === 'home' ? '上门随访' : '门诊随访' }}
            </template>
          </el-table-column>
          <el-table-column prop="bloodPressure" label="血压(mmHg)" min-width="100" align="center" />
          <el-table-column prop="bloodSugar" label="血糖(mmol/L)" min-width="120" align="center" />
          <el-table-column prop="heartRate" label="心率(次/分)" min-width="100" align="center" />
          <el-table-column prop="remark" label="随访备注" min-width="200" show-overflow-tooltip />
          <el-table-column label="操作" min-width="120" align="center" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" class="operate-btn" @click="handleViewDetail(scope.row)">查看</el-button>
              <el-button type="danger" size="small" class="operate-btn" @click="handleDelete(scope.row)">删除</el-button>
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
          class="followup-pagination"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Document, Search, Refresh } from "@element-plus/icons-vue";
// 引入接口
import { getFollowupList, deleteFollowup } from '../api/followup.js'

const router = useRouter()
const isTableLoading = ref(false)
const isSearchLoading = ref(false)
const filterForm = reactive({ patientName: "", dateRange: [] })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const followupList = ref([])

// 获取随访记录列表
const getFollowupData = async () => {
  if (isTableLoading.value) return;
  isTableLoading.value = true;
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...(filterForm.patientName && { patientName: filterForm.patientName }),
      ...(filterForm.dateRange?.length === 2 && {
        startDate: filterForm.dateRange[0],
        endDate: filterForm.dateRange[1]
      }),
    };
    const res = await getFollowupList(params)
    followupList.value = res.data.list || [];
    pagination.total = res.data.total || 0;
  } catch (error) {
    followupList.value = [];
    pagination.total = 0;
  } finally {
    isTableLoading.value = false;
  }
};

// 搜索
const handleSearch = async () => {
  if (isSearchLoading.value) return;
  isSearchLoading.value = true;
  pagination.pageNum = 1;
  setTimeout(async () => {
    await getFollowupData();
    isSearchLoading.value = false;
  }, 300);
};

// 重置筛选
const handleResetFilter = () => {
  Object.keys(filterForm).forEach(key => filterForm[key] = key === 'dateRange' ? [] : "");
  pagination.pageNum = 1;
  getFollowupData();
};

// 分页
const handleSizeChange = (val) => {
  pagination.pageSize = val;
  pagination.pageNum = 1;
  getFollowupData();
};
const handleCurrentChange = (val) => {
  pagination.pageNum = val;
  getFollowupData();
};

// 查看详情
const handleViewDetail = (row) => {
  ElMessage.info(`查看随访记录详情：${row.patientName}`);
};

// 删除随访记录
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除该随访记录？此操作不可撤销！`, "删除确认", { type: "warning" });
    await deleteFollowup(row.id)
    ElMessage.success("删除成功");
    getFollowupData();
  } catch (err) {
    if (err !== "cancel") ElMessage.error("删除失败");
  }
};

// 页面挂载
onMounted(() => {
  if (!localStorage.getItem('token')) {
    router.push('/')
    return
  }
  getFollowupData();
});
</script>

<style scoped>
.followup-list-container {
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

.filter-input {
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

.followup-table {
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
</style>