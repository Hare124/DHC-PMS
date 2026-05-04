<template>
<div class="owner-visitor">
  <!-- 创建邀请按钮 -->
    <div class="create-btn-container">
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        创建访客邀请
      </el-button>
    </div>

  <!-- 创建邀请弹窗 -->
  <el-dialog
      v-model="createDialogVisible"
      title="创建访客邀请"
      width="500px"
      @close="resetCreateForm"
  >
    <el-form
        ref="inviteFormRef"
        :model="inviteForm"
        :rules="inviteRules"
        label-width="100px"
    >
      <el-form-item label="访客姓名" prop="visitorName">
        <el-input
            v-model="inviteForm.visitorName"
            placeholder="请输入访客姓名"
            maxlength="50"
        ></el-input>
      </el-form-item>
      <el-form-item label="访客手机号" prop="visitorPhone">
        <el-input
            v-model="inviteForm.visitorPhone"
            placeholder="请输入访客手机号"
            maxlength="20"
        ></el-input>
      </el-form-item>
      <el-form-item label="预约时间" prop="visitTime">
        <el-date-picker
            v-model="inviteForm.visitTime"
            type="datetime"
            placeholder="选择预约时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"            style="width: 100%;"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="有效期">
        <el-input-number
            v-model="inviteForm.validHours"
            :min="1"
            :max="24"
            placeholder="小时"            style="width: 150px"
        ></el-input-number>
        <span class="unit">小时（默认 2 小时）</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="handleCreateInvite">生成邀请码</el-button>
      <el-button @click="createDialogVisible = false">取消</el-button>
    </template>
  </el-dialog>

  <!-- 二维码展示弹窗 -->
  <el-dialog
      v-model="qrDialogVisible"
      title="邀请二维码"
      width="450px"
      :close-on-click-modal="false"
      @closed="clearRefreshTimer"
  >
    <div class="qrcode-container">
      <!-- 访客信息卡片 -->
      <div class="visitor-info-card">
        <el-descriptions :column="1" size="small" border>
          <el-descriptions-item label="访客姓名">
            {{ visitorInfo.visitorName }}
          </el-descriptions-item>
          <el-descriptions-item label="访客手机号">
            {{ visitorInfo.visitorPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="房号">
            {{ visitorInfo.roomNo }}
          </el-descriptions-item>
          <el-descriptions-item label="有效期">
            {{ visitorInfo.expireTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 二维码 -->
      <div class="qrcode-wrapper" :class="{ 'expired': !qrCodeValid }">
        <img
            v-if="qrCodeBase64"
            :src="`data:image/png;base64,${qrCodeBase64}`"
            alt="邀请二维码"
            class="qrcode-img"
        />
        <div v-else class="qrcode-loading">
          <el-icon class="is-loading" :size="50"><Loading /></el-icon>
          <p>二维码生成中...</p>
        </div>
      </div>

      <!-- 状态提示 -->
      <div class="status-tips">
        <el-alert
            v-if="qrCodeValid"
            title="二维码有效，请访客出示给门岗扫码"
            type="success"
            :closable="false"
            show-icon
        />
        <el-alert
            v-else
            title="二维码已过期，请重新创建邀请"
            type="error"
            :closable="false"
            show-icon
        />
      </div>

      <!-- 倒计时 -->
      <div v-if="qrCodeValid" class="countdown-wrapper">
        <div class="countdown-label">剩余有效时间：</div>
        <div class="countdown-time" :class="countdownClass">
          {{ countdownText }}
        </div>
      </div>
    </div>

    <template #footer>
      <el-button
          type="primary"
          @click="downloadQRCode"
          :disabled="!qrCodeBase64 || !qrCodeValid"
      >
        <el-icon><Download /></el-icon>
        下载二维码
      </el-button>
      <el-button
          type="warning"
          @click="refreshQRStatus"
          :disabled="!qrCodeValid"
      >
        <el-icon><Refresh /></el-icon>
        刷新状态
      </el-button>
      <el-button @click="qrDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 邀请记录列表 -->
  <el-card shadow="never" class="list-card" style="margin-top: 20px">
    <template #header>
      <div class="card-header">
        <span>我的邀请记录</span>
        <el-button type="primary" size="small" @click="getInviteRecords" style="margin-left: 20px;">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </template>
    <div class="invite-list" v-loading="loading">
      <div
          class="invite-item"
          v-for="invite in inviteRecords"
          :key="invite.id"
          @click="handleViewQRCode(invite)"
      >
        <div class="invite-header">
          <div class="invite-title">
            <el-tag
                :type="invite.statusDesc === '待核销' ? 'warning' : invite.statusDesc === '已核销' ? 'success' : 'danger'"
                size="small"              style="margin-right: 8px;"
            >
              {{ invite.statusDesc }}
            </el-tag>
            <span class="invite-name">{{ invite.visitorName }}</span>
          </div>
        </div>

        <div class="invite-content">
          <div class="invite-info">
            <span><el-icon><Phone /></el-icon> 手机号：{{ invite.visitorPhone }}</span>
            <span><el-icon><Calendar /></el-icon> 预约时间：{{ formatTime(invite.visitTime) }}</span>
            <span><el-icon><Timer /></el-icon> 过期时间：{{ formatTime(invite.expireTime) }}</span>
          </div>

          <div class="invite-actions">
            <el-button
                size="small"
                type="primary"
                @click.stop="handleViewQRCode(invite)"
                :disabled="invite.statusDesc !== '待核销'"
            >
              查看二维码
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click.stop="handleCancelInvite(invite.id)"
                :disabled="invite.statusDesc !== '待核销'"
            >
              取消邀请
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="inviteRecords.length === 0" description="暂无邀请记录" />
    </div>

    <!-- 分页 -->
    <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getInviteRecords"
        @current-change="getInviteRecords"
        style="margin-top: 20px; text-align: right"
    />
  </el-card>
</div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Phone,
  Calendar,
  Clock,
  Timer,
  Refresh,
  Plus,
  Download,
  Loading
} from '@element-plus/icons-vue';
import dayjs from 'dayjs';
import {
  testPost,
  testGet,
  ownerCreateInvite,
  ownerGetInviteRecords,
  ownerCancelInvite,
  getInviteQRCode
} from '@/api/visitorApi.js';

// 弹窗控制
const createDialogVisible = ref(false);
const qrDialogVisible = ref(false);


// 表单相关
const inviteFormRef = ref(null);
const inviteForm = reactive({
  visitorName: '',
  visitorPhone: '',
  visitTime: '',
  validHours: 2
});
const inviteRules = reactive({
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  visitorPhone: [
    { required: true, message: '请输入访客手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  visitTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
});

// 二维码相关
const qrCodeBase64 = ref('');
const qrCodeValid = ref(true);
const expireTimestamp = ref(0);
const refreshTimer = ref(null);
const countdownTimer = ref(null);

// 访客信息
const visitorInfo = reactive({
  visitorName: '',
  visitorPhone: '',
  roomNo: '',
  expireTime: ''
});
// 倒计时
const countdownText = ref('');
const countdownClass = ref('normal');

// 获取当前用户ID
const getCurrentUserId = () => {
  return localStorage.getItem('userId')?.trim()
}

// 邀请记录
const inviteRecords = ref([]);
const loading = ref(false);

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

// 时间格式化
const formatTime = (time) => {
  if (!time) return '-';
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

// 更新时间
const updateTime = () => {
  if (!expireTimestamp.value || !qrCodeValid.value) {
    countdownText.value = '已过期'
    countdownClass.value = 'expired'
    return
  }

  const now = Date.now()
  const remaining = expireTimestamp.value - now

  if (remaining <= 0) {
    countdownText.value = '已过期'
    countdownClass.value = 'expired'
    qrCodeValid.value = false
    return
  }

  const hours = Math.floor(remaining / 3600000)
  const minutes = Math.floor((remaining % 3600000) / 60000)
  const seconds = Math.floor((remaining % 60000) / 1000)

  countdownText.value = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`

  // 根据剩余时间设置样式
  if (remaining < 300000) { // 5分钟内
    countdownClass.value = 'danger'
  } else if (remaining < 600000) { // 10分钟内
    countdownClass.value = 'warning'
  } else {
    countdownClass.value = 'normal'
  }
}

// 开始倒计时
const startCountdown = () => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
  countdownTimer.value = setInterval(updateTime, 1000)
  updateTime() // 立即更新一次
}

// 清除定时器
const clearRefreshTimer = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
}
// 页面加载时获取记录
onMounted(() => {
  const userId = getCurrentUserId()
  if (!userId) {
    ElMessage.warning('未检测到用户 ID，请重新登录');
    setTimeout(() => {
      window.location.href = '/login';
    }, 1500);
    return;
  }
  getInviteRecords();
});

// 打开创建弹窗
const openCreateDialog = () => {
  createDialogVisible.value = true;
};

// 创建邀请
const handleCreateInvite = async () => {
  try {
    await inviteFormRef.value.validate();
    const res = await ownerCreateInvite(inviteForm);
    const result = res.data;
    if (result.code === 200) {
      ElMessage.success(result.msg);

      // 关闭创建弹窗
      createDialogVisible.value = false;

      // 设置二维码数据
      qrCodeBase64.value = result.data.qrCodeBase64;

      // 解析过期时间
      const expireTimeStr = result.data.expireTime;
      visitorInfo.expireTime = formatTime(expireTimeStr);
      expireTimestamp.value = new Date(expireTimeStr).getTime();

      // 设置访客信息
      visitorInfo.visitorName = inviteForm.visitorName;
      visitorInfo.visitorPhone = inviteForm.visitorPhone;
      visitorInfo.roomNo = result.data.inviteInfo.roomNo;

      qrCodeValid.value = result.data.inviteInfo.qrCodeValid;

      // 展示二维码弹窗
      qrDialogVisible.value = true;

      // 开始倒计时
      startCountdown();

      // 刷新记录
      getInviteRecords();

      // 重置表单
      resetCreateForm();
    } else {
      ElMessage.error(result.msg || '创建邀请失败');
    }
  } catch (error) {
    console.error('创建邀请失败：', error);
    if (error.response?.status === 401) {
      ElMessage.error('登录状态失效，请重新登录');
    } else {
      ElMessage.error(error.response?.data?.msg || '创建邀请失败，请重试');
    }
  }
};

// 下载二维码
const downloadQRCode = () => {
  if (!qrCodeBase64.value) {
    ElMessage.warning('二维码未生成');
    return;
  }

  try {
    const link = document.createElement('a');
    link.href = `data:image/png;base64,${qrCodeBase64.value}`;
    link.download = `访客二维码_${visitorInfo.visitorName}_${Date.now()}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    ElMessage.success('二维码下载成功');
  } catch (error) {
    console.error('下载二维码失败:', error);
    ElMessage.error('下载失败，请重试');
  }
};

// 刷新二维码状态
const refreshQRStatus = async () => {
  try {
    ElMessage.info('正在刷新状态...');
    await getInviteRecords();

    // 查找最新的待核销记录
    const latestInvite = inviteRecords.value.find(r => r.statusDesc === '待核销');
    if (latestInvite) {
      const now = Date.now();
      const expireTime = new Date(latestInvite.expireTime).getTime();

      if (now > expireTime) {
        qrCodeValid.value = false;
        ElMessage.warning('二维码已过期');
      } else {
        qrCodeValid.value = true;
        ElMessage.success('二维码状态正常');
      }
    } else {
      qrCodeValid.value = false;
      ElMessage.warning('未找到有效邀请');
    }
  } catch (error) {
    console.error('刷新状态失败:', error);
    ElMessage.error('刷新失败，请重试');
  }
};

// 查看二维码
const handleViewQRCode = async (invite) => {
  // 检查是否是待核销状态
  if (invite.statusDesc !== '待核销') {
    ElMessage.warning('该邀请已' + invite.statusDesc + ',无法查看二维码');
    return;
  }

  try {
    loading.value = true;

    // 调用后端获取二维码
    const res = await getInviteQRCode(invite.id);
    const result = res.data;

    if (result.code === 200) {
      // 设置二维码数据
      qrCodeBase64.value = result.data.qrCodeBase64;
      visitorInfo.visitorName = invite.visitorName;
      visitorInfo.visitorPhone = invite.visitorPhone;
      visitorInfo.roomNo = invite.roomNo;
      visitorInfo.expireTime = formatTime(invite.expireTime);
      expireTimestamp.value = new Date(invite.expireTime).getTime();
      qrCodeValid.value = result.data.valid;

      // 显示二维码弹窗
      qrDialogVisible.value = true;

      // 开始倒计时
      startCountdown();
    } else {
      ElMessage.error(result.msg || '获取二维码失败');
    }
  } catch (error) {
    console.error('获取二维码失败:', error);
    ElMessage.error(error.response?.data?.msg || '获取二维码失败');
  } finally {
    loading.value = false;
  }
};

// 从邀请记录生成二维码
const generateQRCodeFromInvite = async (invite) => {
  try {
    // 这里需要调用后端接口重新生成二维码
    // 暂时使用已有的二维码(如果有)
    if (invite.qrCodeBase64) {
      qrCodeBase64.value = invite.qrCodeBase64;
    } else {
      // 如果后端没有返回二维码图片,提示用户
      ElMessage.warning('二维码图片未缓存,建议重新创建邀请');
      qrCodeBase64.value = '';
    }
  } catch (error) {
    console.error('生成二维码失败:', error);
    ElMessage.error('生成二维码失败');
  }
};

// 重置表单
const resetCreateForm = () => {
  if (inviteFormRef.value) {
    inviteFormRef.value.resetFields();
  }
  inviteForm.validHours = 2;
  qrCodeBase64.value = '';
  expireTimestamp.value = 0;
  qrCodeValid.value = true;
  countdownText.value = '';
  createDialogVisible.value = false;
  qrDialogVisible.value = false;
};

// 获取邀请记录
const getInviteRecords = async () => {
  loading.value = true;
  try {
    const userId = getCurrentUserId();
    console.log('当前 userId:', userId);

    if (!userId) {
      ElMessage.error('未找到用户 ID，请重新登录');
      setTimeout(() => {
        window.location.href = '/login';
      }, 1500);
      return;
    }

    const res = await ownerGetInviteRecords();
    const result = res.data;
    console.log('邀请记录响应:', result);

    if (result.code === 200) {
      inviteRecords.value = result.data || [];
      pagination.total = inviteRecords.value.length;
      console.log('邀请记录数量:', inviteRecords.value.length);
    } else {
      ElMessage.error(result.msg || '获取记录失败');
      inviteRecords.value = [];
    }
  } catch (error) {
    console.error('获取邀请记录失败：', error);
    console.error('错误详情:', error.response?.data);
    if (error.response?.status === 401) {
      ElMessage.error('登录状态失效，请重新登录');
    } else {
      ElMessage.error(error.response?.data?.msg || '获取邀请记录失败，请重试');
    }
  } finally {
    loading.value = false;
  }
};

// 取消邀请
const handleCancelInvite = async (inviteId) => {
  try {
    await ElMessageBox.confirm('确定要取消该邀请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    const res = await ownerCancelInvite(inviteId);
    const result = res.data;
    if (result.code === 200) {
      ElMessage.success(result.msg);
      getInviteRecords();
    } else {
      ElMessage.error(result.msg || '取消邀请失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消邀请失败：', error);
      if (error.response?.status === 401) {
        ElMessage.error('登录状态失效，请重新登录');
      } else {
        ElMessage.error('取消邀请失败，请重试');
      }
    }
  }
};

// 测试 POST
const handleTestPost = async () => {
  try {
    const res = await testPost({
      name: '张三',
      age: 18
    });
    ElMessage.success('POST 请求成功');
    console.log('POST 请求成功：', res);
  } catch (err) {
    ElMessage.error('POST 请求失败');
    console.error('POST 请求失败：', err);
  }
};

// 测试 GET
const handleTestGet = async () => {
  try {
    const res = await testGet();
    ElMessage.success('GET 请求成功');
    console.log('GET 请求成功：', res);
  } catch (err) {
    ElMessage.error('GET 请求失败');
    console.error('GET 请求失败：', err);
  }
};
</script>

<style scoped>
.owner-visitor {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.create-btn-card,
.list-card {
  background: #fff;
  margin-bottom: 20px;
}

.create-btn-container {
  text-align: left;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.qrcode-container {
  margin-top: 20px;
}

.visitor-info-card {
  margin-bottom: 20px;
}

.qrcode-wrapper {
  text-align: center;
  margin: 20px 0;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  transition: all 0.3s;
}

.qrcode-wrapper.expired {
  background: #fef0f0;
  border: 2px dashed #f56c6c;
}

.qrcode-img {
  width: 280px;
  height: 280px;
  margin: 0 auto;
  display: block;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.qrcode-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 280px;
  color: #909399;
}

.qrcode-loading p {
  margin-top: 10px;
  font-size: 14px;
}

.status-tips {
  margin-top: 15px;
}

.countdown-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 15px;
  padding: 10px;
  background: #ecf5ff;
  border-radius: 4px;
}

.countdown-label {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
}

.countdown-time {
  font-size: 18px;
  font-weight: bold;
  font-family: 'Courier New', monospace;
}

.countdown-time.normal {
  color: #67c23a;
}

.countdown-time.warning {
  color: #e6a23c;
}

.countdown-time.danger {
  color: #f56c6c;
}

.countdown-time.expired {
  color: #909399;
}

.unit {
  margin-left: 10px;
  color: #909399;
}

.invite-list {
  .invite-item {
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

    .invite-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;

      .invite-title {
        display: flex;
        align-items: center;
        font-size: 16px;
        font-weight: bold;

        .invite-name {
          color: #303133;
        }
      }

      .invite-id {
        font-size: 14px;
        color: #909399;
      }
    }

    .invite-content {
      .invite-info {
        display: flex;
        flex-wrap: wrap;
        gap: 15px;
        font-size: 14px;
        color: #606266;
        margin-bottom: 15px;

        span {
          display: flex;
          align-items: center;
          gap: 5px;

          .el-icon {
            color: #409EFF;
          }
        }
      }

      .invite-actions {
        display: flex;
        gap: 10px;
        justify-content: flex-start;
        padding-top: 10px;
        border-top: 1px solid #f0f0f0;
      }
    }
  }
}
</style>
