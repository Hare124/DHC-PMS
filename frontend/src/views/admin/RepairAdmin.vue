<!-- src/views/repair/RepairAdmin.vue -->
<template>
  <div class="repair-list-container">
    <!-- 页面内角色权限校验 -->
    <div v-if="!hasPermission" class="no-permission">
      <el-alert
          title="无访问权限"
          type="error"
          description="您当前的角色无权限访问该页面，请联系管理员"
          show-icon
      />
      <el-button type="primary" @click="goBack">返回上一页</el-button>
    </div>

    <div v-else>
      <!-- 模糊查询 + 高级筛选 -->
      <div class="search-filter-container" style="margin-bottom: 20px;">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="报修单号">
            <el-input
                v-model="searchForm.id"
                placeholder="请输入报修单号"
                clearable              style="width: 150px;"
            />
          </el-form-item>
          <el-form-item label="报修标题">
            <el-input
                v-model="searchForm.title"
                placeholder="请输入报修标题"
                clearable              style="width: 200px;"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
                v-model="searchForm.status"
                placeholder="请选择状态"
                clearable              style="width: 150px;"
            >
              <el-option label="待审核" :value="0" />
              <el-option label="已审核" :value="1" />
              <el-option label="维修中" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
              <el-option label="待验收" :value="5" />
              <el-option label="已验收" :value="6" />
            </el-select>
          </el-form-item>
          <el-form-item label="报修类型">
            <el-select
                v-model="searchForm.repairTypeId"
                placeholder="请选择报修类型"
                clearable              style="width: 150px;"
            >
              <el-option
                  v-for="item in repairTypeList"
                  :key="item.id"
                  :label="item.typeName"
                  :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 报修列表 -->
      <el-table
          :data="Array.isArray(repairList) ? repairList : []"
          border
          stripe          style="width: 100%; margin-top: 20px;"
          v-loading="loading"
      >
        <el-table-column prop="repairOrderNo" label="报修单号" width="120" />
        <el-table-column prop="title" label="报修标题" width="300" show-overflow-tooltip />
        <el-table-column prop="repairTypeName" label="报修类型" width="120" />
        <el-table-column prop="address" label="报修地址" width="100" />
        <el-table-column prop="statusDesc" label="状态" width="120" />
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ scope.row.createTime ? scope.row.createTime.replace('T', ' ') : '无' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <!-- 查看按钮（所有状态都显示） -->
            <el-button
                type="info"
                size="small"
                @click="handleViewDetail(scope.row)"
            >
              查看
            </el-button>
            <!-- 待审核：审核按钮 -->
            <el-button
                v-if="scope.row.status === 0"
                type="primary"
                size="small"
                @click="openReviewDialog(scope.row)"
            >
              审核
            </el-button>
            <!-- 已审核：派单按钮 -->
            <el-button
                v-if="scope.row.status === 1"
                type="warning"
                size="small"
                @click="openAssignDialog(scope.row)"
            >
              派单
            </el-button>
            <!-- 维修中：标记待验收按钮 -->
            <el-button
                v-if="scope.row.status === 2"
                type="success"
                size="small"
                @click="handleUpdateProgress(scope.row.id)"
            >
              标记待验收
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"          style="margin-top: 20px; justify-content: flex-end;"
      />

      <!-- 审核弹窗 -->
      <el-dialog v-model="reviewDialogVisible" title="审核报修单" width="400px">
        <el-form :model="reviewForm" label-width="80px">
          <el-form-item label="审核结果">
            <el-radio-group v-model="reviewForm.reviewResult">
              <el-radio :label="true">通过</el-radio>
              <el-radio :label="false">驳回</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
              label="驳回原因"
              v-if="!reviewForm.reviewResult"
              :rules="[{ required: true, message: '请输入驳回原因' }]"
          >
            <el-input v-model="reviewForm.rejectReason" type="textarea" rows="3" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleReview">确认审核</el-button>
        </template>
      </el-dialog>

      <!-- 派单弹窗 -->
      <el-dialog v-model="assignDialogVisible" title="派单给维修人员" width="400px">
        <el-form :model="assignForm" label-width="80px">
          <el-form-item label="维修人员" :rules="[{ required: true, message: '请输入维修人员姓名' }]">
            <el-input
                v-model="assignForm.handlerName"
                placeholder="请输入维修人员姓名"
                maxlength="20"                style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="联系电话" :rules="[{ required: true, message: '请输入联系电话' }]">
            <el-input
                v-model="assignForm.handlerPhone"
                placeholder="请输入维修人员联系电话"
                maxlength="11"                style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="预计上门时间" :rules="[{ required: true, message: '请输入预计上门时间' }]">
            <el-date-picker
                v-model="assignForm.expectedVisitTime"
                type="datetime"
                placeholder="请选择预计上门时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm"
                :disabled-date="disabledDate"                style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="派单备注">
            <el-input v-model="assignForm.remark" type="textarea" rows="3" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAssign">确认派单</el-button>
        </template>
      </el-dialog>

      <!-- 查看详情弹窗 -->
      <el-dialog v-model="detailDialogVisible" title="报修详情" width="600px">
        <el-descriptions :column="2" border v-loading="detailLoading">
          <el-descriptions-item label="报修单号">{{ currentDetail?.repairOrderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报修标题">{{ currentDetail?.title || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报修类型">{{ currentDetail?.repairTypeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentDetail?.status)" size="small">
              {{ currentDetail?.statusDesc || '-' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="报修地址">{{ currentDetail?.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentDetail?.createTime ? currentDetail.createTime.replace('T', ' ') : '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="报修内容" :span="2">
            <div style="white-space: pre-wrap; word-break: break-all;">{{ currentDetail?.content || '无' }}</div>
          </el-descriptions-item>

          <!-- 维修人员信息（已派单后显示） -->
          <el-descriptions-item
              v-if="currentDetail?.status >= 1 && currentDetail?.handlerName"
              label="维修人员"
              :span="2"
          >
            {{ currentDetail.handlerName }}（{{ currentDetail.handlerPhone }}）
          </el-descriptions-item>

          <!-- 预计上门时间 -->
          <el-descriptions-item
              v-if="currentDetail?.expectedVisitTime"
              label="预计上门时间"
          >
            {{ currentDetail.expectedVisitTime }}
          </el-descriptions-item>

          <!-- 派单备注 -->
          <el-descriptions-item
              v-if="currentDetail?.assignRemark"
              label="派单备注"
              :span="2"
          >
            {{ currentDetail.assignRemark }}
          </el-descriptions-item>
          <!-- 完成时间 -->
          <el-descriptions-item
              v-if="currentDetail?.completeTime"
              label="完成时间"
          >
            {{ currentDetail.completeTime.replace('T', ' ') }}
          </el-descriptions-item>

          <!-- 评价信息（已完成状态显示） -->
          <el-descriptions-item
              v-if="currentDetail?.status === 3 && currentDetail?.evaluationScore"
              label="评价评分"
          >
            <el-rate v-model="currentDetail.evaluationScore" disabled />
          </el-descriptions-item>
          <el-descriptions-item
              v-if="currentDetail?.status === 3 && currentDetail?.evaluationComment"
              label="评价内容"
              :span="2"
          >
            {{ currentDetail.evaluationComment }}
          </el-descriptions-item>
          <el-descriptions-item
              v-if="currentDetail?.status === 3 && currentDetail?.evaluationTime"
              label="评价时间"
          >
            {{ currentDetail.evaluationTime.replace('T', ' ') }}
          </el-descriptions-item>
        </el-descriptions>
        <template #footer>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>


    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  testPost, testGet, reviewRepairOrder, assignRepairOrder, updateRepairProgress,
  getAdminRepairList, getRepairTypeList, getRepairDetail
} from '@/api/repairApi.js';

// ========== 1. 页面内角色校验（不修改路由守卫） ==========
const userRoleNum = localStorage.getItem('role'); // 数字：1=物业，2=业主，3=管理员
const hasPermission = computed(() => {
  // 物业端报修列表：允许物业人员 (role=1) 和管理员 (role=3) 访问
  return userRoleNum === '1' || userRoleNum === '3';
});

// 返回上一页
const goBack = () => {
  window.history.go(-1);
};

// ========== 2. 基础状态 ==========
const repairList = ref([]);
const loading = ref(false);

// 分页配置
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 搜索表单
const searchForm = ref({
  id: '',
  title: '',
  status: null,
  repairTypeId: null
});

// 报修类型列表
const repairTypeList = ref([]);

// 维修人员模拟数据
const handlerList = ref([
  { id: 1, name: '张师傅', phone: '13800138001', specialty: '水电维修' },
  { id: 2, name: '李师傅', phone: '13800138002', specialty: '家电维修' },
  { id: 3, name: '王师傅', phone: '13800138003', specialty: '公共区域维修' },
  { id: 4, name: '赵师傅', phone: '13800138004', specialty: '综合维修' }
]);
// ========== 3. 测试按钮事件 ==========
const handleTestPost = () => {
  loading.value = true;
  testPost({
    name: '张三',
    age: 18
  }).then(res => {
    ElMessage.success('POST 请求成功');
    console.log('POST 请求成功：', res);
  }).catch(err => {
    ElMessage.error(err?.msg || err?.response?.data?.msg || 'POST 请求失败');
    console.error('POST 请求失败：', err);
  }).finally(() => {
    loading.value = false;
  });
};

const handleTestGet = () => {
  loading.value = true;
  testGet().then(res => {
    ElMessage.success('GET 请求成功');
    console.log('GET 请求成功：', res);
  }).catch(err => {
    ElMessage.error(err?.msg || err?.response?.data?.msg || 'GET 请求失败');
    console.error('GET 请求失败：', err);
  }).finally(() => {
    loading.value = false;
  });
};

// ========== 4. 审核弹窗逻辑 ==========
const reviewDialogVisible = ref(false);
const reviewForm = ref({
  repairOrderId: '',
  reviewResult: true, // 默认通过
  rejectReason: ''
});

const openReviewDialog = (row) => {
  reviewForm.value = {
    repairOrderId: row.id,
    reviewResult: true,
    rejectReason: ''
  };
  reviewDialogVisible.value = true;
};

const handleReview = () => {
  if (!reviewForm.value.reviewResult && !reviewForm.value.rejectReason) {
    ElMessage.warning('请输入驳回原因');
    return;
  }

  loading.value = true;
  reviewRepairOrder(reviewForm.value)
      .then(res => {
        // 后端返回的数据在 res.data 中
        const response = res.data;
        console.log('✅ 审核响应:', response);

        if (response?.code === 200) {
          ElMessage.success(response.msg || '审核成功');
          reviewDialogVisible.value = false;
          refreshRepairList();
        } else {
          ElMessage.warning(response?.msg || '审核失败');
        }
      })
      .catch(err => {
        ElMessage.error(err?.msg || err?.response?.data?.msg || '审核失败');
      })
      .finally(() => {
        loading.value = false;
      });
};

// ========== 5. 查看详情逻辑 ==========
const detailDialogVisible = ref(false);
const currentDetail = ref(null);
const detailLoading = ref(false);

const handleViewDetail = (row) => {
  detailLoading.value = true;
  currentDetail.value = null;

  getRepairDetail(row.id)
      .then(res => {
        const response = res.data;
        if (response?.code === 200 && response.data) {
          currentDetail.value = response.data;
          detailDialogVisible.value = true;
        } else {
          ElMessage.warning(response?.msg || '暂无报修详情');
        }
      })
      .catch(err => {
        ElMessage.error(err?.msg || err?.response?.data?.msg || '获取详情失败');
      })
      .finally(() => {
        detailLoading.value = false;
      });
};

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',      // 待审核 - 灰色
    1: 'primary',   // 已审核 - 蓝色
    2: 'warning',   // 维修中 - 橙色
    3: 'success',   // 已完成 - 绿色
    4: 'danger',    // 已取消 - 红色
    5: 'warning',   // 待验收 - 橙色
    6: 'success'    // 已验收 - 绿色
  };
  return typeMap[status] || 'info';
};


// ========== 5. 派单弹窗逻辑 ==========
const assignDialogVisible = ref(false);
const assignForm = ref({
  repairOrderId: '',
  handlerName: '',
  handlerPhone: '',
  expectedVisitTime: '',
  remark: ''
});

const openAssignDialog = (row) => {
  assignForm.value = {
    repairOrderId: row.id,
    handlerName: '',
    handlerPhone: '',
    expectedVisitTime: '',
    remark: ''
  };
  assignDialogVisible.value = true;
};

const handleAssign = () => {
  if (!assignForm.value.handlerName) {
    ElMessage.warning('请输入维修人员姓名');
    return;
  }
  if (!assignForm.value.handlerPhone) {
    ElMessage.warning('请输入联系电话');
    return;
  }
  if (!assignForm.value.expectedVisitTime) {
    ElMessage.warning('请选择预计上门时间');
    return;
  }

  loading.value = true;
  assignRepairOrder(assignForm.value)
      .then(res => {
        // 后端返回的数据在 res.data 中
        const response = res.data;
        console.log('✅ 派单响应:', response);

        if (response?.code === 200) {
          ElMessage.success(response.msg || '派单成功');
          assignDialogVisible.value = false;
          refreshRepairList();
        } else {
          ElMessage.warning(response?.msg || '派单失败');
        }
      })
      .catch(err => {
        ElMessage.error(err?.msg || err?.response?.data?.msg || '派单失败');
      })
      .finally(() => {
        loading.value = false;
      });
};

// ========== 6. 标记待验收逻辑 ==========
const handleUpdateProgress = (repairOrderId) => {
  ElMessageBox.confirm(
      '确认标记该报修单为待验收状态吗？',
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(() => {
    loading.value = true;
    updateRepairProgress(repairOrderId)
        .then(res => {
          // 后端返回的数据在 res.data 中
          const response = res.data;
          console.log('✅ 标记待验收响应:', response);

          if (response?.code === 200) {
            ElMessage.success(response.msg || '标记成功');
            refreshRepairList();
          } else {
            ElMessage.warning(response?.msg || '标记失败');
          }
        })
        .catch(err => {
          ElMessage.error(err?.msg || err?.response?.data?.msg || '标记失败');
        })
        .finally(() => {
          loading.value = false;
        });
  });
};

// ========== 7. 加载和刷新报修列表 ==========
const loadRepairList = () => {
  loading.value = true;

  // 构建查询参数
  const params = {
    page: pagination.value.currentPage,
    size: pagination.value.pageSize
  };

  if (searchForm.value.status !== null && searchForm.value.status !== '') {
    params.status = searchForm.value.status;
  }
  if (searchForm.value.repairTypeId !== null && searchForm.value.repairTypeId !== '') {
    params.repairTypeId = searchForm.value.repairTypeId;
  }

  getAdminRepairList(params)
      .then(res => {
        const response = res.data;
        if (response?.code === 200) {
          // 后端返回分页数据
          const pageData = response.data;
          let dataList = Array.isArray(pageData.records) ? pageData.records : [];

          // 前端模糊查询：报修单号和标题
          if (searchForm.value.id) {
            dataList = dataList.filter(item =>
                item.id.toString().includes(searchForm.value.id)
            );
          }
          if (searchForm.value.title) {
            dataList = dataList.filter(item =>
                item.title && item.title.toLowerCase().includes(searchForm.value.title.toLowerCase())
            );
          }

          repairList.value = dataList;
          pagination.value.total = pageData.total || 0;
          console.log('✅ 报修列表加载成功:', repairList.value.length, '条记录');
        } else {
          ElMessage.warning(response?.msg || '加载报修列表失败');
          repairList.value = [];
          pagination.value.total = 0;
        }
      })
      .catch(err => {
        ElMessage.error(err?.msg || err?.response?.data?.msg || '加载报修列表失败');
        repairList.value = [];
        pagination.value.total = 0;
      })
      .finally(() => {
        loading.value = false;
      });
};

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  pagination.value.pageSize = val;
  pagination.value.currentPage = 1; // 重置到第一页
  loadRepairList();
};

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.value.currentPage = val;
  loadRepairList();
};

const refreshRepairList = () => {
  // 刷新时回到第一页
  pagination.value.currentPage = 1;
  loadRepairList();
};

const loadRepairTypeList = () => {
  getRepairTypeList()
      .then(res => {
        const response = res.data;
        if (response?.code === 200) {
          repairTypeList.value = response.data || [];
          console.log('✅ 报修类型加载成功:', repairTypeList.value.length, '个类型');
        } else {
          ElMessage.warning(response?.msg || '加载报修类型失败');
        }
      })
      .catch(err => {
        ElMessage.error(err?.msg || err?.response?.data?.msg || '加载报修类型失败');
      });
};

const handleSearch = () => {
  console.log('🔍 执行搜索:', searchForm.value);
  loadRepairList();
};

const handleReset = () => {
  searchForm.value = {
    id: '',
    title: '',
    status: null,
    repairTypeId: null
  };
  loadRepairList();
};

// ========== 8. 页面加载 ==========
onMounted(() => {
  if (hasPermission.value) {
    loadRepairTypeList();
    loadRepairList();
  }
});

// ========== 9. 时间选择器限制方法 ==========
// 禁用过去的日期和时间
const disabledDate = (time) => {
  // 禁用今天之前的日期
  return time.getTime() < Date.now() - 8.64e7; // 8.64e7 = 1 天的毫秒数
};
</script>

<style scoped>
.repair-list-container {
  padding: 10px;
}

.test-btn-group {
  margin-bottom: 20px;
}

.test-btn-group button {
  margin-right: 10px;
  padding: 8px 16px;
  cursor: pointer;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
}

.test-btn-group button:hover {
  background: #f5f5f5;
}

.no-permission {
  padding: 40px;
  text-align: center;
}

.no-permission .el-alert {
  max-width: 600px;
  margin: 0 auto 20px;
}
</style>