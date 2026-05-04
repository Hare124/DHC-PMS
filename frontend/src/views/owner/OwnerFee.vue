<!-- src/views/fee/OwnerFee.vue -->
<template>
  <div class="owner-fee-container">
    <!-- 筛选条件 -->
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="费用类型">
          <el-select style="width: 150px" v-model="searchForm.typeName" placeholder="请选择费用类型" clearable>
            <el-option
                v-for="type in feeTypeOptions"
                :key="type.id"
                :label="type.typeName"
                :value="type.typeName">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="缴费状态">
          <el-select v-model="searchForm.status" placeholder="请选择" style="width: 150px;">
            <el-option label="未缴费" value="0"></el-option>
            <el-option label="已缴费" value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchFeeList">查询</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 费用汇总 -->
    <div class="summary-card">
      <el-card>
        <div class="summary-item">
          <span class="label">待缴金额：</span>
          <span class="value">{{ summary.unpaidAmount || 0 }} 元</span>
        </div>
        <div class="summary-item">
          <span class="label">逾期金额：</span>
          <span class="value">{{ summary.overdueAmount || 0 }} 元</span>
        </div>
        <div class="summary-item">
          <span class="label">已缴金额：</span>
          <span class="value">{{ summary.paidAmount || 0 }} 元</span>
        </div>
      </el-card>
    </div>

    <!-- 费用列表 -->
    <div class="fee-table">
      <el-table :data="feeList" border stripe style="width: 100%">
        <el-table-column prop="roomNo" label="房号" width="120"></el-table-column>
        <el-table-column prop="feeTypeName" label="费用类型" width="120"></el-table-column>
        <el-table-column prop="amount" label="金额（元）" width="120"></el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="120"></el-table-column>
        <el-table-column prop="statusDesc" label="状态" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.statusDesc === '已缴费'" type="success">{{ scope.row.statusDesc }}</el-tag>
            <el-tag v-else-if="scope.row.statusDesc === '待缴费'" type="warning">{{ scope.row.statusDesc }}</el-tag>
            <el-tag v-else type="danger">{{ scope.row.statusDesc }}（{{ scope.row.overdueDays }}天）</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="缴费时间" width="180">
          <template #default="scope">
            {{ scope.row.payTime ? formatPaymentTime(scope.row.payTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="transactionNo" label="缴费流水号" width="200">
          <template #default="scope">
            {{ scope.row.transactionNo || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
                v-if="scope.row.statusDesc !== '已缴费'"
                type="primary"
                size="small"
                @click="handlePay(scope.row.id)"
            >
              缴费
            </el-button>
            <el-button
                v-else
                type="info"
                size="small"
                @click="handleViewVoucher(scope.row.id)"
            >
              查看凭证
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="searchForm.pageNum"
          :page-sizes="[10, 20, 50]"
          :page-size="searchForm.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          style="margin-top: 20px; text-align: right"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
// 导入接口
import { queryOwnerFee, getOwnerFeeSummary, createPaymentOrder, payFee, getPaymentVoucher, getFeeTypes } from '@/api/feeApi.js';

const router = useRouter();

// 核心修复：从 localStorage 获取真实的登录用户信息
// 优先级：user > userId > 默认值
const getUserInfo = () => {
  // 1. 尝试从 localStorage 获取 user 对象
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      console.log('从 localStorage 获取到 user:', user);
      return user;
    } catch (e) {
      console.error('解析 user 失败:', e);
    }
  }

  // 2. 尝试直接获取 userId（兼容不同存储方式）
  const userIdStr = localStorage.getItem('userId');
  if (userIdStr) {
    const userId = parseInt(userIdStr);
    console.log('从 localStorage 获取到 userId:', userId);
    return { id: userId, role: 2 };
  }

  // 3. 兜底：返回默认值（仅用于测试）
  console.warn('未找到用户信息，使用默认值');
  return { id: 2, role: 2 };
};

// 获取当前登录用户信息
const currentUser = getUserInfo();
const userId = currentUser.id;
const userRole = currentUser.role || 2;

console.log('\n========== 当前用户信息 ==========');
console.log('userId:', userId);
console.log('userRole:', userRole);
console.log('=================================\n');

// 搜索表单
const searchForm = reactive({
  userId, // 使用真实的 userId
  pageNum: 1,
  pageSize: 10,
  typeName: '',
  status: '',
  startDueDate: '',
  endDueDate: ''
});

// 格式化缴费时间
const formatPaymentTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 费用类型选项列表
const feeTypeOptions = ref([
  { id: 1, typeName: '物业费' },
  { id: 2, typeName: '水电费' },
  { id: 3, typeName: '停车费' },
  { id: 4, typeName: '垃圾清运费' }
]);


// 日期范围
const dateRange = ref([]);

// 费用列表和汇总数据
const feeList = ref([]);
const summary = ref({});
const total = ref(0);

// 页面加载时获取数据
onMounted(() => {
  fetchFeeList();
  fetchFeeSummary();
  fetchFeeTypes(); // 新增：获取费用类型列表
});

// 获取费用类型列表
const fetchFeeTypes = async () => {
  try {
    console.log('开始获取费用类型列表...');
    const res = await getFeeTypes();
    console.log('完整响应:', res);
    console.log('业务数据:', res.data);

    // 从 axios 响应中提取业务数据
    const businessData = res.data;

    if (businessData && businessData.code === 200) {
      feeTypeOptions.value = businessData.data || [];
      console.log('费用类型加载成功:', feeTypeOptions.value);
    } else {
      const errorMsg = businessData?.msg || '获取费用类型失败';
      ElMessage.error(errorMsg);
    }
  } catch (err) {
    console.error('获取费用类型失败:', err);
    ElMessage.error('网络异常，获取费用类型失败');
  }
};

// 获取费用列表
const fetchFeeList = async () => {
  try {
    // 处理日期范围
    if (dateRange.value.length) {
      searchForm.startDueDate = dateRange.value[0];
      searchForm.endDueDate = dateRange.value[1];
    } else {
      searchForm.startDueDate = '';
      searchForm.endDueDate = '';
    }

    console.log('开始获取费用列表...', searchForm);
    const res = await queryOwnerFee(searchForm);
    console.log('费用列表接口响应:', res);

    // 处理嵌套响应结构
    const businessData = res.data;
    const responseCode = businessData?.code || res.code;

    if (responseCode === 200) {
      feeList.value = businessData?.data?.records || [];
      total.value = businessData?.data?.total || 0;
      console.log('费用列表数据:', feeList.value);
    } else {
      const errorMsg = businessData?.msg || res.msg || '查询失败';
      ElMessage.error(errorMsg);
    }
  } catch (err) {
    console.error('获取费用列表失败:', err);
    ElMessage.error('网络异常，查询失败');
  }
};

// 获取费用汇总
const fetchFeeSummary = async () => {
  try {
    console.log('开始获取费用汇总...');
    const res = await getOwnerFeeSummary(userId);
    console.log('费用汇总接口响应:', res);

    // 处理嵌套响应结构
    const businessData = res.data;
    const responseCode = businessData?.code || res.code;

    if (responseCode === 200) {
      summary.value = businessData?.data || {};
      console.log('费用汇总数据:', summary.value);
    } else {
      const errorMsg = businessData?.msg || res.msg || '获取汇总失败';
      ElMessage.error(errorMsg);
    }
  } catch (err) {
    console.error('获取费用汇总失败:', err);
    ElMessage.error('网络异常，获取费用汇总失败');
  }
};

// 重置筛选表单
const resetForm = () => {
  searchForm.typeName = '';
  searchForm.status = '';
  dateRange.value = [];
  searchForm.pageNum = 1;
  fetchFeeList();
};

// 分页-每页条数变化
const handleSizeChange = (val) => {
  searchForm.pageSize = val;
  fetchFeeList();
};

// 分页-页码变化
const handleCurrentChange = (val) => {
  searchForm.pageNum = val;
  fetchFeeList();
};

// 缴费操作
const handlePay = async (feeRecordId) => {
  try {
    // 直接跳转到缴费确认页面，传递费用记录 ID
    router.push({
      path: '/owner/fee/payment',
      query: { feeRecordIds: feeRecordId }
    });
  } catch (err) {
    console.error('跳转缴费页面失败:', err);
    ElMessage.error('跳转失败，请重试');
  }
};

// 查看缴费凭证
const handleViewVoucher = async (feeRecordId) => {
  try {
    console.log('===== 开始获取缴费凭证 =====');
    console.log('费用记录ID:', feeRecordId);

    const res = await getPaymentVoucher(userId, feeRecordId);
    console.log('接口响应 res:', res);
    console.log('res.data:', res.data);

    // 处理后端返回结构：凭证字符串在 msg 字段，不在 data 字段
    // res.data = { code: 200, msg: '凭证字符串', data: null }
    const businessData = res.data;

    if (businessData && businessData.code === 200) {
      // 关键修复：从 msg 字段获取凭证字符串
      const voucherText = businessData.msg;  // ← 修改这里：从 msg 获取
      console.log('凭证文本:', voucherText);
      console.log('凭证文本类型:', typeof voucherText);

      if (!voucherText || voucherText === 'null' || voucherText.trim() === '') {
        console.error('凭证文本为空');
        ElMessage.warning('该费用记录尚未缴费或凭证生成失败');
        return;
      }

      // 跳转到凭证页面
      console.log('跳转路径：/owner/fee/voucher');
      router.push({
        path: '/owner/fee/voucher',
        query: {
          voucherInfo: encodeURIComponent(voucherText)
        }
      });
    } else {
      console.error('获取凭证失败:', businessData);
      ElMessage.error(businessData?.msg || '获取凭证失败');
    }
  } catch (err) {
    console.error('查看凭证异常:', err);
    ElMessage.error('网络异常，查看凭证失败');
  }
};
</script>

<style scoped>
.owner-fee-container {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.summary-card {
  margin-bottom: 20px;
}

.summary-item {
  display: inline-block;
  margin-right: 30px;
  font-size: 16px;
}

.summary-item .label {
  color: #666;
}

.summary-item .value {
  color: #e6a23c;
  font-weight: bold;
}

.fee-table {
  margin-top: 20px;
}
</style>