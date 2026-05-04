<!-- src/views/fee/OwnerFee02.vue -->
<template>
  <div class="owner-fee">
    <!-- 费用汇总 -->
    <div class="summary-card">
      <el-card>
        <div class="summary-item">
          <span class="label">待缴金额：</span>
          <span class="value unpaid">{{ summary.unpaidAmount || 0 }} 元</span>
        </div>
        <div class="summary-item">
          <span class="label">逾期金额：</span>
          <span class="value overdue">{{ summary.overdueAmount || 0 }} 元</span>
        </div>
        <div class="summary-item">
          <span class="label">已缴金额：</span>
          <span class="value paid">{{ summary.paidAmount || 0 }} 元</span>
        </div>
      </el-card>
    </div>

    <!-- 费用列表 -->
    <el-card shadow="never" class="list-card">
      <div class="fee-list" v-loading="loading">
        <div
            class="fee-item"
            v-for="fee in feeList"
            :key="fee.id"
            @click="handleRowClick(fee)"
        >
          <div class="fee-header">
            <div class="fee-title">
              <el-tag v-if="fee.statusDesc === '已缴费'" size="small" type="success" style="margin-right: 8px;">已缴费</el-tag>
              <el-tag v-else-if="fee.statusDesc === '待缴费'" size="small" type="warning" style="margin-right: 8px;">待缴费</el-tag>
              <el-tag v-else size="small" type="danger" style="margin-right: 8px;">逾期</el-tag>
              <span class="fee-type">{{ fee.feeTypeName }}</span>
            </div>
            <div class="fee-amount">
              <span class="amount-label">金额：</span>
              <span class="amount-value">¥{{ fee.amount }}</span>
            </div>
          </div>
          <div class="fee-content">
            <div class="fee-info">
              <span><el-icon><House /></el-icon> 房号：{{ fee.roomNo }}</span>
              <span><el-icon><Calendar /></el-icon> 截止日期：{{ fee.dueDate }}</span>
              <span v-if="fee.payTime"><el-icon><Clock /></el-icon> 缴费时间：{{ formatPaymentTime(fee.payTime) }}</span>
              <span v-if="fee.transactionNo"><el-icon><Ticket /></el-icon> 流水号：{{ fee.transactionNo }}</span>
            </div>
            <div class="fee-status">
              <span v-if="fee.statusDesc !== '已缴费' && fee.overdueDays">
                逾期 {{ fee.overdueDays }} 天
              </span>
            </div>
          </div>
        </div>
        <el-empty v-if="feeList.length === 0" description="暂无费用记录" />
      </div>

      <!-- 分页 -->
      <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchFeeList"
          @current-change="fetchFeeList"          style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <!-- 缴费确认弹窗 -->
    <el-dialog
        v-model="payDialogVisible"
        title="费用缴纳"
        width="550px"
        @close="resetPayForm"
    >
      <div v-if="currentFee.id" class="pay-dialog-content">
        <!-- 费用信息卡片 -->
        <el-card shadow="never" style="margin-bottom: 16px; background-color: #f5f7fa;">
          <template #header>
            <div style="font-weight: 600; color: #303133;">
              <el-icon><Wallet /></el-icon>
              费用信息
            </div>
          </template>
          <el-descriptions :column="2" size="small">
            <el-descriptions-item label="费用类型">{{ currentFee.feeTypeName }}</el-descriptions-item>
            <el-descriptions-item label="房号">{{ currentFee.roomNo }}</el-descriptions-item>
            <el-descriptions-item label="应收金额" :span="2">
              <span style="color: #f56c6c; font-weight: 600; font-size: 18px;">¥{{ currentFee.amount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="截止日期">{{ currentFee.dueDate }}</el-descriptions-item>
            <el-descriptions-item label="逾期天数" v-if="currentFee.overdueDays">
              <el-tag type="danger" size="small">{{ currentFee.overdueDays }}天</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 支付设置 -->
        <el-form :model="payForm" label-width="100px">
          <el-form-item label="支付方式" required>
            <el-radio-group v-model="payForm.payType">
              <el-radio label="WECHAT">
                <img src="@/assets/wechat.png" style="width: 20px; height: 20px; vertical-align: middle; margin-right: 4px;" />
                微信支付
              </el-radio>
              <el-radio label="ALIPAY">
                <img src="@/assets/alipay.png" style="width: 20px; height: 20px; vertical-align: middle; margin-right: 4px;" />
                支付宝
              </el-radio>
              <el-radio label="BANK">
                <img src="@/assets/bank.png" style="width: 20px; height: 20px; vertical-align: middle; margin-right: 4px;" />
                银行卡
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 提示信息 -->
          <el-alert
              title="温馨提示"
              type="info"
              :closable="false"
              show-icon
          >
            <template #default>
              <div style="font-size: 13px; line-height: 1.6;">
                • 支付成功后将自动生成缴费凭证<br/>
                • 您可随时在费用列表中查看缴费记录<br/>
                • 如有疑问请联系物业服务中心
              </div>
            </template>
          </el-alert>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="confirmPay"
            :loading="paying"
        >
          <el-icon><Promotion /></el-icon>
          确认支付
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import {
  queryOwnerFee,
  getOwnerFeeSummary,
  createPaymentOrder,
  payFee,
  getPaymentVoucher,
  getFeeTypes
} from '@/api/feeApi.js';
import {
  House,
  Calendar,
  Clock,
  Ticket,
  Wallet,
  Promotion
} from '@element-plus/icons-vue';

const router = useRouter();

// 获取当前用户信息
const getUserInfo = () => {
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

  const userIdStr = localStorage.getItem('userId');
  if (userIdStr) {
    const userId = parseInt(userIdStr);
    console.log('从 localStorage 获取到 userId:', userId);
    return { id: userId, role: 2 };
  }

  console.warn('未找到用户信息，使用默认值');
  return { id: 2, role: 2 };
};

const currentUser = getUserInfo();
const userId = currentUser.id;
const userRole = currentUser.role || 2;

// 搜索表单
const searchForm = reactive({
  userId,
  pageNum: 1,
  pageSize: 10,
  typeName: '',
  status: '',
  startDueDate: '',
  endDueDate: ''
});

// 列表数据
const feeList = ref([]);
const summary = ref({});
const total = ref(0);
const loading = ref(false);

// 弹窗控制
const payDialogVisible = ref(false);
const paying = ref(false); // 支付中状态
const currentFee = ref({});
const payForm = reactive({
  payType: 'WECHAT' // 默认微信支付
});

// 时间格式化
const formatPaymentTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// 获取费用列表
const fetchFeeList = async () => {
  loading.value = true;
  try {
    // 处理日期范围
    if (searchForm.startDueDate && searchForm.endDueDate) {
      // 日期范围已设置
    } else {
      searchForm.startDueDate = '';
      searchForm.endDueDate = '';
    }

    console.log('开始获取费用列表...', searchForm);
    const res = await queryOwnerFee(searchForm);
    console.log('费用列表接口响应:', res);

    const businessData = res.data;

    if (businessData && businessData.code === 200) {
      feeList.value = businessData?.data?.records || [];
      total.value = businessData?.data?.total || 0;
      console.log('费用列表数据:', feeList.value);
    } else {
      const errorMsg = businessData?.msg || '查询失败';
      ElMessage.error(errorMsg);
    }
  } catch (err) {
    console.error('获取费用列表失败:', err);
    ElMessage.error('网络异常，查询失败');
  } finally {
    loading.value = false;
  }
};

// 获取费用汇总
const fetchFeeSummary = async () => {
  try {
    console.log('开始获取费用汇总...');
    const res = await getOwnerFeeSummary(userId);
    console.log('费用汇总接口响应:', res);

    const businessData = res.data;

    if (businessData && businessData.code === 200) {
      summary.value = businessData?.data || {};
      console.log('费用汇总数据:', summary.value);
    } else {
      const errorMsg = businessData?.msg || '获取汇总失败';
      ElMessage.error(errorMsg);
    }
  } catch (err) {
    console.error('获取费用汇总失败:', err);
    ElMessage.error('网络异常，获取费用汇总失败');
  }
};

// 行点击事件
const handleRowClick = (row) => {
  if (row.statusDesc === '已缴费') {
    handleViewVoucher(row.id);
  } else {
    handlePay(row.id);
  }
};

// 重置支付表单
const resetPayForm = () => {
  payForm.payType = 'WECHAT';
  currentFee.value = {};
};

// 缴费
const handlePay = (id) => {
  const fee = feeList.value.find(item => item.id === id);
  if (fee) {
    currentFee.value = fee;
    payDialogVisible.value = true;
  }
};

// 确认缴费
const confirmPay = async () => {
  try {
    paying.value = true;

    // 第一步：生成缴费单
    const orderRes = await createPaymentOrder(userId, [currentFee.value.id]);

    if (!orderRes.data || orderRes.data.code !== 200) {
      ElMessage.error(orderRes.data?.msg || '生成缴费单失败');
      return;
    }

    const orderInfo = orderRes.data.data;
    console.log('生成的缴费单:', orderInfo);

    // 第二步：执行缴费（使用用户选择的支付方式）
    const payRes = await payFee(userId, {
      feeRecordIds: orderInfo.feeRecordIds,
      roomNo: orderInfo.roomNo,
      amount: orderInfo.totalAmount,
      payType: payForm.payType // 使用用户选择的支付方式
    });

    if (payRes.data && payRes.data.code === 200) {
      ElMessage.success(payRes.data.msg || '缴费成功');
      payDialogVisible.value = false;

      // 第三步：获取缴费凭证并跳转
      setTimeout(async () => {
        try {
          const voucherRes = await getPaymentVoucher(userId, currentFee.value.id);

          if (voucherRes.data && voucherRes.data.code === 200) {
            const voucherText = voucherRes.data.msg;

            if (voucherText && voucherText !== 'null' && voucherText.trim() !== '') {
              // 跳转到打印凭证页面
              router.push({
                path: '/owner/fee/voucher',
                query: {
                  voucherInfo: encodeURIComponent(voucherText)
                }
              });
            } else {
              ElMessage.warning('凭证生成失败，请前往费用列表查看');
              fetchFeeList();
              fetchFeeSummary();
            }
          } else {
            ElMessage.warning('获取凭证失败，请前往费用列表查看');
            fetchFeeList();
            fetchFeeSummary();
          }
        } catch (err) {
          console.error('获取凭证失败:', err);
          ElMessage.warning('获取凭证异常，请前往费用列表查看');
          fetchFeeList();
          fetchFeeSummary();
        }
      }, 500);
    } else {
      ElMessage.error(payRes.data?.msg || '缴费失败');
    }
  } catch (err) {
    console.error('缴费失败:', err);
    ElMessage.error('缴费失败，请稍后重试');
  } finally {
    paying.value = false;
  }
};

// 查看凭证
const handleViewVoucher = async (id) => {
  try {
    console.log('===== 开始获取缴费凭证 =====');
    console.log('费用记录 ID:', id);

    const res = await getPaymentVoucher(userId, id);
    console.log('接口响应 res:', res);
    console.log('res.data:', res.data);

    const businessData = res.data;

    if (businessData && businessData.code === 200) {
      const voucherText = businessData.msg;
      console.log('凭证文本:', voucherText);

      if (!voucherText || voucherText === 'null' || voucherText.trim() === '') {
        ElMessage.warning('该费用记录尚未缴费或凭证生成失败');
        return;
      }

      router.push({
        path: '/owner/fee/voucher',
        query: {
          voucherInfo: encodeURIComponent(voucherText)
        }
      });
    } else {
      ElMessage.error(businessData?.msg || '获取凭证失败');
    }
  } catch (err) {
    console.error('查看凭证异常:', err);
    ElMessage.error('网络异常，查看凭证失败');
  }
};

// 页面挂载时加载列表
onMounted(() => {
  fetchFeeList();
  fetchFeeSummary();
});
</script>

<style scoped>
.owner-fee {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
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
  color: #606266;
  margin-right: 8px;
}

.summary-item .value {
  font-weight: bold;
}

.summary-item .value.unpaid {
  color: #e6a23c;
}

.summary-item .value.overdue {
  color: #f56c6c;
}

.summary-item .value.paid {
  color: #67c23a;
}

.list-card {
  margin-bottom: 20px;
}

.fee-list {
  .fee-item {
    padding: 20px;
    border-bottom: 1px solid #ebeef5;
    cursor: pointer;
    transition: all 0.3s;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background-color: #f5f7fa;
      transform: translateX(5px);
    }

    .fee-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;

      .fee-title {
        display: flex;
        align-items: center;
        font-size: 16px;
        font-weight: bold;

        .fee-type {
          color: #303133;
        }
      }

      .fee-amount {
        .amount-label {
          font-size: 14px;
          color: #909399;
          margin-right: 5px;
        }

        .amount-value {
          font-size: 20px;
          color: #f56c6c;
          font-weight: bold;
        }
      }
    }

    .fee-content {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;

      .fee-info {
        display: flex;
        flex-direction: column;
        gap: 8px;
        font-size: 14px;
        color: #606266;

        span {
          display: flex;
          align-items: center;
          gap: 5px;

          .el-icon {
            color: #409EFF;
          }
        }
      }

      .fee-status {
        font-size: 14px;
        color: #f56c6c;
        font-weight: bold;
      }
    }
  }
}

.pay-dialog-content {
  padding: 0 10px;
}

.pay-dialog-content :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
}

.pay-dialog-content :deep(.el-descriptions__label) {
  font-weight: 500;
  color: #606266;
}

.pay-dialog-content :deep(.el-radio) {
  margin-right: 20px;
  height: auto;
  margin-bottom: 8px;
}

.pay-dialog-content :deep(.el-radio__label) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pay-dialog-content :deep(.el-alert) {
  margin-top: 16px;
}


</style>
