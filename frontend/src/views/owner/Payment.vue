<template>
  <div class="payment-container">
    <el-card class="payment-card">
      <!-- 缴费信息确认 -->
      <div class="payment-header">
        <h2>缴费确认</h2>
        <el-divider></el-divider>
      </div>

      <!-- 缴费明细 -->
      <div class="payment-detail">
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="房号" :span="2">
            {{ paymentInfo.roomNo }}
          </el-descriptions-item>
          <el-descriptions-item label="费用类型">
            {{ paymentInfo.feeTypeName }}
          </el-descriptions-item>
          <el-descriptions-item label="应收金额">
            <span class="amount-text">¥{{ paymentInfo.amount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="缴费截止日期">
            {{ paymentInfo.dueDate }}
          </el-descriptions-item>
          <el-descriptions-item label="逾期天数" v-if="paymentInfo.overdueDays">
            <span class="overdue-text">{{ paymentInfo.overdueDays }} 天</span>
          </el-descriptions-item>
          <el-descriptions-item label="滞纳金" v-if="paymentInfo.overdueFine > 0">
            <span class="fine-text">¥{{ paymentInfo.overdueFine }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 费用合计 -->
        <div class="total-amount">
          <span class="label">实缴总金额：</span>
          <span class="total-text">¥{{ paymentInfo.totalAmount }}</span>
        </div>
      </div>

      <!-- 支付方式选择 -->
      <div class="payment-method">
        <el-form :model="paymentForm" label-width="100px">
          <el-form-item label="支付方式" required>
            <el-radio-group v-model="paymentForm.payType">
              <el-radio label="WECHAT">
                <div class="pay-option">
                  <img src="../../assets/wechat.png" alt="微信支付" class="pay-icon" />
                  <span>微信支付</span>
                </div>
              </el-radio>
              <el-radio label="ALIPAY">
                <div class="pay-option">
                  <img src="../../assets/alipay.png" alt="支付宝支付" class="pay-icon" />
                  <span>支付宝支付</span>
                </div>
              </el-radio>
              <el-radio label="BANK">
                <div class="pay-option">
                  <img src="../../assets/bank.png" alt="银行卡支付" class="pay-icon" />
                  <span>银行卡支付</span>
                </div>
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 备注（选填） -->
          <el-form-item label="备注">
            <el-input
                v-model="paymentForm.remark"
                type="textarea"
                placeholder="请输入缴费备注（选填）"
                rows="3"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>

      <!-- 操作按钮 -->
      <div class="payment-footer">
        <el-button @click="handleBack">返回</el-button>
        <el-button type="primary" @click="handleSubmitPayment" :loading="isSubmitting">
          确认支付
        </el-button>
      </div>

      <!-- 支付成功弹窗 -->
      <el-dialog
          v-model:visible="showSuccessDialog"
          title="缴费成功"
          width="400px"
          :close-on-click-modal="false"
          :show-close="true"
      >
        <div class="success-content">
          <el-icon size="60" color="#67c23a"><CircleCheck /></el-icon>
          <p class="success-text">缴费成功！</p>
          <p class="hint-text">您可前往「费用列表」查看缴费凭证</p>
        </div>
        <template #footer>
          <el-button @click="showSuccessDialog = false">关闭</el-button>
          <el-button type="primary" @click="handleGoBack">返回费用列表</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { CircleCheck } from '@element-plus/icons-vue';
// 导入缴费接口
import { createPaymentOrder, payFee } from '@/api/feeApi.js';

const route = useRoute();
const router = useRouter();

// 核心修复：从 localStorage 获取真实的登录用户信息
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

  // 2. 尝试直接获取 userId
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

console.log('\n========== Payment.vue 当前用户信息 ==========');
console.log('userId:', userId);
console.log('userRole:', userRole);
console.log('=============================================\n');


// 缴费信息（承接路由参数/接口返回）
const paymentInfo = reactive({
  roomNo: '',
  feeTypeName: '',
  amount: 0,
  dueDate: '',
  overdueDays: 0,
  overdueFine: 0,
  totalAmount: 0,
  feeRecordIds: []
});

// 支付表单
const paymentForm = reactive({
  payType: 'WECHAT', // 默认微信支付
  remark: ''
});

// 加载状态
const isSubmitting = ref(false);
// 支付成功弹窗
const showSuccessDialog = ref(false);

// 页面加载初始化
onMounted(() => {
  initPaymentData();
});

/**
 * 初始化缴费数据
 * 从路由参数获取费用记录ID，调用接口生成缴费单
 */
const initPaymentData = async () => {
  try {
    console.log('===== 开始初始化缴费数据 =====');
    // 获取路由参数中的费用记录 ID（支持单条/多条）
    const feeRecordIds = route.query.feeRecordIds;
    console.log('路由参数 feeRecordIds:', feeRecordIds);

    if (!feeRecordIds) {
      console.error('缴费记录 ID 不存在');
      ElMessage.error('缴费记录 ID 不存在');
      router.push('/fee/owner/list');
      return;
    }

    // 转换为数组（兼容单条/多条）
    const ids = Array.isArray(feeRecordIds)
        ? feeRecordIds.map(Number)
        : [Number(feeRecordIds)];

    console.log('转换后的费用记录 ID 数组:', ids);
    console.log('当前用户 ID:', userId);

    // 调用接口生成缴费单
    console.log('开始调用 createPaymentOrder 接口...');
    const res = await createPaymentOrder(userId, ids);
    console.log('createPaymentOrder 接口响应:', res);
    console.log('业务数据（res.data）:', res.data);
    console.log('业务数据的 code:', res.data?.code);
    console.log('业务数据的 msg:', res.data?.msg);
    console.log('业务数据的 data:', res.data?.data);

    // 处理嵌套响应结构
    const businessData = res.data;
    const responseCode = businessData?.code;

    if (responseCode === 200) {
      // 填充缴费信息
      Object.assign(paymentInfo, businessData.data || res.data);
      // 保存费用记录 ID（用于后续缴费）
      paymentInfo.feeRecordIds = ids;
      console.log('缴费信息填充成功:', paymentInfo);
      ElMessage.success('缴费信息加载成功');
    } else {
      console.error('获取缴费信息失败:', businessData);
      ElMessage.error(businessData?.msg || '获取缴费信息失败');
      router.push('/fee/owner/list');
    }
  } catch (err) {
    console.error('初始化缴费数据失败：', err);
    ElMessage.error('网络异常，请重试');
    router.push('/fee/owner/list');
  }
};


/**
 * 提交缴费
 */
const handleSubmitPayment = async () => {
  if (!paymentForm.payType) {
    ElMessage.warning('请选择支付方式');
    return;
  }

  try {
    // 二次确认缴费
    await ElMessageBox.confirm(
        `您确认使用${getPayTypeName(paymentForm.payType)}支付¥${paymentInfo.totalAmount}元吗？`,
        '缴费确认',
        {
          confirmButtonText: '确认支付',
          cancelButtonText: '取消',
          type: 'warning'
        }
    );

    // 开始支付，设置加载状态
    isSubmitting.value = true;

    // 组装缴费单参数
    const orderParams = {
      ...paymentInfo,
      payType: paymentForm.payType,
      remark: paymentForm.remark
    };

    // 调用缴费接口
    console.log('开始调用 payFee 接口...');
    console.log('缴费参数:', orderParams);
    const res = await payFee(userId, orderParams);
    console.log('payFee 接口响应:', res);
    console.log('业务数据（res.data）:', res.data);
    console.log('业务数据的 code:', res.data?.code);
    console.log('业务数据的 msg:', res.data?.msg);

    // 处理嵌套响应结构
    const businessData = res.data;
    const responseCode = businessData?.code;

    if (responseCode === 200) {
      // 支付成功，通知费用列表页面刷新数据（使用 localStorage 作为通信机制）
      localStorage.setItem('needRefreshFeeList', 'true');
      console.log('已设置刷新标记，费用列表页面将自动刷新');

      ElMessage.success('缴费成功！即将返回费用列表...');

      // 核心修复：延迟 1 秒后自动跳转回费用列表页面
      setTimeout(() => {
        console.log('缴费成功，自动跳转回费用列表页面');
        router.push('/owner/fee/list');
      }, 1000);
    } else {
      console.error('缴费失败:', businessData);
      ElMessage.error(businessData?.msg || '缴费失败，请稍后重试');
    }
  } catch (err) {
    if (err !== 'cancel') { // 排除取消操作的异常
      console.error('缴费失败：', err);
      ElMessage.error('支付异常，请检查网络或联系管理员');
    }
  } finally {
    // 重置加载状态
    isSubmitting.value = false;
  }
};

/**
 * 返回费用列表页
 */
const handleBack = () => {
  router.push('/owner/fee/list');
};

/**
 * 支付成功后返回费用列表
 */
const handleGoBack = () => {
  showSuccessDialog.value = false;
  router.push('/fee/owner/list');
};

/**
 * 获取支付方式名称（用于展示）
 * @param {String} payType 支付方式编码
 * @returns {String} 支付方式名称
 */
const getPayTypeName = (payType) => {
  const payTypeMap = {
    WECHAT: '微信支付',
    ALIPAY: '支付宝支付',
    BANK: '银行卡支付'
  };
  return payTypeMap[payType] || '未知支付方式';
};
</script>

<style scoped>
.payment-container {
  padding: 20px;
  text-align: center;
}

.payment-card {
  display: inline-block;
  width: 80%;
  max-width: 800px;
  padding: 30px;
  text-align: left;
}

.payment-header {
  text-align: center;
  margin-bottom: 20px;
}

.payment-detail {
  margin-bottom: 30px;
}

.amount-text {
  color: #e6a23c;
  font-weight: bold;
  font-size: 16px;
}

.overdue-text {
  color: #f56c6c;
  font-weight: bold;
}

.fine-text {
  color: #f56c6c;
  font-weight: bold;
}

.total-amount {
  text-align: right;
  font-size: 18px;
  margin-top: 10px;
}

.total-amount .label {
  color: #666;
}

.total-amount .total-text {
  color: #e6a23c;
  font-weight: bold;
  margin-left: 10px;
}

.payment-method {
  margin-bottom: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.pay-option {
  display: inline-flex;
  align-items: center;
  padding: 10px 20px;
  cursor: pointer;
}

.pay-icon {
  width: 30px;
  height: 30px;
  margin-right: 10px;
}

.payment-footer {
  text-align: center;
  margin-top: 30px;
}

.payment-footer button {
  margin: 0 10px;
  width: 120px;
}

.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-text {
  font-size: 20px;
  font-weight: bold;
  color: #67c23a;
  margin: 20px 0 10px;
}

.hint-text {
  color: #666;
}
</style>